package com.orangelabs.rcs.core.ims.protocol.msrp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Data chunks
 * 
 * @author jexa7410
 */
public class DataChunks {
	
    /**
     * Current transfered size in bytes
     */
    private int currentSize = 0;
    
    /**
	 * Cache used to save data chunks
	 */
	private ByteArrayOutputStream cache = new ByteArrayOutputStream();	

	/**
	 * Constructor
	 */
	public DataChunks() {
	}
	
	/**
	 * Add a new chunk
	 * 
	 * @param data Data chunk
	 */
	public void addChunk(byte[] data) throws IOException {
		cache.write(data, 0, data.length);		
		currentSize += data.length;
	}

	/**
     * Get received data
     * 
     * @return Byte array
     */
    public byte[] getReceivedData() {
    	byte[] result = cache.toByteArray();
        return result;
    }
	
	/**
     * Rset the cache
     */
    public void resetCache() {
    	cache.reset();
    	currentSize = 0;
    }
    
    /**
	 * Returns the current size of the received chunks
	 * 
	 * @return Size in bytes
	 */
	public int getCurrentSize() {
		return currentSize;
	}
}
