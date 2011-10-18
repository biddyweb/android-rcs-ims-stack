/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright © 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.core.ims.service.im;

import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.FeatureTags;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatUtils;
import com.orangelabs.rcs.core.ims.service.im.chat.GroupChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.ListOfParticipant;
import com.orangelabs.rcs.core.ims.service.im.chat.OriginatingAdhocGroupChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.OriginatingOne2OneChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.TerminatingAdhocGroupChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.TerminatingOne2OneChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.standfw.StoreAndForwardManager;
import com.orangelabs.rcs.core.ims.service.im.filetransfer.FileSharingSession;
import com.orangelabs.rcs.core.ims.service.im.filetransfer.OriginatingFileSharingSession;
import com.orangelabs.rcs.core.ims.service.im.filetransfer.TerminatingFileSharingSession;
import com.orangelabs.rcs.provider.eab.ContactsManager;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.messaging.InstantMessage;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.StringUtils;
import com.orangelabs.rcs.utils.logger.Logger;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Instant messaging services (chat 1-1, chat group and file transfer)
 * 
 * @author jexa7410
 */
public class InstantMessagingService extends ImsService {
    /**
     * Chat features tags
     */
    public final static String[] CHAT_FEATURE_TAGS = { FeatureTags.FEATURE_OMA_IM };

    /**
     * File transfer features tags
     */
    public final static String[] FT_FEATURE_TAGS = { FeatureTags.FEATURE_OMA_IM };

	/**
	 * Max chat sessions
	 */
	private int maxChatSessions;

	/**
	 * Max file transfer sessions
	 */
	private int maxFtSessions;

	/**
	 * Store & Forward manager
	 */
	private StoreAndForwardManager storeAndFwdMgr = new StoreAndForwardManager(this);

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param parent IMS module
     * @throws CoreException
     */
	public InstantMessagingService(ImsModule parent) throws CoreException {
        super(parent, true);

		this.maxChatSessions = RcsSettings.getInstance().getMaxChatSessions();
        this.maxFtSessions = RcsSettings.getInstance().getMaxFileTransferSessions();
	}

	/**
	 * Start the IMS service
	 */
	public synchronized void start() {
		if (isServiceStarted()) {
			// Already started
			return;
		}
		setServiceStarted(true);
	}

    /**
     * Stop the IMS service
     */
	public synchronized void stop() {
		if (!isServiceStarted()) {
			// Already stopped
			return;
		}
		setServiceStarted(false);
	}

	/**
     * Check the IMS service
     */
	public void check() {
	}

	/**
	 * Get Store & Forward manager
	 */
	public StoreAndForwardManager getStoreAndForwardManager() {
		return storeAndFwdMgr;
	}

    /**
     * Returns IM sessions
     * 
     * @return List of sessions
     */
	public Vector<ChatSession> getImSessions() {
		// Search all IM sessions
		Vector<ChatSession> result = new Vector<ChatSession>();
		Enumeration<ImsServiceSession> list = getSessions();
		while(list.hasMoreElements()) {
			ImsServiceSession session = list.nextElement();
			if (session instanceof ChatSession) {
				result.add((ChatSession)session);
			}
		}

		return result;
    }

	/**
     * Returns IM sessions with a given contact
     * 
     * @param contact Contact
     * @return List of sessions
     */
	public Vector<ChatSession> getImSessionsWith(String contact) {
		// Search all IM sessions
		Vector<ChatSession> result = new Vector<ChatSession>();
		Enumeration<ImsServiceSession> list = getSessions();
		while(list.hasMoreElements()) {
			ImsServiceSession session = list.nextElement();
			if ((session instanceof ChatSession) && PhoneUtils.compareNumbers(session.getRemoteContact(), contact)) {
				result.add((ChatSession)session);
			}
		}

		return result;
    }

	/**
     * Returns file transfer sessions with a given contact
     * 
     * @param contact Contact
     * @return List of sessions
     */
	public Vector<FileSharingSession> getFileTransferSessionsWith(String contact) {
		Vector<FileSharingSession> result = new Vector<FileSharingSession>();
		Enumeration<ImsServiceSession> list = getSessions();
		while(list.hasMoreElements()) {
			ImsServiceSession session = list.nextElement();
			if ((session instanceof FileSharingSession) && PhoneUtils.compareNumbers(session.getRemoteContact(), contact)) {
				result.add((FileSharingSession)session);
			}
		}

		return result;
    }

	/**
     * Returns file transfer sessions
     * 
     * @return List of sessions
     */
	public Vector<FileSharingSession> getFileTransferSessions() {
		Vector<FileSharingSession> result = new Vector<FileSharingSession>();
		Enumeration<ImsServiceSession> list = getSessions();
		while(list.hasMoreElements()) {
			ImsServiceSession session = list.nextElement();
			if (session instanceof FileSharingSession) {
				result.add((FileSharingSession)session);
			}
		}

		return result;
    }

	/**
     * Initiate a file transfer session
     * 
     * @param contact Remote contact
     * @param content Content to be sent
     * @return CSh session
     * @throws CoreException
     */
	public FileSharingSession initiateFileTransferSession(String contact, MmContent content) throws CoreException {
		if (logger.isActivated()) {
			logger.info("Initiate a file transfer session with contact " + contact + ", file " + content.toString());
		}

		// Test number of sessions
		if ((maxFtSessions != 0) && (getFileTransferSessions().size() >= maxFtSessions)) {
			if (logger.isActivated()) {
				logger.debug("The max number of file transfer sessions is achieved: cancel the initiation");
			}
			throw new CoreException("Max file transfer sessions achieved");
		}

		// Create a new session
		OriginatingFileSharingSession session = new OriginatingFileSharingSession(
				this,
				content,
				PhoneUtils.formatNumberToSipUri(contact));

		// Start the session
		session.startSession();
		return session;
	}

	/**
     * Reveive a file transfer invitation
     * 
     * @param invite Initial invite
     */
	public void receiveFileTransferInvitation(SipRequest invite) {
		if (logger.isActivated()) {
    		logger.info("Receive a file transfer session invitation");
    	}

		// Test if the contact is blocked
		String remote = ChatUtils.getAssertedIdentity(invite, false);
	    if (ContactsManager.getInstance().isImBlockedForContact(remote)) {
			if (logger.isActivated()) {
				logger.debug("Contact " + remote + " is blocked: automatically reject the file transfer invitation");
			}
			try {
				// Send a 603 Decline response
		    	if (logger.isActivated()) {
		    		logger.info("Send 603 Decline");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 603);
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 603 Decline", e);
				}
			}			
			return;
	    }

		// Test number of sessions
		if ((maxFtSessions != 0) && (getFileTransferSessions().size() >= maxFtSessions)) {
			if (logger.isActivated()) {
				logger.debug("The max number of file transfer sessions is achieved: reject the invitation");
			}
			try {
				// Send a 603 Decline response
		    	if (logger.isActivated()) {
		    		logger.info("Send 603 Decline");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 603);
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 603 Decline", e);
				}
			}			
			return;
		}

    	// Create a new session
		FileSharingSession session = new TerminatingFileSharingSession(this, invite);

		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleFileTransferInvitation(session);
	}

    /**
     * Initiate a one-to-one chat session
     * 
     * @param contact Remote contact
     * @param firstMsg First message
     * @return IM session
     * @throws CoreException
     */
	public ChatSession initiateOne2OneChatSession(String contact, String firstMsg) throws CoreException {
		if (logger.isActivated()) {
			logger.info("Initiate 1-1 chat session with " + contact);
		}

		// Test number of sessions
		if ((maxChatSessions != 0) && (getImSessions().size() >= maxChatSessions)) {
			if (logger.isActivated()) {
				logger.debug("The max number of chat sessions is achieved: cancel the initiation");
			}
			throw new CoreException("Max chat sessions achieved");
		}

		// Create a new session
		OriginatingOne2OneChatSession session = new OriginatingOne2OneChatSession(
				this,
	        	PhoneUtils.formatNumberToSipUri(contact),
	        	StringUtils.encodeUTF8(firstMsg));

		// Start the session
		session.startSession();
		return session;
	}

    /**
     * Receive a one-to-one chat session invitation
     * 
     * @param invite Initial invite
     */
    public void receiveOne2OneChatSession(SipRequest invite) {
		if (logger.isActivated()){
			logger.info("Receive a 1-1 chat session invitation");
		}

		// Test if the contact is blocked
		String remote = ChatUtils.getAssertedIdentity(invite, false);
	    if (ContactsManager.getInstance().isImBlockedForContact(remote)) {
			if (logger.isActivated()) {
				logger.debug("Contact " + remote + " is blocked: automatically reject the chat invitation");
			}

			// Save the message in the spam folder
			String msgId = ChatUtils.getMessageId(invite);
			RichMessaging.getInstance().addSpamMessage(
					new InstantMessage(msgId, remote, StringUtils.decodeUTF8(invite.getSubject()), false));

			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
	    }

		// Test number of sessions
		if ((maxChatSessions != 0) && (getImSessions().size() >= maxChatSessions)) {
			if (logger.isActivated()) {
				logger.debug("The max number of chat sessions is achieved: reject the invitation");
			}

			// Save the message
			String msgId = ChatUtils.getMessageId(invite);
			RichMessaging.getInstance().addIncomingChatMessage(
					new InstantMessage(msgId, remote,
							StringUtils.decodeUTF8(invite.getSubject()),
							ChatUtils.isImdnDisplayedRequested(invite)));

			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
		}

		// Create a new session
		TerminatingOne2OneChatSession session = new TerminatingOne2OneChatSession(
						this,
						invite);

		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleOneOneChatSessionInvitation(session);
    }

    /**
     * Initiate an ad-hoc group chat session
     * 
     * @param group Group of contacts
     * @param firstMsg First message
     * @return IM session
     * @throws CoreException
     */
    public ChatSession initiateAdhocGroupChatSession(List<String> group, String firstMsg) throws CoreException {
		if (logger.isActivated()) {
			logger.info("Initiate an ad-hoc group chat session");
		}

		// Test number of sessions
		if ((maxChatSessions != 0) && (getImSessions().size() >= maxChatSessions)) {
			if (logger.isActivated()) {
				logger.debug("The max number of chat sessions is achieved: cancel the initiation");
			}
			throw new CoreException("Max chat sessions achieved");
		}

		// Create a new session
		OriginatingAdhocGroupChatSession session = new OriginatingAdhocGroupChatSession(
				this,
				ImsModule.IMS_USER_PROFILE.getImConferenceUri(),
				StringUtils.encodeUTF8(firstMsg),
				new ListOfParticipant(group));

		// Start the session
		session.startSession();
		return session;
    }

    /**
     * Receive ad-hoc group chat session invitation
     * 
     * @param invite Initial invite
     */
    public void receiveAdhocGroupChatSession(SipRequest invite) {
		if (logger.isActivated()) {
			logger.info("Receive an ad-hoc group chat session invitation");
		}

		// Test if the contact is blocked
		String remote = ChatUtils.getAssertedIdentity(invite, false);
	    if (ContactsManager.getInstance().isImBlockedForContact(remote)) {
			if (logger.isActivated()) {
				logger.debug("Contact " + remote + " is blocked: automatically reject the chat invitation");
			}
			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
	    }

		// Test number of sessions
		if ((maxChatSessions != 0) && (getImSessions().size() >= maxChatSessions)) {
			if (logger.isActivated()) {
				logger.debug("The max number of chat sessions is achieved: reject the invitation");
			}
			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
		}

		// Create a new session
		TerminatingAdhocGroupChatSession session = new TerminatingAdhocGroupChatSession(
						this,
						invite);

		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleAdhocGroupChatSessionInvitation(session);
    }

    /**
     * Receive a conference notification
     * 
     * @param notify Received notify
     */
    public void receiveConferenceNotification(SipRequest notify) {
    	// Dispatch the notification to the corresponding session
    	Vector<ChatSession> sessions = getImSessions();
    	for (int i=0; i < sessions.size(); i++) {
    		ChatSession session = (ChatSession)sessions.get(i);
    		if (session instanceof GroupChatSession) {
    			GroupChatSession groupChatSession = (GroupChatSession)session;
	    		if (groupChatSession.getConferenceEventSubscriber().isNotifyForThisSubscriber(notify)) {
	    			groupChatSession.getConferenceEventSubscriber().receiveNotification(notify);
	    		}
    		}
    	}
    }

	/**
     * Receive a message delivery status
     * 
     * @param message Received message
     */
    public void receiveMessageDeliveryStatus(SipRequest message) {
    	 try {
 	    	// Create a 200 OK response
 	        SipResponse resp = SipMessageFactory.createResponse(message, 200);

 	        // Send response
 	        getImsModule().getSipManager().sendSipResponse(resp);
 	    } catch(Exception e) {
         	if (logger.isActivated()) {
         		logger.error("Can't send 200 OK for MESSAGE", e);
         	}
 	    }

 	    // Get session from the message ID
 	    // TODO: instead of From use the PAI ?
 	    // TODO: manage the SIP MESSAGE independtly of the chat session 
		Vector<ChatSession> sessions = Core.getInstance().getImService().getImSessionsWith(message.getFromUri());
		if (sessions.size() > 0) {
			ChatSession session = sessions.lastElement();
	 	    session.receiveMessageDeliveryStatus(message);
		}
    }

    /**
     * Receive a S&F invitation
     * 
     * @param invite Received invite
     */
    public void receiveStoredAndForwardInvitation(SipRequest invite) {
    	if (logger.isActivated()) {
			logger.debug("Receive a S&F invitation");
		}

		// Test if the contact is blocked
		String remote = invite.getFromUri();
	    if (ContactsManager.getInstance().isImBlockedForContact(remote)) {
			if (logger.isActivated()) {
				logger.debug("Contact " + remote + " is blocked: automatically reject the chat invitation");
			}

			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipResponse(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
	    }

		// Create a new session
    	getStoreAndForwardManager().receiveStoredMessages(invite);
    }
}