package com.orangelabs.rcs.core.ims.protocol.rtp.core;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * RTCP APP packet
 * 
 * @author jexa7410
 */
public class RtcpAppPacket extends RtcpPacket {
	public int ssrc;
	public int name;
	public int subtype;
	public byte data[];

	public RtcpAppPacket(RtcpPacket parent) {
		super(parent);
		
		super.type = 204;
	}

	public RtcpAppPacket(int ssrc, int name, int subtype, byte data[]) {
		this.ssrc = ssrc;
		this.name = name;
		this.subtype = subtype;
		this.data = data;
		super.type = 204;

		if ((data.length & 3) != 0) {
			throw new IllegalArgumentException("Bad data length");
		}
		if (subtype < 0 || subtype > 31) {
			throw new IllegalArgumentException("Bad subtype");
		} else {
			return;
		}
	}

	public int calcLength() {
		return 12 + data.length;
	}

	public void assemble(DataOutputStream out) throws IOException {
		out.writeByte(128 + subtype);
		out.writeByte(204);
		out.writeShort(2 + (data.length >> 2));
		out.writeInt(ssrc);
		out.writeInt(name);
		out.write(data);
	}
}
