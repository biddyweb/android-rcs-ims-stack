package com.orangelabs.rcs.service.api.client.messaging;

/**
 * Messaging API intents 
 * 
 * @author jexa7410
 */
public interface MessagingApiIntents {
    /**
     * Intent broadcasted when a new IM has been received
     */
	public final static String INSTANT_MESSAGE = "com.orangelabs.rcs.messaging.IM";
	
    /**
     * Intent broadcasted when a new file transfer invitation has been received
     */
	public final static String FILE_TRANSFER_INVITATION = "com.orangelabs.rcs.messaging.FILE_TRANSFER_INVITATION";
	
    /**
     * Intent broadcasted when a new chat invitation has been received
     */
	public final static String CHAT_INVITATION = "com.orangelabs.rcs.messaging.CHAT_INVITATION";
}
