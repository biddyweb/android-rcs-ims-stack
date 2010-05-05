package com.orangelabs.rcs.core.ims.protocol.rtp;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.Codec;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.DummyPacketSourceStream;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.RtpOutputStream;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Dummy packet generator for maintaining alive the network address in NAT 
 * 
 * @author jexa7410
 */
public class DummyPacketGenerator extends Thread {
    /**
     * Media processor
     */
    private Processor processor = null;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     */
    public DummyPacketGenerator() {
    }

    /**
     * Prepare the RTP session
     * 
	 * @param remoteAddress Remote address
	 * @param remotePort Remote port
     * @throws RtpException
     */
    public void prepareSession(String remoteAddress, int remotePort) throws RtpException {
    	try {
    		// Create the input stream
    		DummyPacketSourceStream inputStream = new DummyPacketSourceStream();
    		inputStream.open();
			if (logger.isActivated()) {
				logger.debug("Input stream: " + inputStream.getClass().getName());
			}

			// Create the output stream 
        	RtpOutputStream outputStream = new RtpOutputStream(remoteAddress, remotePort);
    		outputStream.open();
			if (logger.isActivated()) {
				logger.debug("Output stream: " + outputStream.getClass().getName());
			}
        	
            // Create the media processor
    		processor = new Processor(inputStream, outputStream, new Codec[0]);
    		
        	if (logger.isActivated()) {
        		logger.debug("Session has been prepared with success");
        	}			
        	
        } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Can't prepare resources correctly", e);
        	}
        	throw new RtpException("Can't prepare resources");
        }
    }

    /**
     * Start the RTP session
     */
    public void startSession() {
    	if (logger.isActivated()) {
    		logger.debug("Start the session");
    	}

    	// Start the media processor
		if (processor != null) {
			processor.startProcessing();
		}
    }

    /**
     * Stop the RTP session
     */
    public void stopSession() {
    	if (logger.isActivated()) {
    		logger.debug("Stop the session");
    	}

    	// Stop the media processor
		if (processor != null) {
			processor.stopProcessing();
		}
    }
}
