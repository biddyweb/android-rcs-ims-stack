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
package com.orangelabs.rcs.service;

import java.io.File;
import java.util.Vector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.addressbook.ContactsManager;
import com.orangelabs.rcs.core.Core;
import com.orangelabs.rcs.core.CoreListener;
import com.orangelabs.rcs.core.TerminalInfo;
import com.orangelabs.rcs.core.ims.ImsError;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.service.capability.Capabilities;
import com.orangelabs.rcs.core.ims.service.im.InstantMessage;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageSession;
import com.orangelabs.rcs.core.ims.service.im.chat.TerminatingAdhocGroupChatSession;
import com.orangelabs.rcs.core.ims.service.im.chat.TerminatingOne2OneChatSession;
import com.orangelabs.rcs.core.ims.service.presence.FavoriteLink;
import com.orangelabs.rcs.core.ims.service.presence.Geoloc;
import com.orangelabs.rcs.core.ims.service.presence.PhotoIcon;
import com.orangelabs.rcs.core.ims.service.presence.PresenceError;
import com.orangelabs.rcs.core.ims.service.presence.PresenceInfo;
import com.orangelabs.rcs.core.ims.service.presence.pidf.OverridingWillingness;
import com.orangelabs.rcs.core.ims.service.presence.pidf.Person;
import com.orangelabs.rcs.core.ims.service.presence.pidf.PidfDocument;
import com.orangelabs.rcs.core.ims.service.presence.pidf.Tuple;
import com.orangelabs.rcs.core.ims.service.sharing.streaming.ContentSharingStreamingSession;
import com.orangelabs.rcs.core.ims.service.sharing.transfer.ContentSharingTransferSession;
import com.orangelabs.rcs.core.ims.service.toip.TerminatingToIpSession;
import com.orangelabs.rcs.core.ims.userprofile.UserProfileNotProvisionnedException;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.platform.logger.AndroidAppender;
import com.orangelabs.rcs.provider.messaging.RichMessaging;
import com.orangelabs.rcs.provider.messaging.RichMessagingData;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.sharing.RichCall;
import com.orangelabs.rcs.provider.sharing.RichCallData;
import com.orangelabs.rcs.service.api.client.management.IManagementApi;
import com.orangelabs.rcs.service.api.client.management.ManagementApiIntents;
import com.orangelabs.rcs.service.api.client.messaging.IMessagingApi;
import com.orangelabs.rcs.service.api.client.messaging.MessagingApiIntents;
import com.orangelabs.rcs.service.api.client.presence.IPresenceApi;
import com.orangelabs.rcs.service.api.client.presence.PresenceApiIntents;
import com.orangelabs.rcs.service.api.client.richcall.IRichCallApi;
import com.orangelabs.rcs.service.api.client.richcall.RichCallApiIntents;
import com.orangelabs.rcs.service.api.client.toip.IToIpApi;
import com.orangelabs.rcs.service.api.client.toip.ToIpApiIntents;
import com.orangelabs.rcs.service.api.server.management.ManagementApiService;
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
	 * Notification ID
	 */
	private final static int RCS_SERVICE_NOTIFICATION = 1000;
	
	/**
	 * Application context
	 */
    public static Context CONTEXT = null;

	/**
	 * CPU manager
	 */
	private CpuManager cpuManager = new CpuManager();

	/**
	 * Presence API
	 */
    private PresenceApiService presenceApi = new PresenceApiService(); 

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
	 * Management API
	 */
	private ManagementApiService mgtApi = new ManagementApiService(); 
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
    public void onCreate() {
		// Set logger appenders
		Appender[] appenders = new Appender[] { 
				new AndroidAppender()
			};
		Logger.setAppenders(appenders);

		// Set global application context
		CONTEXT = getApplicationContext();

		// Set the terminal version
		TerminalInfo.PRODUCT_VERSION = getString(R.string.rcs_core_release_number);
		
		// Start the core
		startCore();
	}

    @Override
    public void onDestroy() {
    	// Close APIs
		presenceApi.close();
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
			Intent intent = new Intent(ManagementApiIntents.SERVICE_STATUS);
			intent.putExtra("status", ManagementApiIntents.SERVICE_STATUS_STARTING);
			getApplicationContext().sendBroadcast(intent);

			// Instanciate platform factory
			AndroidFactory.loadFactory(getApplicationContext());

			// Instanciate the settings manager
            RcsSettings.createInstance(getApplicationContext());
            
    		// Set the logger properties
    		Logger.activationFlag = RcsSettings.getInstance().isTraceActivated();;
    		String traceLevel = RcsSettings.getInstance().getTraceLevel();
    		if (traceLevel.equalsIgnoreCase("DEBUG")){
        		Logger.traceLevel = Logger.DEBUG_LEVEL;    			
    		}else if (traceLevel.equalsIgnoreCase("INFO")){
        		Logger.traceLevel = Logger.INFO_LEVEL;
    		}else if (traceLevel.equalsIgnoreCase("WARN")){
        		Logger.traceLevel = Logger.WARN_LEVEL;
    		}else if (traceLevel.equalsIgnoreCase("ERROR")){
        		Logger.traceLevel = Logger.ERROR_LEVEL;
    		}else if (traceLevel.equalsIgnoreCase("FATAL")){
        		Logger.traceLevel = Logger.FATAL_LEVEL;
    		}    		
            
    		// Dump the settings values
            RcsSettings.getInstance().dump();
            
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
			createDirectory(FileFactory.getFactory().getPhotoRootDirectory());
			createDirectory(FileFactory.getFactory().getVideoRootDirectory());
			createDirectory(FileFactory.getFactory().getFileRootDirectory());
			
			// Init CPU manager
			cpuManager.init();
			
	        // Show a first notification
	    	addRcsServiceNotification(false, getString(R.string.rcs_core_label_rcs_loaded));

			// Send service intent 
			intent = new Intent(ManagementApiIntents.SERVICE_STATUS);
			intent.putExtra("status", ManagementApiIntents.SERVICE_STATUS_STARTED);
			getApplicationContext().sendBroadcast(intent);

			if (logger.isActivated()) {
				logger.info("RCS core service started with success");
			}
        } catch(UserProfileNotProvisionnedException e) {
			// Send service intent 
			Intent intent = new Intent(ManagementApiIntents.SERVICE_STATUS);
			intent.putExtra("status", ManagementApiIntents.SERVICE_STATUS_STOPPED);
			getApplicationContext().sendBroadcast(intent);

			// User profile not well provisionned
			if (logger.isActivated()) {
				logger.error("User profile not well provisionned: " + e.getMessage());
			}
	    	Intent provisionning = new Intent("com.orangelabs.rcs.PROVISIONING");
	    	provisionning.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	startActivity(provisionning);
	    	stopSelf();
		} catch(Exception e) {
			// Unexpected error
			if (logger.isActivated()) {
				logger.error("Can't instanciate the RCS core service", e);
			}
			
			// Send service intent 
			Intent intent = new Intent(ManagementApiIntents.SERVICE_STATUS);
			intent.putExtra("status", ManagementApiIntents.SERVICE_STATUS_FAILED);
			getApplicationContext().sendBroadcast(intent);

			// Show error in notification bar
	    	addRcsServiceNotification(false, getString(R.string.rcs_core_label_rcs_failed));
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
		Intent intent = new Intent(ManagementApiIntents.SERVICE_STATUS);
		intent.putExtra("status", ManagementApiIntents.SERVICE_STATUS_STOPPING);
		getApplicationContext().sendBroadcast(intent);
		
		// Terminate the core in background
		Core.terminateCore();

		// Close CPU manager
		cpuManager.close();

		// Send service intent 
		intent = new Intent(ManagementApiIntents.SERVICE_STATUS);
		intent.putExtra("status", ManagementApiIntents.SERVICE_STATUS_STOPPED);
		getApplicationContext().sendBroadcast(intent);

		if (logger.isActivated()) {
			logger.info("RCS core service stopped with success");
		}
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (IPresenceApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("Presence API binding");
    		}
            return presenceApi;
        }
        if (IMessagingApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("Messaging API binding");
    		}
            return messagingApi;
        }
        if (IRichCallApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("Rich call API binding");
    		}
            return richcallApi;
        }
        if (IManagementApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("Settings API binding");
    		}
            return mgtApi;
        }
        if (IToIpApi.class.getName().equals(intent.getAction())) {
    		if (logger.isActivated()) {
    			logger.debug("ToIP API binding");
    		}
            return toipApi;
        }
        return null;
    }

	/**
	 * Create directory
	 * 
	 * @param path Direcory path
	 */
	private void createDirectory(String path) {
		File dir = new File(path); 
		if (!dir.exists()) {
			dir.mkdirs(); 
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
    	Intent intent = new Intent(ManagementApiIntents.RCS_SETTINGS);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(RcsCoreService.CONTEXT, 0, intent, 0);
		int iconId; 
		if (state) {
			iconId  = R.drawable.rcs_core_notif_on_icon;
		} else {
			iconId  = R.drawable.rcs_core_notif_off_icon; 
		}
        Notification notif = new Notification(iconId, "", System.currentTimeMillis());
        notif.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_FOREGROUND_SERVICE;
        notif.setLatestEventInfo(RcsCoreService.CONTEXT, RcsCoreService.CONTEXT.getString(R.string.rcs_core_rcs_notification_title),
        		label, contentIntent);
        
        // Send notification
		NotificationManager notificationManager = (NotificationManager)RcsCoreService.CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(RCS_SERVICE_NOTIFICATION, notif);
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
		addRcsServiceNotification(false, getString(R.string.rcs_core_label_rcs_started));
    }

    /**
     * Core layer has been terminated
     */
    public void handleCoreLayerStopped() {
		if (logger.isActivated()) {
			logger.debug("Handle event core terminated");
		}

		// Display a notification
		addRcsServiceNotification(false, getString(R.string.rcs_core_label_rcs_stopped));
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
		Intent intent = new Intent(ManagementApiIntents.SERVICE_REGISTRATION);
		intent.putExtra("status", true);
		getApplicationContext().sendBroadcast(intent);
		
		// Display a notification
		addRcsServiceNotification(true, getString(R.string.rcs_core_label_connected_to_rcs));
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
		Intent intent = new Intent(ManagementApiIntents.SERVICE_REGISTRATION);
		intent.putExtra("status", false);
		getApplicationContext().sendBroadcast(intent);

		// Display a notification
		addRcsServiceNotification(false, getString(R.string.rcs_core_label_rcs_connection_failed));
	}

	/**
	 * Handle "registration terminated" event
	 */
	public void handleRegistrationTerminated() {
		if (logger.isActivated()) {
			logger.debug("Handle event registration terminated");
		}

		// Send registration intent
		Intent intent = new Intent(ManagementApiIntents.SERVICE_REGISTRATION);
		intent.putExtra("status", false);
		getApplicationContext().sendBroadcast(intent);

		// Display a notification
		addRcsServiceNotification(false, getString(R.string.rcs_core_label_disconnected_from_rcs));
	}
	
    /**
     * Handle "publish successful" event
     */
    public void handlePublishPresenceSuccessful() {
		if (logger.isActivated()) {
			logger.debug("Handle event publish ok");
		}
		
		// Nothing to do
    }

    /**
     * Handle "publish presence failed" event
     * 
     * @param error Presence error 
     */
    public void handlePublishPresenceFailed(PresenceError error) {
		if (logger.isActivated()) {
			logger.debug("Handle event publish failed (error " + error.getErrorCode() + ")");
		}
		
		// Nothing to do
    }

    /**
     * Handle "publish presence terminated" event
     */
    public void handlePublishPresenceTerminated() {
		if (logger.isActivated()) {
			logger.debug("Handle event publish terminated");
		}
		
		// Nothing to do
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
	
				// Broadcast intent
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
			ContactsManager.getInstance().setMyPresenceInfo(currentPresenceInfo);

    		// Broadcast intent
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
			// Get the current presence info for the given contact
    		PresenceInfo currentPresenceInfo = ContactsManager.getInstance().getContactPresenceInfo(contact);
    		if (currentPresenceInfo == null) {
    			currentPresenceInfo = new PresenceInfo();
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
				if (id.equalsIgnoreCase(Capabilities.VIDEO_SHARING_CAPABILITY)) {
					capabilities.setVideoSharingSupport(state);
				} else
				if (id.equalsIgnoreCase(Capabilities.IMAGE_SHARING_CAPABILITY)) {
					capabilities.setImageSharingSupport(state);
				} else
				if (id.equalsIgnoreCase(Capabilities.FILE_SHARING_CAPABILITY)) {
					capabilities.setFileTransferSupport(state);
				} else
				if (id.equalsIgnoreCase(Capabilities.CS_VIDEO_CAPABILITY)) {
					capabilities.setCsVideoSupport(state);
				} else
				if (id.equalsIgnoreCase(Capabilities.IM_SESSION_CAPABILITY)) {
					capabilities.setImSessionSupport(state);
				}
			}
			currentPresenceInfo.setCapabilities(capabilities);

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
			ContactsManager.getInstance().setContactPresenceInfo(contact, currentPresenceInfo);

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
				
				// Broadcast intent
				Intent intent = new Intent(PresenceApiIntents.CONTACT_PHOTO_CHANGED);
		    	intent.putExtra("contact", contact);
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
	    	   		    		
    		// Broadcast intent
	    	Intent intent = new Intent(PresenceApiIntents.CONTACT_INFO_CHANGED);
	    	intent.putExtra("contact", contact);
	    	getApplicationContext().sendBroadcast(intent);
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Internal exception", e);
    		}
		}
    }
    
    /**
     * A new anonymous-fetch notification has been received
     * 
     * @param contact Contact
     * @param capabilities Capabilities
     */
    public void handleAnonymousFetchNotification(String contact, Capabilities capabilities) {
    	if (logger.isActivated()) {
			logger.debug("Handle anonymous fetch notification for " + contact);
		}

		try {
			// Create intent
	    	Intent intent = new Intent(PresenceApiIntents.CONTACT_CAPABILITIES);
	    	intent.putExtra("contact", contact);
	    	intent.putExtra("timestamp", capabilities.getTimestamp());
	    	intent.putExtra(Capabilities.CS_VIDEO_CAPABILITY, capabilities.isCsVideoSupported());
	    	intent.putExtra(Capabilities.IMAGE_SHARING_CAPABILITY, capabilities.isImageSharingSupported());
	    	intent.putExtra(Capabilities.VIDEO_SHARING_CAPABILITY, capabilities.isVideoSharingSupported());
	    	intent.putExtra(Capabilities.FILE_SHARING_CAPABILITY, capabilities.isFileTransferSupported());
	    	intent.putExtra(Capabilities.IM_SESSION_CAPABILITY, capabilities.isImSessionSupported());

			// Broadcast intent
	    	getApplicationContext().sendBroadcast(intent);
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Internal exception", e);
    		}
		}
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
		    		byte[] data = Core.getInstance().getPresenceService().getXdmManager().downloadContactPhoto(url);    		
		    		if (data != null) {
		    			// Update the presence info
		    			// TODO: remove -1 values
		    			PhotoIcon photoIcon = new PhotoIcon(data, -1, -1, etag);
		    			Core.getInstance().getPresenceService().getPresenceInfo().setPhotoIcon(photoIcon);
		    			
						// Update contacts database
		    			ContactsManager.getInstance().setMyPhotoIcon(photoIcon);
						
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
		    		byte[] data = Core.getInstance().getPresenceService().getXdmManager().downloadContactPhoto(url);    		
		    		if (data != null) {
		    			// Create a bitmap from the received photo data
		    			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		    			if (bitmap != null) {		    				
			    			// Update contacts database
		    				ContactsManager.getInstance().setContactPhotoIcon(contact, new PhotoIcon(data, bitmap.getWidth(), bitmap.getHeight(), etag));
	    				
				    		// Broadcast intent
					    	Intent intent = new Intent(PresenceApiIntents.CONTACT_PHOTO_CHANGED);
					    	intent.putExtra("contact", contact);
					    	getApplicationContext().sendBroadcast(intent);
		    			}
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
     * Content sharing capabilities indication 
     * 
     * @param contact Remote contact
     * @param image Image sharing supported
     * @param video Video sharing supported
     * @parem others Other supported services
     */
    public void handleContentSharingCapabilitiesIndication(String contact, boolean image, boolean video, Vector<String> others) {
		if (logger.isActivated()) {
			logger.debug("Handle event rich call capabilities indication for " + contact);
		}

		// Convert vector as string array
		String[] othersSupportedCapabilities = null;
		if (others != null) {
			othersSupportedCapabilities = new String[others.size()];
			others.toArray(othersSupportedCapabilities);
		}
		
		// Notify event listeners
    	Intent intent = new Intent(RichCallApiIntents.SHARING_CAPABILITIES);
    	intent.putExtra("contact", contact);
    	intent.putExtra("image", image);
    	intent.putExtra("video", video);
    	intent.putExtra("others", othersSupportedCapabilities);
    	getApplicationContext().sendBroadcast(intent);		
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
		
        // Notify event listeners
    	Intent intent = new Intent(PresenceApiIntents.PRESENCE_INVITATION);
    	intent.putExtra("contact", contact);
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

		// Set the file transfer session ID: if there is an existing chat session, use the same
		// ID as the chat session
		String ftSessionId = session.getSessionID();
		String chatSessionId = ftSessionId;
		InstantMessageSession chatSession = Core.getInstance().getImService().getImSession(number);
		if (chatSession != null) {
			chatSessionId = chatSession.getSessionID();
		}
		
		// Update rich messaging history
    	RichMessaging.getInstance().addMessage(RichMessagingData.TYPE_FILETRANSFER, chatSessionId, ftSessionId, number, null, RichMessagingData.EVENT_INCOMING, session.getContent().getEncoding(), session.getContent().getName(), session.getContent().getSize(), null, RichMessagingData.STATUS_STARTED);
    	
        // Notify event listeners
    	Intent intent = new Intent(MessagingApiIntents.FILE_TRANSFER_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("sessionId", session.getSessionID());
    	intent.putExtra("size", session.getContent().getSize());
    	getApplicationContext().sendBroadcast(intent);
	}
    
	/**
	 * Handle "receive instant message" event
	 *
	 * @param message Received message
	 */
	public void handleReceiveInstantMessage(InstantMessage message) {
		if (logger.isActivated()) {
			logger.debug("Handle event receive IM");
		}

		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(message.getRemote());
		
        // Notify event listeners
    	Intent intent = new Intent(MessagingApiIntents.INSTANT_MESSAGE);
    	intent.putExtra("contact", number);
    	intent.putExtra("message", message.getTextMessage());
    	intent.putExtra("receivedAt", message.getDate());
    	getApplicationContext().sendBroadcast(intent);
    }

	/**
     * New one-to-one chat session invitation
     * 
     * @param session Chat session
     */
	public void handleOne2OneChatSessionInvitation(TerminatingOne2OneChatSession session) {
		if (logger.isActivated()) {
			logger.debug("Handle event receive 1-1 chat session invitation");
		}

		// Extract number from contact 
		String number = PhoneUtils.extractNumberFromUri(session.getRemoteContact());

		// Update rich messaging history
		RichMessaging.getInstance().addMessage(RichMessagingData.TYPE_CHAT, session.getSessionID(), null, number, session.getSubject(), RichMessagingData.EVENT_INCOMING, "text/plain", null, session.getSubject().length(), null, RichMessagingData.STATUS_STARTED);

    	// Notify event listeners
    	Intent intent = new Intent(MessagingApiIntents.CHAT_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("subject", session.getSubject());
    	intent.putExtra("sessionId", session.getSessionID());
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
		RichMessaging.getInstance().addMessage(RichMessagingData.TYPE_CHAT, session.getSessionID(), null, number, session.getSubject(), RichMessagingData.EVENT_INCOMING, "text/plain", null, session.getSubject().length(), null, RichMessagingData.STATUS_STARTED);

    	// Notify event listeners
    	Intent intent = new Intent(MessagingApiIntents.CHAT_INVITATION);
    	intent.putExtra("contact", number);
    	intent.putExtra("subject", session.getSubject());
    	intent.putExtra("sessionId", session.getSessionID());
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
