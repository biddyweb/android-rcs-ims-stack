package com.orangelabs.rcs.core.ims.protocol.rtp;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.Codec;
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtpConfig;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.MediaRendererStream;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.RtpInputStream;
import com.orangelabs.rcs.core.media.MediaRenderer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Media RTP receiver
 */
public class MediaRtpReceiver {
    /**
     * Media processor
     */
    private Processor processor = null;

	/**
	 * Local port number (RTP listening port)
	 */
	private int localPort;
	
	/**
	 * The logger
	 */
	private Logger logger =	Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param localPort Local port number
	 */
	public MediaRtpReceiver(int localPort) {
		this.localPort = localPort;
		
		// Activate symetric RTP configuration
		RtpConfig.SYMETRIC_RTP = true;
	}
	
    /**
     * Prepare the RTP session
     * 
     * @param renderer Media renderer
     * @param format Media format
     * @throws RtpException
     */
    public void prepareSession(MediaRenderer renderer, Format format) throws RtpException {
    	try {
			// Create the input stream
    		RtpInputStream inputStream = new RtpInputStream(localPort, format);
    		inputStream.open();
			if (logger.isActivated()) {
				logger.debug("Input stream: " + inputStream.getClass().getName());
			}

			// Create the output stream 
        	MediaRendererStream outputStream = new MediaRendererStream(renderer);
    		outputStream.open();
			if (logger.isActivated()) {
				logger.debug("Output stream: " + outputStream.getClass().getName());
			}
        	
        	// Create the codec chain
        	Codec[] codecChain = MediaRegistry.generateDecodingCodecChain(format.getCodec());
        	
            // Create the media processor
    		processor = new Processor(inputStream, outputStream, codecChain);
    		
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
	 * 
	 * @throws RtpException
	 */
	public void startSession() throws RtpException {
		if (logger.isActivated()) {
			logger.info("Start the session");
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
			logger.info("Stop the session");
		}

		// Stop the media processor
		if (processor != null) {
			processor.stopProcessing();
		}
	}
}
