package com.orangelabs.rcs.service.api.server;

import com.orangelabs.rcs.core.media.MediaListener;
import com.orangelabs.rcs.service.api.client.IMediaEventListener;

/**
 * Remote media listener
 * 
 * @author mrivoalen 
 */
public class RemoteMediaListener extends IMediaEventListener.Stub {

	/**
	 * Media listener
	 */
	private MediaListener listener = null;
	
	/**
	 * Constructor
	 * 
	 * @param listener Media listener
	 */
	public RemoteMediaListener(MediaListener listener){
		this.listener = listener;
	}
	
	/**
	 * Media started
	 */
	public void mediaStarted() {
		listener.mediaStarted();
	}

	/**
	 * Media stopped
	 */
	public void mediaStopped() {
		listener.mediaStopped();
	}

	/**
	 * Media error
	 */
	public void mediaError(String error) {
		listener.mediaError(error);
	}
}
