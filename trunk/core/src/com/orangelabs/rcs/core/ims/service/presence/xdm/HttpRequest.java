package com.orangelabs.rcs.core.ims.service.presence.xdm;


/**
 * HTTP request
 * 
 * @author jexa7410
 */
public abstract class HttpRequest {
	/**
	 * URL
	 */
	private String url;

	/**
	 * Content
	 */
	private String content;
	
	/**
	 * Content type
	 */
	private String contentType;
	
	/**
	 * Cookie
	 */
	private String cookie = null;

	/**
	 * HTTP authentication agent MD5
	 */
	private HttpAuthenticationAgent authenticationAgent = new HttpAuthenticationAgent();

    /**
	 * Constructor
	 * 
	 * @param url URL
	 * @param content Content
	 * @param contentType Content type
	 */
	public HttpRequest(String url, String content, String contentType) {
		this.url = url;
		this.content = content;
		this.contentType = contentType;
	}

	/**
	 * Returns the authentication agent
	 * 
	 * @return Authentication agent
	 */
	public HttpAuthenticationAgent getAuthenticationAgent() {
		return authenticationAgent;
	}
	
	/**
	 * Returns the HTTP method
	 * 
	 * @return Method
	 */
	public abstract String getMethod();
	
	/**
	 * Returns the HTTP URL
	 * 
	 * @return URL
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Returns the HTTP content
	 * 
	 * @return Conetnt
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * Returns the HTTP content
	 * 
	 * @return Conetnt
	 */
	public int getContentLength() {
		int length = 0;
		if (content != null) {
			length = content.length();
		}
		return length;
	}

	/**
	 * Returns the content type
	 * 
	 * @return Mime content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Returns the cookie
	 * 
	 * @return Cookie
	 */
	public String getCookie() {
		return cookie;
	}

	/**
	 * Set the cookie
	 * 
	 * @param cookie Cookie
	 */
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
}
