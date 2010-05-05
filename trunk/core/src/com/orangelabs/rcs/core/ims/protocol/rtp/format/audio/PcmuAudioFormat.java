package com.orangelabs.rcs.core.ims.protocol.rtp.format.audio;

/**
 * G711 PCMU audio format
 * 
 * @author jexa7410
 */
public class PcmuAudioFormat extends AudioFormat {

	/**
	 * Encoding name
	 */
	public static final String ENCODING = "pcmu";
	
	/**
	 * Payload type
	 */
	public static final int PAYLOAD = 0;
	
	/**
	 * Constructor
	 */
	public PcmuAudioFormat() {
		super(ENCODING, PAYLOAD);
	}
}
