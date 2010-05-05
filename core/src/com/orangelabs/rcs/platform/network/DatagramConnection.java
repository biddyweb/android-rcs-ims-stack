package com.orangelabs.rcs.platform.network;

import java.io.IOException;

/**
 * Datagram connection
 * 
 * @author jexa7410
 */
public interface DatagramConnection {
	/**
	 * Default datagram packet size
	 */
	public static int DEFAULT_DATAGRAM_SIZE = 4096 * 8;
	
	/**
	 * Open the datagram connection
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException;
	
	/**
	 * Open the datagram connection
	 * 
	 * @param port Local port
	 * @throws IOException
	 */
	public void open(int port) throws IOException;

	/**
	 * Close the datagram connection
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 * Send data
	 * 
	 * @param remoteAddr Remote address
	 * @param remotePort Remote port
	 * @param data Data as byte array
	 * @throws IOException
	 */
	public void send(String remoteAddr, int remotePort, byte[] data) throws IOException;
	
	/**
	 * Receive data
	 * 
	 * @return Byte array
	 * @throws IOException
	 */
	public byte[] receive() throws IOException;
	
	/**
	 * Receive data with a specific buffer size
	 * 
	 * @param bufferSize Buffer size 
	 * @return Byte array
	 * @throws IOException
	 */
	public byte[] receive(int bufferSize) throws IOException;	
	
	/**
	 * Returns the local address
	 * 
	 * @return Address
	 * @throws IOException
	 */
	public String getLocalAddress() throws IOException;

	/**
	 * Returns the local port
	 * 
	 * @return Port
	 * @throws IOException
	 */
	public int getLocalPort() throws IOException;
}