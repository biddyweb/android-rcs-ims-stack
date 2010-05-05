package com.orangelabs.rcs.core.media;

/**
 * Media listener
 * 
 * @author jexa7410
 */
public interface MediaListener {
	/**
	 * Media is started
	 */
	public void mediaStarted();
	
	/**
	 * Media is stopped
	 */
	public void mediaStopped();

	/**
	 * Media has failed
	 * 
	 * @param error Error code
	 */
	public void mediaError(String error);
}
