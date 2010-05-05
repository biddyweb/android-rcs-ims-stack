package com.orangelabs.rcs.core.ims.service.im.session;

import java.util.Date;

import com.orangelabs.rcs.core.content.FileContent;
import com.orangelabs.rcs.core.content.PhotoContent;
import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.service.ImsSessionListener;
import com.orangelabs.rcs.core.ims.service.im.InstantMessage;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageError;

/**
 * IM session listener
 * 
 * @author JM. Auffret
 */
public interface InstantMessageSessionListener extends ImsSessionListener {
	/**
	 * New message received
	 * 
	 * @param message Message
	 */
    public void handleReceiveMessage(InstantMessage message);
    
    /**
     * New picture received
     * 
     * @param contact Remote contact
     * @param date Date of reception 
     * @param content Picture content 
     */
    public void handleReceivePicture(String contact, Date date, PhotoContent content);

    /**
     * New video received
     * 
     * @param contact Remote contact
     * @param date Date of reception
     * @param content Video content
     */
    public void handleReceiveVideo(String contact, Date date, VideoContent content);
    
    /**
     * New file received
     * 
     * @param contact Remote contact
     * @param date Date of reception
     * @param content File content
     */
    public void handleReceiveFile(String contact, Date date, FileContent content);
    
	/**
	 * Message has been transfered
	 */
    public void handleMessageTransfered();
    
    /**
     * IM error
     * 
     * @param error Error
     */
    public void handleImError(InstantMessageError error);
}