package com.orangelabs.rcs.core.ims.protocol.rtp.event;

/**
 * RTCP events listener interface
 * 
 * @author jexa7410
 */
public interface RtcpEventListener {
	/**
	 * Receive RTCP event
	 * 
	 * @param event RTCP event
	 */
	void receiveRtcpEvent(RtcpEvent event);
}
