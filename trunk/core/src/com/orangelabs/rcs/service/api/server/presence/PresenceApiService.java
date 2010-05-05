package com.orangelabs.rcs.service.api.server.presence;

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
import com.orangelabs.rcs.core.ims.service.presence.PresenceInfo;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.provider.eab.RichAddressBook;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.RcsCoreService;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.client.presence.IPresenceApi;
import com.orangelabs.rcs.service.api.client.presence.PresenceApiIntents;
import com.orangelabs.rcs.service.api.server.ServerApiException;
import com.orangelabs.rcs.service.api.server.ServerApiUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Presence API service
 * 
 * @author jexa7410
 */
public class PresenceApiService extends IPresenceApi.Stub {
	/**
	 * Notification ID
	 */
	private final static int PRESENCE_INVITATION_NOTIFICATION = 1002;

    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public PresenceApiService() {
		if (logger.isActivated()) {
			logger.info("Presence API service is loaded");
		}
	}

	/**
	 * Close API
	 */
	public void close() {
		// Remove notification
		PresenceApiService.removeSharingInvitationNotification();
	}
	
    /**
     * Add presence sharing invitation notification
     * 
     * @param contact Contact
     */
    public static void addSharingInvitationNotification(String contact) {
		// Create notification
		Intent intent = new Intent(PresenceApiIntents.PRESENCE_INVITATION);
		intent.putExtra("contact", contact);
        PendingIntent contentIntent = PendingIntent.getActivity(RcsCoreService.CONTEXT, 0, intent, 0);
        Notification notif = new Notification(R.drawable.rcs_core_notif_presence_icon,
        		RcsCoreService.CONTEXT.getString(R.string.title_presence_invitation_notification),
        		System.currentTimeMillis());
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(RcsCoreService.CONTEXT,
        		RcsCoreService.CONTEXT.getString(R.string.title_presence_invitation_notification),
        		RcsCoreService.CONTEXT.getString(R.string.label_presence_notification),
        		contentIntent);

        // Set ringtone
        String ringtone = RcsSettings.getInstance().getPresenceInvitationRingtone();
        if (!TextUtils.isEmpty(ringtone)) {
			notif.sound = Uri.parse(ringtone);
        }
        
        // Set vibration
        if (RcsSettings.getInstance().isPhoneVibrateForPresenceInvitation()) {
        	notif.defaults |= Notification.DEFAULT_VIBRATE;
        }
        
        // Send notification
		NotificationManager notificationManager = (NotificationManager)RcsCoreService.CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(PRESENCE_INVITATION_NOTIFICATION, notif);
    }

    /**
     * Remove presence sharing invitation notification
     */
    public static void removeSharingInvitationNotification() {
		// Remove the notification
		NotificationManager notificationManager = (NotificationManager)RcsCoreService.CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(PRESENCE_INVITATION_NOTIFICATION);
    }
    
    /**
	 * Set my presence info
	 * 
	 * @param info Presence info
	 * @return Boolean result
	 * @throws ServerApiException
	 */
	public boolean setMyPresenceInfo(PresenceInfo info) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Set my presence info");
		}

		// Test IMS connection 
		ServerApiUtils.testIms();

		try {
			// Publish presence info
			boolean result = Core.getInstance().getPresenceService().publishPresenceInfo(info);
			if (result) {
				// Update EAB content provider
				RichAddressBook.getInstance().setMyPresenceInfo(info);
		
				// Broadcast intent
				Intent intent = new Intent(PresenceApiIntents.MY_PRESENCE_INFO_CHANGED);
				AndroidFactory.getApplicationContext().sendBroadcast(intent);
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e);
		}
	}

	/**
	 * Set my hyper-availability status
	 * 
	 * @param status Status
	 * @return Boolean result
	 * @throws ServerApiException
	 */
	public boolean setMyHyperAvailabilityStatus(boolean status) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Set my hyper-availability status to " + status);
		}
		
		// Test core availability
		ServerApiUtils.testCore();

		try {
			return Core.getInstance().getImsModule().getPresenceService().publishPoke(status);
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Get the hyper-availability expiration time
	 * 
	 * @return Expiration time in milliseconds or 0 if there is no poke activated
	 * @throws ClientApiException
	 */
	public long getHyperAvailabilityExpiration() throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Get the hyper-availability expiration time");
		}
		
		// Test core availability
		ServerApiUtils.testCore();

		try {
			return Core.getInstance().getImsModule().getPresenceService().getPokeManager().getPokeExpireDate();
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Invite a contact to share its presence
	 * 
	 * @param contact Contact
	 * @return Boolean result
	 * @throws ServerApiException
	 */
	public boolean inviteContact(String contact) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Invite " + contact + " to share presence");
		}

		// Test core availability
		ServerApiUtils.testCore();

		try {
			// Update presence server
			boolean result = Core.getInstance().getPresenceService().inviteContactToSharePresence(contact);
			if (result){
				// Put "pending_out" as presence status for contact in EAB content provider
				RichAddressBook.getInstance().setContactSharingStatus(contact, "pending_out", "");
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
		
	/**
	 * Accept sharing invitation
	 * 
	 * @param contact Contact
	 * @return Boolean result
	 * @throws ServerApiException
	 */
	public boolean acceptSharingInvitation(String contact) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Accept sharing invitation from " + contact);
		}

        // Test core availability
		ServerApiUtils.testCore();

		try {
			// Update presence server
			boolean result = Core.getInstance().getPresenceService().acceptPresenceSharingInvitation(contact);
			if (result){
				// Create contact in address book if needed
				RichAddressBook.getInstance().createContact(contact);
				
				// Set this contact presence status to "active"
				RichAddressBook.getInstance().setContactSharingStatus(contact, "active", "");
				
				// Remove the notification
				PresenceApiService.removeSharingInvitationNotification();
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Reject sharing invitation
	 * 
	 * @param contact Contact
	 * @return Boolean result
	 * @throws ServerApiException
	 */
	public boolean rejectSharingInvitation(String contact) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Reject sharing invitation from " + contact);
		}

        // Test core availability
		ServerApiUtils.testCore();

		try {
			// Update presence server
			boolean result = Core.getInstance().getPresenceService().blockPresenceSharingInvitation(contact);
			if (result){
				// Remove the notification
				PresenceApiService.removeSharingInvitationNotification();

				// Put contact in blocked contacts list of EAB content provider
				RichAddressBook.getInstance().blockContact(contact);
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Ignore sharing invitation
	 * 
	 * @param contact Contact
	 * @throws ServerApiException
	 */
	public void ignoreSharingInvitation(String contact) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Ignore sharing invitation from " + contact);
		}

		try {
			//TODO see what behaviour is awaited
			// Create contact in address book if needed
			RichAddressBook.getInstance().createContact(contact);
	
			// Set this contact presence status to "pending"
			RichAddressBook.getInstance().setContactSharingStatus(contact, "pending", "");
	
			// Remove the notification
			PresenceApiService.removeSharingInvitationNotification();
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Revoke a contact
	 * 
	 * @param contact Contact
	 * @return Boolean result
	 * @throws ServerApiException
	 */
	public boolean revokeContact(String contact) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Revoke contact " + contact);
		}
		
		// Test core availability
		ServerApiUtils.testCore();

		try {
			// Update presence server
			boolean result = Core.getInstance().getPresenceService().revokeSharedContact(contact);
			if (result){
				// Put contact in revoked contacts list of EAB content provider
				RichAddressBook.getInstance().revokeContact(contact);
				
				// The contact should be automatically unrevoked after a given timeout. Here the
				// timeout period is 0, so the contact can receive invitations again now
				unrevokeContact(contact);			
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
     * Unrevoke a contact
     * 
     * @param contact Contact
	 * @return Boolean result
     * @throws ServerApiException
     */
	public boolean unrevokeContact(String contact) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Unrevoke contact " + contact);
		}
		
		// Test core availability
		ServerApiUtils.testCore();

		try {
			// Update presence server
			boolean result = Core.getInstance().getPresenceService().removeRevokedContact(contact);
			if (result){
				// Remove contact from revoked contacts list of EAB content provider
				RichAddressBook.getInstance().unrevokeContact(contact);
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

    /**
     * Unblock a contact
     * 
     * @param contact Contact
	 * @return Boolean result
     * @throws ServerApiException
     */
	public boolean unblockContact(String contact) throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Unblock contact " + contact);
		}
		
		// Test core availability
		ServerApiUtils.testCore();

		try {
			// Update presence server
			boolean result = Core.getInstance().getPresenceService().removeBlockedContact(contact);
			if (result){
				// Remove contact from blocked contacts list of EAB content provider
				RichAddressBook.getInstance().unblockContact(contact);
			}
			return result;
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Get the list of granted contacts
	 * 
	 * @return List of contacts
	 * @throws ServerApiException
	 */
	public List<String> getGrantedContacts() throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Returns list of granted contacts");
		}

		// Test core availability
		ServerApiUtils.testCore();

		try {
			return Core.getInstance().getPresenceService().getXdmManager().getGrantedContacts();
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
	
	/**
	 * Get the list of revoked contacts
	 * 
	 * @return List of contacts
	 * @throws ServerApiException
	 */
	public List<String> getRevokedContacts() throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Returns list of revoked contacts");
		}

		// Test core availability
		ServerApiUtils.testCore();

		try {
			return Core.getInstance().getPresenceService().getXdmManager().getRevokedContacts();
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}

	/**
	 * Get the list of blocked contacts
	 * 
	 * @return List of contacts
	 * @throws ServerApiException
	 */
	public List<String> getBlockedContacts() throws ServerApiException {
		if (logger.isActivated()) {
			logger.info("Returns list of blocked contacts");
		}
		
		// Test core availability
		ServerApiUtils.testCore();

		try {
			return Core.getInstance().getPresenceService().getXdmManager().getBlockedContacts();
		} catch(Exception e) {
			throw new ServerApiException(e.getMessage());
		}
	}
}