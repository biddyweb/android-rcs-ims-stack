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

package com.orangelabs.rcs.core.ims.service.richcall.video;

import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.H263Config;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;
import com.orangelabs.rcs.service.api.client.media.video.VideoCodec;

/**
 * Video codec
 *
 * @author hlxn7157
 */
public class VideoCodecManager {

    /**
     * Create the SDP part for a given codec
     *
     * @param codec Codec
     * @param localRtpPort Local RTP port
     * @return SDP part
     */
    public static String createCodecSdpPart(VideoCodec codec, int localRtpPort) {
        String result = "m=video " + localRtpPort + " RTP/AVP" + " " + codec.getPayload() + SipUtils.CRLF
                + "a=rtpmap:" + codec.getPayload() + " " + codec.getCodecName() + "/" + codec.getClockRate() + SipUtils.CRLF
                + "a=framesize:" + codec.getPayload() + " " + codec.getWidth() + "-" + codec.getHeight() + SipUtils.CRLF
                + "a=framerate:" + codec.getFramerate() + SipUtils.CRLF
                + "a=fmtp:" + codec.getPayload() + " " + codec.getCodecParams() + SipUtils.CRLF;
        return result;
    }

    /**
     * Create the SDP part for a list of codecs
     *
     * @param codecs list of codecs
     * @return SDP part
     */
    public static String createCodecSdpPart(VideoCodec[] codecs, int localRtpPort) {
        StringBuffer result = new StringBuffer();

        result.append("m=video " + localRtpPort + " RTP/AVP");
        for (int i = 0; i < codecs.length; i++) {
            result.append(" " + codecs[i].getPayload());
        }
        result.append(SipUtils.CRLF);
        int framerate = 0;
        for (int i = 0; i < codecs.length; i++) {
            if (codecs[i].getFramerate() > framerate) {
                framerate = codecs[i].getFramerate();
            }
        }
        result.append("a=framerate:" + framerate + SipUtils.CRLF);
        for (int i = 0; i < codecs.length; i++) {
            result.append("a=rtpmap:"
                    + codecs[i].getPayload() + " "
                    + codecs[i].getCodecName() + "/" + codecs[i].getClockRate() + SipUtils.CRLF
                    + "a=framesize:"
                    + codecs[i].getPayload() + " "
                    + codecs[i].getWidth() + "-" + codecs[i].getHeight() + SipUtils.CRLF
                    + "a=fmtp:"
                    + codecs[i].getPayload() + " "
                    + codecs[i].getCodecParams() + SipUtils.CRLF);
        }

        return result.toString();
    }

    /**
     * Video codec negotiation
     *
     * @param supportedCodecs List of supported codecs
     * @param proposedCodecs List of proposed codecs
     * @return Selected codec or null if no codec supported
     */
    public static VideoCodec negociateVideoCodec(MediaCodec[] supportedCodecs, VideoCodec[] proposedCodecs) {
        for (int i = 0; i < proposedCodecs.length; i++) {
            for (int j = 0; j < supportedCodecs.length; j++) {
                VideoCodec videoCodec = new VideoCodec(supportedCodecs[j]);
                if (proposedCodecs[i].compare(videoCodec)) {
                    return new VideoCodec(
                            proposedCodecs[i].getCodecName(),
                            (proposedCodecs[i].getPayload()==0)?videoCodec.getPayload():proposedCodecs[i].getPayload(),
                            (proposedCodecs[i].getClockRate()==0)?videoCodec.getClockRate():proposedCodecs[i].getClockRate(),
                            (proposedCodecs[i].getCodecParams().length()==0)?videoCodec.getCodecParams():proposedCodecs[i].getCodecParams(),
                            (proposedCodecs[i].getFramerate()==0)?videoCodec.getFramerate():proposedCodecs[i].getFramerate(),
                            (proposedCodecs[i].getBitrate()==0)?videoCodec.getBitrate():proposedCodecs[i].getBitrate(),
                            proposedCodecs[i].getWidth(),
                            proposedCodecs[i].getHeight());
                }
            }
        }
        return null;
    }

    /**
     * Extract list of video codec from SDP part
     *
     * @param mediaVideo Media video description
     * @return List of video codec
     */
    public static VideoCodec[] extractVideoCodecsFromSdp(MediaDescription mediaVideo) {
    	try {
	        String rtpmap = mediaVideo.getMediaAttribute("rtpmap").getValue();
	
	        // Extract encoding name
	        String encoding = rtpmap.substring(
	        		rtpmap.indexOf(mediaVideo.payload) + mediaVideo.payload.length() + 1);
	        String codecName = encoding.toLowerCase().trim();
	
	        // Extract clock rate
	        int clockRate = 0;
	        int index = encoding.indexOf("/");
	        if (index != -1) {
	            codecName = encoding.substring(0, index);
	            clockRate = Integer.parseInt(encoding.substring(index + 1));
	        }
	
	        // Extract video size
	        MediaAttribute frameSize = mediaVideo.getMediaAttribute("framesize");
	        int videoWidth = H263Config.VIDEO_WIDTH; // default value
	        int videoHeight = H263Config.VIDEO_HEIGHT; // default value
	        if (frameSize != null) {
	        	try {
		            String value = frameSize.getValue();
		            int index2 = value.indexOf(mediaVideo.payload);
		            int separator = value.indexOf('-');
		            if ((index2 != -1) && (separator != -1)) {
			            videoWidth = Integer.parseInt(
			            		value.substring(index2 + mediaVideo.payload.length() + 1,
			                    separator));
			            videoHeight = Integer.parseInt(value.substring(separator + 1));
		            }
	        	} catch(NumberFormatException e) {
	        		// Use default value
	        	}
	        }
	        
	        // Extract frame rate
	        MediaAttribute attr = mediaVideo.getMediaAttribute("framerate");
	        int frameRate = H263Config.FRAME_RATE; // default value
	        if (attr != null) {
	            frameRate = Integer.parseInt(attr.getValue());
	        }
	
	        // Extract the video codec parameters.
	        MediaAttribute fmtp = mediaVideo.getMediaAttribute("fmtp");
	        String codecParameters = "";
	        if (fmtp != null) {
	            String value = fmtp.getValue();
	            int index2 = value.indexOf(mediaVideo.payload);
	            if ((index2 > 0) && (value.length() > mediaVideo.payload.length())) {
	            	codecParameters = value.substring(index2 + mediaVideo.payload.length() + 1);
	            }
	        }

	        // Create a video codec
	        VideoCodec[] sdpVideoCodecs = new VideoCodec[1];
	        sdpVideoCodecs[0] = new VideoCodec(codecName,
	        		Integer.parseInt(mediaVideo.payload), clockRate,
	        		codecParameters, frameRate, 0,
	                videoWidth, videoHeight);
	        return sdpVideoCodecs;
    	} catch(NullPointerException e) {
        	return new VideoCodec[0];
		} catch(IndexOutOfBoundsException e) {
        	return new VideoCodec[0];
		}
    }
}
