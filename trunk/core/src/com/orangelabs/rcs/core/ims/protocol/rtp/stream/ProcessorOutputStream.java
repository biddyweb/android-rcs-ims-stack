package com.orangelabs.rcs.core.ims.protocol.rtp.stream;

import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;

/**
 * Processor output stream
 */
public interface ProcessorOutputStream {
    /**
	 * Open the output stream
	 * 
     * @throws Exception
	 */	
    public void open() throws Exception;

    /**
     * Close from the output stream
     */
    public void close();
    
    /**
     * Write to the stream without blocking
     * 
     * @param buffer Input buffer
     * @throws Exception
     */
    public void write(Buffer buffer) throws Exception;
}