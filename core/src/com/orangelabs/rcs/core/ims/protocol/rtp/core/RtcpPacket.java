package com.orangelabs.rcs.core.ims.protocol.rtp.core;

import java.io.DataOutputStream;
import java.io.IOException;

import com.orangelabs.rcs.core.ims.protocol.rtp.util.Packet;

/**
 * Abstract RCTP packet
 * 
 * @author jexa7410
 */
public abstract class RtcpPacket extends Packet {
	public static final int SR = 200;
	public static final int RR = 201;
	public static final int SDES = 202;
	public static final int BYE = 203;
	public static final int APP = 204;
	public static final int COMPOUND = -1;

	public Packet base;
	
	public int type;

	public RtcpPacket() {
	}

	public RtcpPacket(RtcpPacket rtcppacket) {
		super((Packet)rtcppacket);
		
		base = rtcppacket.base;
	}

	public RtcpPacket(Packet packet) {
		super(packet);
		
		base = packet;
	}

	public abstract void assemble(DataOutputStream dataoutputstream) throws IOException;

	public abstract int calcLength();
}
