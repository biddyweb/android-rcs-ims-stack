package com.orangelabs.rcs.core.ims.service.sharing.streaming;

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingSession;
import com.orangelabs.rcs.core.media.MediaRenderer;

/**
 * Content sharing streaming session
 * 
 * @author jexa7410
 */
public abstract class ContentSharingStreamingSession extends ContentSharingSession {
    /**
	 * Media renderer
	 */
	private MediaRenderer renderer = null;
	
	/**
	 * Session listener
	 */
	private ContentSharingStreamingSessionListener listener = null;

    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param content Content to be shared
	 * @param contact Remote contact
	 */
	public ContentSharingStreamingSession(ImsService parent, MmContent content, String contact) {
		super(parent, content, contact);
	}
	
	/**
	 * Get the media renderer
	 * 
	 * @return Renderer
	 */
	public MediaRenderer getMediaRenderer() {
		return renderer;
	}
	
	/**
	 * Set the media renderer
	 * 
	 * @param renderer Renderer
	 */
	public void setMediaRenderer(MediaRenderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * Add a listener for receiving events
	 * 
	 * @param listener Listener
	 */
	public void addListener(ContentSharingStreamingSessionListener listener) {
		this.listener = listener;
	}

	/**
	 * Remove the listener
	 */
	public void removeListener() {
		listener = null;
	}

	/**
	 * Returns the event listener
	 * 
	 * @return Listener
	 */
	public ContentSharingStreamingSessionListener getListener() {
		return listener;
	}
}
