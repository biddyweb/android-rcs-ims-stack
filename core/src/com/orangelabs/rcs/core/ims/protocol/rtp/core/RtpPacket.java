package com.orangelabs.rcs.core.ims.protocol.rtp.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.orangelabs.rcs.core.ims.protocol.rtp.util.Packet;

/**
 * Abstract RTP packet
 * 
 * @author jexa7410
 */
public class RtpPacket extends Packet {
	public Packet base;
	public int marker;
	public int payloadType;
	public int seqnum;
	public long timestamp;
	public int ssrc;
	public int payloadoffset;
	public int payloadlength;

	public RtpPacket() {
	}

	public RtpPacket(Packet packet) {
		super(packet);
		
		base = packet;
	}

	public void assemble(int length) throws IOException {
		this.length = length;
		this.offset = 0;

		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(length);
		DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
		dataoutputstream.writeByte(128);
		int i = payloadType;
		if (marker == 1) {
			i = payloadType | 0x80;
		}
		dataoutputstream.writeByte((byte) i);
		dataoutputstream.writeShort(seqnum);
		dataoutputstream.writeInt((int) timestamp);
		dataoutputstream.writeInt(ssrc);
		dataoutputstream.write(base.data, payloadoffset, payloadlength);
		data = bytearrayoutputstream.toByteArray();
	}

	public int calcLength() {
		return payloadlength + 12;
	}
}
