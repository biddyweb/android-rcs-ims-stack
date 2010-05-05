package com.orangelabs.rcs.platform.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * HTTP connection
 * 
 * @author jexa7410
 */
public interface HttpConnection {
	/**
	 * GET method
	 */
	public final static String GET_METHOD = "GET";
	
	/**
	 * POST method
	 */
	public final static String POST_METHOD = "POST";

	/**
	 * Open the HTTP connection
	 * 
	 * @param url Remote URL
	 * @throws IOException
	 */
	public void open(String url) throws IOException;

	/**
	 * Close the HTTP connection
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 * HTTP GET request
	 * 
	 * @return Response
	 * @throws IOException
	 */
	public ByteArrayOutputStream get() throws IOException;
	
	/**
	 * HTTP POST request
	 * 
	 * @return Response
	 * @throws IOException
	 */
	public ByteArrayOutputStream post() throws IOException;
}