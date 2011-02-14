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

import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * RTP packet receiver
 * 
 * @author jexa7410
 */
public class RtpPacketReceiver {

	/**
	 * Statistics 
	 */
	private RtpStatisticsReceiver stats = new RtpStatisticsReceiver();
	
	/**
	 * Flag that indicates if the received buffer size has been set or not
	 */	
	private boolean recvBufSizeSet = false;
	
	/**
	 * Buffer size needed to received RTP packet
	 */	
	private int bufferSize = RtpConfig.DEFAULT_DATAGRAM_SIZE;
	
	/**
	 * Datagram connection
	 */
	public static DatagramConnection datagramConnection = null;
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param port Listenning port
	 * @throws IOException
	 */
	public RtpPacketReceiver(int port) throws IOException {
		// Create the UDP server
		datagramConnection = NetworkFactory.getFactory().createDatagramConnection();
		datagramConnection.open(port);
		if (logger.isActivated()) {
			logger.debug("RTP receiver created on port " + port);				
		}
	}
	
	/**
	 * Close the receiver
	 */
	public void close() throws IOException {
		// Close the datagram connection
		if (datagramConnection != null) {
			datagramConnection.close();
			datagramConnection = null;
		}
	}

	/**
	 * Read a RTP packet (blocking method)
	 * 
	 * @return RTP packet
	 */
	public RtpPacket readRtpPacket() {
		try {
			// Wait a new packet
			byte[] data = datagramConnection.receive(bufferSize);			

			// Parse the RTP packet
			RtpPacket pkt = parseRtpPacket(data);
			if (pkt.payloadType != 12) {
				// Update statistics
				stats.numPackets++;
				stats.numBytes += data.length;				
				return pkt;
			} else {
				// Drop the keep alive packets (payload 12)
				return readRtpPacket();
			}

		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't parse the RTP packet", e);
			}
			stats.numBadRtpPkts++;
			return null;
		}
	}

	/**
	 * Set the size of the received buffer
	 *  
	 * @param size New buffer size
	 */
    public void setRecvBufSize(int size) {
    	this.bufferSize = size;
    }

	/**
	 * Parse the RTP packet
	 * 
	 * @param data RTP packet not yet parsed
	 * @return RTP packet
	 */	
	private RtpPacket parseRtpPacket(byte[] data) {
		RtpPacket rtpPkt = new RtpPacket();
		try {
			// Read RTP packet length
			rtpPkt.length = data.length; 

			// Read marker
			if ((byte)((data[1] & 0xff) & 0x80) == (byte) 0x80){
				rtpPkt.marker = 1;
			}else{
				rtpPkt.marker = 0;
			}
			
			// Read payload type
			rtpPkt.payloadType = (byte) ((data[1] & 0xff) & 0x7f);
	
			// Read seq number
			rtpPkt.seqnum = (short)((data[2] << 8) | (data[3] & 0xff));
	
			// Read timestamp
			rtpPkt.timestamp = (((data[4] & 0xff) << 24) | ((data[5] & 0xff) << 16)
					| ((data[6] & 0xff) << 8) | (data[7] & 0xff));
	
			// Read SSRC
			rtpPkt.ssrc = (((data[8] & 0xff) << 24) | ((data[9] & 0xff) << 16)
					| ((data[10] & 0xff) << 8) | (data[11] & 0xff));
	
			// Read media data after the 12 byte header which is constant
			rtpPkt.payloadoffset = 12;
			rtpPkt.payloadlength = rtpPkt.length - rtpPkt.payloadoffset;
			rtpPkt.data = new byte[rtpPkt.payloadlength];
			System.arraycopy(data, rtpPkt.payloadoffset, rtpPkt.data, 0, rtpPkt.payloadlength);
			
			// Update the buffer size
			if (!recvBufSizeSet) {
				recvBufSizeSet = true;
				switch (rtpPkt.payloadType) {
					case 14:
					case 26:
					case 34:
					case 42:
						setRecvBufSize(64000);
						break;
					case 31:
						setRecvBufSize(0x1f400);
						break;	
					case 32:
						setRecvBufSize(0x1f400);
						break;
	
					default:
						if ((rtpPkt.payloadType >= 96) && (rtpPkt.payloadType <= 127)) {
							setRecvBufSize(64000);
						}
						break;
				}
			}			
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("RTP packet parsing error", e);
			}
			return null;
		}
		return rtpPkt;		
	}

	/**
	 * Returns the statistics of RTP reception
	 * 
	 * @return Statistics
	 */
	public RtpStatisticsReceiver getRtpReceptionStats() {
		return stats;
	}
}
