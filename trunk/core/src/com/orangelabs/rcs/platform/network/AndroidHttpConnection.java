package com.orangelabs.rcs.platform.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Android HTTP connection
 * 
 * @author jexa7410
 */
public class AndroidHttpConnection implements HttpConnection {
	/**
	 * HTTP connection
	 */
	private HttpURLConnection connection = null;
	
	/**
	 * Open the HTTP connection
	 * 
	 * @param url Remote URL
	 * @throws IOException
	 */
	public void open(String url) throws IOException {
		URL urlConn = new URL(url);
		connection = (HttpURLConnection)urlConn.openConnection();
		connection.connect();
	}

	/**
	 * Close the HTTP connection
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (connection != null) {
			connection.disconnect();
		}
	}
	
	/**
	 * HTTP GET request
	 * 
	 * @return Response
	 * @throws IOException
	 */
	public ByteArrayOutputStream get() throws IOException {
		if (connection != null) {
			return sendHttpRequest(HttpConnection.GET_METHOD);
		} else {
			throw new IOException("Connection not openned");
		}
	}
	
	/**
	 * HTTP POST request
	 * 
	 * @return Response
	 * @throws IOException
	 */
	public ByteArrayOutputStream post() throws IOException {
		if (connection != null) {
			return sendHttpRequest(HttpConnection.POST_METHOD);
		} else {
			throw new IOException("Connection not openned");
		}
	}
	
	/**
	 * Send HTTP request
	 * 
	 * @param method HTTP method
	 * @return Response
	 * @throws IOException
	 */
	private ByteArrayOutputStream sendHttpRequest(String method) throws IOException {
        connection.setRequestMethod(method);
        int rc = connection.getResponseCode();
        if (rc != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error " + rc);
        }
        
        InputStream inputStream = connection.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
    	int ch;
    	while((ch = inputStream.read()) != -1) {
    		result.write(ch);
    	}
    	inputStream.close();
    	
        return result;		
	}
}