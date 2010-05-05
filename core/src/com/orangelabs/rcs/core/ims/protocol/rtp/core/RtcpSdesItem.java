package com.orangelabs.rcs.core.ims.protocol.rtp.core;

/**
 * RCTP SDES item
 * 
 * @author jexa7410
 */
public class RtcpSdesItem {
	public int type;
	public byte[] data;

	public RtcpSdesItem() {
	}

	public RtcpSdesItem(int i, String string) {
		type = i;
		data = new byte[string.length()];
		data = string.getBytes();
	}
}
