package com.orangelabs.rcs.core.ims.protocol.rtp.stream;

import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;
import com.orangelabs.rcs.core.media.MediaPlayer;
import com.orangelabs.rcs.core.media.MediaSample;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Media capture stream
 * 
 * @author jexa7410
 */
public class MediaCaptureStream implements ProcessorInputStream {
	/**
     * Media player
     */
	private MediaPlayer player;

	/**
	 * Media format
	 */
	private Format format;
	
    /**
     * Sequence number
     */
    private long seqNo = 0;

    /**
     * Input buffer
     */
	private Buffer buffer = new Buffer();

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param format Input format
     * @param player Media player
	 */
    public MediaCaptureStream(Format format, MediaPlayer player) {
    	this.format = format;
		this.player = player;
	}
    
    
    /**
	 * Open the input stream
	 * 
     * @throws Exception
	 */	
    public void open() throws Exception {
    	try {
	    	player.open();
			if (logger.isActivated()) {
				logger.debug("Media capture stream openned");
			}
    	} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Media capture stream failed", e);
			}
			throw e;
    	}
	}    	
	
    /**
     * Close the input stream
     */
    public void close() {
		player.close();
		if (logger.isActivated()) {
			logger.debug("Media capture stream closed");
		}
    }
    
    /**
     * Format of the data provided by the source stream
     * 
     * @return Format
     */
    public Format getFormat() {
    	return format;
    }

    /**
     * Read from the stream
     * 
     * @return Buffer
     * @throws Exception
     */
    public Buffer read() throws Exception {
    	// Read a new sample from the media player
    	MediaSample sample = player.readSample();
    	
    	// Create a buffer
	    buffer.setData(sample.getData());   	
	    buffer.setLength(sample.getLength());
    	buffer.setFormat(format);
    	buffer.setSequenceNumber(seqNo++);
    	buffer.setFlags(Buffer.FLAG_SYSTEM_TIME | Buffer.FLAG_LIVE_DATA);
    	buffer.setTimeStamp(sample.getTimeStamp());
    	return buffer;  
    }    
}