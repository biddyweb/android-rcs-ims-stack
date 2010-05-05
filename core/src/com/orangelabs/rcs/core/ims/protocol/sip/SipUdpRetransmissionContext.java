package com.orangelabs.rcs.core.ims.protocol.sip;

import gov.nist.core.sip.header.CSeqHeader;
import gov.nist.core.sip.message.Message;

import java.util.TimerTask;


/**
 * SIP UDP retransmission context 
 *
 * @author JM. Auffret
 */
public class SipUdpRetransmissionContext extends Object {	
	/**
	 * Context ID
	 */
	private String contextId;
	
	/**
	 * Outgoing SIP message
	 */
	private Message out = null;

	/**
	 * Incoming SIP message
	 */
	private Message in = null;

	/**
	 * Timer T1
	 */
	private TimerTask t1 = null;

	/**
	 * Timer T2
	 */
	private TimerTask t2 = null;

	/**
	 * Date of creation
	 */
    private long creationTimestamp = System.currentTimeMillis();

	/**
	 * Constructor
	 * 
	 * @param contextId Context id
	 * @param in Incoming SIP message
	 * @param out Outgoing SIP message
	 */
	public SipUdpRetransmissionContext(String contextId, Message in, Message out) {
		super();
		
		this.contextId = contextId;
		this.in = in;
		this.out = out;
	}

	/**
	 * Returns the context ID
	 * 
	 * @return ID
	 */
	public String getContextId() {
		return contextId;
	}    	

	/**
	 * Returns the date of creation of the context
	 * 
	 * @return Time as long
	 */
	public long getCreationTimestamp() {
		return creationTimestamp;
	}    	

	/**
	 * Get the outgoing SIP message
	 * 
	 * @return SIP message
	 */
	public Message getOutgoingMessage() {
		return out;
	}

	/**
	 * Set the outgoing SIP message
	 * 
	 * @param msg SIP message
	 */
	public void setOutgoingMessage(Message msg) {
		this.out = msg;
	}
	
	/**
	 * Get the incoming SIP message
	 * 
	 * @return SIP message
	 */
	public Message getIncomingMessage() {
		return in;
	}
	
	/**
	 * Set the incoming SIP message
	 * 
	 * @param msg SIP message
	 */
	public void setIncomingMessage(Message msg) {
		this.in = msg;
	}

	/**
	 * Get timer T1
	 * 
	 * @return Timer
	 */
	public TimerTask getRetransmissionTimer() {
		return t1;
	}
	
	/**
	 * Add retransmission timer
	 * 
	 * @param t1 Timer task
	 */
	public void addRetransmissionTimer(TimerTask t1) {
		this.t1 = t1;
	}
		
	/**
	 * Get timer T2
	 * 
	 * @return Timer
	 */
	public TimerTask getExpirationTimer() {
		return t2;
	}

	/**
	 * Start expiration timer
	 * 
	 * @param t2 Timer task
	 */
	public void addExpirationTimer(TimerTask t2) {
		this.t2 = t2;
	}
	
	/**
	 * Generate a unique context ID that corresponds to a SIP exchange
	 * 
	 * @param msg SIP message
	 * @return Context ID
	 */
	public static String generateUdpContextId(Message msg)  {
		String callId = msg.getCallIdentifier();
		CSeqHeader cseqHeader = msg.getCSeqHeader();
		int cseq = cseqHeader.getSequenceNumber();
		String method = cseqHeader.getMethod();
		return callId + "_" + method + "_" + cseq;
	}
}