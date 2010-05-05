package com.orangelabs.rcs.service.api.server.messaging;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingError;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSessionListener;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferEventListener;
import com.orangelabs.rcs.service.api.client.messaging.IFileTransferSession;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * File transfer session
 * 
 * @author jexa7410
 */
public class FileTransferSession extends IFileTransferSession.Stub implements ContentSharingTransferSessionListener {
	
	/**
	 * Core session
	 */
	private ContentSharingTransferSession session;

	/**
	 * List of listeners
	 */
	private RemoteCallbackList<IFileTransferEventListener> listeners = new RemoteCallbackList<IFileTransferEventListener>();

    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param session Session
	 */
	public FileTransferSession(ContentSharingTransferSession session) {
		this.session = session;
		session.addListener(this);
	}

	/**
	 * Get session ID
	 * 
	 * @return Session ID
	 */
	public String getSessionID() {
		return session.getSessionID();
	}
	
	/**
	 * Accept the session invitation
	 */
	public void acceptSession() {
		if (logger.isActivated()) {
			logger.info("Accept session invitation");
		}
		
		// Remove the notification
		MessagingApiService.removeFileTransferInvitationNotification();
		
		// Accept invitation
		session.acceptSession();

		// Update messaging database
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), "accepted");
	}
	
	/**
	 * Reject the session invitation
	 */
	public void rejectSession() {
		if (logger.isActivated()) {
			logger.info("Reject session invitation");
		}
		
		// Remove the notification
		MessagingApiService.removeFileTransferInvitationNotification();

        // Reject invitation
		session.rejectSession();

		// Update messaging database
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), "rejected");
	}

	/**
	 * Cancel the session
	 */
	public void cancelSession() {
		if (logger.isActivated()) {
			logger.info("Abort session");
		}
		
		// Abort the session
		session.abortSession();

		// Update messaging database
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), "aborted");
	}
	
	/**
	 * Add session listener
	 * 
	 * @param listener Listener
	 */
	public void addSessionListener(IFileTransferEventListener listener) {
		if (logger.isActivated()) {
			logger.info("Add an event listener");
		}

		listeners.register(listener);
	}
	
	/**
	 * Remove session listener
	 * 
	 * @param listener Listener
	 */
	public void removeSessionListener(IFileTransferEventListener listener) {
		if (logger.isActivated()) {
			logger.info("Remove an event listener");
		}

		listeners.unregister(listener);
	}
	
	/**
	 * Session is started
	 */
    public void handleSessionStarted() {
		if (logger.isActivated()) {
			logger.info("Session started");
		}

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionStarted();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }

    /**
     * Session has been aborted
     */
    public void handleSessionAborted() {
		if (logger.isActivated()) {
			logger.info("Session aborted");
		}

		// Remove the notification
		MessagingApiService.removeFileTransferInvitationNotification();

		// Update messaging database
 		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), "aborted");
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionAborted();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }

    /**
     * Session has been terminated
     */
    public void handleSessionTerminated() {
		if (logger.isActivated()) {
			logger.info("Session terminated");
		}

		// Update messaging database
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), "finished");
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionTerminated();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }
    
    /**
     * Session has been terminated by remote
     */
    public void handleSessionTerminatedByRemote() {
		if (logger.isActivated()) {
			logger.info("Session terminated by remote");
		}

		// Update messaging database
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), "terminated by remote");
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleSessionTerminatedByRemote();
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }
	
    /**
     * Content sharing error
     * 
     * @param error Error
     */
    public void handleSharingError(ContentSharingError error) {
		if (logger.isActivated()) {
			logger.info("Sharing error");
		}

		// Update messaging database
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), "failed");
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleTransferError(error.getErrorCode());
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }

    /**
	 * Content sharing progress
	 * 
	 * @param currentSize Data size transfered 
	 * @param totalSize Total size to be transfered
	 */
    public void handleSharingProgress(long currentSize, long totalSize) {
		if (logger.isActivated()) {
			logger.info("Sharing progress");
		}
		
		// Update messaging database
		// TODO: merge into one SQL request
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), "in_progress");
  		RichMessaging.getInstance().updateFileTransferDownloadedSize(session.getSessionID(), currentSize);
		
  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleTransferProgress(currentSize, totalSize);
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
     }
    
    /**
     * Content has been transfered
     * 
     * @param filename Filename associated to the received content
     */
    public void handleContentTransfered(String filename) {
		if (logger.isActivated()) {
			logger.info("Content transfered");
		}

		// Update messaging database
		// TODO: merge into one SQL request
  		RichMessaging.getInstance().updateFileTransferStatus(session.getSessionID(), "finished");
  		RichMessaging.getInstance().updateFileTransferUrl(session.getSessionID(), filename);

  		// Notify event listeners
		final int N = listeners.beginBroadcast();
        for (int i=0; i < N; i++) {
            try {
            	listeners.getBroadcastItem(i).handleFileTransfered(filename);
            } catch (RemoteException e) {
            	if (logger.isActivated()) {
            		logger.error("Can't notify listener", e);
            	}
            }
        }
        listeners.finishBroadcast();		
    }	
}
