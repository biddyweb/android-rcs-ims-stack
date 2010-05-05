package com.orangelabs.rcs.core.ims.protocol.rtp.core;

/**
 * RTCP report
 * 
 * @author jexa7410
 */
public class RtcpReport {
	public int ssrc;
	public int fractionlost;
	public int packetslost;
	public long lastseq;
	public int jitter;
	public long lsr;
	public long dlsr;
	public long receiptTime;

	public long getDLSR() {
		return dlsr;
	}

	public int getFractionLost() {
		return fractionlost;
	}

	public long getJitter() {
		return (long) jitter;
	}

	public long getLSR() {
		return lsr;
	}

	public long getNumLost() {
		return (long) packetslost;
	}

	public long getSSRC() {
		return (long) ssrc;
	}

	public long getXtndSeqNum() {
		return lastseq;
	}
}
