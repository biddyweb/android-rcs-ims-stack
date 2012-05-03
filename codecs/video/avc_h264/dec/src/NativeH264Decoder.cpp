/* ------------------------------------------------------------------
 * Copyright (C) 2009 OrangeLabs
 *
 * Author: Alexis Gilabert Senar
 * Date: 2009-07-01
 * -------------------------------------------------------------------
 */
#define LOG_TAG "NativeDec"
#include "android/log.h"
#include "NativeH264Decoder.h"
#include "pvavcdecoder.h"
#include "3GPVideoParser.h"
#include "yuv2rgb.h"

#define MB_BASED_DEBLOCK

typedef enum {
  SPS,
  PPS,
  SLICE
} DEC_STATE;

/*
 * Global variables
 *
*/

  PVAVCDecoder *decoder;
  int parserInitialized = 0;
  int decoderInitialized = 0;

  uint8*	aOutBuffer;
  uint8*	aInputBuf;

  DEC_STATE 	state;
  AVCFrameIO outVid;

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder
 * Method:    InitDecoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_InitDecoder
  (JNIEnv * env, jclass clazz){

  state = SPS;
  aOutBuffer = (uint8*)malloc(176*144*3/2);
  decoder = PVAVCDecoder::New();
  return (decoder!=NULL)?1:0;
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder
 * Method:    DeinitDecoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DeinitDecoder
  (JNIEnv * env, jclass clazz){
    state = SPS;
    free(aOutBuffer);
    delete(decoder);
    return 1;
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder
 * Method:    DecodeAndConvert
 * Signature: ([B[IJ)[I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DecodeAndConvert
  (JNIEnv *env, jclass clazz, jbyteArray h264Frame, jintArray decoded)
{
  int32 size = 0;
  int32 status;
  int 		indexFrame;
  int 		releaseFrame;
  /* Set volbuf with h263Frame data*/
  jint len = env->GetArrayLength(h264Frame);
  jbyte data[len];
  env->GetByteArrayRegion(h264Frame, 0, len, data);

  aInputBuf = (uint8*)malloc(len);
  memcpy(aInputBuf,(uint8*)data,len);
  size = len;

  switch (state){
    case SPS:
      if (decoder->DecodeSPS(aInputBuf,size)==AVCDEC_SUCCESS){
		state = PPS;
      } else {
		return 0;
      }
      break;
    case PPS:
      if (decoder->DecodePPS(aInputBuf,size)==AVCDEC_SUCCESS){
		state = SLICE;
      } else {
		return 0;
      }
      break;
    case SLICE:
      if ((status=decoder->DecodeAVCSlice(aInputBuf,&size))>AVCDEC_FAIL){
		  decoder->GetDecOutput(&indexFrame,&releaseFrame,&outVid);
	
		  if (releaseFrame == 1){
		    decoder->AVC_FrameUnbind(indexFrame);
		  }
	
		  /* Copy result to YUV  array ! */
		  memcpy(aOutBuffer,outVid.YCbCr[0],176*144);
		  memcpy(aOutBuffer+(176*144),outVid.YCbCr[1],(176*144)/4);
		  memcpy(aOutBuffer+(176*144)+((176*144)/4),outVid.YCbCr[2],(176*144)/4);
		  /* Create the output buffer */
		  uint32* resultBuffer= (uint32*) malloc(176*144*sizeof(uint32));
		  if (resultBuffer == NULL) return 0;
		  
		  /**********  Convert to rgb  ***********/
		  convert(176,144,aOutBuffer,resultBuffer);
		  /* Return Bitmap image */
		  (env)->SetIntArrayRegion(decoded, 0, 176*144, (const jint*)resultBuffer);
		  free(resultBuffer);
      } else {
		__android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "status: %ld",status);
		  return 0;
      }
      break;
    default:
		__android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "unknown state %d", state);
        return 0;
  }
  return 1;
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder
 * Method:    InitParser
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_InitParser
  (JNIEnv *env, jclass clazz, jstring pathToFile){

  return 0;

}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    DeinitParser
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DeinitParser
  (JNIEnv *env, jclass clazz){
	parserInitialized = 0;
	return release();
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoLength
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getVideoLength
  (JNIEnv *env, jclass clazz)
{
  jint videoLength = getVideoDuration();
  return videoLength;
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoWidth
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getVideoWidth
  (JNIEnv *env, jclass clazz)
{
    return getVideoWidth();
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoHeight
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getVideoHeight
  (JNIEnv *env, jclass clazz)
{
    return getVideoHeight();
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoCoding
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getVideoCoding
  (JNIEnv *env, jclass clazz)
{
  char* charVideoCoding = getVideoCodec();
  jstring stringVideoCoding = (env)->NewStringUTF(charVideoCoding);
  return stringVideoCoding;
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoSample
 * Signature: ([I)Lcom/orangelabs/rcs/core/ims/protocol/rtp/codec/video/VideoSample
 */
JNIEXPORT jobject JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getVideoSample
  (JNIEnv *env, jclass clazz, jintArray Decoded)
{
	jobject object = NULL;
	// Return created object
	return object;
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

