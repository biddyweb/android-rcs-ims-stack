package com.orangelabs.rcs.core.ims.service.sharing;

import com.orangelabs.rcs.core.content.LiveVideoContent;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;

/**
 * Content sharing session
 * 
 * 
 * @author jexa7410
 */
public abstract class ContentSharingSession extends ImsServiceSession {
	/**
	 * Content to be shared
	 */
	private MmContent content;

    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param content Content to be shared
	 * @param contact Remote contact
	 */
	public ContentSharingSession(ImsService parent, MmContent content, String contact) {
		super(parent, contact);
		
		this.content = content;
	}
	
	/**
	 * Returns the content
	 * 
	 * @return Content 
	 */
	public MmContent getContent() {
		return content;
	}
	
	/**
	 * Set the content
	 * 
	 * @param content Content  
	 */
	public void setContent(MmContent content) {
		this.content = content;
	}

	/**
	 * Returns the "X-type" attribute
	 * 
	 * @return String
	 */
	public String getXTypeAttribute() {
		if (content instanceof LiveVideoContent) {
			return "videolive";
		} else {
			return null;
		}
	}

	/**
	 * Returns the "file-selector" attribute
	 * 
	 * @return String
	 */
	public String getFileSelectorAttribute() {
		return "name:\"" + content.getName() + "\"" + 
			" type:" + content.getEncoding() +
			" size:" + content.getSize();
	}
	
	/**
	 * Returns the "file-location" attribute
	 * 
	 * @return String
	 */
	public String getFileLocationAttribute() {
		if ((content.getUrl() != null) && content.getUrl().startsWith("http")) {
			return content.getUrl();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the "file-transfer-id" attribute
	 * 
	 * @return String
	 */
	public String getFileTransferId() {
		return "" + System.currentTimeMillis();
	}

	/**
	 * Receive re-INVITE request 
	 * 
	 * @param reInvite re-INVITE request
	 */
	public void receiveReInvite(SipRequest reInvite) {
		send405Error(reInvite);		
	}

	/**
	 * Receive UPDATE request 
	 * 
	 * @param update UPDATE request
	 */
	public void receiveUpdate(SipRequest update) {
		send405Error(update);		
	}
}
