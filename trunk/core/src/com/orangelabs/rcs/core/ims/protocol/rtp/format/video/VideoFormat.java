package com.orangelabs.rcs.core.ims.protocol.rtp.format.video;

import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;

/**
 * Video format
 */
public class VideoFormat extends Format {
    /**
     * Constructor
     * 
     * @param codec Codec
     * @param payload Payload type
     */
    public VideoFormat(String codec, int payload) {
    	super(codec, payload);
    }
}
