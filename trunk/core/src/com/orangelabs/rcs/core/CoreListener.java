package com.orangelabs.rcs.core;

import java.util.Vector;

import com.orangelabs.rcs.core.ims.ImsError;
import com.orangelabs.rcs.core.ims.service.im.InstantMessage;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageError;
import com.orangelabs.rcs.core.ims.service.im.session.TerminatingAdhocGroupChatSession;
import com.orangelabs.rcs.core.ims.service.im.session.TerminatingOne2OneChatSession;
import com.orangelabs.rcs.core.ims.service.presence.PresenceError;
import com.orangelabs.rcs.core.ims.service.presence.pidf.PidfDocument;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.ContentSharingStreamingSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.core.ims.service.toip.TerminatingToIpSession;
import com.orangelabs.rcs.core.ims.service.voip.TerminatingVoIpSession;

/**
 * Observer of core events
 * 
 * @author JM. Auffret
 */
public interface CoreListener {
	
    /**
     * Core layer has been started
     */
    public void handleCoreLayerStarted();

    /**
     * Core layer has been stopped
     */
    public void handleCoreLayerStopped();

    /**
     * Registered to IMS 
     */
    public void handleRegistrationSuccessful();
    
    /**
     * IMS registration has failed
     * 
     * @param error Error
     */
    public void handleRegistrationFailed(ImsError error);
    
    /**
     * Unregistered from IMS 
     */
    public void handleRegistrationTerminated();

    /**
     * Publish presence status successfull
     */
    public void handlePublishPresenceSuccessful();
    
    /**
     * Publish presence status has failed
     * 
     * @param error Error
     */
    public void handlePublishPresenceFailed(PresenceError error);
    
    /**
     * Publish presence session terminated 
     */
    public void handlePublishPresenceTerminated();    
    
    /**
     * A new presence sharing notification has been received
     * 
     * @param contact Contact
     * @param status Status
     * @param reason Reason
     */
    public void handlePresenceSharingNotification(String contact, String status, String reason);

    /**
     * A new presence info notification has been received
     * 
     * @param contact Contact
     * @param presense Presence info document
     */
    public void handlePresenceInfoNotification(String contact, PidfDocument presence);

    /**
     * Poke period is started
     * 
     * @param expiration Expiration time
     */
    public void handlePokePeriodStarted(long expiration);

    /**
     * Poke period has been terminated
     */
    public void handlePokePeriodTerminated();
    
    /**
     * A new presence sharing invitation has been received
     * 
     * @param contact Contact
     */
    public void handlePresenceSharingInvitation(String contact);

    /**
     * Content sharing capabilities indication 
     * 
     * @param contact Remote contact
     * @param image Image sharing supported
     * @param video Video sharing supported
     * @parem others Other supported services
     */
    public void handleContentSharingCapabilitiesIndication(String contact, boolean image, boolean video, Vector<String> others);
    
    /**
     * A new content sharing transfer invitation has been received
     * 
     * @param session CSh session
     */
    public void handleContentSharingTransferInvitation(ContentSharingTransferSession session);
    
    /**
     * A new content sharing streaming invitation has been received
     * 
     * @param session CSh session
     */
    public void handleContentSharingStreamingInvitation(ContentSharingStreamingSession session);
    
	/**
	 * A new file transfer invitation has been received
	 * 
	 * @param session File transfer session
	 */
	public void handleFileTransferInvitation(ContentSharingTransferSession session);

	/**
     * IM has been sent with success 
     */
	public void handleSendInstantMessageSuccessful();
	
	/**
	 * Outgoing IM has failed
	 * 
	 * @param error Error
	 */
	public void handleSendInstantMessageFailed(InstantMessageError error);

	/**
	 * A new IM has been received
	 * 
	 * @param message IM message
	 */
	public void handleReceiveInstantMessage(InstantMessage message);
   
    /**
     * New one-to-one chat session invitation
     * 
     * @param session Chat session
     */
    public void handleOne2OneChatSessionInvitation(TerminatingOne2OneChatSession session);
    
    /**
     * New ad-hoc group chat session invitation
     * 
     * @param session Chat session
     */
    public void handleAdhocGroupChatSessionInvitation(TerminatingAdhocGroupChatSession session);

    /**
     * New VoIP call invitation
     * 
     * @param session VoIP session
     */
    public void handleVoIpCallInvitation(TerminatingVoIpSession session);
    
    /**
     * New ToIP call invitation
     * 
     * @param session ToIP session
     */
    public void handleToIpCallInvitation(TerminatingToIpSession session);
}