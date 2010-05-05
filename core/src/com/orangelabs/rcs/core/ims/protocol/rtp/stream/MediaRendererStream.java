package com.orangelabs.rcs.core.ims.protocol.rtp.stream;

import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;
import com.orangelabs.rcs.core.media.MediaRenderer;
import com.orangelabs.rcs.core.media.MediaSample;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Media renderer stream 
 * 
 * @author jexa7410
 */
public class MediaRendererStream implements ProcessorOutputStream {
	/**
     * Media renderer
     */
	private MediaRenderer renderer;
    
    /**
	 * The logger
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
     * @param renderer Media renderer
	 */
	public MediaRendererStream(MediaRenderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * Open the output stream
	 * 
     * @throws Exception
	 */	
    public void open() throws Exception {
    	try {
	    	renderer.open();
			if (logger.isActivated()) {
				logger.debug("Media renderer stream openned");
			}
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Media renderer stream failed", e);
			}
			throw e; 
		}
    }

    /**
     * Close the output stream
     */
    public void close() {
		renderer.close();
		if (logger.isActivated()) {
			logger.debug("Media renderer stream closed");
		}    	
    }
        
    /**
     * Write to the stream without blocking
     * 
     * @param buffer Input buffer 
     * @throws Exception
     */
    public void write(Buffer buffer) throws Exception {
    	MediaSample sample = new MediaSample((byte[])buffer.getData(), buffer.getTimeStamp());
    	renderer.writeSample(sample);
    }
}
