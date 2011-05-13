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

package com.orangelabs.rcs.service;

import java.util.Vector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.CoreListener;
import com.orangelabs.rcs.core.UserAccountException;
import com.orangelabs.rcs.core.TerminalInfo;
import com.orangelabs.rcs.core.ims.ImsError;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.service.im.chat.ChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.TerminatingAdhocGroupChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.TerminatingOne2OneChatSession;
import com.orangelabs.rcs.core.ims.service.presence.pidf.OverridingWillingness;
import com.orangelabs.rcs.core.ims.service.presence.pidf.Person;
import com.orangelabs.rcs.core.ims.service.presence.pidf.PidfDocument;
import com.orangelabs.rcs.core.ims.service.presence.pidf.Tuple;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.ContentSharingStreamingSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.core.ims.service.toip.TerminatingToIpSession;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.platform.logger.AndroidAppender;
import com.orangelabs.rcs.provider.eab.ContactsManager;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.sharing.RichCall;
import com.orangelabs.rcs.provider.sharing.RichCallData;
import com.orangelabs.rcs.service.api.client.ClientApiIntents;
import com.orangelabs.rcs.service.api.client.IImsApi;
import com.orangelabs.rcs.service.api.client.capability.Capabilities;
import com.orangelabs.rcs.service.api.client.capability.CapabilityApiIntents;
import com.orangelabs.rcs.service.api.client.capability.ICapabilityApi;
import com.orangelabs.rcs.service.api.client.contacts.ContactInfo;
import com.orangelabs.rcs.service.api.client.messaging.IMessagingApi;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApiIntents;
import com.orangelabs.rcs.service.api.client.presence.FavoriteLink;
import com.orangelabs.rcs.service.api.client.presence.Geoloc;
import com.orangelabs.rcs.service.api.client.presence.IPresenceApi;
import com.orangelabs.rcs.service.api.client.presence.PhotoIcon;
import com.orangelabs.rcs.service.api.client.presence.PresenceApiIntents;
import com.orangelabs.rcs.service.api.client.presence.PresenceInfo;
import com.orangelabs.rcs.service.api.client.richcall.IRichCallApi;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApiIntents;
import com.orangelabs.rcs.service.api.client.toip.IToIpApi;
import com.orangelabs.rcs.service.api.client.toip.ToIpApiIntents;
import com.orangelabs.rcs.service.api.server.ImsApiService;
import com.orangelabs.rcs.service.api.server.capability.CapabilityApiService;
import com.orangelabs.rcs.service.api.server.messaging.MessagingApiService;
import com.orangelabs.rcs.service.api.server.presence.PresenceApiService;
import com.orangelabs.rcs.service.api.server.richcall.RichCallApiService;
import com.orangelabs.rcs.service.api.server.toip.ToIpApiService;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Appender;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * RCS core service. This service offers a flat API to any other process (activities)
 * to access to RCS features. This service is started automatically at device boot.
 * 
 * @author jexa7410
 */
public class RcsCoreService extends Service implements CoreListener {
	/**
	 * RCS service name
	 */
	public static final String SERVICE_NAME = "com.orangelabs.rcs.SERVICE";
	
	/**
	 * Notification ID
	 */
	private final static int SERVICE_NOTIFICATION = 1000;
	
	/**
	 * CPU manager
	 */
	private CpuManager cpuManager = new CpuManager();

	/**
	 * IMS API
	 */
	private ImsApiService imsApi = new ImsApiService(); 
	
	/**
	 * Presence API
	 */
    private PresenceApiService presenceApi = new PresenceApiService(); 

	/**
	 * Capability API
	 */
    private CapabilityApiService capabilityApi = new CapabilityApiService(); 
    
	/**
	 * Messaging API
	 */
	private MessagingApiService messagingApi = new MessagingApiService(); 

	/**
	 * Rich call API
	 */
	private RichCallApiService richcallApi = new RichCallApiService(); 

	/**
	 * ToIP API
	 */
	private ToIpApiService toipApi = new ToIpApiService(); 
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
    public void onCreate() {
		// Set application context
		AndroidFactory.setApplicationContext(getApplicationContext());

		// Set logger appenders
		Appender[] appenders = new Appender[] { 
				new AndroidAppender()
			};
		Logger.setAppenders(appenders);
		
		// Set the terminal version
		TerminalInfo.PRODUCT_VERSION = getString(R.string.rcs_core_release_number);
		
		// Start the core
		startCore();
	}

    @Override
    public void onDestroy() {
    	// Close APIs
    	imsApi.close();
		presenceApi.close();
		capabilityApi.close();
		richcallApi.close();
		messagingApi.close();
		toipApi.close();
		
        // Stop the core
        stopCore();
    }

    /**
     * Start core
     */
    public synchronized void startCore() {
		if (Core.getInstance() != null) {
			// Already started
			return;
		}

        try {
			// Send service intent 
			Intent intent = new Intent(ClientApiIntents.SERVICE_STATUS);
			intent.putExtra("status", ClientApiIntents.SERVICE_STATUS_STARTING);
			getApplicationContext().sendBroadcast(intent);

			// Instanciate the settings manager
            RcsSettings.createInstance(getApplicationContext());
            
    		// Dump the settings values
            RcsSettings.getInstance().dump();

            // Set the logger properties
    		Logger.activationFlag = RcsSettings.getInstance().isTraceActivated();
    		String traceLevel = RcsSettings.getInstance().getTraceLevel();
    		if (traceLevel.equalsIgnoreCase("DEBUG")){
        		Logger.traceLevel = Logger.DEBUG_LEVEL;    			
    		} else if (traceLevel.equalsIgnoreCase("INFO")){
        		Logger.traceLevel = Logger.INFO_LEVEL;
    		} else if (traceLevel.equalsIgnoreCase("WARN")){
        		Logger.traceLevel = Logger.WARN_LEVEL;
    		} else if (traceLevel.equalsIgnoreCase("ERROR")){
        		Logger.traceLevel = Logger.ERROR_LEVEL;
    		} else if (traceLevel.equalsIgnoreCase("FATAL")){
        		Logger.traceLevel = Logger.FATAL_LEVEL;
    		}    		
            
            // Instanciate the contacts manager
            ContactsManager.createInstance(getApplicationContext());

            // Instanciate the rich messaging history 
            RichMessaging.createInstance(getApplicationContext());
            
            // Instanciate the rich call history 
            RichCall.createInstance(getApplicationContext());

            // Create the core
			Core.createCore(this);

			// Start the core
			Core.getInstance().startCore();		

			// Create multimedia directory on sdcard
			FileFactory.createDirectory(FileFactory.getFactory().getPhotoRootDirectory());
			FileFactory.createDirectory(FileFactory.getFactory().getVideoRootDirectory());
			FileFactory.createDirectory(FileFactory.getFactory().getFileRootDirectory());
			
			// Init CPU manager
			cpuManager.init();
			
	        // Show a first notification
	    	addRcsServiceNotification(false, getString(R.string.rcs_core_loaded));

			// Send service intent 
			intent = new Intent(ClientApiIntents.SERVICE_STATUS);
			intent.putExtra("status", ClientApiIntents.SERVICE_STATUS_STARTED);
			getApplicationContext().sendBroadcast(intent);

			if (logger.isActivated()) {
				logger.info("RCS core service started with success");
			}
        } catch(UserAccountException e){
			// User account can't be initialized (no radio to read IMSI, .etc)
			if (logger.isActivated()) {
				logger.error("Can't create the user account", e);
			}

			// Send service intent 
			Intent intent = new Intent(ClientApiIntents.SERVICE_STATUS);
			intent.putExtra("status", ClientApiIntents.SERVICE_STATUS_STOPPED);
			getApplicationContext().sendBroadcast(intent);

	    	// Exit service
	    	stopSelf();
		} catch(Exception e) {
			// Unexpected error
			if (logger.isActivated()) {
				logger.error("Can't instanciate the RCS core service", e);
			}
			
			// Send service intent 
			Intent intent = new Intent(ClientApiIntents.SERVICE_STATUS);
			intent.putExtra("status", ClientApiIntents.SERVICE_STATUS_FAILED);
			getApplicationContext().sendBroadcast(intent);

			// Show error in notification bar
	    	addRcsServiceNotification(false, getString(R.string.rcs_core_failed));
	    	
			// Exit service
	    	stopSelf();
		}
    }
    
    /**
     * Stop core
     */
    public synchronized void stopCore() {
		if (Core.getInstance() == null) {
			// Already stopped
			return;
		}
		
		// Send service intent 
		Intent intent = new Intent(ClientApiIntents.SERVICE_STATUS);
		intent.putExtra("status", ClientApiIntents.SERVICE_STATUS_STOPPING);
		getApplicationContext().sendBroadcast(intent);
		
		// Terminate the core in background
		Core.terminateCore();

		// Close CPU manager
		cpuManager.close();

		// Send service intent 
		intent = new Intent(ClientApiIntents.SERVICE_STATUS);
		intent.putExtra("status", ClientApiIntents.SERVICE_STATUS_STOPPED);
		getApplicationContext().sendBroadcast(intent);

		if (logger.isActivated()) {
			logger.info("RCS core service stopped with success");
		}
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (IImsApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("IMS API binding");
    		}
            return imsApi;
        } else
        if (IPresenceApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("Presence API binding");
    		}
            return presenceApi;
        } else
        if (ICapabilityApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("Capability API binding");
    		}
            return capabilityApi;
        } else
        if (IMessagingApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("Messaging API binding");
    		}
            return messagingApi;
        } else
        if (IRichCallApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("Rich call API binding");
    		}
            return richcallApi;
        } else
        if (IToIpApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("ToIP API binding");
    		}
            return toipApi;
        } else {
        	return null;
        }
    }
    
    /**
     * Add RCS service notification
     * 
     * @param state Service state (ON|OFF)
     * @param label Label
     */
    public static void addRcsServiceNotification(boolean state, String label) {
    	// Create notification
    	Intent intent = new Intent(ClientApiIntents.RCS_SETTINGS);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(AndroidFactory.getApplicationContext(), 0, intent, 0);
		int iconId; 
		if (state) {
			iconId  = R.drawable.rcs_core_notif_on_icon;
		} else {
			iconId  = R.drawable.rcs_core_notif_off_icon; 
		}
        Notification notif = new Notification(iconId, "", System.currentTimeMillis());
        notif.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_FOREGROUND_SERVICE;
        notif.setLatestEventInfo(AndroidFactory.getApplicationContext(),
        		AndroidFactory.getApplicationContext().getString(R.string.rcs_core_rcs_notification_title),
        		label, contentIntent);
        
        // Send notification
		NotificationManager notificationManager = (NotificationManager)AndroidFactory.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(SERVICE_NOTIFICATION, notif);
    }
    
    /*---------------------------- CORE EVENTS ---------------------------*/
    
    /**
     * Core layer has been started
     */
    public void handleCoreLayerStarted() {
		if (logger.isActivated()) {
			logger.debug("Handle event core started");
		}

		// Display a notification
		addRcsServiceNotification(false, getString(R.string.rcs_core_started));
    }

    /**
     * Core layer has been terminated
     */
    public void handleCoreLayerStopped() {
		if (logger.isActivated()) {
			logger.debug("Handle event core terminated");
		}

		// Display a notification
		addRcsServiceNotification(false, getString(R.string.rcs_core_stopped));
    }
    
	/**
	 * Handle "registration successful" event
	 * 
	 * @param registered Registration flag
	 */
	public void handleRegistrationSuccessful() {
		if (logger.isActivated()) {
			logger.debug("Handle event registration ok");
		}
		
		// Send registration intent
		Intent intent = new Intent(ClientApiIntents.SERVICE_REGISTRATION);
		intent.putExtra("status", true);
		getApplicationContext().sendBroadcast(intent);
		
		// Display a notification
		addRcsServiceNotification(true, getString(R.string.rcs_core_ims_connected));
	}

	/**
	 * Handle "registration failed" event
	 * 
     * @param error IMS error
   	 */
	public void handleRegistrationFailed(ImsError error) {
		if (logger.isActivated()) {
			logger.debug("Handle event registration failed");
		}

		// Send registration intent
		Intent intent = new Intent(ClientApiIntents.SERVICE_REGISTRATION);
		intent.putExtra("status", false);
		getApplicationContext().sendBroadcast(intent);

		// Display a notification
		addRcsServiceNotification(false, getString(R.string.rcs_core_ims_connection_failed));
	}

	/**
	 * Handle "registration terminated" event
	 */
	public void handleRegistrationTerminated() {
		if (logger.isActivated()) {
			logger.debug("Handle event registration terminated");
		}

		// Send registration intent
		Intent intent = new Intent(ClientApiIntents.SERVICE_REGISTRATION);
		intent.putExtra("status", false);
		getApplicationContext().sendBroadcast(intent);

		// Display a notification
		addRcsServiceNotification(false, getString(R.string.rcs_core_ims_disconnected));
	}

    /**
     * A new presence sharing notification has been received
     * 
     * @param contact Contact
     * @param status Status
     * @param reason Reason
     */
    public void handlePresenceSharingNotification(String contact, String status, String reason) {
		if (logger.isActivated()) {
			logger.debug("Handle event presence sharing notification for " + contact + " (" + status + ":" + reason + ")");
		}

		try {
			// Check if its a notification for a contact or for the end user
			String me = ImsModule.IMS_USER_PROFILE.getPublicUri();
			if (PhoneUtils.compareNumbers(me, contact)) {
				// End user notification
				if (logger.isActivated()) {
					logger.debug("Presence sharing notification for me: by-pass it");
				}
	    	} else { 
		    	// Update contacts database
				ContactsManager.getInstance().setContactSharingStatus(contact, status, reason);
	
				// Send intent
				Intent intent = new Intent(PresenceApiIntents.PRESENCE_SHARING_CHANGED);
		    	intent.putExtra("contact", contact);
		    	intent.putExtra("status", status);
		    	intent.putExtra("reason", reason);
				AndroidFactory.getApplicationContext().sendBroadcast(intent);
	    	}
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Internal exception", e);
    		}
    	}
    }

    /**
     * A new presence info notification has been received
     * 
     * @param contact Contact
     * @param presense Presence info document
     */
    public void handlePresenceInfoNotification(String contact, PidfDocument presence) {
    	if (logger.isActivated()) {
			logger.debug("Handle event presence info notification for " + contact);
		}

		try {
			// Test if person item is not null
			Person person = presence.getPerson();
			if (person == null) {
				if (logger.isActivated()) {
					logger.debug("Presence info is empty (i.e. no item person found) for contact " + contact);
				}
				return;
			}

			// Check if its a notification for a contact or for me
			String me = ImsModule.IMS_USER_PROFILE.getPublicUri();
			if (PhoneUtils.compareNumbers(me, contact)) {
				// Notification for me
				presenceInfoNotificationForMe(presence);
			} else {
				// Check that the contact exist in database
				String rcsStatus = ContactsManager.getInstance().getContactSharingStatus(contact);
				if (rcsStatus == null) {
					if (logger.isActivated()) {
						logger.debug("Contact " + contact + " is not a RCS contact, by-pass the notification");
					}
					return;
				}

				// Notification for a contact
				presenceInfoNotificationForContact(contact, presence);
			}
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Internal exception", e);
    		}
		}
	}

    /**
     * A new presence info notification has been received for me
     * 
     * @param contact Contact
     * @param presense Presence info document
     */
    public void presenceInfoNotificationForMe(PidfDocument presence) {
    	if (logger.isActivated()) {
			logger.debug("Presence info notification for me");
		}

    	try {
			// Get the current presence info for me
    		PresenceInfo currentPresenceInfo = ContactsManager.getInstance().getMyPresenceInfo();
    		if (currentPresenceInfo == null) {
    			currentPresenceInfo = new PresenceInfo();
    		}

			// Update presence status
			String presenceStatus = PresenceInfo.UNKNOWN;
			Person person = presence.getPerson();
			OverridingWillingness willingness = person.getOverridingWillingness();
			if (willingness != null) {
				if ((willingness.getBasic() != null) && (willingness.getBasic().getValue() != null)) {
					presenceStatus = willingness.getBasic().getValue();
				}
			}				
			currentPresenceInfo.setPresenceStatus(presenceStatus);
    		
    		// Update the presence info
			currentPresenceInfo.setTimestamp(person.getTimestamp());
			if (person.getNote() != null) {
				currentPresenceInfo.setFreetext(person.getNote().getValue());
			}
			if (person.getHomePage() != null) {
				currentPresenceInfo.setFavoriteLink(new FavoriteLink(person.getHomePage()));
			}
			
    		// Get photo Etag values
			String lastEtag = null;
			String newEtag = null; 
			if (person.getStatusIcon() != null) {
				newEtag = person.getStatusIcon().getEtag();
			}
			if (currentPresenceInfo.getPhotoIcon() != null) {
				lastEtag = currentPresenceInfo.getPhotoIcon().getEtag();
			}
    		
    		// Test if the photo has been removed
			if ((lastEtag != null) && (person.getStatusIcon() == null)) {
	    		if (logger.isActivated()) {
	    			logger.debug("Photo has been removed for me");
	    		}
	    		
    			// Update the presence info
				currentPresenceInfo.setPhotoIcon(null);

				// Update EAB provider
				ContactsManager.getInstance().removeMyPhotoIcon();
			} else		
	    	// Test if the photo has been changed
	    	if ((person.getStatusIcon() != null) &&	(newEtag != null)) {
	    		if ((lastEtag == null) || (!lastEtag.equals(newEtag))) {
		    		if (logger.isActivated()) {
		    			logger.debug("Photo has changed for me, download it in background");
		    		}
		
		    		// Download the photo in background
		    		downloadPhotoForMe(presence.getPerson().getStatusIcon().getUrl(), newEtag);
	    		}
	    	}
	    	   		    		
	    	// Update EAB provider
			ContactsManager.getInstance().setMyInfo(currentPresenceInfo);

    		// Send intent
	    	Intent intent = new Intent(PresenceApiIntents.MY_PRESENCE_INFO_CHANGED);
	    	getApplicationContext().sendBroadcast(intent);
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Internal exception", e);
    		}
		}
    }

    /**
     * A new presence info notification has been received for a given contact
     * 
     * @param contact Contact
     * @param presense Presence info document
     */
    public void presenceInfoNotificationForContact(String contact, PidfDocument presence) {
    	if (logger.isActivated()) {
			logger.debug("Presence info notification for contact " + contact);
		}

    	try {
    		// Extract number from contact 
    		String number = PhoneUtils.extractNumberFromUri(contact);

    		// Get the current presence info
    		ContactInfo contactInfo = ContactsManager.getInstance().getContactInfo(contact);
    		if (contactInfo == null) {
    			if (logger.isActivated()) {
    				logger.warn("Contact " + contact + " not found in EAB: by-pass the notification");
    			}
    			return;
    		}
    		PresenceInfo currentPresenceInfo = contactInfo.getPresenceInfo();
    		if (currentPresenceInfo == null) {
    			currentPresenceInfo = new PresenceInfo();
    			contactInfo.setPresenceInfo(currentPresenceInfo);
    		}

			// Update the current capabilities
			Capabilities capabilities =  new Capabilities(); 
			Vector<Tuple> tuples = presence.getTuplesList();
			for(int i=0; i < tuples.size(); i++) {
				Tuple tuple = (Tuple)tuples.elementAt(i);
				
				boolean state = false; 
				if (tuple.getStatus().getBasic().getValue().equals("open")) {
					state = true;
				}
					
				String id = tuple.getService().getId();
				if (id.equalsIgnoreCase(SipUtils.FEATURE_RCS2_VIDEO_SHARE)) {
					capabilities.setVideoSharingSupport(state);
				} else
				if (id.equalsIgnoreCase(SipUtils.FEATURE_RCS2_IMAGE_SHARE)) {
					capabilities.setImageSharingSupport(state);
				} else
				if (id.equalsIgnoreCase(SipUtils.FEATURE_RCS2_FT)) {
					capabilities.setFileTransferSupport(state);
				} else
				if (id.equalsIgnoreCase(SipUtils.FEATURE_RCS2_CS_VIDEO)) {
					capabilities.setCsVideoSupport(state);
				} else
				if (id.equalsIgnoreCase(SipUtils.FEATURE_RCS2_CHAT)) {
					capabilities.setImSessionSupport(state);
				}
			}
			contactInfo.setCapabilities(capabilities);

    		// Store & forward support
            if (RcsSettings.getInstance().isImAlwaysOn()) {
				capabilities.setImSessionSupport(true);
            }		    		
			
			// Update presence status
			String presenceStatus = PresenceInfo.UNKNOWN;
			Person person = presence.getPerson();
			OverridingWillingness willingness = person.getOverridingWillingness();
			if (willingness != null) {
				if ((willingness.getBasic() != null) && (willingness.getBasic().getValue() != null)) {
					presenceStatus = willingness.getBasic().getValue();
				}
			}				
			currentPresenceInfo.setPresenceStatus(presenceStatus);

			// Update the presence info
			currentPresenceInfo.setTimestamp(person.getTimestamp());
			if (person.getNote() != null) {
				currentPresenceInfo.setFreetext(person.getNote().getValue());
			}
			if (person.getHomePage() != null) {
				currentPresenceInfo.setFavoriteLink(new FavoriteLink(person.getHomePage()));
			}
			
			// Update geoloc info
			if (presence.getGeopriv() != null) {
				Geoloc geoloc = new Geoloc(presence.getGeopriv().getLatitude(),
						presence.getGeopriv().getLongitude(),
						presence.getGeopriv().getAltitude());
				currentPresenceInfo.setGeoloc(geoloc);
			}
			
	    	// Update contacts database
			ContactsManager.getInstance().setContactInfo(contact, contactInfo);

    		// Get photo Etag values
			String lastEtag = ContactsManager.getInstance().getContactPhotoEtag(contact);
			String newEtag = null; 
			if (person.getStatusIcon() != null) {
				newEtag = person.getStatusIcon().getEtag();
			}

    		// Test if the photo has been removed
			if ((lastEtag != null) && (person.getStatusIcon() == null)) {
	    		if (logger.isActivated()) {
	    			logger.debug("Photo has been removed for " + contact);
	    		}

	    		// Update contacts database
	    		ContactsManager.getInstance().setContactPhotoIcon(contact, null);
				
	    		// Send intent
				Intent intent = new Intent(PresenceApiIntents.CONTACT_PHOTO_CHANGED);
		    	intent.putExtra("contact", number);
				AndroidFactory.getApplicationContext().sendBroadcast(intent);
			} else		
	    	// Test if the photo has been changed
	    	if ((person.getStatusIcon() != null) &&	(newEtag != null)) {
	    		if ((lastEtag == null) || (!lastEtag.equals(newEtag))) {
		    		if (logger.isActivated()) {
		    			logger.debug("Photo has changed for " + contact + ", download it in background");
		    		}
		
		    		// Download the photo in background
		    		downloadPhotoForContact(contact, presence.getPerson().getStatusIcon().getUrl(), newEtag);
	    		}
	    	}    	
	    	   		    		
    		// Send intent
	    	Intent intent = new Intent(PresenceApiIntents.CONTACT_INFO_CHANGED);
	    	intent.putExtra("contact", number);
	    	getApplicationContext().sendBroadcast(intent);
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Internal exception", e);
    		}
		}
    }
    
    /**
     * Capabilities update notification has been received
     * 
     * @param contact Contact
     * @param capabilities Capabilities
     */
    public void handleCapabilitiesNotification(String contact, Capabilities capabilities) {
    	if (logger.isActivated()) {
			logger.debug("Handle capabilities update notification for " + contact);
		}

		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(contact);

		// Send intent
    	Intent intent = new Intent(CapabilityApiIntents.CONTACT_CAPABILITIES);
    	intent.putExtra("contact", number);
    	intent.putExtra("capabilities", capabilities);
    	getApplicationContext().sendBroadcast(intent);
    }
    
    /**
     * Download photo for me
     * 
     * @param url Photo URL
     * @param etag New Etag associated to the photo
     */
    private void downloadPhotoForMe(final String url, final String etag) {
		Thread t = new Thread() {
			public void run() {
		    	try {
		    		// Download from XDMS
		    		PhotoIcon icon = Core.getInstance().getPresenceService().getXdmManager().downloadContactPhoto(url, etag);    		
		    		if (icon != null) {
		    			// Update the presence info
		    			Core.getInstance().getPresenceService().getPresenceInfo().setPhotoIcon(icon);
		    			
						// Update contacts database
		    			ContactsManager.getInstance().setMyPhotoIcon(icon);
						
			    		// Broadcast intent
		    			// TODO : use a specific intent for the end user photo
				    	Intent intent = new Intent(PresenceApiIntents.MY_PRESENCE_INFO_CHANGED);
				    	getApplicationContext().sendBroadcast(intent);
			    	}
		    	} catch(Exception e) {
		    		if (logger.isActivated()) {
		    			logger.error("Internal exception", e);
		    		}
	    		}
			}
		};
		t.start();
    }
    
    /**
     * Download photo for a given contact
     * 
     * @param contact Contact
     * @param url Photo URL 
     * @param etag New Etag associated to the photo
     */
    private void downloadPhotoForContact(final String contact, final String url, final String etag) {
		Thread t = new Thread() {
			public void run() {
		    	try {
		    		// Download from XDMS
		    		PhotoIcon icon = Core.getInstance().getPresenceService().getXdmManager().downloadContactPhoto(url, etag);    		
		    		if (icon != null) {
		    			// Update contacts database
		    			ContactsManager.getInstance().setContactPhotoIcon(contact, icon);

		    			// Extract number from contact 
		    			String number = PhoneUtils.extractNumberFromUri(contact);

		    			// Broadcast intent
		    			Intent intent = new Intent(PresenceApiIntents.CONTACT_PHOTO_CHANGED);
		    			intent.putExtra("contact", number);
		    			getApplicationContext().sendBroadcast(intent);
			    	}
		    	} catch(Exception e) {
		    		if (logger.isActivated()) {
		    			logger.error("Internal exception", e);
		    		}
	    		}
			}
		};
		t.start();
    }
    
    /**
     * A new presence sharing invitation has been received
     * 
     * @param contact Contact
     */
    public void handlePresenceSharingInvitation(String contact) {
		if (logger.isActivated()) {
			logger.debug("Handle event presence sharing invitation");
		}
		
		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(contact);
		
        // Notify event listeners
    	Intent intent = new Intent(PresenceApiIntents.PRESENCE_INVITATION);
    	intent.putExtra("contact", number);
    	getApplicationContext().sendBroadcast(intent);
    }
    
    /**
     * New content sharing transfer invitation
     * 
     * @param session Content sharing transfer invitation
     */
    public void handleContentSharingTransferInvitation(ContentSharingTransferSession session) {
		if (logger.isActivated()) {
			logger.debug("Handle event content sharing transfer invitation");
		}

		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(session.getRemoteContact());

		// Update rich call history
		RichCall.getInstance().addCall(number, session.getSessionID(),
    			RichCallData.EVENT_INCOMING, 
    			session.getContent(),
    			RichCallData.STATUS_STARTED);
		
        // Notify event listeners
    	Intent intent = new Intent(RichCallApiIntents.IMAGE_SHARING_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("sessionId", session.getSessionID());
    	intent.putExtra("size", session.getContent().getSize());
    	getApplicationContext().sendBroadcast(intent);		
    }
    
    /**
     * New content sharing streaming invitation
     * 
     * @param session CSh session
     */
    public void handleContentSharingStreamingInvitation(ContentSharingStreamingSession session) {
		if (logger.isActivated()) {
			logger.debug("Handle event content sharing streaming invitation");
		}

		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(session.getRemoteContact());

		// Update rich call history
		RichCall.getInstance().addCall(number, session.getSessionID(),
    			RichCallData.EVENT_INCOMING, 
    			session.getContent(),
    			RichCallData.STATUS_STARTED);
		
        // Notify event listeners
    	Intent intent = new Intent(RichCallApiIntents.VIDEO_SHARING_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("sessionId", session.getSessionID());
    	getApplicationContext().sendBroadcast(intent);		
    }

	/**
	 * A new file transfer invitation has been received
	 * 
	 * @param session File transfer session
	 */
	public void handleFileTransferInvitation(ContentSharingTransferSession session) {
		if (logger.isActivated()) {
			logger.debug("Handle event file transfer invitation");
		}
		
		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(session.getRemoteContact());

		// Set the file transfer session ID from the chat session if a chat already exist
		String ftSessionId = session.getSessionID();
		String chatSessionId = ftSessionId;
		Vector<ChatSession> chatSessions = Core.getInstance().getImService().getImSessionsWith(number);
		if (chatSessions.size() > 0) {
			ChatSession chatSession = chatSessions.lastElement();
			chatSessionId = chatSession.getSessionID();
		}
		
		// Update rich messaging history
    	RichMessaging.getInstance().addFileTransferInvitation(number, chatSessionId, ftSessionId, session.getContent());
    	
        // Notify event listeners
    	Intent intent = new Intent(MessagingApiIntents.FILE_TRANSFER_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("sessionId", session.getSessionID());
    	intent.putExtra("name", session.getContent().getName());
    	intent.putExtra("size", session.getContent().getSize());
    	getApplicationContext().sendBroadcast(intent);
	}
    
	/**
     * New one-to-one chat session invitation
     * 
     * @param session Chat session
     */
	public void handleOneOneChatSessionInvitation(TerminatingOne2OneChatSession session) {
		if (logger.isActivated()) {
			logger.debug("Handle event receive 1-1 chat session invitation");
		}

		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(session.getRemoteContact());

		// Update rich messaging history
		RichMessaging.getInstance().addChatInvitation(session);

    	// Notify event listeners
    	Intent intent = new Intent(MessagingApiIntents.CHAT_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("subject", session.getSubject());
    	intent.putExtra("sessionId", session.getSessionID());
    	intent.putExtra("isChatGroup", false);
    	getApplicationContext().sendBroadcast(intent);
    }

    /**
     * New ad-hoc group chat session invitation
     * 
     * @param session Chat session
     */
	public void handleAdhocGroupChatSessionInvitation(TerminatingAdhocGroupChatSession session) {
		if (logger.isActivated()) {
			logger.debug("Handle event receive ad-hoc group chat session invitation");
		}

		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(session.getRemoteContact());
		
		// Update rich messaging history
		RichMessaging.getInstance().addChatInvitation(session);

    	// Notify event listeners
    	Intent intent = new Intent(MessagingApiIntents.CHAT_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("subject", session.getSubject());
    	intent.putExtra("sessionId", session.getSessionID());
    	intent.putExtra("isChatGroup", true);
    	getApplicationContext().sendBroadcast(intent);
	}
    
    /**
     * New ToIP call invitation
     * 
     * @param session ToIP session
     */
    public void handleToIpCallInvitation(TerminatingToIpSession session) {
		if (logger.isActivated()) {
			logger.debug("Handle event receive ToIP session invitation");
		}
		
		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(session.getRemoteContact());

    	// Notify event listeners
    	Intent intent = new Intent(ToIpApiIntents.TOIP_CALL_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("sessionId", session.getSessionID());
    	getApplicationContext().sendBroadcast(intent);
    }
}
