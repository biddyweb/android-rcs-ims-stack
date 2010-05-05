package com.orangelabs.rcs.core.ims.protocol.rtp.event;

import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpByePacket;

/**
 * RTCP bye event
 * 
 * @author jexa7410
 */
public class RtcpByeEvent extends RtcpEvent {

	/**
	 * Constructor
	 * 
	 * @param packet RTCP BYE packet
	 */
	public RtcpByeEvent(RtcpByePacket packet) {
		super(packet);
	}
}
