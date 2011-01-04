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
package com.orangelabs.rcs.utils;

import java.io.IOException;

import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.platform.network.SocketServerConnection;

/**
 * Network ressource manager
 * 
 * @author jexa7410
 */
public class NetworkRessourceManager {
    /**
     * Default local RTP port base
     */
    public static int DEFAULT_LOCAL_RTP_PORT_BASE = 10000;

    /**
     * Default local MSRP port base
     */
    public static int DEFAULT_LOCAL_MSRP_PORT_BASE = 20000;

    /**
     * Generate a default free RTP port number
     * 
     * @return Local RTP port
     */
    public static synchronized int generateLocalRtpPort() {
    	return generateLocalUdpPort(DEFAULT_LOCAL_RTP_PORT_BASE);
    }	
    
    /**
     * Generate a default free MSRP port number
     * 
     * @return Local MSRP port
     */
    public static synchronized int generateLocalMsrpPort() {
    	return generateLocalTcpPort(DEFAULT_LOCAL_MSRP_PORT_BASE);
    }	
    
    /**
     * Convert an IP address to its integer representation
     * 
     * @param addr IP address
     * @return Integer
     */
    public static int ipToInt(String addr) {
        String[] addrArray = addr.split("\\.");
        int num = 0;
        for (int i=0; i<addrArray.length; i++) {
            int power = 3-i;
            num += ((Integer.parseInt(addrArray[i])%256 * Math.pow(256,power)));
        }
        return num;
    }
        
    /**
     * Generate a free UDP port number from a specific port base
     * 
     * @param portBase UDP port base
     * @return Local UDP port
     */
    public static synchronized int generateLocalUdpPort(int portBase) {
    	int resp = -1;
		int port = portBase;
		while(resp == -1) {
			if (isLocalUdpPortFree(port)) {
				// Free UDP port found
				resp = port;
			} else {
				port = port + 2;
			}
		}
    	return resp;
    }	
	
	/**
     * Test if the given local UDP port is really free (not used by
     * other applications)
     * 
     * @param port Port to check
     * @return Boolean
     */
    public static boolean isLocalUdpPortFree(int port) {
    	boolean res = false;
    	try {
    		DatagramConnection conn = NetworkFactory.getFactory().createDatagramConnection();
    		conn.open(port);
    		conn.close(); 
    		res = true;
    	} catch(IOException e) {
    		res = false;
    	}
    	return res;
    }

    /**
     * Generate a free TCP port number
     * 
     * @param portBase TCP port base
     * @return Local TCP port
     */
    public static synchronized int generateLocalTcpPort(int portBase) {
    	int resp = -1;
		int port = portBase;
		while(resp == -1) {
			if (isLocalTcpPortFree(port)) {
				// Free TCP port found
				resp = port;
			} else {
				port++;
			}
		}
    	return resp;
    }
	
	/**
     * Test if the given local TCP port is really free (not used by
     * other applications)
     * 
     * @param port Port to check
     * @return Boolean
     */
    public static boolean isLocalTcpPortFree(int port) {
    	boolean res = false;
    	try {
    		SocketServerConnection conn = NetworkFactory.getFactory().createSocketServerConnection();
    		conn.open(port);
    		conn.close(); 
    		res = true;
    	} catch(IOException e) {
    		res = false;
    	}
    	return res;
    }
    
    /**
     * Is a valid IP address
     * 
     * @param ipAddress IP address
     * @return Boolean
     */
    public static boolean isValidIpAddress(String ipAddress) {
    	boolean result = false;
		if ((ipAddress != null) &&
				(!ipAddress.equals("127.0.0.1")) &&
					(!ipAddress.equals("localhost"))) {
			result = true;
		}
    	return result;    	
    }
}
