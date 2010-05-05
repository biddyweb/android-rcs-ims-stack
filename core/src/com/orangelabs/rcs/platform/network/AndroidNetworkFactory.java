package com.orangelabs.rcs.platform.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Android network factory
 * 
 * @author jexa7410
 */
public class AndroidNetworkFactory extends NetworkFactory {
	
	/**
	 * Returns the local IP address
	 *
	 * @return IP address
	 */
	public String getLocalIpAddress() {
		try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = (NetworkInterface)en.nextElement();
	            for (Enumeration<InetAddress> addr = intf.getInetAddresses(); addr.hasMoreElements();) {
	                InetAddress inetAddress = (InetAddress)addr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                	return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	        return null;
		} catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * Create a datagram connection
	 * 
	 * @return Datagram connection
	 */
	public DatagramConnection createDatagramConnection() {
		return new AndroidDatagramConnection();
	}

	/**
	 * Create a socket client connection
	 * 
	 * @return Socket connection
	 */
	public SocketConnection createSocketClientConnection() {
		return new AndroidSocketConnection();
	}

	/**
	 * Create a socket server connection
	 * 
	 * @return Socket server connection
	 */
	public SocketServerConnection createSocketServerConnection() {
		return new AndroidSocketServerConnection();
	}

	/**
	 * Create an HTTP connection
	 * 
	 * @return HTTP connection
	 */
	public HttpConnection createHttpConnection() {
		return new AndroidHttpConnection();
	}
}