package com.orangelabs.rcs.core.ims.service.presence.xdm;

/**
 * HTTP GET request
 * 
 * @author jexa7410
 */
public class HttpGetRequest extends HttpRequest {
	/**
	 * Constructor
	 * 
	 * @param url URL
	 */
	public HttpGetRequest(String url) {
		super(url, null, null);
	}
	
	/**
	 * Returns the HTTP method
	 * 
	 * @return Method
	 */
	public String getMethod() {
		return "GET";
	}
}
