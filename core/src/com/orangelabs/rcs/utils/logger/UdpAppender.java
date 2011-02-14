/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright © 2010 France Telecom S.A.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
