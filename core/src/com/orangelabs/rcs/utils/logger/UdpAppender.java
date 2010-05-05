package com.orangelabs.rcs.utils.logger;

import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;

/**
 * UDP appender 
 * 
 * @author jexa7410
 */
public class UdpAppender extends Appender {
	
	/**
	 * Unique ID
	 */
	private static long ID = System.currentTimeMillis() + Class.class.hashCode();

	/**
	 * Remote console address
	 */
	public static String REMOTE_CONSOLE_ADDR = "172.20.41.60";
	
	/**
	 * Remote console port
	 */
	public static int REMOTE_CONSOLE_PORT = 1000;

	/**
	 * Datagram connection
	 */
	private DatagramConnection datagram = null;
	
	/**
	 * Constructor
	 */
	public UdpAppender() {
		super();
	}

	/**
	 * Print a trace
	 *
	 * @param classname Classname
	 * @param level Trace level
	 * @param trace Trace
	 */
	public synchronized void printTrace(String classname, int level, String trace) {
		sendTrace("[" + LoggerUtils.formatDate() + "][" +
				LoggerUtils.formatClassname(classname) + "][" + LoggerUtils.formatLevel(level) + "]" + trace);
	 }

	/**
	 * Send a trace via UDP
	 *
	 * @param trace Trace
	 */
	private void sendTrace(String trace) {
		try {
			if ((datagram == null) && (NetworkFactory.getFactory() != null)) {
				datagram = NetworkFactory.getFactory().createDatagramConnection();
				datagram.open();
			}
			if (datagram != null) {
				trace = ID + trace;
				datagram.send(REMOTE_CONSOLE_ADDR, REMOTE_CONSOLE_PORT, trace.getBytes());
			}
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}	
}
