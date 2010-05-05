package com.orangelabs.rcs.core.ims.protocol.rtp.event;

import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpPacket;

/**
 * Abstract RTCP event
 * 
 * @author jexa7410
 */
public abstract class RtcpEvent {
	/**
	 * RTCP packet
	 */
	private RtcpPacket packet;
	
	/**
	 * Constructor
	 * 
	 * @param packet RTCP packet
	 */
	public RtcpEvent(RtcpPacket packet) {
		this.packet = packet;
	}

	/**
	 * Returns the RTCP packet
	 * 
	 * @return Packet
	 */
	public RtcpPacket getPacket() {
		return packet;
	}	
}
