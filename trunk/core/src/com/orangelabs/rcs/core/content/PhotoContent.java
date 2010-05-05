package com.orangelabs.rcs.core.content;


/**
 * Photo content
 * 
 * @author jexa7410
 */
public class PhotoContent extends MmContent {

	/**
	 * Constructor
	 * 
	 * @param url URL
	 * @aparam encoding Encoding
	 * @param size Content size
	 */
	public PhotoContent(String url, String encoding, long size) {
		super(url, encoding, size);
	}

	/**
	 * Constructor
	 * 
	 * @param url URL
	 * @aparam encoding Encoding
	 */
	public PhotoContent(String url, String encoding) {
		super(url, encoding);
	}
}
