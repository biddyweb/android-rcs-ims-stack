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
import gov.nist.core.sip.parser.StringMsgParser;

import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * SIP UDP manager
 * 
 * @author jexa7410
 */
public class SipUdpManager extends Thread {

	/**
	 * Processing status flag
	 */
	private boolean running = false;

	/**
	 * SIP stack
	 */
	private SipStack sipstack;

	/**
	 * Datagram connection
	 */
	private DatagramConnection datagramConnection;

	/**
	 * Listening port
	 */
	private int port;
	
	/**
	 * UDP retransmission manager
	 */
	private SipUdpRetransmissionManager retransMgr;

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param sipstack SIP stack
	 * @param port Listening port
	 * @throws SipException
	 */
	public SipUdpManager(SipStack sipstack, int port) throws SipException {
		super("SipUdpManager");
		
		this.sipstack = sipstack;
		this.port = port;

		// Create the retransmission manager
		retransMgr = new SipUdpRetransmissionManager(this);
		
		// Create the UDP server
		try {
			datagramConnection = NetworkFactory.getFactory().createDatagramConnection();
			datagramConnection.open(port);			
		} catch (Exception ex) {
			throw new SipException("Can't open datagram socket on port " + port);
		}
		
		// Start background processing
		start();
	}

	/**
	 * Start processing
	 */
	public void start() {
		running = true;
		super.start();
	}

	/**
	 * Terminate processing
	 */
	public void terminate() {
		running = false;
		try {
			interrupt();
		} catch(Exception e) {}

		try {
			// Close the retransmission manager
			retransMgr.terminate();
			
			// Close the UDP connection
			if (datagramConnection != null) {
				datagramConnection.close();
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Error when terminating the UDP server", e);
			}
		}
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
			while (running) {
				if (logger.isActivated()) {
					logger.debug("Wait SIP messages");
				}

				// Wait datagram packet
				byte[] data = datagramConnection.receive();
	
				// Process received packet
				processReceivedPacket(data);
			}
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.warn("Datagram socket server failed: " + e.getMessage());
			}
		}
		running = false;
		
		if (logger.isActivated()) {
			logger.debug("Datagram socket server terminated");
		}
	}
	
	/**
	 * Process the recieved UDP packet
	 * 
	 * @param data Received data
	 */
	private void processReceivedPacket(byte[] data) {
		Message sipMessage = null;
		
		// Process the received message
		if (logger.isActivated()) {
			logger.debug(">>>>>>>>>> SIP message received (" + data.length + " bytes):\n" + new String(data));
		}
        
		// Create a new parser to parse SIP messages
		try {
			StringMsgParser parser = new StringMsgParser();
			sipMessage = parser.parseSIPMessage(data);
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Parsing error", e);
			}
			return;
		}

		// Check for the required headers.
		if ((sipMessage == null) ||
			(sipMessage.getFromHeader() == null) ||
			(sipMessage.getToHeader() == null) ||
			(sipMessage.getCallId() == null) ||
			(sipMessage.getCSeqHeader() == null) ||
			(sipMessage.getViaHeaders() == null)) {
			if (logger.isActivated()) {
				logger.debug("Bad SIP message: missing mandatory header");
			}
			return;
		}

		// Check if a context exist for incoming message
		String contextId = SipUdpRetransmissionContext.generateUdpContextId(sipMessage);
		SipUdpRetransmissionContext ctx = retransMgr.getRetransmissionContext(contextId);
		if (ctx != null)  {
			// A context already exist for this incoming message
			if (sipMessage.isRequest()) {
				if (logger.isActivated()) {
					logger.debug("New request received for context " + contextId);
				}

				// Incoming request: retransmit the response if it exists, then return
				Message resp = ctx.getOutgoingMessage();
				if (resp != null) { 
					retransMgr.retransmitMessage(contextId, resp);
				}
				
				// Don't notify upper layer
				if (logger.isActivated()) {
					logger.debug("By-pass retransmission to upper level for context " + contextId);
				}
				return;
			} else {
				if (logger.isActivated()) {
					logger.debug("New response received for context " + contextId);
				}

				// Incoming response: update the received response
				ctx.setIncomingMessage(sipMessage);
			}
		} else {
			// Context does not exist, add a new entry in the retransmission manager
			// to filter the next retransmissions if it arrives
			retransMgr.addIncomingContext(sipMessage);
		}
		
		// Notify SIP message listeners
		sipstack.notifySipListeners(sipMessage);
	}

	/**
	 * Send a SIP message
	 * 
	 * @param sipMessage SIP message to be sent
	 * @throws SipException
	 */
	public void sendMessage(Message sipMessage) throws SipException {
		try {
			// Send data
			byte[] msg = sipMessage.encodeAsBytes();
			datagramConnection.send(sipstack.getOutboundProxyAddr(), sipstack.getOutboundProxyPort(), msg);
			if (logger.isActivated()) {
				String sipTxt = sipMessage.encode();
				logger.debug(">>>>>>>>>> SIP message sent (" + sipTxt.length() + " bytes):\n" + sipTxt);
			}

			// Add the outgoing context
			retransMgr.addOutgoingContext(sipMessage);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("The SIP message can't be sent", e);
			}
			throw new SipException("Can't send a new datagram");
		}
	}
	
	/**
	 * Retransmit a SIP message
	 * 
	 * @param sipMessage SIP message to be sent
	 * @throws SipException
	 */
	public void retransmitMessage(Message sipMessage) throws SipException {
		try {
			// Send data
			byte[] msg = sipMessage.encodeAsBytes();
			datagramConnection.send(sipstack.getOutboundProxyAddr(), sipstack.getOutboundProxyPort(), msg);
			if (logger.isActivated()) {
				String sipTxt = sipMessage.encode();
				logger.debug(">>>>>>>>>> Retransmit SIP message (" + sipTxt.length() + " bytes):\n" + sipTxt);
			}
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("The SIP message can't be retransmitted", e);
			}
			throw new SipException("Can't send a new datagram");
		}
	}

	/**
	 * Get the port from which the UDP server is reading messages
	 * 
	 * @return Port number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns the SIP stack
	 * 
	 * @return SIP stack object
	 */
	public SipStack getSipStack() {
		return sipstack;
	}
}
