package com.orangelabs.rcs.core.ims.protocol.rtp;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.Codec;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.ProcessorOutputStream;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Codec chain
 * 
 * @author jexa7410
 */
public class CodecChain {
	/**
	 * List of codecs
	 */
	private Codec[] codecs = null;
	
	/**
	 * List of buffers
	 */
	private Buffer[] buffers = null;
	
	/**
	 * Renderer
	 */
	private ProcessorOutputStream renderer;
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param codecs Codecs list
	 */
	public CodecChain(Codec[] codecs, ProcessorOutputStream renderer) {
		this.codecs = codecs;
		this.renderer = renderer;

		// Create the buffer chain
		buffers = new Buffer[codecs.length+1];
		for (int i = 0; i < codecs.length; i++) {
			buffers[i] = new Buffer();
		}
		
		// Prepare codecs
    	for(int i=0; i < codecs.length; i++) {
    		if (logger.isActivated()) {
    			logger.debug("Open codec " + codecs[i].getClass().getName());
    		}
    		codecs[i].open();
    	}
	}
	
	/**
	 * Codec chain processing
	 * 
	 * @param input Input buffer
	 * @return Result
	 */
	public int process(Buffer input) {
		int codecNo = 0;
		return doProcess(codecNo, input);
	}

	/**
	 * Recursive codec processing
	 * 
	 * @param codecNo Codec index
	 * @param input Input buffer
	 * @return Result
	 */
	private int doProcess(int codecNo, Buffer input) {
		if (codecNo == codecs.length) {
			// End of chain
			try {
				// Write data to the output stream
				renderer.write(input);
				return Codec.BUFFER_PROCESSED_OK;
			} catch (Exception e) {
				return Codec.BUFFER_PROCESSED_FAILED;
			}
		} else {
			// Process this codec
			Codec codec = codecs[codecNo];
			int returnVal;
			do {
				try {
					returnVal = codec.process(input, buffers[codecNo]);
				} catch (Exception e) {
					return Codec.BUFFER_PROCESSED_FAILED;
				}

				if (returnVal == Codec.BUFFER_PROCESSED_FAILED)
					return Codec.BUFFER_PROCESSED_FAILED;
				
				if ((returnVal & Codec.OUTPUT_BUFFER_NOT_FILLED) == 0) {
					if (!(buffers[codecNo].isDiscard() || buffers[codecNo].isEOM())) {
						doProcess(codecNo + 1, buffers[codecNo]);
					}
					buffers[codecNo].setOffset(0);
					buffers[codecNo].setLength(0);
					buffers[codecNo].setFlags(0);
				}
			} while((returnVal & Codec.INPUT_BUFFER_NOT_CONSUMED) != 0);

			return returnVal;
		}
	}
}
