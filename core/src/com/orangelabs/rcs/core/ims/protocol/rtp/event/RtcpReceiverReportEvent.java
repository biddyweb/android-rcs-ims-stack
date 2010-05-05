package com.orangelabs.rcs.core.ims.protocol.rtp.event;

import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpReceiverReportPacket;

/**
 * RTCP receiver report event
 * 
 * @author jexa7410
 */
public class RtcpReceiverReportEvent extends RtcpEvent {

	/**
	 * Constructor
	 * 
	 * @param packet RTCP RR packet
	 */
	public RtcpReceiverReportEvent(RtcpReceiverReportPacket packet) {
		super(packet);
	}
}
