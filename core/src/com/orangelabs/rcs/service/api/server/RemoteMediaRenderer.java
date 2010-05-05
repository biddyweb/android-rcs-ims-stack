package com.orangelabs.rcs.service.api.server;

import com.orangelabs.rcs.core.media.MediaException;
import com.orangelabs.rcs.core.media.MediaRenderer;
import com.orangelabs.rcs.core.media.MediaSample;
import com.orangelabs.rcs.service.api.client.IMediaRenderer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Remote media renderer
 * 
 * @author jexa7410
 */
public class RemoteMediaRenderer extends MediaRenderer {
	/**
	 * Remote media renderer
	 */
	private IMediaRenderer remote;
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param remote Remote renderer
	 */
	public RemoteMediaRenderer(IMediaRenderer remote) {
		this.remote = remote;
	}
	
	/**
	 * Open the renderer
	 * 
	 * @throws MediaException
	 */
	public void open() throws MediaException {
		try {
			remote.open();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't open the media renderer", e);
			}
			throw new MediaException("Can't open the media renderer");
		}
	}
	
	/**
	 * Close the renderer
	 */
	public void close() {
		try {
			remote.close();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't close the media renderer", e);
			}
		}
	}

	/**
	 * Start the renderer
	 * 
	 * @throws MediaException
	 */
	public void start() throws MediaException {
		try {
			remote.start();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't start the media renderer", e);
			}
			throw new MediaException("Can't start the media renderer");
		}
	}
	
	/**
	 * Stop the renderer
	 */
	public void stop() {
		try {
			remote.stop();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't stop the media renderer", e);
			}
		}
	}

	/**
	 * Write a media sample
	 * 
	 * @param sample Media sample
	 * @throws MediaException
	 */
	public void writeSample(MediaSample sample) throws MediaException {
		try {
			remote.writeSample(new com.orangelabs.rcs.service.api.client.MediaSample(sample.getData(), sample.getTimeStamp()));
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't write media sample", e);
			}
			throw new MediaException("Can't write media sample");
		}
	}
}