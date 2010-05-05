package com.orangelabs.rcs.core.content;


/**
 * Video content
 * 
 * @author jexa7410
 */
public class VideoContent extends MmContent {
	/**
	 * Constructor
	 * 
	 * @param url URL
	 * @aparam encoding Encoding
	 * @param size Content size
	 */
	public VideoContent(String url, String encoding, long size) {
		super(url, encoding, size);
	}

	/**
	 * Constructor
	 * 
	 * @param url URL
	 * @aparam encoding Encoding
	 */
	public VideoContent(String url, String encoding) {
		super(url, encoding);
	}	
}
