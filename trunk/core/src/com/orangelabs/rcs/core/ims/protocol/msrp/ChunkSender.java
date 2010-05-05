package com.orangelabs.rcs.core.ims.protocol.msrp;

import java.io.IOException;
import java.io.OutputStream;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Chunks sender
 * 
 * @author jexa7410
 */
public class ChunkSender extends Thread {
	/**
	 * MSRP connection
	 */
	private MsrpConnection connection;

	/**
	 * MSRP output stream
	 */
	private OutputStream stream;
	
	/**
	 * Buffer of chunks
	 */
	private FifoBuffer buffer = new FifoBuffer();

	/**
	 * Termination flag
	 */
	private boolean terminated = false;

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param connection MSRP connection
	 * @param stream TCP output stream
	 */
	public ChunkSender(MsrpConnection connection, OutputStream stream) {
		this.connection = connection;
		this.stream = stream;
	}	
	
	/**
	 * Returns the MSRP connection
	 * 
	 * @return MSRP connection
	 */
	public MsrpConnection getConnection() {
		return connection;
	}
	
	/**
	 * Terminate the sender
	 */
	public void terminate() {
		terminated = true; 
		buffer.unblockRead();
		try {
			interrupt();
		} catch(Exception e) {}
		if (logger.isActivated()) {
			logger.debug("Sender is terminated");
		}
	}
	
	/**
	 * Background processing
	 */
	public void run() {
		try {
			if (logger.isActivated()) {
				logger.debug("Sender is started");
			}

			// Read chunk to be sent
			byte chunk[] = null;
			while ((chunk = (byte[])buffer.getMessage()) != null) {
				// Write chunk to the output stream
				if (logger.isActivated()) {
					logger.debug("MSRP message sent:\n" + new String(chunk));
				}
				stream.write(chunk);
				stream.flush();
			}
		} catch (Exception e) {
			if (terminated) { 
				if (logger.isActivated()) {
					logger.debug("Chunk sender thread terminated");
				}
			} else {
				if (logger.isActivated()) {
					logger.error("Chunk sender has failed", e);
				}
			}
		}
	}
	
	/**
	 * Send a chunk
	 * 
	 * @param chunk New chunk
	 * @throws IOException
	 */
	public void sendChunk(byte chunk[]) throws IOException {
		// TODO : why this test necessary ?
		if (connection.getSession().isFailureReportRequested()) {
			buffer.putMessage(chunk);
		} else {
			stream.write(chunk);
			stream.flush();
		}
	}	
}
