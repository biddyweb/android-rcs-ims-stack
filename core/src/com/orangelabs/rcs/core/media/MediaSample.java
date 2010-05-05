package com.orangelabs.rcs.core.media;

/**
 * Media sample
 * 
 * @author jexa7410
 */
public class MediaSample {

	/**
	 * Data
	 */
	private byte[] data;
	
	/**
	 * Time stamp
	 */
	private long time;
	
	/**
	 * Constructor
	 * 
	 * @param data Data
	 * @param time Time stamp
	 */
	public MediaSample(byte[] data, long time) {
		this.data = data;
		this.time = time;
	}

	/**
	 * Returns the data sample
	 * 
	 * @return Byte array
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Returns the length of the data sample
	 * 
	 * @return Data sample length
	 */
	public int getLength() {
		if (data != null) {
			return data.length;
		} else {
			return 0;
		}
	}

	/**
	 * Returns the time stamp of the sample
	 * 
	 * @return Time in microseconds
	 */
	public long getTimeStamp() {
		return time;
	}	
}
