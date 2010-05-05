package com.orangelabs.rcs.core.ims.protocol.rtp.util;

/**
 * Time base
 */
public class SystemTimeBase {

	/**
	 * Offset time (start-up time)
	 */
	private static long offset = System.currentTimeMillis() * 1000000L;

	/**
	 * Returns a time base value in nanoseconds
	 * 
	 * @return Time
	 */
	public long getTime() {
		return (System.currentTimeMillis() * 1000000L) - offset;
	}
}
