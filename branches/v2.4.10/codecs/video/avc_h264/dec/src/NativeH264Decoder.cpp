/* ------------------------------------------------------------------
 * Copyright (C) 2009 OrangeLabs
 * -------------------------------------------------------------------
 */
#define LOG_TAG "NativeH264Decoder"
#include "android/log.h"
#include "NativeH264Decoder.h"
#include "pvavcdecoder.h"
#include "3GPVideoParser.h"

#include "avcapi_common.h" //+OrangeLabs
#include "oscl_mem.h" //+OrangeLabs
#include "avcdec_api.h"
#include "yuv2rgb.h"

#define MB_BASED_DEBLOCK

/* ------------------------------------------------------------------------------------- */
/*  Callback declaration for pvAvcDecoder                                                */
/*                                                                                       */
/* ------------------------------------------------------------------------------------- */
#define AVC_DEC_TIMESTAMP_ARRAY_SIZE 17    // don't ask me why :) look at avc_dev.h

typedef struct s_decoderData {
    uint32          FrameSize;
    uint8*          pDpbBuffer;
    uint32   	    DisplayTimestampArray[AVC_DEC_TIMESTAMP_ARRAY_SIZE];
    uint32     		CurrInputTimestamp;

    PVAVCDecoder *  pvAvDec;
    AVCDecSPSInfo   SeqInfo;
} DecoderData;

DecoderData    gDecodeData = {0};

/* ------------------------------------------------------------------------------------- */
/*                                                                                       */
/* ------------------------------------------------------------------------------------- */
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

/* ------------------------------------------------------------------------------------- */
/*                                                                                       */
/* ------------------------------------------------------------------------------------- */
int cb_AVC_Malloc(void *userData, int32 size, int attribute) {
	DecoderData *avcDec = (DecoderData*) userData;
    return avcDec->pvAvDec->AVC_Malloc(size, attribute);
}

/* ------------------------------------------------------------------------------------- */
/*                                                                                       */
/* ------------------------------------------------------------------------------------- */
void cb_AVC_Free(void *userData, int mem) {
	DecoderData *avcDec = (DecoderData*) userData;
	avcDec->pvAvDec->AVC_Free(mem);
}

/* ------------------------------------------------------------------------------------- */
/*                                                                                       */
/* ------------------------------------------------------------------------------------- */
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

/* ------------------------------------------------------------------------------------- */
/*                                                                                       */
/* ------------------------------------------------------------------------------------- */
void cb_AVC_FrameUnbind(void *userData, int indx) {
    OSCL_UNUSED_ARG(userData);
    OSCL_UNUSED_ARG(indx);
    return;
}

/* ------------------------------------------------------------------------------------- */
/*  JNI wrapper                                                                          */
/*                                                                                       */
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

/* Live video Width */
int32 videoWidth;

/* Live video Height */
int32 videoHeight;

/**
 * Method:    Decode
 */
jint Decode(JNIEnv * env, uint8* input, int32 size, jintArray decoded) {
    int32 status;
    int indexFrame;
    int releaseFrame;

    // Get type of NAL
    u_int8_t type = input[0] & 0x1f;
    switch (type) {
        case 7: // SPS
            if ((status = decoder->DecodeSPS(input, size)) != AVCDEC_SUCCESS) {
                __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Failed decode SPS: %ld", status);
                return 0;
            }
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Receive SPS");
            break;

        case 8: // PPS
            if ((status = decoder->DecodePPS(input, size)) != AVCDEC_SUCCESS) {
                __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Failed to decode PPS: %ld", status);
                return 0;
            }
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Receive PPS");
            break;

        case 5: // IDR slice
        case 1: // non-IDR slice
            // Decode the buffer
            status = decoder->DecodeAVCSlice(input, &size);
            if (status > AVCDEC_FAIL) {
                AVCFrameIO outVid;
                decoder->GetDecOutput(&indexFrame, &releaseFrame, &outVid);

                //+OrangeLabs
                //if (releaseFrame == 1) {
                //      decoder->AVC_FrameUnbind(indexFrame);
                //  }
                //+OrangeLabs

                // Copy result to YUV array
                memcpy(aOutBuffer, outVid.YCbCr[0], videoWidth * videoHeight);
                memcpy(aOutBuffer + (videoWidth * videoHeight), outVid.YCbCr[1], (videoWidth * videoHeight) / 4);
                memcpy(aOutBuffer + (videoWidth * videoHeight) + ((videoWidth * videoHeight) / 4), outVid.YCbCr[2], (videoWidth * videoHeight) / 4);

                // Create the output buffer
                uint32* resultBuffer = (uint32*) malloc(videoWidth * videoHeight * sizeof(uint32));
                if (resultBuffer == NULL) {
                    return 0;
				}

                // Convert to RGB
                convert(videoWidth, videoHeight, aOutBuffer, resultBuffer);

                (env)->SetIntArrayRegion(decoded, 0, videoWidth * videoHeight, (const jint*) resultBuffer);
                free(resultBuffer);
            } else {
                __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Decoder error %ld", status);
                return 0;
            }
            break;
    }
    return 1;
}


/**
 * Method:    InitDecoder
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_InitDecoder
  (JNIEnv * env, jclass clazz, jint width, jint height) {
    videoWidth = width;
    videoHeight = height;

    aOutBuffer = (uint8*)malloc(videoWidth * videoHeight * 3/2);
    decoder = PVAVCDecoder::New();

    //+OrangeLabs
    memset (&gDecodeData, '\0', sizeof(DecoderData));

    gDecodeData.pvAvDec = decoder;
    decoder->InitAVCDecoder(
    		&cb_AVC_init_SPS,
    		&cb_AVC_DPBAlloc,
    		&cb_AVC_FrameUnbind,
            &cb_AVC_Malloc,
            &cb_AVC_Free,
            &gDecodeData);
    //-OrangeLabs

	return (decoder!=NULL)?1:0;
//    return ((decoder!=NULL)? 0:1);  //OrangeLabs invert 0 and 1 to have the same behaviour as enc
}

/**
 * Method:    DeinitDecoder
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DeinitDecoder
  (JNIEnv * env, jclass clazz) {
    free(aOutBuffer);
    delete(decoder);
    return 1;
}

/**
 * Method:    DecodeAndConvert
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DecodeAndConvert
  (JNIEnv *env, jclass clazz, jbyteArray h264Frame, jintArray decoded) {
    int32 size = 0;
    jint len = env->GetArrayLength(h264Frame);
    jbyte data[len];

    // Copy jbyteArray to uint8*
    env->GetByteArrayRegion(h264Frame, 0, len, data);
    uint8* aInputBuf = (uint8*)malloc(len);
    memcpy(aInputBuf, (uint8*)data, len);
    size = len;

    // Decode
    return Decode(env, aInputBuf, size, decoded);
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






