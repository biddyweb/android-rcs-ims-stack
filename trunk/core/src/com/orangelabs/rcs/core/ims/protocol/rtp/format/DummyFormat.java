package com.orangelabs.rcs.core.ims.protocol.rtp.format;

/**
 * Dummy format 
 * 
 * @author jexa7410
 */
public class DummyFormat extends Format {
	
	/**
	 * Encoding name
	 */
	public static final String ENCODING = "dummy";
	
	/**
	 * Payload type
	 */
	public static final int PAYLOAD = 12;

	/**
	 * Constructor
	 */
	public DummyFormat() {
		super(ENCODING, PAYLOAD);
	}

	/**
	 * Get the size of a chunk of data from the source
	 * 
	 * @return The minimum size of the buffer needed to read a chunk of data
	 */
    public int getDataChunkSize() {
    	return 0;
    }
}
