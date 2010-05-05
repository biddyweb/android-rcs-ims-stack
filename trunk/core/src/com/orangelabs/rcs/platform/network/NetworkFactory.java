package com.orangelabs.rcs.platform.network;

import com.orangelabs.rcs.platform.FactoryException;

/**
 * Network factory 
 * 
 * @author jexa7410
 */
public abstract class NetworkFactory {
	/**
	 * Current platform factory
	 */
	private static NetworkFactory factory = null;
	
	/**
	 * Load the factory
	 * 
	 * @param classname Factory classname
	 * @throws Exception
	 */
	public static void loadFactory(String classname) throws FactoryException {
		if (factory != null) {
			return;
		}
		
		try {
			factory = (NetworkFactory)Class.forName(classname).newInstance();
		} catch(Exception e) {
			throw new FactoryException("Can't load the factory " + classname);
		}
	}
	
	/**
	 * Returns the current factory
	 * 
	 * @return Factory
	 */
	public static NetworkFactory getFactory() {
		return factory;
	}

	/**
	 * Returns the local IP address
	 * 
	 * @return Address
	 */
	public abstract String getLocalIpAddress();
	
	/**
	 * Create a datagram connection
	 * 
	 * @return Datagram connection
	 */
	public abstract DatagramConnection createDatagramConnection();

	/**
	 * Create a socket client connection
	 * 
	 * @return Socket connection
	 */
	public abstract SocketConnection createSocketClientConnection();

	/**
	 * Create a socket server connection
	 * 
	 * @return Socket server connection
	 */
	public abstract SocketServerConnection createSocketServerConnection();
	
	/**
	 * Create an HTTP connection
	 * 
	 * @return HTTP connection
	 */
	public abstract HttpConnection createHttpConnection();
}
