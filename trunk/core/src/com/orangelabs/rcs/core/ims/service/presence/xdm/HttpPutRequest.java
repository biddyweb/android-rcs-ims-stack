package com.orangelabs.rcs.core.ims.service.presence.xdm;


/**
 * HTTP PUT request
 * 
 * @author jexa7410
 */
public class HttpPutRequest extends HttpRequest {
	/**
	 * Constructor
	 * 
	 * @param url URL
	 * @param content Content
	 * @param contentType Content type
	 */
	public HttpPutRequest(String url, String content, String contentType) {
		super(url, content, contentType);
	}
	
	/**
	 * Returns the HTTP method
	 * 
	 * @return Method
	 */
	public String getMethod() {
		return "PUT";
	}
}
