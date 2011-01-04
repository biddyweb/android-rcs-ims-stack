/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright © 2010 France Telecom S.A.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.orangelabs.rcs.core.ims.protocol.rtp.core;

import java.io.IOException;
import java.util.Vector;

import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * RTCP packet transmitter
 * 
 * @author jexa7410
 */
public class RtcpPacketTransmitter extends Thread {
    /**
	 * Remote address
	 */
	private String remoteAddress;

    /**
	 * Remote port
	 */
	private int remotePort;

	/**
	 * Statistics
	 */
	private RtcpStatisticsTransmitter stats = new RtcpStatisticsTransmitter();
	
	/**
	 * Datagram connection
	 */
	public static DatagramConnection datagramConnection = null;

	/**
	 * The logger
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param address Remote address
	 * @param port Remote port
	 * @throws IOException
	 */
	public RtcpPacketTransmitter(String address, int port) throws IOException {
		super("RtcpTransmitter");
		
		this.remoteAddress = address;
		this.remotePort = port;

		// Open the connection
		if (RtpConfig.SYMETRIC_RTP && (RtcpPacketReceiver.datagramConnection != null)) {
			datagramConnection = RtcpPacketReceiver.datagramConnection;
			if (logger.isActivated()) {
				logger.debug("Use RTCP receiver connection (symetric RTP)");				
			}
		} else {
			datagramConnection = NetworkFactory.getFactory().createDatagramConnection();
			datagramConnection.open();
		}

		if (logger.isActivated()) {
			logger.debug("RTCP transmitter connected to " + remoteAddress + ":" + remotePort);				
		}
		
		// Start the transmitter
		start();
	}

	/**
	 * Close the transmitter
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		// Close the datagram connection
		if (datagramConnection != null) {
			datagramConnection.close();
		}
		if (logger.isActivated()) {
			logger.debug("RTCP transmitter closed");				
		}
	}
	
	/**
	 * Background processing
	 */
	public void run() {
		try {
			// Send a SDES packet
			sendSdesPacket();
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't send the SDES packet", e);
			}
		}
	}

	/**
	 * Send a SDES packet
	 */
	public void sendSdesPacket() {
		// Create a report
		Vector<RtcpSdesPacket> repvec = makereports();
		RtcpPacket packets[] = new RtcpPacket[repvec.size()];
		repvec.copyInto(packets);

		// Create a RTCP compound packet
		RtcpCompoundPacket cp = new RtcpCompoundPacket(packets);

		// Assemble the RTCP packet
		int i = cp.calcLength();
		cp.assemble(i, false);

		// Send the RTCP packet
		transmit(cp);
	}

	/**
	 * Send a BYE packet
	 */
	public void sendByePacket() {
		// Create a report
	    Vector<RtcpSdesPacket> repvec = makereports();
	    RtcpPacket[] packets = new RtcpPacket[repvec.size() + 1];
	    repvec.copyInto(packets);

	    // Create a RTCP bye packet
	    int ssrc[] = {RtpSource.SSRC};
	    RtcpByePacket rtcpbyepacket = new RtcpByePacket(ssrc, null);
	    packets[packets.length - 1] = rtcpbyepacket;
	    
		// Create a RTCP compound packet
	    RtcpCompoundPacket cp = new RtcpCompoundPacket(packets);

	    // Send the RTCP packet
		transmit(cp);
	}
	
	/**
	 * Generate a RTCP report
	 * 
	 * @return Vector
	 */
	public Vector<RtcpSdesPacket> makereports() {
		Vector<RtcpSdesPacket> packets = new Vector<RtcpSdesPacket>();

		RtcpSdesPacket rtcpsdespacket = new RtcpSdesPacket(new RtcpSdesBlock[1]);
		rtcpsdespacket.sdes[0] = new RtcpSdesBlock();
		rtcpsdespacket.sdes[0].ssrc = RtpSource.SSRC;

		Vector<RtcpSdesItem> vector = new Vector<RtcpSdesItem>();
		vector.addElement(new RtcpSdesItem(1, RtpSource.CNAME));
		rtcpsdespacket.sdes[0].items = new RtcpSdesItem[vector.size()];
		vector.copyInto(rtcpsdespacket.sdes[0].items);

		packets.addElement(rtcpsdespacket);
		return packets;
	}

	/**
	 * Transmit a RTCP compound packet to the remote destination
	 *  
	 * @param packet Compound packet to be sent
	 */
	private void transmit(RtcpCompoundPacket packet) {
		// Prepare data to be sent
		byte[] data = packet.data;
		if (packet.offset > 0) {
			System.arraycopy(data, packet.offset,
					data = new byte[packet.length], 0, packet.length);
		}
		
		// Update statistics
		stats.numBytes += packet.length;
		stats.numPackets++;	
	
		// Send data over UDP
		try {
			datagramConnection.send(remoteAddress, remotePort, data);
		} catch(IOException e) {
			if (logger.isActivated()) {
				logger.error("Can't send the RTCP packet", e);
			}
		}
	}

    /**
	 * Returns the statistics of RTCP transmission
	 * 
	 * @return Statistics
	 */
	public RtcpStatisticsTransmitter getStatistics() {
		return stats;
	}
}
