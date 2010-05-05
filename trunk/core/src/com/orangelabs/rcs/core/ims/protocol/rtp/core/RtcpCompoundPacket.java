package com.orangelabs.rcs.core.ims.protocol.rtp.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.orangelabs.rcs.core.ims.protocol.rtp.util.Packet;

/**
 * RTCP compound packet
 * 
 * @author jexa7410
 */
public class RtcpCompoundPacket extends RtcpPacket {
	public RtcpPacket[] packets;

	public RtcpCompoundPacket(Packet packet) {
		super(packet);
		type = -1;
	}

	public RtcpCompoundPacket(RtcpPacket[] rtcppackets) {
		packets = rtcppackets;
		type = -1;
	}

	public void assemble(int i, boolean bool) {
		length = i;
		offset = 0;
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(
				i);
		DataOutputStream dataoutputstream = new DataOutputStream(
				bytearrayoutputstream);
		int i_0_;
		try {
			if (bool)
				offset += 4;
			i_0_ = offset;
			for (int i_1_ = 0; i_1_ < packets.length; i_1_++) {
				i_0_ = bytearrayoutputstream.size();
				packets[i_1_].assemble(dataoutputstream);
			}
		} catch (IOException ioexception) {
			throw new NullPointerException("Impossible IO Exception");
		}
		int i_2_ = bytearrayoutputstream.size();
		data = bytearrayoutputstream.toByteArray();
		if (i_2_ > i)
			throw new NullPointerException("RTCP Packet overflow");
		if (i_2_ < i) {
			if (data.length < i)
				System.arraycopy(data, 0, data = new byte[i], 0, i_2_);
			data[i_0_] |= 0x20;
			data[i - 1] = (byte) (i - i_2_);
			int i_3_ = (data[i_0_ + 3] & 0xff) + (i - i_2_ >> 2);
			if (i_3_ >= 256)
				data[i_0_ + 2] += i - i_2_ >> 10;
			data[i_0_ + 3] = (byte) i_3_;
		}
	}

	public void assemble(DataOutputStream dataoutputstream) throws IOException {
		throw new IllegalArgumentException("Recursive Compound Packet");
	}

	public int calcLength() {
		int i = 0;
		if (packets == null || packets.length < 1)
			throw new IllegalArgumentException("Bad RTCP Compound Packet");
		for (int i_4_ = 0; i_4_ < packets.length; i_4_++)
			i += packets[i_4_].calcLength();
		return i;
	}
}
