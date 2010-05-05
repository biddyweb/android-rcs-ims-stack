package com.orangelabs.rcs.platform.network;

import java.io.IOException;

/**
 * Socket server connection
 * 
 * @author jexa7410
 */
public interface SocketServerConnection {
	/**
	 * Open the socket
	 * 
	 * @param port Local port
	 * @throws IOException
	 */
	public void open(int port) throws IOException;

	/**
	 * Close the socket
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 * Accept connection
	 * 
	 * @return Socket connection
	 * @throws IOException
	 */
	public SocketConnection acceptConnection() throws IOException;
}
