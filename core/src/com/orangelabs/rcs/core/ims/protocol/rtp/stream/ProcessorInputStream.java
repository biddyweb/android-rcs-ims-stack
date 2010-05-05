package com.orangelabs.rcs.core.ims.protocol.rtp.stream;

import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;

/**
 * Processor input stream
 */
public interface ProcessorInputStream {

    /**
	 * Open the input stream
	 * 
     * @throws Exception
	 */	
    public void open() throws Exception;

    /**
     * Close the input stream
     */
    public void close();
    
    /**
     * Read from the input stream without blocking
     * 
     * @return Buffer 
     * @throws Exception
     */
    public Buffer read() throws Exception;
}