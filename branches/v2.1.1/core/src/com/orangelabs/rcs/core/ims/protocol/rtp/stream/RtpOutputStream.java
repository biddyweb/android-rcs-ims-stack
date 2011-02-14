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
package com.orangelabs.rcs.core.ims.protocol.rtp.stream;

import java.io.IOException;

import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpPacketTransmitter;
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtpPacketTransmitter;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * RTP output stream
 * 
 * @author jexa7410
 */
public class RtpOutputStream implements ProcessorOutputStream {
    /**
     * Remote address
     */
    private String remoteAddress;

    /**
     * Remote port
     */
    private int remotePort;

	/**
	 * RTP transmitter
	 */
	private RtpPacketTransmitter rtpTransmitter =  null;

	/**
	 * RTCP transmitter
	 */
	private RtcpPacketTransmitter rtcpTransmitter =  null;
	
    /**
	 * The logger
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param remoteAddress Remote address
	 * @param remotePort Remote port
	 */
	public RtpOutputStream(String remoteAddress, int remotePort) {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
    }

    /**
	 * Open the output stream
	 * 
     * @throws Exception
	 */	
    public void open() throws Exception {
    	// Create the RTP transmitter
		rtpTransmitter = new RtpPacketTransmitter(remoteAddress, remotePort);

		// Create the RTCP transmitter
		rtcpTransmitter = new RtcpPacketTransmitter(remoteAddress, remotePort+1);
    }

    /**
     * Close the output stream
     */
    public void close() {
		try {
			// Close the RTP transmitter
			if (rtpTransmitter != null) {
				rtpTransmitter.close();
			}
			
			// Send a bye event
			if (rtcpTransmitter != null) {
				rtcpTransmitter.sendByePacket();
			}

			// Close the RTCP transmitter
			if (rtcpTransmitter != null) {
				rtcpTransmitter.close();
			}
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't close correctly RTP ressources", e);
			}
		}
	}
    
    /**
     * Write to the stream without blocking
     * 
     * @param buffer Input buffer
     * @throws IOException
     */
    public void write(Buffer buffer) throws IOException {
		rtpTransmitter.sendRtpPacket(buffer);
    }
}
