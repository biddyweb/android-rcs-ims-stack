package com.orangelabs.rcs.core.ims.protocol.rtp.format.text;

import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;

/**
 * Text format
 */
public class T140Format extends Format {
	/**
	 * Encoding name
	 */
	public static final String ENCODING = "t140";
	
	/**
	 * Payload type
	 */
	public static final int PAYLOAD = 98;
	
	/**
	 * Constructor
	 */
	public T140Format() {
		super(ENCODING, PAYLOAD);
	}

    /**
     * Constructor
     * 
     * @param codec Codec
     * @param payload Payload type
     */
    public T140Format(String codec, int payload) {
    	super(codec, payload);
    }
}
