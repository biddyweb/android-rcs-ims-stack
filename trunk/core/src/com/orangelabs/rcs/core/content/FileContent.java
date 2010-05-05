package com.orangelabs.rcs.core.content;


/**
 * File content
 * 
 * @author jexa7410
 */
public class FileContent extends MmContent {
	/**
	 * Constructor
	 * 
	 * @param url URL
	 * @param size Content size
	 */
	public FileContent(String url, long size) {
		super(url, "application/octet-stream", size);
	}
	/**
	 * Constructor
	 * 
	 * @param url URL
	 */
	public FileContent(String url) {
		super(url, "application/octet-stream");
	}
}
