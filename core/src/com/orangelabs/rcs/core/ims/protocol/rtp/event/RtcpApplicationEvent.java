package com.orangelabs.rcs.core.ims.protocol.rtp.event;

import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpAppPacket;

/**
 * RTCP application event
 * 
 * @author jexa7410
 */
public class RtcpApplicationEvent extends RtcpEvent {

	/**
	 * Constructor
	 * 
	 * @param packet RTCP APP packet
	 */
	public RtcpApplicationEvent(RtcpAppPacket packet) {
		super(packet);
	}
}
