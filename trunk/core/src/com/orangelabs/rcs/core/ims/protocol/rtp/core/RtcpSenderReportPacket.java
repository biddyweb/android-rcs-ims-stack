package com.orangelabs.rcs.core.ims.protocol.rtp.core;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * RCTP SR packet
 * 
 * @author jexa7410
 */
public class RtcpSenderReportPacket extends RtcpPacket {
	public int ssrc;
	public long ntptimestampmsw;
	public long ntptimestamplsw;
	public long rtptimestamp;
	public long packetcount;
	public long octetcount;
	public RtcpReport[] reports;

	public RtcpSenderReportPacket(int i, RtcpReport[] rtcpreportblocks) {
		ssrc = i;
		reports = rtcpreportblocks;
		if (rtcpreportblocks.length > 31)
			throw new IllegalArgumentException("Too many reports");
	}

	public RtcpSenderReportPacket(RtcpPacket rtcppacket) {
		super(rtcppacket);
		type = 200;
	}

	public void assemble(DataOutputStream dataoutputstream) throws IOException {
		dataoutputstream.writeByte(128 + reports.length);
		dataoutputstream.writeByte(200);
		dataoutputstream.writeShort(6 + reports.length * 6);
		dataoutputstream.writeInt(ssrc);
		dataoutputstream.writeInt((int) ntptimestampmsw);
		dataoutputstream.writeInt((int) ntptimestamplsw);
		dataoutputstream.writeInt((int) rtptimestamp);
		dataoutputstream.writeInt((int) packetcount);
		dataoutputstream.writeInt((int) octetcount);
		for (int i = 0; i < reports.length; i++) {
			dataoutputstream.writeInt(reports[i].ssrc);
			dataoutputstream.writeInt((reports[i].packetslost & 0xffffff)
					+ (reports[i].fractionlost << 24));
			dataoutputstream.writeInt((int) reports[i].lastseq);
			dataoutputstream.writeInt(reports[i].jitter);
			dataoutputstream.writeInt((int) reports[i].lsr);
			dataoutputstream.writeInt((int) reports[i].dlsr);
		}
	}

	public int calcLength() {
		return 28 + reports.length * 24;
	}
}
