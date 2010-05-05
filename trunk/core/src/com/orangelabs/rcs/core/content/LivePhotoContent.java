package com.orangelabs.rcs.core.content;

/**
 * Live photo content
 * 
 * @author jexa7410
 */
public class LivePhotoContent extends PhotoContent {
	/**
	 * Constructor
	 * 
	 * @param url URL
	 * @param encoding Encoding
	 * @param photo Photo
	 */
	public LivePhotoContent(String url, String encoding, byte[] photo) {
		super(url, encoding, photo.length);
		
		setData(photo);
	}
	
	/**
	 * Constructor
	 * 
	 * @param url URL
	 * @aparam encoding Encoding
	 * @param size Content size
	 */
	public LivePhotoContent(String url, String encoding, long size) {
		super(url, encoding, size);
	}
}
