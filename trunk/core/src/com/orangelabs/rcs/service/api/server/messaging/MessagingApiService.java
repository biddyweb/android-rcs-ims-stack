package com.orangelabs.rcs.service.api.server.messaging;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;
import com.orangelabs.rcs.core.ims.service.im.InstantMessage;
import com.orangelabs.rcs.core.ims.service.im.session.InstantMessageSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.platform.file.FileDescription;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.RcsCoreService;
import com.orangelabs.rcs.service.api.client.messaging.IChatSession;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.service.api.client.messaging.IInstantMessageSession;
import com.orangelabs.rcs.service.api.client.messaging.IMessagingApi;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApiIntents;
import com.orangelabs.rcs.service.api.server.ServerApiException;
import com.orangelabs.rcs.service.api.server.ServerApiUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Messaging API service
 * 
 * @author jexa7410
 */
public class MessagingApiService extends IMessagingApi.Stub {
	/**
	 * Notification ID
	 */
	private final static int FILETRANSFER_INVITATION_NOTIFICATION = 1003;
	
	/**
	 * Notification ID
	 */
	private final static int CHAT_INVITATION_NOTIFICATION = 1004;

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public MessagingApiService() {
		if (logger.isActivated()) {
			logger.info("Messaging API service is loaded");
		}
	}
	
	/**
	 * Close API
	 */
	public void close() {
		// Remove notification
		MessagingApiService.removeFileTransferInvitationNotification();
	}

	/**
     * Add file transfer invitation notification
     * 
     * @param contact Contact
     * @param sessionId Session ID
     */
    public static void addFileTransferInvitationNotification(String contact, String sessionId) {
		// Create notification
		Intent intent = new Intent(MessagingApiIntents.FILE_TRANSFER_INVITATION);
		intent.putExtra("contact", contact);
		intent.putExtra("sessionId", sessionId);
        PendingIntent contentIntent = PendingIntent.getActivity(RcsCoreService.CONTEXT, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notif = new Notification(R.drawable.rcs_core_notif_filetransfer_icon,
        		RcsCoreService.CONTEXT.getString(R.string.title_filetransfer_notification),
        		System.currentTimeMillis());
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(RcsCoreService.CONTEXT,
        		RcsCoreService.CONTEXT.getString(R.string.title_filetransfer_notification),
        		RcsCoreService.CONTEXT.getString(R.string.label_filetransfer_notification),
        		contentIntent);
        
        // Set ringtone
        String ringtone = RcsSettings.getInstance().getFileTransferInvitationRingtone();        	
        if (!TextUtils.isEmpty(ringtone)) {
			notif.sound = Uri.parse(ringtone);
        }

        // Set vibration
        if (RcsSettings.getInstance().isPhoneVibrateForFileTransferInvitation()) {
        	notif.defaults |= Notification.DEFAULT_VIBRATE;
        }
        
        // Send notification
		NotificationManager notificationManager = (NotificationManager)RcsCoreService.CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(FILETRANSFER_INVITATION_NOTIFICATION, notif);		
    }

    /**
     * Remove file transfer invitation notification
     */
    public static void removeFileTransferInvitationNotification() {
		// Remove the notification
		NotificationManager notificationManager = (NotificationManager)RcsCoreService.CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(FILETRANSFER_INVITATION_NOTIFICATION);
    }

	/**
     * Add chat invitation notification
     * 
     * @param contact Contact
     * @param sessionId Session ID
     */
    public static void addChatInvitationNotification(String contact, String sessionId) {
		// Create notification
		Intent intent = new Intent(MessagingApiIntents.CHAT_INVITATION);
		intent.putExtra("contact", contact);
    	// TODO: add suject
		intent.putExtra("sessionId", sessionId);
        PendingIntent contentIntent = PendingIntent.getActivity(RcsCoreService.CONTEXT, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notif = new Notification(R.drawable.rcs_core_notif_chat_icon,
        		RcsCoreService.CONTEXT.getString(R.string.title_chat_notification),
        		System.currentTimeMillis());
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(RcsCoreService.CONTEXT,
        		RcsCoreService.CONTEXT.getString(R.string.title_chat_notification),
        		RcsCoreService.CONTEXT.getString(R.string.label_chat_notification),
        		contentIntent);
        
/* TODO       // Set ringtone
        String ringtone = RcsSettings.getInstance().getFileTransferInvitationRingtone();        	
        if (!TextUtils.isEmpty(ringtone)) {
			notif.sound = Uri.parse(ringtone);
        }

        // Set vibration
        if (RcsSettings.getInstance().isPhoneVibrateForFileTransferInvitation()) {
        	notif.defaults |= Notification.DEFAULT_VIBRATE;
        }*/
        
        // Send notification
		NotificationManager notificationManager = (NotificationManager)RcsCoreService.CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(CHAT_INVITATION_NOTIFICATION, notif);		
    }

    /**
     * Remove chat invitation notification
     */
    public static void removeChatInvitationNotification() {
		// Remove the notification
		NotificationManager notificationManager = (NotificationManager)RcsCoreService.CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CHAT_INVITATION_NOTIFICATION);
    }

    /**
     * Transfer a file
     *
     * @param contact Contact
     * @param file File to be transfered
     * @param File transfer session
     * @throws ServerApiException
     */
    public IFileTransferSession transferFile(String contact, String file) throws ServerApiException {	
		if (logger.isActivated()) {
			logger.info("Transfer file " + file + " to " + contact);
		}

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			FileDescription desc = FileFactory.getFactory().getFileDescription(file);
			MmContent content = ContentManager.createMmContentFromUrl(file, desc.getSize());
			ContentSharingTransferSession session = Core.getInstance().getImService().transferFile(contact, content);

			String sessionId = session.getSessionID();
			RichMessaging.getInstance().addFileTransfer(sessionId, contact, file, RichMessagingData.OUTGOING, session.getContent().getEncoding(), session.getContent().getName(), session.getContent().getSize());
			return new FileTransferSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
    }
    
	/**
	 * Get the file transfer session from its session id
	 * 
	 * @param id Session ID
	 * @return Session
	 * @throws ServerApiException
	 */
	public IFileTransferSession getFileTransferSession(String id) throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ImsServiceSession session = Core.getInstance().getImService().getSession(id);
			if ((session != null) && (session instanceof ContentSharingTransferSession)) {
				return new FileTransferSession((ContentSharingTransferSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Send an instant message in short mode
	 * 
     * @param contact Contact
     * @param txt Text message
     * @throws ServerApiException
	 */
	public void sendShortIM(String contact, String txt) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Send IM in short mode to " + contact);
		}

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			InstantMessage msg = new InstantMessage(contact, txt); 
			Core.getInstance().getImService().sendInstantMessage(msg);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Send an instant message in large message
	 * 
     * @param contact Contact
     * @param txt Text message
     * @return IM session
     * @throws ServerApiException
	 */
	public IInstantMessageSession sendLargeIM(String contact, String txt) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Send IM in large mode to " + contact);
		}

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			InstantMessage msg = new InstantMessage(contact, txt); 
			ImsServiceSession session = Core.getInstance().getImService().sendLargeInstantMessage(msg);
			return new ImSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Initiate a one-to-one chat session
	 * 
     * @param contact Remote contact
     * @param subject Subject of the conference
	 * @return Chat session
     * @throws ServerApiException
	 */
	public IChatSession initiateOne2OneChatSession(String contact, String subject) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Initiate a 1-1 chat session with " + contact);
		}

		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			InstantMessageSession session = Core.getInstance().getImService().initiateOne2OneChatSession(contact, subject);
			return new ChatSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Initiate an ad-hoc group chat session
	 * 
     * @param subject Subject of the conference
     * @param participants List of participants
	 * @return Chat session
     * @throws ServerApiException
	 */
	public IChatSession initiateAdhocGroupChatSession(String subject, List<String> participants) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Initiate an ad-hoc group chat session");
		}
		
		// Test IMS connection
		ServerApiUtils.testIms();

		try {
			// TODO: use subject
			InstantMessageSession session = Core.getInstance().getImService().initiateAdhocGroupChatSession(subject, participants);
			return new ChatSession(session);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Get a chat session from its session id
	 * 
	 * @param id Session ID
	 * @return Session
	 * @throws ServerApiException
	 */
	public IChatSession getChatSession(String id) throws ServerApiException {
		// Test core availability
		ServerApiUtils.testCore();
		
		try {
			ImsServiceSession session = Core.getInstance().getImService().getSession(id);
			if ((session != null) && (session instanceof InstantMessageSession)) {
				return new ChatSession((InstantMessageSession)session);
			} else {
				return null;
			}
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
}
