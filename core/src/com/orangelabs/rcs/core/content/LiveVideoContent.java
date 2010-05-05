package com.orangelabs.rcs.core.content;


/**
 * Live video content
 * 
 * @author jexa7410
 */
public class LiveVideoContent extends VideoContent {
	/**
	 * Constructor
	 * 
	 * @param encoding Encoding
	 */
	public LiveVideoContent(String encoding) {
		super("capture://video", encoding);
	}
}
