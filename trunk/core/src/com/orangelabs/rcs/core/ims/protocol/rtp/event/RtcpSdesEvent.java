package com.orangelabs.rcs.core.ims.protocol.rtp.event;

import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpSdesPacket;

/**
 * RTCP session description event
 * 
 * @author jexa7410
 */
public class RtcpSdesEvent extends RtcpEvent {

	/**
	 * Constructor
	 * 
	 * @param packet RTCP SDES packet
	 */
	public RtcpSdesEvent(RtcpSdesPacket packet) {
		super(packet);
	}
}
