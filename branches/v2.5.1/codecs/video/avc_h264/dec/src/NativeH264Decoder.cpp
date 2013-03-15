/* ------------------------------------------------------------------
 * Copyright (C) 2009 OrangeLabs
 * -------------------------------------------------------------------
 */
#define LOG_TAG "NativeH264Decoder"
#include "android/log.h"
#include "NativeH264Decoder.h"
#include "pvavcdecoder.h"
#include "3GPVideoParser.h"

#include "avcapi_common.h"
#include "oscl_mem.h"
#include "avcdec_api.h"
#include "yuv2rgb.h"

#define MB_BASED_DEBLOCK

/* ------------------------------------------------------------------------------------- */
/*  Callback declaration for pvAvcDecoder                                                */
/* ------------------------------------------------------------------------------------- */

#define AVC_DEC_TIMESTAMP_ARRAY_SIZE 17 // don't ask me why :) look at avc_dev.h

typedef struct s_decoderData {
    uint32 FrameSize;
    uint8* pDpbBuffer;
    uint32 DisplayTimestampArray[AVC_DEC_TIMESTAMP_ARRAY_SIZE];
    uint32 CurrInputTimestamp;
    PVAVCDecoder * pvAvDec;
    AVCDecSPSInfo SeqInfo;
} DecoderData;

DecoderData gDecodeData = { 0 };


int cb_AVC_init_SPS(void *userData, uint aSizeInMbs, uint aNumBuffers) {
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "CB init_SPS");

    DecoderData *avcDec = (DecoderData*) userData;
    if (NULL == avcDec) {
        return 0;
    }
    avcDec->pvAvDec->GetSeqInfo(&avcDec->SeqInfo);
    if (avcDec->pDpbBuffer) {
        free(avcDec->pDpbBuffer);
        avcDec->pDpbBuffer = NULL;
    }
    avcDec->FrameSize = (aSizeInMbs << 7) * 3;
    avcDec->pDpbBuffer = (uint8*) malloc(aNumBuffers * (avcDec->FrameSize));

    return 1;
}

int cb_AVC_Malloc(void *userData, int32 size, int attribute) {
    DecoderData *avcDec = (DecoderData*) userData;
    return avcDec->pvAvDec->AVC_Malloc(size, attribute);
}

void cb_AVC_Free(void *userData, int mem) {
    DecoderData *avcDec = (DecoderData*) userData;
    avcDec->pvAvDec->AVC_Free(mem);
}

int cb_AVC_DPBAlloc(void* userData, int32 i, uint8** aYuvBuffer) {
    //__android_log_print(ANDROID_LOG_INFO, LOG_TAG, "CB cb_AVC_DPBAlloc");
    DecoderData *avcDec = (DecoderData*) userData;
    if (NULL == avcDec) {
        return 0;
    }

    *aYuvBuffer = avcDec->pDpbBuffer + i * avcDec->FrameSize;
    //Store the input timestamp at the correct index
    avcDec->DisplayTimestampArray[i] = avcDec->CurrInputTimestamp;

    return 1;
}

void cb_AVC_FrameUnbind(void *userData, int indx) {
    OSCL_UNUSED_ARG(userData);
    OSCL_UNUSED_ARG(indx);
    return;
}

/* ------------------------------------------------------------------------------------- */
/* Global variables                                                                      */
/* ------------------------------------------------------------------------------------- */
/* Packet video decoder */
PVAVCDecoder *decoder;

/* Parser is initialized ?*/
int parserInitialized = 0;

/* Buffer for the decoded pictures */
uint8* aOutBuffer;

/* Concatenated buffer */
uint8* aConcatBuf;

/* Size of the concatenated buffer */
int32 aConcatSize;

/** Return value from decoder */
int iStatus;

/* Protection mutex */
pthread_mutex_t iMutex = PTHREAD_MUTEX_INITIALIZER;

/** Returned values. */
enum INIT_RETVAL {
    SUCCESS             = 0,
    FAIL                = -1
};

/* ------------------------------------------------------------------------------------- */
/* Methods                                                                               */
/* ------------------------------------------------------------------------------------- */

/**
 * Method:    Decode
 */
jintArray Decode(JNIEnv * env, uint8* input, int32 size) {
    int32 status;
    int indexFrame;
    int releaseFrame;
    jintArray decoded;

    // Get type of NAL
    u_int8_t type = input[0] & 0x1f;
    switch (type) {
        case 7: // SPS
            if ((status = decoder->DecodeSPS(input, size)) != AVCDEC_SUCCESS) {
                __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Failed decode SPS: %ld", status);
                decoded = (env)->NewIntArray(0);
                pthread_mutex_unlock(&iMutex);
                iStatus = 0;
                return decoded;
            }
            decoded = (env)->NewIntArray(0);
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Receive SPS");
            break;

        case 8: // PPS
            if ((status = decoder->DecodePPS(input, size)) != AVCDEC_SUCCESS) {
                __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Failed to decode PPS: %ld", status);
                decoded = (env)->NewIntArray(0);
                pthread_mutex_unlock(&iMutex);
                iStatus = 0;
                return decoded;
            }
            decoded = (env)->NewIntArray(0);
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Receive PPS");
            break;

        case 5: // IDR slice
        case 1: // non-IDR slice
            // Decode the buffer
            status = decoder->DecodeAVCSlice(input, &size);
            if (status > AVCDEC_FAIL) {
                AVCFrameIO outVid;
                decoder->GetDecOutput(&indexFrame, &releaseFrame, &outVid);

                int width = outVid.pitch;
                int height = outVid.height;
                if(aOutBuffer == NULL) {
                    aOutBuffer = (uint8*)malloc(width*height*3/2);
                }
                decoded = (env)->NewIntArray(width*height);

                // Copy result to YUV array
                memcpy(aOutBuffer, outVid.YCbCr[0], width * height);
                memcpy(aOutBuffer + (width * height), outVid.YCbCr[1], (width * height) / 4);
                memcpy(aOutBuffer + (width * height) + ((width * height) / 4), outVid.YCbCr[2], (width * height) / 4);

                // Create the output buffer
                uint32* resultBuffer = (uint32*) malloc(width * height * sizeof(uint32));
                if (resultBuffer == NULL) {
                    decoded = (env)->NewIntArray(0);
                    pthread_mutex_unlock(&iMutex);
                    iStatus = 0;
                    return decoded;
				}

                // Convert to RGB
                convert(width, height, aOutBuffer, resultBuffer);

                (env)->SetIntArrayRegion(decoded, 0, width * height, (const jint*) resultBuffer);
                free(resultBuffer);
            } else {
                __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Decoder error %ld", status);
                decoded = (env)->NewIntArray(0);
                pthread_mutex_unlock(&iMutex);
                iStatus = 0;
                return decoded;
            }
            break;
    }
    pthread_mutex_unlock(&iMutex);
    iStatus = 1;
    return decoded;
}


/**
 * Method:    InitDecoder
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_InitDecoder
  (JNIEnv * env, jclass clazz) {
    aOutBuffer = NULL;
    decoder = PVAVCDecoder::New();

    memset (&gDecodeData, '\0', sizeof(DecoderData));

    gDecodeData.pvAvDec = decoder;
    decoder->InitAVCDecoder(
    		&cb_AVC_init_SPS,
    		&cb_AVC_DPBAlloc,
    		&cb_AVC_FrameUnbind,
            &cb_AVC_Malloc,
            &cb_AVC_Free,
            &gDecodeData);

    return (decoder!=NULL)?SUCCESS:FAIL;
}

/**
 * Method:    DeinitDecoder
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DeinitDecoder
  (JNIEnv * env, jclass clazz) {
    pthread_mutex_lock(&iMutex);

    free(aOutBuffer);
    delete(decoder);

    pthread_mutex_unlock(&iMutex);

    return SUCCESS;
}

/**
 * Method:    DecodeAndConvert
 */
JNIEXPORT jintArray JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DecodeAndConvert
  (JNIEnv *env, jclass clazz, jbyteArray h264Frame) {
    pthread_mutex_lock(&iMutex);

    int32 size = 0;
    jint len = env->GetArrayLength(h264Frame);
    jbyte data[len];

    // Copy jbyteArray to uint8*
    env->GetByteArrayRegion(h264Frame, 0, len, data);
    uint8* aInputBuf = (uint8*)malloc(len);
    memcpy(aInputBuf, (uint8*)data, len);
    size = len;

    // Decode
    return Decode(env, aInputBuf, size);
}

/*
 * Method:    getLastDecodeStatus
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getLastDecodeStatus
  (JNIEnv *env, jclass clazz){
    return iStatus;
}

/**
 * This is called by the VM when the shared library is first loaded.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }
    /* success -- return valid version number */
    result = JNI_VERSION_1_4;
bail:
    return result;
}






