package com.orangelabs.rcs.core.ims.protocol.rtp.event;

import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpSenderReportPacket;

/**
 * RTCP sender report event
 * 
 * @author jexa7410
 */
public class RtcpSenderReportEvent extends RtcpEvent {

	/**
	 * Constructor
	 * 
	 * @param packet RTCP SR packet
	 */
	public RtcpSenderReportEvent(RtcpSenderReportPacket packet) {
		super(packet);
	}
}
