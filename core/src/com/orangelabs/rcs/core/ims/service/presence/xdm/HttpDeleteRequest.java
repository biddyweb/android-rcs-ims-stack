package com.orangelabs.rcs.core.ims.service.presence.xdm;

/**
 * HTTP DELETE request
 * 
 * @author jexa7410
 */
public class HttpDeleteRequest extends HttpRequest {
	/**
	 * Constructor
	 * 
	 * @param url URL
	 */
	public HttpDeleteRequest(String url) {
		super(url, null, null);
	}
	
	/**
	 * Returns the HTTP method
	 * 
	 * @return Method
	 */
	public String getMethod() {
		return "DELETE";
	}
}
