package com.orangelabs.rcs.core.ims.protocol.rtp.format.audio;

import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;

/**
 * Audio format
 */
public class AudioFormat extends Format {
    /**
     * Constructor
     * 
     * @param codec Codec
     * @param payload Payload type
     */
    public AudioFormat(String codec, int payload) {
    	super(codec, payload);
    }
}
