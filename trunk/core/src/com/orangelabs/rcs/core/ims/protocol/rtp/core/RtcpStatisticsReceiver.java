package com.orangelabs.rcs.core.ims.protocol.rtp.core;

/**
 * RTCP packet statistics receiver
 * 
 * @author jexa7410
 */
public class RtcpStatisticsReceiver {
	/**
	 * Number of RTCP packets received
	 */
	public int numRtcpPkts = 0;
	
	/**
	 * Number of RTCP bytes received
	 */
	public int numRtcpBytes = 0;

	/**
	 * Number of RTCP SR packets received
	 */
	public int numSrPkts = 0;
	
	/**
	 * Number of bad RTCP packets received
	 */
	public int numBadRtcpPkts = 0;
	
	/**
	 * Number of unknown RTCP packets received
	 */
	public int numUnknownTypes = 0;
	
	/**
	 * Number of malformed RTCP packets received
	 */
	public int numMalformedRtcpPkts = 0;
}
