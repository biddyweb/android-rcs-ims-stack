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

package com.orangelabs.rcs.utils;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.H264Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.profiles.H264Profile1_2;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.profiles.H264Profile1_3;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.profiles.H264Profile1b;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.H264VideoFormat;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;
import com.orangelabs.rcs.service.api.client.media.video.VideoCodec;

/**
 * Codecs Utils
 *
 * @author hlxn7157
 *
 */
public class CodecsUtils {

    /**
     * Get list of supported video codecs according to current network
     *
     * @return codecs list
     */
    public static MediaCodec[] getSupportedCodecList() {
        int networkLevel = NetworkUtils.getNetworkLevel();
        int payload_count = H264VideoFormat.PAYLOAD - 1;
        int i = -1;

        // Set number of codecs
        int size = 1; // default
        if (networkLevel == NetworkUtils.NETWORK_LEVEL_WIFI || networkLevel == NetworkUtils.NETWORK_LEVEL_4G) {
            size = 4;
        } else if (networkLevel == NetworkUtils.NETWORK_LEVEL_3GPLUS) {
            size = 3;
        }
        MediaCodec[] supportedMediaCodecs = new MediaCodec[size];

        // Add codecs settings (ordered list)
        /*
         * 3G   -> level 1.B: PAYLOAD=96, profile-level-id=42900b, frame_rate=12, frame_size=QCIF, bit_rate=96k
         * 3G+  -> level 1.2: profile-level-id=42800c, frame_rate=10, frame_size=QVGA, bit_rate=176k
         * 3G+  -> level 1.2: profile-level-id=42800c, frame_rate=10, frame_size=CIF, bit_rate=176k
         * WIFI -> level 1.3: profile-level-id=42800d, frame_rate=15, frame_size=CIF, bit_rate=384k
         */

        if (networkLevel == NetworkUtils.NETWORK_LEVEL_WIFI || networkLevel == NetworkUtils.NETWORK_LEVEL_4G) {
            supportedMediaCodecs[++i] = new VideoCodec(H264Config.CODEC_NAME,
                    ++payload_count,
                    H264Config.CLOCK_RATE,
                    H264Config.CODEC_PARAM_PROFILEID + "=" + H264Profile1_3.BASELINE_PROFILE_ID + ";" + H264Config.CODEC_PARAM_PACKETIZATIONMODE + "=1",
                    15,
                    384000,
                    H264Config.CIF_WIDTH, 
                    H264Config.CIF_HEIGHT).getMediaCodec();
        }
        if (networkLevel >= NetworkUtils.NETWORK_LEVEL_3GPLUS) {
            supportedMediaCodecs[++i] = new VideoCodec(H264Config.CODEC_NAME,
                    ++payload_count,
                    H264Config.CLOCK_RATE,
                    H264Config.CODEC_PARAM_PROFILEID + "=" + H264Profile1_2.BASELINE_PROFILE_ID + ";" + H264Config.CODEC_PARAM_PACKETIZATIONMODE + "=1",
                    10,
                    176000,
                    H264Config.CIF_WIDTH, 
                    H264Config.CIF_HEIGHT).getMediaCodec();
            supportedMediaCodecs[++i] = new VideoCodec(H264Config.CODEC_NAME,
                    ++payload_count,
                    H264Config.CLOCK_RATE,
                    H264Config.CODEC_PARAM_PROFILEID + "=" + H264Profile1_2.BASELINE_PROFILE_ID + ";" + H264Config.CODEC_PARAM_PACKETIZATIONMODE + "=1",
                    10,
                    176000,
                    H264Config.QVGA_WIDTH, 
                    H264Config.QVGA_HEIGHT).getMediaCodec();
        }
        supportedMediaCodecs[++i] = new VideoCodec(H264Config.CODEC_NAME,
                ++payload_count,
                H264Config.CLOCK_RATE,
                H264Config.CODEC_PARAM_PROFILEID + "=" + H264Profile1b.BASELINE_PROFILE_ID + ";" + H264Config.CODEC_PARAM_PACKETIZATIONMODE + "=1",
                12,
                96000,
                H264Config.QCIF_WIDTH, 
                H264Config.QCIF_HEIGHT).getMediaCodec();

        return supportedMediaCodecs;
    }
}
