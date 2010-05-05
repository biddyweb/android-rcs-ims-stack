package com.orangelabs.rcs.core.ims.service.sharing.transfer;

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingSession;

/**
 * Content sharing transfer session
 * 
 * @author jexa7410
 */
public abstract class ContentSharingTransferSession extends ContentSharingSession {
	/**
	 * Max content sharing size (in bytes)
	 */
	public final static int MAX_CONTENT_SIZE = 512 * 1024;
	
	/**
	 * Session listener
	 */
	private ContentSharingTransferSessionListener listener = null;

    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param content Content to be shared
	 * @param contact Remote contact
	 */
	public ContentSharingTransferSession(ImsService parent, MmContent content, String contact) {
		super(parent, content, contact);
	}
	
	/**
	 * Add a listener for receiving events
	 * 
	 * @param listener Listener
	 */
	public void addListener(ContentSharingTransferSessionListener listener) {
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
	public ContentSharingTransferSessionListener getListener() {
		return listener;
	}
}
