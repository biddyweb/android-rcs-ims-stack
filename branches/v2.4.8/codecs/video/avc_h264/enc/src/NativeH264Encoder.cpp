/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
 
#define LOG_TAG "NativeEnc"
#include "NativeH264Encoder.h"
#include "pvavcencoder.h"
#include "android/log.h"

int FrameSize;

/** variables needed in operation */
PVAVCEncoder *encoder;
TAVCEIInputFormat *iInputFormat;
TAVCEIEncodeParam *iEncodeParam;
TAVCEIInputData *iInData;
TAVCEIOutputData *iOutData;
TAVCEI_RETVAL status;

/** Returned values. */
enum INIT_RETVAL {
    SUCCESS             = EAVCEI_SUCCESS,
    FAIL                = -1,
    GETPARAMS_FAIL      = -2,
    FRAMEWIDTH_FAIL     = -3,
    FRAMEHEIGHT_FAIL    = -4,
    FRAMERATE_FAIL      = -5,
    ENCODER_CREATE_FAIL = -6,
    MALLOC_FAIL         = -7,
    GETFIELD_FAIL       = -8
};

/*
 * Method:    InitEncoder
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder_InitEncoder
  (JNIEnv *env, jclass iclass, jobject params)
{
    jclass objClass = (env)->GetObjectClass(params);
    if (objClass == NULL) {
        return GETPARAMS_FAIL;
    }

    jfieldID iFrameWidth = (env)->GetFieldID(objClass,"frameWidth","I");
    if (iFrameWidth == NULL) {
        return FRAMEWIDTH_FAIL;
    }
    int iSrcWidth = (env)->GetIntField(params,iFrameWidth);

    jfieldID iFrameHeight = (env)->GetFieldID(objClass,"frameHeight","I");
    if (iFrameHeight == NULL) {
        return FRAMEHEIGHT_FAIL;
    }
    int iSrcHeight = (env)->GetIntField(params,iFrameHeight);

    jfieldID iFrameRate = (env)->GetFieldID(objClass,"frameRate","F");
    if (iFrameRate == NULL) {
        return FRAMERATE_FAIL;
    }
    int iSrcFrameRate = (env)->GetFloatField(params,iFrameRate);
    FrameSize = (iSrcWidth*iSrcHeight*3)>>1;

    encoder = PVAVCEncoder::New();
    if (encoder==NULL) {
        return ENCODER_CREATE_FAIL;
    }

    iInputFormat = (TAVCEIInputFormat*)malloc(sizeof(TAVCEIInputFormat));
    if (iInputFormat==NULL) {
        delete(encoder);
        return MALLOC_FAIL;
    }

    iEncodeParam = (TAVCEIEncodeParam*)malloc(sizeof(TAVCEIEncodeParam));
    if (iEncodeParam==NULL) {
      free(iInputFormat);
      delete(encoder);
      return MALLOC_FAIL;
    }

    iInData = (TAVCEIInputData*)malloc(sizeof(TAVCEIInputData));
    if(iInData==NULL){
      free(iEncodeParam);
      free(iInputFormat);
      delete(encoder);
      return MALLOC_FAIL;
    }

    iOutData = (TAVCEIOutputData*)malloc(sizeof(TAVCEIOutputData));
    if(iOutData==NULL){
      free(iInData);
      free(iEncodeParam);
      free(iInputFormat);
      delete(encoder);
      return MALLOC_FAIL;
    }
    iOutData->iBitstream = (uint8*)malloc(FrameSize);
    iOutData->iBitstreamSize = FrameSize;

    // Set Encoder params
    iInputFormat->iFrameWidth = iSrcWidth;
    iInputFormat->iFrameHeight = iSrcHeight;
    iInputFormat->iFrameRate = (OsclFloat)(iSrcFrameRate);


    jfieldID iFrameOrientation = (env)->GetFieldID(objClass,"frameOrientation","I");
    if (iFrameOrientation == NULL) return GETFIELD_FAIL;
    iInputFormat->iFrameOrientation = (env)->GetIntField(params,iFrameOrientation);

    jfieldID iVideoFormat = (env)->GetFieldID(objClass,"videoFormat","I");
    if (iVideoFormat == NULL) return GETFIELD_FAIL;
    iInputFormat->iVideoFormat = (TAVCEIVideoFormat)(env)->GetIntField(params,iVideoFormat);

    jfieldID iEncodeID = (env)->GetFieldID(objClass,"encodeID","I");
    if (iEncodeID == NULL) return GETFIELD_FAIL;
    iEncodeParam->iEncodeID = (env)->GetIntField(params,iEncodeID);

    jfieldID iProfile = (env)->GetFieldID(objClass,"profile","I");
    if (iProfile == NULL) return GETFIELD_FAIL;
    iEncodeParam->iProfile = (TAVCEIProfile)(env)->GetIntField(params,iProfile);

    jfieldID iLevel = (env)->GetFieldID(objClass,"level","I");
    if (iLevel == NULL) return GETFIELD_FAIL;
    iEncodeParam->iLevel = (TAVCEILevel)(env)->GetIntField(params,iLevel);

    jfieldID iNumLayer = (env)->GetFieldID(objClass,"numLayer","I");
    if (iNumLayer == NULL) return GETFIELD_FAIL;
    iEncodeParam->iNumLayer = (env)->GetIntField(params,iNumLayer);

    iEncodeParam->iFrameWidth[0] = iInputFormat->iFrameWidth;
    iEncodeParam->iFrameHeight[0] = iInputFormat->iFrameHeight;

    jfieldID iBitRate = (env)->GetFieldID(objClass,"bitRate","I");
    if (iBitRate == NULL) return GETFIELD_FAIL;
    iEncodeParam->iBitRate[0] = (env)->GetIntField(params,iBitRate);

    iEncodeParam->iFrameRate[0] = (OsclFloat)iInputFormat->iFrameRate;

    jfieldID iEncMode = (env)->GetFieldID(objClass,"encMode","I");
    if (iEncMode == NULL) return GETFIELD_FAIL;
    iEncodeParam->iEncMode = (TAVCEIEncodingMode)(env)->GetIntField(params,iEncMode);

    jfieldID iOutOfBandParamSet = (env)->GetFieldID(objClass,"outOfBandParamSet","Z");
    if (iOutOfBandParamSet == NULL) return GETFIELD_FAIL;
    iEncodeParam->iOutOfBandParamSet = (env)->GetBooleanField(params,iOutOfBandParamSet);

    jfieldID iOutputFormat = (env)->GetFieldID(objClass,"outputFormat","I");
    if (iOutputFormat == NULL) return GETFIELD_FAIL;
    iEncodeParam->iOutputFormat = (TAVCEIOutputFormat)(env)->GetIntField(params,iOutputFormat);

    jfieldID iPacketSize = (env)->GetFieldID(objClass,"packetSize","I");
    if (iPacketSize == NULL) return GETFIELD_FAIL;
    iEncodeParam->iPacketSize = (env)->GetIntField(params,iPacketSize);

    jfieldID iRateControlType = (env)->GetFieldID(objClass,"rateControlType","I");
    if (iRateControlType == NULL) return GETFIELD_FAIL;
    iEncodeParam->iRateControlType = (TAVCEIRateControlType)(env)->GetIntField(params,iRateControlType);

    jfieldID iBufferDelay = (env)->GetFieldID(objClass,"bufferDelay","F");
    if (iBufferDelay == NULL) return GETFIELD_FAIL;
    iEncodeParam->iBufferDelay = (OsclFloat)(env)->GetFloatField(params,iBufferDelay);

    jfieldID iIquant = (env)->GetFieldID(objClass,"iquant","I");
    if (iIquant == NULL) return GETFIELD_FAIL;
    iEncodeParam->iIquant[0] = (env)->GetIntField(params,iIquant);

    jfieldID iPquant = (env)->GetFieldID(objClass,"pquant","I");
    if (iPquant == NULL) return GETFIELD_FAIL;
    iEncodeParam->iPquant[0] = (env)->GetIntField(params,iPquant);

    jfieldID iBquant = (env)->GetFieldID(objClass,"bquant","I");
    if (iBquant == NULL) return GETFIELD_FAIL;
    iEncodeParam->iBquant[0] = (env)->GetIntField(params,iBquant);

    jfieldID iSceneDetection = (env)->GetFieldID(objClass,"sceneDetection","Z");
    if (iSceneDetection == NULL) return GETFIELD_FAIL;
    iEncodeParam->iSceneDetection = (env)->GetBooleanField(params,iSceneDetection);

    jfieldID iIFrameInterval = (env)->GetFieldID(objClass,"iFrameInterval","I");
    if (iIFrameInterval == NULL) return GETFIELD_FAIL;
    iEncodeParam->iIFrameInterval = (env)->GetIntField(params,iIFrameInterval);

    jfieldID iNumIntraMBRefresh = (env)->GetFieldID(objClass,"numIntraMBRefresh","I");
    if (iNumIntraMBRefresh == NULL) return GETFIELD_FAIL;
    iEncodeParam->iNumIntraMBRefresh = (env)->GetIntField(params,iNumIntraMBRefresh);

    jfieldID iFSIBuffLength = (env)->GetFieldID(objClass,"fSIBuffLength","I");
    if (iFSIBuffLength == NULL) return GETFIELD_FAIL;
    iEncodeParam->iFSIBuffLength = (env)->GetIntField(params,iFSIBuffLength);

    if(iEncodeParam->iFSIBuffLength > 0) {
        jfieldID iFSIBuff = (env)->GetFieldID(objClass,"fSIBuff","[B");
        if (iFSIBuff == NULL) return GETFIELD_FAIL;

        jbyteArray iFSIBuffByteArray = (jbyteArray)(env)->GetObjectField(params,iFSIBuff);
        if (iFSIBuffByteArray == NULL) return GETFIELD_FAIL;

        iEncodeParam->iFSIBuff = (uint8*) malloc(iEncodeParam->iFSIBuffLength * sizeof(jbyte));
        jbyte* originaliFSIBuff = env->GetByteArrayElements(iFSIBuffByteArray, 0);
        memcpy(iEncodeParam->iFSIBuff, originaliFSIBuff, iEncodeParam->iFSIBuffLength);
        if (originaliFSIBuff)
            env->ReleaseByteArrayElements(iFSIBuffByteArray, originaliFSIBuff, JNI_ABORT);
    } else {
        iEncodeParam->iFSIBuff = NULL;
    }

    // Init encoder
    jint result = encoder->Initialize(iInputFormat,iEncodeParam);
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Init encoder %d", result);
    return result;
	

    // Set Encoder params
    /*
	iInputFormat->iFrameWidth = width;
    iInputFormat->iFrameHeight = height;
    iInputFormat->iFrameRate = (OsclFloat)(framerate);
    iInputFormat->iFrameOrientation = 0;
    iInputFormat->iVideoFormat = EAVCEI_VDOFMT_YUV420SEMIPLANAR;

    iEncodeParam->iEncodeID = 0;
    iEncodeParam->iProfile = EAVCEI_PROFILE_BASELINE;
    iEncodeParam->iLevel = EAVCEI_LEVEL_13 ;// EAVCEI_LEVEL_1B;
    iEncodeParam->iNumLayer = 1;
    iEncodeParam->iFrameWidth[0] = iInputFormat->iFrameWidth;
    iEncodeParam->iFrameHeight[0] = iInputFormat->iFrameHeight;
    iEncodeParam->iBitRate[0] = 96000;
    iEncodeParam->iFrameRate[0] = (OsclFloat)iInputFormat->iFrameRate;
    iEncodeParam->iEncMode = EAVCEI_ENCMODE_TWOWAY;  // EAVCEI_ENCMODE_STREAMING EAVCEI_ENCMODE_TWOWAY;
    iEncodeParam->iOutOfBandParamSet = true;
    iEncodeParam->iOutputFormat = EAVCEI_OUTPUT_RTP;
    iEncodeParam->iPacketSize = 1400;
    iEncodeParam->iRateControlType = EAVCEI_RC_CBR_1;
    iEncodeParam->iBufferDelay = (OsclFloat)2.0;
    iEncodeParam->iIquant[0]=15;
    iEncodeParam->iPquant[0]=12;
    iEncodeParam->iBquant[0]=0;
    iEncodeParam->iSceneDetection = true; //false;
    iEncodeParam->iIFrameInterval = 3; //15;
    iEncodeParam->iNumIntraMBRefresh = 50;
    iEncodeParam->iClipDuration = 0;
    iEncodeParam->iFSIBuff = NULL;
    iEncodeParam->iFSIBuffLength = 0;
	*/
}

/*
 * Method:    GetNAL
 */
JNIEXPORT jbyteArray JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder_getNAL
  (JNIEnv *env, jclass iclass)
{
    jbyteArray result;
    int32 NalSize = 30;
    int NalType = 0;
    uint8* NalBuff = (uint8*)malloc(NalSize*sizeof(uint8));
    if (encoder->GetParameterSet(NalBuff,&NalSize,&NalType)== EAVCEI_SUCCESS) {
        result = (env)->NewByteArray(NalSize);
        (env)->SetByteArrayRegion(result, 0, NalSize, (jbyte*)NalBuff);
    } else {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,  "NAL fail with code: %d",status);
        result = (env)->NewByteArray(0);
    }
    free(NalBuff);
    return result;
}

/*
 * Method:    EncodeFrame
 */
JNIEXPORT jbyteArray JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder_EncodeFrame
  (JNIEnv *env, jclass iclass, jbyteArray frame, jlong timestamp)
{
    jbyteArray result;

    // EncodeFrame
    jint len = env->GetArrayLength(frame);
    uint8* data = (uint8*)malloc(len);
    env->GetByteArrayRegion (frame, (jint)0, (jint)len, (jbyte*)data);

    iInData->iSource=(uint8*)data;
    iInData->iTimeStamp = timestamp;
    status = encoder->Encode(iInData);
    if(status != EAVCEI_SUCCESS){
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode fail with code: %d",status);
        result=(env)->NewByteArray(0);
        free(data);
        return result;
    }

    int remainingByte = 0;
    iOutData->iBitstreamSize = FrameSize;
    status = encoder->GetOutput(iOutData,&remainingByte);
    if(status != EAVCEI_SUCCESS){
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Get output fail with code: %d",status);
        result=(env)->NewByteArray(0);
        free(data);
        return result;
    }

    // Copy aOutBuffer into result
    result=(env)->NewByteArray(iOutData->iBitstreamSize);
    (env)->SetByteArrayRegion(result, 0, iOutData->iBitstreamSize, (jbyte*)iOutData->iBitstream);
    free(data);
    return result;
}

/*
 * Method:    getLastEncodeStatus
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder_getLastEncodeStatus
  (JNIEnv *env, jclass clazz){
    return status;
}

/*
 * Method:    DeinitEncoder
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder_DeinitEncoder
  (JNIEnv *env, jclass clazz){
    delete(encoder);
    free(iInputFormat);
    free(iEncodeParam);
    free(iInData);
    free(iOutData);
    return 1;
}

/*
 * This is called by the VM when the shared library is first loaded.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }

    // success -- return valid version number
    result = JNI_VERSION_1_4;

bail:
    return result;
}
