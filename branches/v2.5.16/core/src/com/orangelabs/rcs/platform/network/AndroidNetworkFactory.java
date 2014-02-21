/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

package com.orangelabs.rcs.platform.network;

import java.net.Socket;

import com.orangelabs.rcs.core.ims.network.ImsNetworkInterface.DnsResolvedFields;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Android network factory
 * 
 * @author jexa7410
 */
public class AndroidNetworkFactory extends NetworkFactory {
	// Changed by Deutsche Telekom
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Returns the local IP address of a given network interface
	 * 
	 * @param dnsEntry remote address to find an according local socket address
	 * @return Address
	 */
	// Changed by Deutsche Telekom
	public String getLocalIpAddress(DnsResolvedFields dnsEntry) {
		String ipAddress = null;
		try {
			// The local IP address depends on the remote address to be reached (in a multi IP stack
			// environment); so let the Android OS set up an appropriate socket for the given
			// (P-CSCF) remote address and discover the local IP address from that socket.
			Socket clientSock = new Socket(dnsEntry.ipAddress, dnsEntry.port);
			ipAddress = clientSock.getLocalAddress().getHostAddress();
			clientSock.close();
		} catch(Exception e) {
			if(logger.isActivated()){
				logger.error("getLocalIpAddress failed with ", e);
			}
		}
		return ipAddress;
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
     * Create a datagram connection with a specific SO timeout
     *
     * @param timeout SO timeout
     * @return Datagram connection
     */
    public DatagramConnection createDatagramConnection(int timeout) {
        return new AndroidDatagramConnection(timeout);
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
	 * Create a secure socket client connection
	 * 
	 * @return Socket connection
	 */
	public SocketConnection createSecureSocketClientConnection() {
		return new AndroidSecureSocketConnection();
	}
	
	// Changed by Deutsche Telekom
	/**
	 * Create a secure socket client connection w/o checking certificates
	 * 
	 * @param fingerprint
	 * @return Socket connection
	 */
	public SocketConnection createSimpleSecureSocketClientConnection(String fingerprint) {
		return new AndroidSecureSocketConnection(fingerprint);
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
