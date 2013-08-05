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

package com.orangelabs.rcs.core.ims.service.ipcall;

import java.util.Vector;

import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;
import com.orangelabs.rcs.service.api.client.media.audio.AudioCodec;

/**
 * Builds the Audio SDP
 * 
 * @author Olivier Briand
 */
public class AudioSdpBuilder {

    /**
     * Creates the SIP INVITE SDP
     * 
     * @param localRtpPort Local RTP port
     * @return The SDP 
     */
    public static String buildSdp(MediaCodec[] supportedCodecs, int localRtpPort) {
        StringBuilder result = new StringBuilder();
        
        // Create video codec list
        Vector<AudioCodec> codecs = new Vector<AudioCodec>();
        for (int i = 0; i < supportedCodecs.length; i++) {
            codecs.add(new AudioCodec(supportedCodecs[i]));
        }
        
        // First Sdp line
        result.append("m=audio " + localRtpPort + " RTP/AVP");
        for (AudioCodec codec : codecs) {
            result.append(" ").append(codec.getPayload());
        }
        result.append(SipUtils.CRLF);
        
        // For each codecs
        for (AudioCodec codec : codecs) {
            result.append("a=rtpmap:" + codec.getPayload() + " " + codec.getCodecName() + "/" + codec.getSamplerate() + SipUtils.CRLF);
            if (!codec.getCodecParams().equals("")) result.append("a=fmtp:" + codec.getPayload() + " " + codec.getCodecParams() + SipUtils.CRLF);
        }

        return result.toString();
    }

    /**
     * Builds the SDP for a SIP INVITE response. If the SIP
     * 
     * @param selectedMediaCodec selected audio codec after negociation
     * @param localRtpPort Local RTP Port
     * @return Response SDP
     */
    public static String buildResponseSdp(MediaCodec selectedMediaCodec, int localRtpPort) {
    	
    	StringBuilder result = new StringBuilder();

        result.append("m=audio " + localRtpPort + " RTP/AVP");
        AudioCodec codec = new AudioCodec(selectedMediaCodec);
        result.append(" ").append(codec.getPayload());
        result.append(SipUtils.CRLF);
        result.append("a=rtpmap:" + codec.getPayload() + " " + codec.getCodecName() + "/" + codec.getSamplerate() + SipUtils.CRLF);

        return result.toString();
    }
}
