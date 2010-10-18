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
package com.orangelabs.rcs.core.ims.protocol.sip;

import gov.nist.core.sip.message.Message;
import gov.nist.core.sip.message.Request;
import gov.nist.core.sip.message.Response;

import java.util.Vector;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * SIP stack
 * 
 * @author JM. Auffret
 */
public class SipStack {
	
	/**
	 * Default SIP port 
	 */
	public final static int DEFAULT_SIP_PORT = 5060;

	/**
	 * Local IP address
	 */
	private String localIpAddress;
	
	/**
	 * Outbound proxy address
	 */
	private String outboundProxyAddr;
	
	/**
	 * Outbound proxy port
	 */
	private int outboundProxyPort;

	/**
	 * SIP listening port
	 */
	private int listeningPort;

	/**
	 * UDP manager
	 */
	private SipUdpManager udpManager;	
	
    /**
	 *  List of current SIP transactions
	 */
	private SipTransactionList transactions = new SipTransactionList();

	/**
	 * SIP listeners
	 */
	private Vector<SipListener> listeners = new Vector<SipListener>();

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Constructor
	 * 
	 * @param outboundProxy Outbound proxy locator
	 * @param listeningPort SIP listening port
	 * @throws SipException
	 */
	public SipStack(String localIpAddress, String outboundProxy, int listeningPort) throws SipException {
		this.localIpAddress = localIpAddress;
		this.listeningPort = listeningPort;
		
		try {
			String[] parts = outboundProxy.split(":");
			this.outboundProxyAddr = parts[0];
			this.outboundProxyPort = Integer.parseInt(parts[1]);
		} catch(Exception e) {
			throw new SipException("Bad outbound proxy address");
		}
		
		if (logger.isActivated()) {
			logger.debug("SIP outbound proxy set to " + outboundProxyAddr + ":" + outboundProxyPort);
		}

		// Create the UDP server
		udpManager = new SipUdpManager(this, listeningPort);

		if (logger.isActivated()) {
			logger.debug("SIP stack started at " + localIpAddress + ":" + listeningPort);
		}
	}
	
    /**
	 * Returns the local IP address
	 * 
	 * @return IP address
	 */
	public String getLocalIpAddress() {
		return localIpAddress;
	}
	
    /**
	 * Returns the UDP manager
	 * 
	 * @return UDP manager
	 */
	public SipUdpManager getUdpManager() {
		return udpManager;
	}
	
	/**
	 * Returns the outbound proxy address
	 * 
	 * @return Outbound proxy address
	 */
	public String getOutboundProxyAddr() {
		return outboundProxyAddr;
	}

	/**
	 * Returns the outbound proxy port
	 * 
	 * @return Outbound proxy port
	 */
	public int getOutboundProxyPort() {
		return outboundProxyPort;
	}

	/**
	 * Returns the proxy protocol
	 * 
	 * @return Outbound proxy protocol
	 */
	public String getProxyProtocol() {
		return "udp";
	}

	/**
	 * Returns the listening port
	 * 
	 * @return Port number
	 */
	public int getListeningPort() {
		return listeningPort;
	}
	
    /**
     * Returns the default route path
     * 
     * @return Route path
     */
    public Vector<String> getDefaultRoutePath() {
    	Vector<String> path = new Vector<String>();
        String route = "<sip:" +
        		outboundProxyAddr+":"+outboundProxyPort+
        		";lr;transport=" + getProxyProtocol()+">";
    	path.addElement(route);
		return path;
	}	
	
	/**
	 * Close the SIP stack 
	 */
	public void close() {
		// Remove all listeners
		listeners.removeAllElements();
		
		// Terminate the UDP server
		udpManager.terminate();
	}
	
	/**
	 * Generate a unique call-ID
	 * 
	 * @return Call-Id
	 */
	public String generateCallId() {
		return IdGenerator.getIdentifier() + "@" + localIpAddress;
	}	

	/**
	 * Get the current transaction context associated to the received message
	 * 
	 * @param msg Received SIP message
	 * @return Transaction context object
	 */
	public SipTransactionContext getTransactionContext(SipMessage msg) {
		// Get the transaction context Id
		String transactionId = SipTransactionContext.generateTransactionContextId(msg);

		// Get the associated context
		SipTransactionContext ctx = (SipTransactionContext)transactions.get(transactionId);
		return ctx;
	}

	/**
	 * Remove the transaction context associated to a call-Id
	 * 
	 * @param callId Call-Id
	 */
	public synchronized void removeTransactionContext(String callId) {
		transactions.remove(callId);
	}	
	
	/**
	 * Add a SIP event listener
	 * 
	 * @param listener Listener
	 */
	public void addSipListener(SipListener listener) {
		if (logger.isActivated()) {
			logger.debug("Add a SIP listener");
		}
		listeners.addElement(listener);
	}
	
	/**
	 * Remove a SIP event listener
	 * 
	 * @param listener Listener
	 */
	public void removeSipListener(SipListener listener) {
		if (logger.isActivated()) {
			logger.debug("Remove a SIP listener");
		}
		listeners.removeElement(listener);
	}
	
	/**
	 * Notify SIP event listeners
	 * 
	 * @param msg SIP message
	 */
	public void notifySipListeners(Message msg) {
		for(int i=0; i < listeners.size(); i++) {
			if (logger.isActivated()) {
				logger.debug("Notify a SIP listener");
			}

			SipListener listener = (SipListener)listeners.elementAt(i);
			if (msg.isRequest()) {
				// Request received
				SipRequest req = new SipRequest((Request)msg);
				listener.receiveSipRequest(req);
				
				// Ack management
				if (req.getMethod().equals("ACK")) {
					// Search context associated to the received Ack & notify it
					notifyTransactionContext(req);
				}
			} else {
				// Response received
				SipResponse resp = new SipResponse((Response)msg);
				if (resp.getStatusCode() < 200) {
					// Provisionnal response: by-pass it
				} else {
					// Final response: search context associated to the received response & notify it
					notifyTransactionContext(resp);
				}
			}
		}
	}
		
	/**
	 * Notify the transaction context that a message has been received (response or ack)
	 * 
	 * @param msg SIP message
	 */
	private void notifyTransactionContext(SipMessage msg) { 
		// Search the context associated to the received response and notify it
        SipTransactionContext callbackObj = getTransactionContext(msg);
        if (callbackObj != null) {
        	// Callback found
			String callId = msg.getCallId();
        	if (logger.isActivated()) {
        		logger.debug("Callback object found for " + callId);
        	}
            removeTransactionContext(callId);
            callbackObj.responseReceived(msg);
        }				
	}
	
	/**
	 * Send a SIP message and wait
	 * 
	 * @param msg SIP message
	 * @return Transaction context
	 * @throws SipException
	 */
	public SipTransactionContext sendSipMessageAndWait(SipMessage msg) throws SipException {
		try {
			// Generate a unique transaction Id
			String transactionId = SipTransactionContext.generateTransactionContextId(msg);
			
			// Create a transaction context
			if (logger.isActivated()) {
				logger.debug("Create a transaction context for " + transactionId);
			}
			SipTransactionContext ctx = new SipTransactionContext(msg);
			transactions.put(transactionId, ctx);

			// Send the SIP message to the network
			udpManager.sendMessage(msg.getStackMessage());

			// Returns the created transaction to wait synchronously the response
			return ctx;
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't send SIP message", e);
			}
			throw new SipException("Can't send SIP message");
		}
	}

	/**
	 * Send a SIP message
	 * 
	 * @param msg SIP message
	 * @throws SipException
	 */
	public void sendSipMessage(SipMessage msg) throws SipException {
		try {
			// Send the SIP message to the network
			udpManager.sendMessage(msg.getStackMessage());
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't send SIP message", e);
			}
			throw new SipException("Can't send SIP message");
		}
	}	
}
