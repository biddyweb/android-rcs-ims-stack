package com.orangelabs.rcs.core.ims.protocol.rtp.core;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * RTCP BYE packet
 * 
 * @author jexa7410
 */
public class RtcpByePacket extends RtcpPacket {

	public int ssrc[];
	public byte reason[];

	public RtcpByePacket(RtcpPacket parent) {
		super(parent);
		super.type = 203;
	}

	public RtcpByePacket(int ssrc[], byte reason[]) {
		this.ssrc = ssrc;
		if (reason != null) {
			this.reason = reason;
		} else {
			this.reason = new byte[0];
		}
		if (ssrc.length > 31) {
			throw new IllegalArgumentException("Too many SSRCs");
		} else {
			return;
		}
	}

	public int calcLength() {
		return 4 + (ssrc.length << 2)
				+ (reason.length <= 0 ? 0 : reason.length + 4 & -4);
	}

	public void assemble(DataOutputStream out) throws IOException {
		out.writeByte(128 + ssrc.length);
		out.writeByte(203);
		out.writeShort(ssrc.length
				+ (reason.length <= 0 ? 0 : reason.length + 4 >> 2));
		for (int i = 0; i < ssrc.length; i++) {
			out.writeInt(ssrc[i]);
		}

		if (reason.length > 0) {
			out.writeByte(reason.length);
			out.write(reason);
			for (int i = (reason.length + 4 & -4) - reason.length - 1; i > 0; i--) {
				out.writeByte(0);
			}
		}
	}
}
