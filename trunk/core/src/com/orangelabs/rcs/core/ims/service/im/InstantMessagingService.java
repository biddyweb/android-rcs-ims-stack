/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
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

import gov.nist.core.sip.header.ContentTypeHeader;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.protocol.sip.SipTransactionContext;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.SessionAuthenticationAgent;
import com.orangelabs.rcs.core.ims.service.im.chat.OriginatingAdhocGroupChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.OriginatingOne2OneChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.TerminatingAdhocGroupChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.TerminatingOne2OneChatSession;
import com.orangelabs.rcs.core.ims.service.im.filetransfer.OriginatingFileTransferSession;
import com.orangelabs.rcs.core.ims.service.im.filetransfer.TerminatingFileTransferSession;
import com.orangelabs.rcs.core.ims.service.im.large.OriginatingLargeInstantMessageSession;
import com.orangelabs.rcs.core.ims.service.im.large.TerminatingLargeInstantMessageSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Instant Messaging service. Only one IM session per contact number. 
 * 
 * @author jexa7410
 */
public class InstantMessagingService extends ImsService {
	/**
	 * Conference factory URI for ad-hoc session
	 */
	private String conferenceFactoryUri;
	
	/**
	 * Authentication agent
	 */
	private SessionAuthenticationAgent authenticationAgent = new SessionAuthenticationAgent();
	
	/**
	 * Max chat sessions
	 */
	private int maxChatSessions;
	
	/**
	 * Max file transfer sessions
	 */
	private int maxFtSessions;

	/**
	 * Max file transfer size (in bytes)
	 */
	private long maxFtSize;

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param parent IMS module
	 * @param activated Activation flag
	 * @throws CoreException
	 */
	public InstantMessagingService(ImsModule parent, boolean activated) throws CoreException {
		super(parent, "im_service.xml", activated);
		
		this.maxChatSessions = RcsSettings.getInstance().getMaxChatSessions();
		this.maxFtSessions = RcsSettings.getInstance().getMaxFileTransferSessions();
		this.maxFtSize = RcsSettings.getInstance().getMaxFileTransferSize() * 1024;
		this.conferenceFactoryUri = ImsModule.IMS_USER_PROFILE.getImConferenceUri();
	}

	/**
	 * Start the IMS service
	 */
	public void start() {
	}

	/**
	 * Stop the IMS service 
	 */
	public void stop() {
	}
	
	/**
	 * Check the IMS service 
	 */
	public void check() {
	}

	/**
	 * Returns existing IM sessions
	 * 
	 * @return List of sessions
	 */
	public Vector<InstantMessageSession> getImSessions() {
		// Search all IM sessions
		Vector<InstantMessageSession> result = new Vector<InstantMessageSession>();
		Enumeration<ImsServiceSession> list = getSessions();
		while(list.hasMoreElements()) {
			ImsServiceSession session = list.nextElement();
			if (session instanceof InstantMessageSession) {
				result.add((InstantMessageSession)session);
			}
		}
		
		return result;
	}	

	/**
	 * Returns the existing IM session associated to a contact
	 * 
	 * @param contact Contact number 
	 * @return Session
	 */
	public InstantMessageSession getImSession(String contact) {
		// Format contact number
		contact = PhoneUtils.formatNumberToInternational(contact);
		// Search all IM sessions related to a given contact
		Enumeration<ImsServiceSession> list = getSessions();
		while(list.hasMoreElements()) {
			ImsServiceSession session = list.nextElement();
			if (session instanceof InstantMessageSession) {
				String contactSession = PhoneUtils.extractNumberFromUri(session.getRemoteContact());
				if (contactSession.equals(contact)){
					return (InstantMessageSession)session;
				}
			}
		}
		
		return null;
	}	

	/**
	 * Returns existing file transfer sessions
	 * 
	 * @return List of sessions
	 */
	public Vector<ContentSharingTransferSession> getFileTransferSessions() {
		// Search all file transfer sessions
		Vector<ContentSharingTransferSession> result = new Vector<ContentSharingTransferSession>();
		Enumeration<ImsServiceSession> list = getSessions();
		while(list.hasMoreElements()) {
			ImsServiceSession session = list.nextElement();
			if (session instanceof ContentSharingTransferSession) {
				result.add((ContentSharingTransferSession)session);
			}
		}
		
		return result;
	}	

	/**
	 * Returns the existing file transfer sessions associated to a contact
	 * 
	 * @param contact Contact number 
	 * @return Session
	 */
	public ContentSharingTransferSession getFileTransferSession(String contact) {
		// Format contact number
		contact = PhoneUtils.formatNumberToInternational(contact);
		
		// Search all file transfer sessions related to a given contact
		Enumeration<ImsServiceSession> list = getSessions();
		while(list.hasMoreElements()) {
			ImsServiceSession session = list.nextElement();
			if (session instanceof ContentSharingTransferSession) {
				String contactSession = PhoneUtils.extractNumberFromUri(session.getRemoteContact());
				if (contactSession.equals(contact)){
					return (ContentSharingTransferSession)session;
				}
			}
		}
		
		return null;
	}	

	/**
	 * Initiate a file transfer session
	 * 
	 * @param contact Remote contact
	 * @param content Content to be sent 
	 * @return CSh session 
	 */
	public ContentSharingTransferSession initiateFileTransferSession(String contact, MmContent content) {
		if (logger.isActivated()) {
			logger.info("Initiate a file transfer session with contact " + contact + ", file " + content.toString());
		}
			
		// Create a new session
		OriginatingFileTransferSession session = new OriginatingFileTransferSession(
				this,
				content,
				PhoneUtils.formatNumberToSipAddress(contact));

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

		// Test number of sessions
		if (getFileTransferSessions().size() >= maxFtSessions) {
			if (logger.isActivated()) {
				logger.debug("The max number of file transfer sessions is achieved: reject the invitation");
			}
			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipMessage(resp);
			} catch(Exception e) {
				if (logger.isActivated()) {
					logger.error("Can't send 486 Busy here", e);
				}
			}
			return;
		}
		
		// Test file size
		// TODO

    	// Create a new session
		ContentSharingTransferSession session = new TerminatingFileTransferSession(
					this,
					invite);
		
		// Start the session
		session.startSession();

		// Notify listener
		getImsModule().getCore().getListener().handleFileTransferInvitation(session);
	}
	
    /**
     * Send a pager instant message
     * 
     * @param message Message to be sent
     * @return Boolean result
     */
    public boolean sendPagerInstantMessage(InstantMessage message) {
    	if (logger.isActivated()) {
    		logger.info("Send an instant message to " + message.getRemote());                
    	}
    	
    	boolean result = false;
		
        try {
	        // Create a dialog path
        	String contactUri = PhoneUtils.formatNumberToSipAddress(message.getRemote());
        	SipDialogPath dialogPath = new SipDialogPath(
        					getImsModule().getSipManager().getSipStack(),
        					getImsModule().getSipManager().generateCallId(),
            				1,
            				contactUri,
            				ImsModule.IMS_USER_PROFILE.getNameAddress(),
            				contactUri,
            				getImsModule().getSipManager().getSipStack().getDefaultRoutePath());        	
        	
	        // Create the SIP request
        	if (logger.isActivated()) {
        		logger.info("Send first MESSAGE");
        	}
	        SipRequest msg = SipMessageFactory.createInstantMessage(dialogPath, message.getTextMessage());
	        
	        // Send message
	        SipTransactionContext ctx = getImsModule().getSipManager().sendSipMessageAndWait(msg);
	
	        // Wait response
        	if (logger.isActivated()) {
        		logger.info("Wait response");
        	}
	        ctx.waitResponse(SipManager.TIMEOUT);
	
	        // Analyze received message
            if (ctx.getStatusCode() == 407) {
                // 407 response received
            	if (logger.isActivated()) {
            		logger.info("407 response received");
            	}

    	        // Set the Proxy-Authorization header
            	authenticationAgent.readProxyAuthenticateHeader(ctx.getSipResponse());

                // Increment the Cseq number of the dialog path
                dialogPath.incrementCseq();

                // Send a second MESSAGE with the right token
                if (logger.isActivated()) {
                	logger.info("Send second MESSAGE");
                }
    	        msg = SipMessageFactory.createInstantMessage(dialogPath, message.getTextMessage());
                
    	        // Set the Authorization header
                authenticationAgent.setProxyAuthorizationHeader(msg);
                
                // Send message
    	        ctx = getImsModule().getSipManager().sendSipMessageAndWait(msg);

                // Wait response
                if (logger.isActivated()) {
                	logger.info("Wait response");
                }
                ctx.waitResponse(SipManager.TIMEOUT);

                // Analyze received message
                if (ctx.getStatusCode() == 200) {
                    // 200 OK response
                	if (logger.isActivated()) {
                		logger.info("200 OK response received");
                	}
                	result = true;
                } else {
                    // Error
                	if (logger.isActivated()) {
                		logger.info("Instant message has failed: " + ctx.getStatusCode()
    	                    + " response received");
                	}
                	result = false;
                }
            } else if (ctx.getStatusCode() == 200) {
	            // 200 OK received
            	if (logger.isActivated()) {
            		logger.info("200 OK response received");
            	}
            	result = true;
	        } else {
	            // Error responses
            	if (logger.isActivated()) {
            		logger.info("Instant message has failed: " + ctx.getStatusCode()
	                    + " response received");
            	}
            	result = false;
	        }
        } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Instant message has failed", e);
        	}
        	result = false;
        }        
        return result;
    }
    
    /**
     * Receive a pager instant message
     * 
     * @param im Received instant message
     */
    public void receivePagerInstantMessage(SipRequest im) {
    	if (logger.isActivated()) {
    		logger.info("Receive an instant message");
    	}
        
    	// Send 200 OK
	    try {
	        SipResponse resp = SipMessageFactory.createResponse(im, 200);
	        getImsModule().getSipManager().sendSipMessage(resp);
	    } catch(SipException e) {
        	if (logger.isActivated()) {
        		logger.error("Can't send 200 OK for MESSAGE: " + e.getMessage());
        	}
	    }

	    String contentType = im.getHeader(ContentTypeHeader.NAME);
	    if (contentType.indexOf("text/plain") != -1) {
		    InstantMessage message = new InstantMessage(im.getFrom(), im.getContent());

		    // Notify listener
		    getImsModule().getCore().getListener().handleReceiveInstantMessage(message);
	    } else {
        	if (logger.isActivated()) {
        		logger.error("IM content type " + contentType + " is not supported");
        	}
	    }
    }
    
    /**
     * Send a large instant message 
     * 
     * @param message Message to be sent
     * @return IM session
     */
    public ImsServiceSession sendLargeInstantMessage(InstantMessage message) {
		if (logger.isActivated()) {
			logger.info("Send a large instant message to " + message.getRemote());
		}
			
		// Create a new session
		OriginatingLargeInstantMessageSession session = new OriginatingLargeInstantMessageSession(this,
				message,
	        	PhoneUtils.formatNumberToSipAddress(message.getRemote()));		
		
		// Start the session
		session.startSession();
		return session;
    }

    /**
     * Receive a large instant message
     * 
	 * @param invite Initial invite
     */
    public void receiveLargeInstantMessage(SipRequest invite) {
    	if (logger.isActivated()) {
    		logger.info("Receive a large instant message session invitation");
    	}

    	// Create a new session
		ImsServiceSession session = new TerminatingLargeInstantMessageSession(this, invite);
		
		// Start the session
		session.startSession();
    }
    
    /**
	 * Initiate a one-to-one chat session
	 * 
	 * @param contact Remote contact
	 * @param subject Subject
	 * @return IM session
	 */
	public InstantMessageSession initiateOne2OneChatSession(String contact, String subject) {
		if (logger.isActivated()) {
			logger.info("Initiate 1-1 chat session with " + contact);
		}
			
		// Create a new session
		OriginatingOne2OneChatSession session = new OriginatingOne2OneChatSession(
				this,
	        	PhoneUtils.formatNumberToSipAddress(contact),
	        	subject);
		
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

		// Test number of sessions
		if (getImSessions().size() >= maxChatSessions) {
			if (logger.isActivated()) {
				logger.debug("The max number of chat sessions is achieved: reject the invitation");
			}
			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipMessage(resp);
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
		getImsModule().getCore().getListener().handleOne2OneChatSessionInvitation(session);
    }

    /**
     * Initiate an ad-hoc group chat session
     * 
     * @param subject Subject of the conference
	 * @param group Group of contacts
	 * @return IM session
     */
    public InstantMessageSession initiateAdhocGroupChatSession(String subject, List<String> group) {
		if (logger.isActivated()) {
			logger.info("Initiate an ad-hoc group chat session");
		}
			
		// Create a new session
		OriginatingAdhocGroupChatSession session = new OriginatingAdhocGroupChatSession(
				this,
				conferenceFactoryUri,
				subject,
				group);
		
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

		// Test number of sessions
		if (getImSessions().size() >= maxChatSessions) {
			if (logger.isActivated()) {
				logger.debug("The max number of chat sessions is achieved: reject the invitation");
			}
			try {
				// Send a 486 Busy response
		    	if (logger.isActivated()) {
		    		logger.info("Send 486 Busy here");
		    	}
		        SipResponse resp = SipMessageFactory.createResponse(invite, 486);
		        getImsModule().getSipManager().sendSipMessage(resp);
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
}
