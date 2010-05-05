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
			
			if (rtcpTransmitter != null) {
				// Send a bye event
				rtcpTransmitter.sendByePacket();
			
				// Close the RTCP transmitter
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
