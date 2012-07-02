/* ------------------------------------------------------------------
 * Copyright (C) 2009 OrangeLabs
 * -------------------------------------------------------------------
 */
#define LOG_TAG "NativeH264Decoder"
#include "android/log.h"
#include "NativeH264Decoder.h"
#include "pvavcdecoder.h"
#include "3GPVideoParser.h"
#include "yuv2rgb.h"

#define MB_BASED_DEBLOCK

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

        case 28: { // FU-A fragmented NAL
            // Save FU indicator
            uint8 fu_indicator = input[0];

            // Skip FU indicator
            input++;
            size--;

            // Check FU header
            uint8 fu_header = *input;
            uint8 start_bit = fu_header >> 7;
            uint8 end_bit = (fu_header & 0x40) >> 6;
            uint8 nal_type = fu_header & 0x1f;

            if (start_bit && end_bit) {
                __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                        "Not possible to have start_bit and end_bit in the same FU-A frame");
                return 0;
            }

            if (start_bit) {
                // Build a new NAL
                uint8 reconstructed_nal = fu_indicator & 0xe0;
                reconstructed_nal |= nal_type;

                // Skip FU header
                input++;
                size--;

                // Create the concatenated buffer
                aConcatSize = size + 1;
                aConcatBuf = (uint8*) malloc(aConcatSize);
                aConcatBuf[0] = reconstructed_nal;
                memcpy(aConcatBuf + 1, input, size);
            } else {
                // Skip FU header
                input++;
                size--;

                // Add to concatenated buffer
                aConcatBuf = (uint8*) realloc(aConcatBuf, aConcatSize + size);
                memcpy(aConcatBuf + aConcatSize, input, size);
                aConcatSize += size;
            }

            if (end_bit) {
                // Decode the concatenated buffer
                Decode(env, aConcatBuf, aConcatSize, decoded);
            }
        } break;

        default:
            // Decode the buffer
            status = decoder->DecodeAVCSlice(input, &size);
            if (status > AVCDEC_FAIL) {
                AVCFrameIO outVid;
                decoder->GetDecOutput(&indexFrame, &releaseFrame, &outVid);
                if (releaseFrame == 1) {
                    decoder->AVC_FrameUnbind(indexFrame);
                }

                // Copy result to YUV array
                memcpy(aOutBuffer, outVid.YCbCr[0], videoWidth * videoHeight);
                memcpy(aOutBuffer + (videoWidth * videoHeight), outVid.YCbCr[1], (videoWidth * videoHeight) / 4);
                memcpy(aOutBuffer + (videoWidth * videoHeight) + ((videoWidth * videoHeight) / 4), outVid.YCbCr[2], (videoWidth * videoHeight) / 4);

                // Create the output buffer
                uint32* resultBuffer = (uint32*) malloc(videoWidth * videoHeight * sizeof(uint32));
                if (resultBuffer == NULL)
                    return 0;

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
  (JNIEnv * env, jclass clazz, jint width, jint height){
    videoWidth = width;
    videoHeight = height;

    aOutBuffer = (uint8*)malloc(videoWidth * videoHeight * 3/2);
    decoder = PVAVCDecoder::New();
    return (decoder!=NULL)?1:0;
}

/**
 * Method:    DeinitDecoder
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DeinitDecoder
  (JNIEnv * env, jclass clazz){
    free(aOutBuffer);
    delete(decoder);
    return 1;
}

/**
 * Method:    DecodeAndConvert
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DecodeAndConvert
  (JNIEnv *env, jclass clazz, jbyteArray h264Frame, jintArray decoded)
{
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

