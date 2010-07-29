/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0.0
 * 
 * Copyright � 2010 France Telecom S.A.
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
	public static boolean permanentState = false;

	/**
     * Presence info
     */
    private PresenceInfo presenceInfo = new PresenceInfo();

	/**
	 * Publish manager
	 */
	private PublishManager publisher;
	
	/**
	 * Poke manager
	 */
	private PokeManager pokeManager;

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
		PresenceService.permanentState = getConfig().getBoolean("PermanentState");
		
    	// Instanciate the poke manager
        pokeManager = new PokeManager(this, getConfig().getInteger("HyperAvailabilityPeriod"));
        
    	// Instanciate the XDM manager
    	xdm = new XdmManager(parent);
    	
    	// Instanciate the publish manager
        publisher = new PublishManager(parent,
        		getConfig().getInteger("PublishExpirePeriod"),
        		getConfig().getBoolean("PublishGeoloc"));
    	
    	// Instanciate the subscribe manager for watcher info
    	watcherInfoSubscriber = new WatcherInfoSubscribeManager(parent,
    			ImsModule.IMS_USER_PROFILE.getPublicUri(),
    			getConfig().getInteger("SubscribeExpirePeriod"));
    	
    	// Instanciate the subscribe manager for presence
    	presenceSubscriber = new PresenceSubscribeManager(parent,
    			ImsModule.IMS_USER_PROFILE.getPublicUri()+";pres-list=rcs",
    			getConfig().getInteger("SubscribeExpirePeriod"));    	
	}

	/**
	 * Start the IMS service
	 */
	public void start() {	
    	// Initialize XDMS lists if they don't exist
		HttpResponse response = xdm.getRcsList();
		if ((response != null) && response.isNotFoundResponse()) {
	    	// Set RCS list
	    	xdm.setRcsList();
		}

		response = xdm.getResourcesList();
		if ((response != null) && response.isNotFoundResponse()) {
	    	// Set resource list
	    	xdm.setResourcesList(); 
		}	

		response = xdm.getPresenceRules();
		if ((response != null) && response.isNotFoundResponse()) {
	    	// Set presence rules
	    	xdm.setPresenceRules();
    	}

    	// Publish initial presence info
		String xml = buildAllPresenceInfo(presenceInfo);
    	if (publisher.publish(xml)) {
        	if (logger.isActivated()) {
        		logger.debug("Publish manager is started with success");
        	}
		} else {
        	if (logger.isActivated()) {
        		logger.debug("Publish manager can't be started");
        	}
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
	}

	/**
	 * Stop the IMS service 
	 */
	public void stop() {
    	// Publish last presence info before to quit
		String xml = buildAllPresenceInfo(presenceInfo);
    	publisher.publish(xml);
    	
    	// Stop publish
    	publisher.terminate();
    	
    	// Stop subscriptions
    	watcherInfoSubscriber.terminate();
    	presenceSubscriber.terminate();
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
     * Returns the poke manager
     * 
     * @return Poke manager
     */
    public PokeManager getPokeManager() {
    	return pokeManager;
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
    	if (logger.isActivated()) {
    		logger.info("Receive a new notification");
    	}
        
    	// Send 200 OK
	    try {
	        SipResponse resp = SipMessageFactory.createResponse(notify, 200);
	        getImsModule().getSipManager().sendSipMessage(resp);
	    } catch(SipException e) {
        	if (logger.isActivated()) {
        		logger.error("Can't send 200 OK for NOTIFY: " + e.getMessage());
        	}
	    }
    	
	    // Check the content type
	    String event = notify.getHeader("Event");
	    if (event == null) {
        	if (logger.isActivated()) {
        		logger.debug("Unknown notification event type");
        	}
	    	return;
	    }
	    
	    // Dispatch the notification to the corresponding event package
	    if (event.indexOf("presence.winfo") != -1) {
	    	watcherInfoSubscriber.receiveNotification(notify);
	    } else
	    if (event.indexOf("presence") != -1) {
	    	presenceSubscriber.receiveNotification(notify);
	    } else {
        	if (logger.isActivated()) {
        		logger.debug("Unsupported notification content type");
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
     * Build presence document
     * 
     * @param freetext Freetext
     * @param favoriteLink FavoriteLink
     * @param photoIcon Photo-icon
     * @return Document
     */
    private String buildPresence(String freetext, FavoriteLink favoriteLink, PhotoIcon photoIcon) {
    	String document = "";
    	if ((favoriteLink != null) && (favoriteLink.getLink() != null)) {
    		document += "  <ci:homepage>" + favoriteLink.getLink() + "</ci:homepage>" + SipUtils.CRLF;
    	}
    	if ((photoIcon != null) && (photoIcon.getEtag() != null)) {
    		document +=
    			"  <rpid:status-icon opd:etag=\"" + photoIcon.getEtag() +
    			"\" opd:fsize=\"" + photoIcon.getSize() +
    			"\" opd:contenttype=\"" + photoIcon.getType() +
    			"\" opd:resolution=\"" + photoIcon.getResolution() + "\">http://" + xdm.getEndUserPhotoIconUrl() +
    			"  </rpid:status-icon>" + SipUtils.CRLF;
    	}
    	if (freetext != null){
    		document += "  <pdm:note>" + StringUtils.forXML(freetext) + "</pdm:note>" + SipUtils.CRLF;
    	}
    	return document;
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
			     geolocInfo.getLongitude() + " " + geolocInfo.getLongitude() +"</gml:pos>" + SipUtils.CRLF +
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
     * Build hyper-availability document
     * 
     * @param status Status
     * @return Document
     */
    private String buildHyperavailability(boolean status) {
    	String document = "";
    	if (status) {
    		// Hyper-available case
    		long pokeExpireDate = pokeManager.generateNextPokeExpireDate();
    		String until = DateUtils.encodeDate(pokeExpireDate);
    		document += "  <op:overriding-willingness opd:until=\"" + until + "\">" + SipUtils.CRLF + 
 			    	    "    <op:basic>open</op:basic>" + SipUtils.CRLF +
				    	"  </op:overriding-willingness>" + SipUtils.CRLF;
    	} else {
    		// Normal case
    		document += "  <op:overriding-willingness>" + SipUtils.CRLF + 
			    	    "    <op:basic>open</op:basic>" + SipUtils.CRLF +
			    	    "  </op:overriding-willingness>" + SipUtils.CRLF;
    	}
    	return document;
    }
    
    /**
     * Build all presence info document
     * 
     * @param info Presence info
     * @return Document
     */
    private String buildAllPresenceInfo(PresenceInfo info) {    	
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

    	// Build person info (freetext, favorite link, photo-icon and poke status)
    	document += "<pdm:person id=\"p1\">" + SipUtils.CRLF +
					buildHyperavailability(info.isHyperavailable()) +
					buildPresence(info.getFreetext(), info.getFavoriteLink(), info.getPhotoIcon()) +
    				"  <pdm:timestamp>" + timestamp + "</pdm:timestamp>" + SipUtils.CRLF +
				    "</pdm:person>" + SipUtils.CRLF +
				    "</presence>" + SipUtils.CRLF;
    	
    	return document;    	
    }

    /**
     * Build partial presence info document
     * 
     * @param info Presence info
     * @return Document
     */
    private String buildPartialPresenceInfo(PresenceInfo info) {    	
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

    	// Build person info (freetext, favorite link, photo-icon and poke status)
    	document += "<pdm:person id=\"p1\">" + SipUtils.CRLF +
					buildHyperavailability(info.isHyperavailable()) +
    				"  <pdm:timestamp>" + timestamp + "</pdm:timestamp>" + SipUtils.CRLF +
				    "</pdm:person>" + SipUtils.CRLF +
				    "</presence>" + SipUtils.CRLF;
    	
    	return document;    	
    }

    /**
     * Build permanent presence info document
     * 
     * @param info Presence info
     * @return Document
     */
    private String buildPermanentPresenceInfo(PresenceInfo info) {    	
    	String document= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + SipUtils.CRLF +
    		"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" +
    		" xmlns:opd=\"urn:oma:xml:pde:pidf:ext\"" +
			" xmlns:pdm=\"urn:ietf:params:xml:ns:pidf:data-model\"" +
			" xmlns:ci=\"urn:ietf:params:xml:ns:pidf:cipid\"" + 
			" xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\"" + 
    		" entity=\""+ ImsModule.IMS_USER_PROFILE.getPublicUri() + "\">" + SipUtils.CRLF;
    	
    	// Encode timestamp
    	String timestamp = DateUtils.encodeDate(info.getTimestamp());
    	
    	// Build person info (freetext, favorite link and photo-icon)
    	document += "<pdm:person id=\"p1\">" + SipUtils.CRLF +
					buildPresence(info.getFreetext(), info.getFavoriteLink(), info.getPhotoIcon()) +
    				"  <pdm:timestamp>" + timestamp + "</pdm:timestamp>" + SipUtils.CRLF +
				    "</pdm:person>" + SipUtils.CRLF +
				    "</presence>" + SipUtils.CRLF;
    	
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
    		// Nothing todo
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
    	if (PresenceService.permanentState) {
        	// Build permanent presence info
    		String xml = buildPermanentPresenceInfo(info);

    		// Permanent state procedure: publish the new presence info via XCAP
    		if (logger.isActivated()) {
    			logger.info("Publish presence info via XDM request (permanent state)");
    		}
    		HttpResponse response = xdm.setPresenceInfo(xml);
    		if ((response != null) && response.isSuccessfullResponse()) { 
    			result = true;
    		}
    	} else {
        	// Build presence info
    		String xml = buildAllPresenceInfo(info);

    		// SIP procedure: publish the new presence info via SIP
    		if (logger.isActivated()) {
    			logger.info("Publish presence info via SIP request");
    		}
			result = publisher.publish(xml);
    	}

    	if (result) {
			// Update the cache
    		presenceInfo = info;
		}
    	
		return result;
    }
    
    /**
     * Publish poke status
     * 
     * @param status Status
	 * @returns Returns true if poke has been publish with success, else returns false
     */
    public boolean publishPoke(boolean status) {
    	// Reset timestamp
    	presenceInfo.resetTimestamp();

    	// Update presence info
    	boolean currentStatus = presenceInfo.isHyperavailable();
    	presenceInfo.setHyperavailabilityStatus(status);
    	
    	// Build presence info
		String xml;
		if (PresenceService.permanentState) {
			xml = buildPartialPresenceInfo(presenceInfo);
		} else {
			xml = buildAllPresenceInfo(presenceInfo);
		}

		// SIP procedure: publish the new presence info via SIP
    	boolean result = publisher.publish(xml);
		if (result) {
    		// Poke management
			if (presenceInfo.isHyperavailable()) {
	    		// Poke UP
	        	pokeManager.pokeUp();
	    	} else {
	    		// Poke DOWN
	        	pokeManager.pokeDown();
	    	}
		} else {
			// Restore the previous status
    		presenceInfo.setHyperavailabilityStatus(currentStatus);
		}
		return result;
    }    
    
    /**
     * Poke period has expired
     */
    public void pokePeriodHasExpired() {
		// Reset the poke status
    	presenceInfo.setHyperavailabilityStatus(false);
    	
    	// Publish the new poke status
		publishPoke(false);
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
		String contactUri = PhoneUtils.formatNumberToTelUri(contact);
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
		String contactUri = PhoneUtils.formatNumberToTelUri(contact);
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
		String contactUri = PhoneUtils.formatNumberToTelUri(contact);
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
		String contactUri = PhoneUtils.formatNumberToTelUri(contact);
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
		String contactUri = PhoneUtils.formatNumberToTelUri(contact);
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
		String contactUri = PhoneUtils.formatNumberToTelUri(contact);
		HttpResponse response = xdm.removeContactFromBlockedList(contactUri);
		if ((response != null) && (response.isSuccessfullResponse() || response.isNotFoundResponse())) { 
			return true;
		} else {
			return false;
		}
	}
}