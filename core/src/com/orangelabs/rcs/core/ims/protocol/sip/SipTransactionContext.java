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

/**
 * SIP transaction context object composed of a request and of the corresponding
 * response. The Transaction context is used for waiting responses of requests
 * and also for waiting an ACK message (special case).
 *
 * @author JM. Auffret
 */
public class SipTransactionContext extends Object {
	
	/**
	 * SIP message that is sent
	 */
	private SipMessage out = null;

	/**
	 * SIP message that is received
	 */
	private SipMessage in = null;

    /**
	 * Constructor
	 * 
	 * @param msg SIP message to be sent
	 */
	public SipTransactionContext(SipMessage msg) {
		super();
		
		this.out = msg;
	}

	/**
	 * Get the SIP message that has been sent
	 * 
	 * @return SIP message
	 */
	public SipMessage getMessageSent() {
		return out;
	}

	/**
	 * Get the SIP message that has been received
	 * 
	 * @return SIP message
	 */
	public SipMessage getMessageReceived() {
		return in;
	}

	/**
	 * Determine if a timeout has occured
	 * 
	 * @return Returns True if there is a timeout else returns False
	 */
	public boolean isTimeout() {
		return (in == null);
	}

	/**
	 * Determine if the received message is a SIP response
	 * 
	 * @return Returns True if it's a SIP response else returns False
	 */
	public boolean isSipResponse() {
		if (in != null) {
			return in.isSipResponse();
		} else {
			return false;
		}
	}

	/**
	 * Determine if the received message is an intermediate response
	 * 
	 * @return Returns True if it's an intermediate response else returns False
	 */
	public boolean isSipIntermediateResponse() {
		int code = getStatusCode();
	    return (code < 200);
	}

	/**
	 * Determine if the received message is a successfull response
	 * 
	 * @return Returns True if it's a successfull response else returns False
	 */
	public boolean isSipSuccessfullResponse() {
		int code = getStatusCode();
	    return ((code >= 200) && (code < 300));
	}

	/**
	 * Determine if the received message is a SIP ACK
	 * 
	 * @return Returns True if it's a SIP ACK else returns False
	 */
	public boolean isSipAck() {
		if (in != null) {
			SipRequest req = (SipRequest)in;
			if (req.getMethod().equals("ACK"))
				return true;
			else
				return false;
		} else {
			return false;
		}
	}

	/**
	 * Get the SIP response that has been received
	 * 
	 * @return SIP response or null if it's not a response (e.g. ACK message)
	 */
	public SipResponse getSipResponse() {
		if (isSipResponse())
			return (SipResponse)in;
		else
			return null;
	}

	/**
	 * Get the status code of the received SIP response
	 * 
	 * @return Returns a status code or -1 if it's not a SIP response (e.g. ACK message)
	 */
	public int getStatusCode() {
	    int ret = -1;
		if (isSipResponse()) {
			SipResponse resp = (SipResponse)in;
			ret = resp.getStatusCode();
		}
		return ret;
	}

	/**
	 * Get the reason phrase of the received SIP response
	 * 
	 * @return Returns a reason phrase or null if it's not a SIP response (e.g. ACK message)
	 */
	public String getReasonPhrase() {
	    String ret = null;
		SipResponse resp = getSipResponse();
		if (resp != null)
		    ret = resp.getReasonPhrase();
		return ret;
	}

	/**
	 * Wait the response of a request until a timeout occurs
	 * 
	 * @param timeout Timeout value
	 */
	public void waitResponse(int timeout) {
		try {
			if (in != null) {
				// Response already received, no need to wait
				return;
			}			
			synchronized(this) {
				wait(timeout * 1000);
			}
		} catch(java.lang.InterruptedException e) {
			// Thread has been interrupted
			in = null;
		}
	}

	/**
	 * A response has been received (SIP response or ACK or any other SIP message) 
	 * 
	 * @param msg SIP message object
	 */
	public void responseReceived(SipMessage msg) {
		synchronized(this) {
			this.in = msg;
			super.notify();
		}
	}

	/**
	 * Reset transaction context
	 */
	public void resetContext() {
		synchronized (this) {
			this.in = null;
			super.notify();
		}
	}
	
	/**
	 * Generate a unique ID that corresponds to a transaction context
	 * 
	 * @param msg SIP message
	 * @return Transaction ID
	 */
	public static String generateTransactionContextId(SipMessage msg)  {
		// Transaction ID = callId + cseq
		return msg.getCallId() + "_" + msg.getCSeq();
	}
}
