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
package com.orangelabs.rcs.core.ims.service.presence;

import java.util.ArrayList;
import java.util.List;

import javax.sip.header.EventHeader;

import com.orangelabs.rcs.addressbook.ContactsManager;
import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.capability.Capabilities;
import com.orangelabs.rcs.core.ims.service.presence.xdm.HttpResponse;
import com.orangelabs.rcs.core.ims.service.presence.xdm.XdmManager;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.DateUtils;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.StringUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Presence service
 * 
 * @author jexa7410
 */
public class PresenceService extends ImsService {
	/**
	 * Permanent state feature
	 */
	public boolean permanentState;

	/**
	 * Max photo-icon size (in bytes)
	 */
	public int maxPhotoIconSize;

	/**
     * Presence info
     */
    private PresenceInfo presenceInfo = new PresenceInfo();

	/**
	 * Publish manager
	 */
	private PublishManager publisher;
	
	/**
	 * XDM manager
	 */
	private XdmManager xdm;
	
	/**
	 * Watcher info subscribe manager
	 */
	private SubscribeManager watcherInfoSubscriber;

	/**
	 * Presence subscribe manager
	 */
	private SubscribeManager presenceSubscriber;

	/**
	 * Anonymous fetch manager
	 */
	private AnonymousFetchManager anonymousFetchManager;

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
	public PresenceService(ImsModule parent, boolean activated) throws CoreException {
		super(parent, "presence_service.xml", activated);
		
		// Set presence service options
		this.permanentState = RcsSettings.getInstance().isPermanentStateModeActivated();
		this.maxPhotoIconSize = RcsSettings.getInstance().getMaxPhotoIconSize() * 1024;
		
		// Restore the last presence info from the contacts database
		presenceInfo = ContactsManager.getInstance().getMyPresenceInfo();
		if (logger.isActivated()) {
			logger.debug("Last presence info:\n" + presenceInfo.toString());
		}

		// Instanciate the XDM manager
    	xdm = new XdmManager(parent);
    	
    	// Instanciate the publish manager
        publisher = new PublishManager(parent);
    	
    	// Instanciate the subscribe manager for watcher info
    	watcherInfoSubscriber = new WatcherInfoSubscribeManager(parent);
    	
    	// Instanciate the subscribe manager for presence
    	presenceSubscriber = new PresenceSubscribeManager(parent);  
    	
    	// Instanciate the anonymous fetch manager
    	anonymousFetchManager = new AnonymousFetchManager(parent);
	}

	/**
	 * Start the IMS service
	 */
	public void start() {	
		// Initialize the XDM interface
		xdm.initialize();
		
    	// Add me in the granted list if necessary
		List<String> grantedContacts = xdm.getGrantedContacts();
		String me = ImsModule.IMS_USER_PROFILE.getPublicUri();
		if (!grantedContacts.contains(me)) {
        	if (logger.isActivated()) {
        		logger.debug("The enduser is not in the granted list: add it now");
        	}	    				
			xdm.addContactToGrantedList(me);
		}
		
		// It may be necessary to initiate the address book first launch or account check procedure
		if (getImsModule().getCore().getAccountManager().isFirstLaunch() 
				|| getImsModule().getCore().getAccountManager().hasChangedAccount()){
			List<String> blockedContacts = xdm.getBlockedContacts();
			getImsModule().getCore().getAddressBookManager().firstLaunchOrAccountChangedCheck(grantedContacts, blockedContacts);
		}
		
		// Subscribe to watcher-info events 
    	if (watcherInfoSubscriber.subscribe()) {
        	if (logger.isActivated()) {
        		logger.debug("Subscribe manager is started with success for watcher-info");
        	}	    				
		} else {
        	if (logger.isActivated()) {
        		logger.debug("Subscribe manager can't be started for watcher-info");
        	}
		}
		
		// Subscribe to presence events
    	if (presenceSubscriber.subscribe()) {
        	if (logger.isActivated()) {
        		logger.debug("Subscribe manager is started with success for presence");
        	}	    				
		} else {
        	if (logger.isActivated()) {
        		logger.debug("Subscribe manager can't be started for presence");
        	}
		}

    	// Publish initial presence info
    	String xml;
    	if (permanentState) {
    		xml = buildPartialPresenceInfoDocument(presenceInfo);
    	} else {
    		xml = buildPresenceInfoDocument(presenceInfo);
    	}
    	if (publisher.publish(xml)) {
        	if (logger.isActivated()) {
        		logger.debug("Publish manager is started with success");
        	}
		} else {
        	if (logger.isActivated()) {
        		logger.debug("Publish manager can't be started");
        	}
		}	
	}

	/**
	 * Stop the IMS service 
	 */
	public void stop() {
    	if (!permanentState) {
	    	// If not permanent state mode: publish a last presence info before to quit 
			if ((getImsModule().getCurrentNetworkInterface() != null) &&
					getImsModule().getCurrentNetworkInterface().isRegistered() &&
				publisher.isPublished()) {
				String xml = buildPresenceInfoDocument(presenceInfo);
		    	publisher.publish(xml);
			}
    	}
    	
    	// Stop publish
    	publisher.terminate();
    	
    	// Stop subscriptions
    	watcherInfoSubscriber.terminate();
    	presenceSubscriber.terminate();
    	anonymousFetchManager.terminate();
	}
	
	/**
	 * Check the IMS service 
	 */
	public void check() {
    	if (logger.isActivated()) {
    		logger.debug("Check presence service");
    	}

		// Check subscribe manager status for watcher-info events 
		if (!watcherInfoSubscriber.isSubscribed()) {
        	if (logger.isActivated()) {
        		logger.debug("Subscribe manager not yet started for watcher-info");
        	}

        	if (watcherInfoSubscriber.subscribe()) {
	        	if (logger.isActivated()) {
	        		logger.debug("Subscribe manager is started with success for watcher-info");
	        	}	    				
			} else {
	        	if (logger.isActivated()) {
	        		logger.debug("Subscribe manager can't be started for watcher-info");
	        	}
			}
		}
		
		// Check subscribe manager status for presence events
		if (!presenceSubscriber.isSubscribed()) {
        	if (logger.isActivated()) {
        		logger.debug("Subscribe manager not yet started for presence");
        	}

        	if (presenceSubscriber.subscribe()) {
	        	if (logger.isActivated()) {
	        		logger.debug("Subscribe manager is started with success for presence");
	        	}	    				
			} else {
	        	if (logger.isActivated()) {
	        		logger.debug("Subscribe manager can't be started for presence");
	        	}
			}
		}
	}
		
	/**
	 * Is permanent state procedure
	 * 
	 * @return Boolean
	 */
	public boolean isPermanentState() {
		return permanentState;
	}
	
	/**
     * Set the presence info
     * 
     * @param info Presence info
     */
	public void setPresenceInfo(PresenceInfo info) {
		presenceInfo = info;
	}
	
	/**
     * Returns the presence info
     * 
     * @return Presence info
     */
	public PresenceInfo getPresenceInfo() {
		return presenceInfo;
	}
	
	/**
     * Returns the publish manager
     * 
     * @return Publish manager
     */
    public PublishManager getPublishManager() {
        return publisher;
    }
    
	/**
     * Returns the watcher-info subscribe manager
     * 
     * @return Subscribe manager
     */
	public SubscribeManager getWatcherInfoSubscriber() {
		return watcherInfoSubscriber;
	}

	/**
     * Returns the presence subscribe manager
     * 
     * @return Subscribe manager
     */
	public SubscribeManager getPresenceSubscriber() {
		return presenceSubscriber;
	}

    /**
     * Returns the XDM manager
     * 
     * @return XDM manager
     */
    public XdmManager getXdmManager() {
        return xdm;
    }
	
	/**
     * Receive a notification
     * 
     * @param notify Received notify
     */
    public void receiveNotification(SipRequest notify) {
    	try {
	    	if (logger.isActivated()) {
	    		logger.info("Receive a new notification");
	    	}
	    	
	    	// Send 200 OK
		    try {
		        SipResponse resp = SipMessageFactory.createResponse(notify, 200);
		        getImsModule().getSipManager().sendSipResponse(resp);
		    } catch(SipException e) {
	        	if (logger.isActivated()) {
	        		logger.error("Can't send 200 OK for NOTIFY: " + e.getMessage());
	        	}
		    }

		    // Check the content type
		    EventHeader eventHeader = (EventHeader)notify.getHeader(EventHeader.NAME);
		    if (eventHeader == null) {
	        	if (logger.isActivated()) {
	        		logger.debug("Unknown notification event type");
	        	}
		    	return;
		    }
		    
		    // Dispatch the notification to the corresponding event package
		    if (eventHeader.getEventType().equalsIgnoreCase("presence.winfo")) {
		    	watcherInfoSubscriber.receiveNotification(notify);
		    } else
		    if (eventHeader.getEventType().equalsIgnoreCase("presence")) {
		    	if (notify.getTo().indexOf("anonymous") == -1) {
			    	presenceSubscriber.receiveNotification(notify);
		    	} else {
		    		anonymousFetchManager.receiveNotification(notify);
		    	}	    	
		    } else {
	        	if (logger.isActivated()) {
	        		logger.debug("Unsupported notification content type");
	        	}
		    }
    	} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Unexpected error on NOTIFY", e);
        	}
    	}
    }

	/**
	 * Build boolean status value
	 * 
	 * @param state Boolean state
	 * @return String
	 */
	private String buildBooleanStatus(boolean state) {
		if (state) {
			return "open";
		} else {
			return "closed";
		}
	}

    /**
     * Build capabilities document
     * 
     * @param timestamp Timestamp
     * @param capabilities Capabilities
     * @return Document
     */
    private String buildCapabilities(String timestamp, Capabilities capabilities) {
    	return
    	    "<tuple id=\"t1\">" + SipUtils.CRLF +
		    "  <status><basic>" + buildBooleanStatus(capabilities.isFileTransferSupported()) + "</basic></status>" + SipUtils.CRLF +
			"  <op:service-description>" + SipUtils.CRLF +
			"    <op:service-id>" + Capabilities.FILE_SHARING_CAPABILITY + "</op:service-id>" + SipUtils.CRLF +
			"    <op:version>1.0</op:version>" + SipUtils.CRLF +
			"  </op:service-description>" + SipUtils.CRLF +
			"  <contact>" + ImsModule.IMS_USER_PROFILE.getPublicUri() + "</contact>" + SipUtils.CRLF +
			"  <timestamp>" + timestamp + "</timestamp>" + SipUtils.CRLF +
			"</tuple>" + SipUtils.CRLF +
	    	"<tuple id=\"t2\">" + SipUtils.CRLF +
		    "  <status><basic>" + buildBooleanStatus(capabilities.isImageSharingSupported()) + "</basic></status>" + SipUtils.CRLF +
			"  <op:service-description>" + SipUtils.CRLF +
			"    <op:service-id>" + Capabilities.IMAGE_SHARING_CAPABILITY + "</op:service-id>" + SipUtils.CRLF +
			"    <op:version>1.0</op:version>" + SipUtils.CRLF +
			"  </op:service-description>" + SipUtils.CRLF +
			"  <contact>" + ImsModule.IMS_USER_PROFILE.getPublicUri() + "</contact>" + SipUtils.CRLF +
			"  <timestamp>" + timestamp + "</timestamp>" + SipUtils.CRLF +
			"</tuple>" + SipUtils.CRLF +
			"<tuple id=\"t3\">" + SipUtils.CRLF +
		    "  <status><basic>" + buildBooleanStatus(capabilities.isVideoSharingSupported()) + "</basic></status>" + SipUtils.CRLF +
			"  <op:service-description>" + SipUtils.CRLF +
			"    <op:service-id>" + Capabilities.VIDEO_SHARING_CAPABILITY + "</op:service-id>" + SipUtils.CRLF +
			"    <op:version>1.0</op:version>" + SipUtils.CRLF +
			"  </op:service-description>" + SipUtils.CRLF +
			"  <contact>" + ImsModule.IMS_USER_PROFILE.getPublicUri() + "</contact>" + SipUtils.CRLF +
			"  <timestamp>" + timestamp + "</timestamp>" + SipUtils.CRLF +
			"</tuple>" + SipUtils.CRLF +
			"<tuple id=\"t4\">" + SipUtils.CRLF +
		    "  <status><basic>" + buildBooleanStatus(capabilities.isImSessionSupported()) + "</basic></status>" + SipUtils.CRLF +
			"  <op:service-description>" + SipUtils.CRLF +
			"    <op:service-id>" + Capabilities.IM_SESSION_CAPABILITY + "</op:service-id>" + SipUtils.CRLF +
			"    <op:version>1.0</op:version>" + SipUtils.CRLF +
			"  </op:service-description>" + SipUtils.CRLF +
			"  <contact>" + ImsModule.IMS_USER_PROFILE.getPublicUri() + "</contact>" + SipUtils.CRLF +
			"  <timestamp>" + timestamp + "</timestamp>" + SipUtils.CRLF +
			"</tuple>" + SipUtils.CRLF +
			"<tuple id=\"t5\">" + SipUtils.CRLF +
		    "  <status><basic>" + buildBooleanStatus(capabilities.isCsVideoSupported()) + "</basic></status>" + SipUtils.CRLF +
			"  <op:service-description>" + SipUtils.CRLF +
			"    <op:service-id>" + Capabilities.CS_VIDEO_CAPABILITY + "</op:service-id>" + SipUtils.CRLF +
			"    <op:version>1.0</op:version>" + SipUtils.CRLF +
			"  </op:service-description>" + SipUtils.CRLF +
			"  <contact>tel:" + ImsModule.IMS_USER_PROFILE.getUsername() + "</contact>" + SipUtils.CRLF +
			"  <timestamp>" + timestamp + "</timestamp>" + SipUtils.CRLF +
			"</tuple>" + SipUtils.CRLF;
    }
      
    /**
     * Build geoloc document
     * 
     * @param timestamp Timestamp
     * @param geolocInfo Geoloc info
     * @return Document
     */
    private String buildGeoloc(String timestamp, Geoloc geolocInfo) {
    	String document = "";
    	if (geolocInfo != null) {
    		document +=
    			 "<tuple id=\"g1\">" + SipUtils.CRLF +
			     "  <status><basic>open</basic></status>" + SipUtils.CRLF +
			     "   <gp:geopriv>" + SipUtils.CRLF +
			     "    <gp:location-info><gml:location>" + SipUtils.CRLF +
			     "        <gml:Point srsDimension=\"3\"><gml:pos>" + geolocInfo.getLatitude() + " " +
			     				geolocInfo.getLongitude() + " " +
			     				geolocInfo.getLongitude() + "</gml:pos>" + SipUtils.CRLF +
			     "        </gml:Point></gml:location>" + SipUtils.CRLF +
			     "    </gp:location-info>" + SipUtils.CRLF +
			     "    <gp:method>GPS</gp:method>" + SipUtils.CRLF +
			     "   </gp:geopriv>"+SipUtils.CRLF +
				 "  <contact>tel:" + ImsModule.IMS_USER_PROFILE.getUsername() + "</contact>" + SipUtils.CRLF +
				 "  <timestamp>" + timestamp + "</timestamp>" + SipUtils.CRLF +
			     "</tuple>" + SipUtils.CRLF;
    	}
    	return document;
    }    
    
    /**
     * Build person info document
     *
     * @param info Presence info
     * @return Document
     */
    private String buildPersonInfo(PresenceInfo info) {
    	String document = "  <op:overriding-willingness>" + SipUtils.CRLF + 
			    "    <op:basic>" + info.getPresenceStatus() + "</op:basic>" + SipUtils.CRLF +
			    "  </op:overriding-willingness>" + SipUtils.CRLF;
    		
    	FavoriteLink favoriteLink = info.getFavoriteLink();
    	if ((favoriteLink != null) && (favoriteLink.getLink() != null)) {
    		document += "  <ci:homepage>" + StringUtils.encodeUTF8(favoriteLink.getLink()) + "</ci:homepage>" + SipUtils.CRLF;
    	}
    	
    	PhotoIcon photoIcon = info.getPhotoIcon();
    	if ((photoIcon != null) && (photoIcon.getEtag() != null)) {
    		document +=
    			"  <rpid:status-icon opd:etag=\"" + photoIcon.getEtag() +
    			"\" opd:fsize=\"" + photoIcon.getSize() +
    			"\" opd:contenttype=\"" + photoIcon.getType() +
    			"\" opd:resolution=\"" + photoIcon.getResolution() + "\">" + xdm.getEndUserPhotoIconUrl() +
    			"</rpid:status-icon>" + SipUtils.CRLF;
    	}
    	
    	String freetext = info.getFreetext();    	
    	if (freetext != null){
    		document += "  <pdm:note>" + StringUtils.encodeXML(freetext) + "</pdm:note>" + SipUtils.CRLF;
    	}
    	
    	return document;
    }
    
    /**
     * Build presence info document (RCS 1.0)
     * 
     * @param info Presence info
     * @return Document
     */
    private String buildPresenceInfoDocument(PresenceInfo info) {    	
    	String document= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + SipUtils.CRLF +
    		"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" +
    		" xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\"" +
    		" xmlns:opd=\"urn:oma:xml:pde:pidf:ext\"" +
    		" xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\"" +
    		" xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\"" + 
    		" xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\"" + 
    		" xmlns:gp=\"urn:ietf:params:xml:ns:pidf:geopriv10\"" + 
			" xmlns:gml=\"urn:opengis:specification:gml:schema-xsd:feature:v3.0\"" + 
    		" entity=\""+ ImsModule.IMS_USER_PROFILE.getPublicUri() + "\">" + SipUtils.CRLF;
    	
    	// Encode timestamp
    	String timestamp = DateUtils.encodeDate(info.getTimestamp());
    	
    	// Build capabilities
    	document += buildCapabilities(timestamp, info.getCapabilities());
    	
		// Build geoloc
    	document += buildGeoloc(timestamp, info.getGeoloc());

    	// Build person info
    	document += "<pdm:person id=\"p1\">" + SipUtils.CRLF +
					buildPersonInfo(info) +
    				"  <pdm:timestamp>" + timestamp + "</pdm:timestamp>" + SipUtils.CRLF +
				    "</pdm:person>" + SipUtils.CRLF;
    	
    	// Add last header
    	document += "</presence>" + SipUtils.CRLF;
    	
    	return document;    	
    }

    /**
     * Build partial presence info document (all presence info except permanent state info)
     * 
     * @param info Presence info
     * @return Document
     */
    private String buildPartialPresenceInfoDocument(PresenceInfo info) {    	
    	String document= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + SipUtils.CRLF +
    		"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" +
    		" xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\"" +
    		" xmlns:opd=\"urn:oma:xml:pde:pidf:ext\"" +
    		" xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\"" +
    		" xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\"" + 
    		" xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\"" + 
    		" xmlns:gp=\"urn:ietf:params:xml:ns:pidf:geopriv10\"" + 
			" xmlns:gml=\"urn:opengis:specification:gml:schema-xsd:feature:v3.0\"" + 
    		" entity=\""+ ImsModule.IMS_USER_PROFILE.getPublicUri() + "\">" + SipUtils.CRLF;
    	
    	// Encode timestamp
    	String timestamp = DateUtils.encodeDate(info.getTimestamp());
    	
    	// Build capabilities
    	document += buildCapabilities(timestamp, info.getCapabilities());
    	
		// Build geoloc
    	document += buildGeoloc(timestamp, info.getGeoloc());

    	// Add last header
    	document += "</presence>" + SipUtils.CRLF;
    	
    	return document;    	
    }

    /**
     * Build permanent presence info document (RCS R2.0)
     * 
     * @param info Presence info
     * @return Document
     */
    private String buildPermanentPresenceInfoDocument(PresenceInfo info) {    	
    	String document= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + SipUtils.CRLF +
    		"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" +
    		" xmlns:op=\"urn:oma:xml:prs:pidf:oma-pres\"" +
    		" xmlns:opd=\"urn:oma:xml:pde:pidf:ext\"" +
			" xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\"" +
			" xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\"" + 
			" xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\"" + 
    		" entity=\""+ ImsModule.IMS_USER_PROFILE.getPublicUri() + "\">" + SipUtils.CRLF;
    	
    	// Encode timestamp
    	String timestamp = DateUtils.encodeDate(info.getTimestamp());
    	
    	// Build person info (freetext, favorite link and photo-icon)
    	document += "<pdm:person id=\"p1\">" + SipUtils.CRLF +
					buildPersonInfo(info) +
    				"  <pdm:timestamp>" + timestamp + "</pdm:timestamp>" + SipUtils.CRLF +
				    "</pdm:person>" + SipUtils.CRLF;
    	
    	// Add last header
	    document += "</presence>" + SipUtils.CRLF;
    	
    	return document;    	
    }

    /**
     * Update photo-icon
     * 
     * @param photoIcon Photo-icon
     * @return Boolean result
     */
    private boolean updatePhotoIcon(PhotoIcon photoIcon) {
    	boolean result = false;
    	
    	// Photo-icon management
    	PhotoIcon currentPhoto = presenceInfo.getPhotoIcon();
    	if ((photoIcon != null) && (photoIcon.getEtag() == null)) {
    		// Test photo icon size
        	if (photoIcon.getSize() > maxPhotoIconSize) {
    			if (logger.isActivated()) {
    				logger.debug("Max photo size achieved");
    			}
    			return false;
        	}            	
    		
    		// Upload the new photo-icon
    		if (logger.isActivated()) {
    			logger.info("Upload the photo-icon");
    		}
    		result = uploadPhotoIcon(photoIcon);
    	} else
    	if ((photoIcon == null) && (currentPhoto != null)) {
    		// Delete the current photo-icon
    		if (logger.isActivated()) {
    			logger.info("Delete the photo-icon");
    		}
    		result = deletePhotoIcon();
    	} else {
    		// Nothing to do
    		result = true;
    	}
    	
    	return result;
    }
        
    /**
     * Publish presence info
     * 
     * @param info Presence info
	 * @returns Returns true if the presence info has been publish with success, else returns false
     */
    public boolean publishPresenceInfo(PresenceInfo info) {
    	boolean result = false;

    	// Photo-icon management
    	result = updatePhotoIcon(info.getPhotoIcon());
    	if (!result) {
    		// Can't update the photo-icon in the XDM server
    		return result;
    	}
    	
    	// Reset timestamp
    	info.resetTimestamp();
    	
		// Publish presence info
    	if (permanentState) {
    		// Permanent state procedure: publish the new presence info via XCAP
    		if (logger.isActivated()) {
    			logger.info("Publish presence info via XDM request (permanent state)");
    		}
    		String xml = buildPermanentPresenceInfoDocument(info);
    		HttpResponse response = xdm.setPresenceInfo(xml);
    		if ((response != null) && response.isSuccessfullResponse()) { 
    			result = true;
    		} else {
    			result = false;
    		}
    	} else {
    		// SIP procedure: publish the new presence info via SIP
    		if (logger.isActivated()) {
    			logger.info("Publish presence info via SIP request");
    		}
    		String xml = buildPresenceInfoDocument(info);
			result = publisher.publish(xml);
    	}

		// If server updated with success then update presence info cache
    	if (result) {
    		presenceInfo = info;
		}
    	
    	return result;
    }
    
    /**
	 * Upload photo icon
	 * 
	 * @param photo Photo icon
	 * @returns Boolean result 
	 */
	public boolean uploadPhotoIcon(PhotoIcon photo) {
		// Upload the photo to the XDM server
		HttpResponse response = xdm.uploadEndUserPhoto(photo);
		if ((response != null) && response.isSuccessfullResponse()) {
			// Extract the Etag value in the 200 OK response
			String etag = response.getHeader("Etag");
			if (etag != null) {
				// Removed separators
				if (etag.startsWith("\"")) {
					etag = etag.substring(1, etag.length()-1);
				}
			} else {
				etag = "" + System.currentTimeMillis();
			}
			
			// Set the Etag of the photo-icon
			photo.setEtag(etag);
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Delete photo icon
	 * 
	 * @returns Boolean result 
	 */
	public boolean deletePhotoIcon(){
		// Delete the photo from the XDM server
		HttpResponse response = xdm.deleteEndUserPhoto();
		if ((response != null) && (response.isSuccessfullResponse() || response.isNotFoundResponse())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
     * Invite a contact to share its presence
     * 
     * @param contact Contact
	 * @returns Returns true if XDM request was successful, else false 
     */
    public boolean inviteContactToSharePresence(String contact) {
		// Remove contact from the blocked contacts list
		String contactUri = PhoneUtils.formatNumberToSipAddress(contact);
		xdm.removeContactFromBlockedList(contactUri);

		// Remove contact from the revoked contacts list
		xdm.removeContactFromRevokedList(contactUri);

		// Add contact in the granted contacts list
		HttpResponse response = xdm.addContactToGrantedList(contactUri);
		if ((response != null) && response.isSuccessfullResponse()) { 
			return true;
		} else {
			return false;
		}
    }
    
    /**
     * Revoke a shared contact
     * 
     * @param contact Contact
	 * @returns Returns true if XDM request was successful, else false 
     */
    public boolean revokeSharedContact(String contact){
		// Add contact in the revoked contacts list
		String contactUri = PhoneUtils.formatNumberToSipAddress(contact);
		HttpResponse response = xdm.addContactToRevokedList(contactUri);
		if ((response == null) || (!response.isSuccessfullResponse())) {
			return false;
		}

		// Remove contact from the granted contacts list
		response = xdm.removeContactFromGrantedList(contactUri);
		if ((response != null) && (response.isSuccessfullResponse() || response.isNotFoundResponse())) { 
			return true;
		} else {
			return false;
		}
    }
    
	/**
	 * Accept a presence sharing invitation
	 * 
	 * @param contact Contact
	 * @returns Returns true if XDM request was successful, else false 
	 */
	public boolean acceptPresenceSharingInvitation(String contact) {
		// Add contact in the granted contacts list
		String contactUri = PhoneUtils.formatNumberToSipAddress(contact);
		HttpResponse response = xdm.addContactToGrantedList(contactUri);
		if ((response != null) && response.isSuccessfullResponse()) { 
			return true;
		} else {
			return false;
		}
	}
    
	/**
	 * Block a presence sharing invitation
	 * 
	 * @param contact Contact
	 * @returns Returns true if XDM request was successful, else false 
	 */
	public boolean blockPresenceSharingInvitation(String contact){
		// Add contact in the blocked contacts list
		String contactUri = PhoneUtils.formatNumberToSipAddress(contact);
		HttpResponse response = xdm.addContactToBlockedList(contactUri);
		if ((response != null) && response.isSuccessfullResponse()) { 
			return true;
		} else {
			return false;
		}
	}
	
    /**
     * Remove a revoked contact
     * 
     * @param contact Contact
	 * @returns Returns true if XDM request was successful, else false 
     */
	public boolean removeRevokedContact(String contact) {
		// Remove contact from the revoked contacts list
		String contactUri = PhoneUtils.formatNumberToSipAddress(contact);
		HttpResponse response = xdm.removeContactFromRevokedList(contactUri);
		if ((response != null) && (response.isSuccessfullResponse() || response.isNotFoundResponse())) { 
			return true;
		} else {
			return false;
		}
	}

    /**
     * Remove a blocked contact
     * 
     * @param contact Contact
	 * @returns Returns true if XDM request was successful, else false 
     */
	public boolean removeBlockedContact(String contact) {
		// Remove contact from the blocked contacts list
		String contactUri = PhoneUtils.formatNumberToSipAddress(contact);
		HttpResponse response = xdm.removeContactFromBlockedList(contactUri);
		if ((response != null) && (response.isSuccessfullResponse() || response.isNotFoundResponse())) { 
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Request capabilities for a given contact (i.e anonymous fetch)
	 * 
	 * @param contact Contact
	 * @return Capabilities
	 */
	public Capabilities requestCapabilities(String contact) {
		return anonymousFetchManager.requestCapabilities(contact);
	}	

	/**
	 * Request capabilities for multiple contacts (i.e anonymous fetch)
	 * 
	 * @param contactList List of contacts
	 */
	public void requestCapabilities(ArrayList<String> contactList) {
		anonymousFetchManager.requestCapabilities(contactList);
	}
}
