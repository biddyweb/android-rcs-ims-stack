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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Vector;

import org.xml.sax.InputSource;

import com.orangelabs.rcs.addressbook.ContactsManager;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.capability.Capabilities;
import com.orangelabs.rcs.core.ims.service.presence.pidf.PidfDocument;
import com.orangelabs.rcs.core.ims.service.presence.pidf.PidfParser;
import com.orangelabs.rcs.core.ims.service.presence.pidf.Tuple;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Anonymous fetch manager
 * 
 * @author jexa7410
 */
public class AnonymousFetchManager {
	   /**
     * IMS module
     */
    private ImsModule imsModule;
    
	/***
	 * Refresh timeout (in seconds)
	 */
	private int refreshTimeout;
	
	/**
     * The log4j logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param parent IMS module
     */
    public AnonymousFetchManager(ImsModule parent) {
        this.imsModule = parent;
        this.refreshTimeout = RcsSettings.getInstance().getAnonymousFetchRefrehTimeout();
        
    	if (logger.isActivated()) {
    		logger.info("Anonymous fetch manager created, refresh timeout=" + refreshTimeout + "s");
    	}
    }

    /**
     * Terminate manager
     */
    public void terminate() {
    	if (logger.isActivated()) {
    		logger.info("Terminate the anonymous fetch manager");
    	}
    }
    
	/**
	 * Request capabilities for a given contact
	 * 
	 * @param contact Contact
	 * @return Capabilities
	 */
	public synchronized Capabilities requestCapabilities(String contact) {
    	if (logger.isActivated()) {
    		logger.debug("Request capabilities for " + contact);
    	}

		// Read capabilities from the database
		Capabilities capabilities = ContactsManager.getInstance().getContactCapabilities(contact);
		if (capabilities == null) {
	    	if (logger.isActivated()) {
	    		logger.debug("No capabilities exist for " + contact);
	    	}

            // Request an anonymous fetch
			requestAnonymousFetch(contact);
		} else {
	    	if (logger.isActivated()) {
	    		logger.debug("Capabilities exist for " + contact);
	    	}
			long delta = (System.currentTimeMillis()-capabilities.getTimestamp())/1000;
			if ((delta > refreshTimeout) || (delta < 0)) {
		    	if (logger.isActivated()) {
		    		logger.debug("Capabilities have expired for " + contact);
		    	}

		    	// Capabilities are too old: request a new anonymous fetch
				requestAnonymousFetch(contact);
			}
		}
		return capabilities;
	}	
	
	/**
	 * Request capabilities for a list of contacts
	 * 
	 * @param contactList Contact list
	 * @return Capabilities
	 */
	public synchronized void requestCapabilities(ArrayList<String> contactList) {
		if (logger.isActivated()){
			logger.debug("Requesting capabilities for a list of contacts");
		}

        // Request an anonymous fetch
		MultipleAnonymousFetchCapabilities request = new MultipleAnonymousFetchCapabilities(imsModule, contactList, refreshTimeout);
		request.start();
	}	
	
	/**
	 * Request an anonymous fetch in background
	 * 
	 * @param contact Contact
	 */
	private void requestAnonymousFetch(String contact) {
    	if (logger.isActivated()) {
    		logger.debug("Request an anonymous fetch in background for " + contact);
    	}
		AnonymousFetchCapabilities request = new AnonymousFetchCapabilities(imsModule, contact);
		request.start();
	}
	
	/**
     * Receive a notification
     * 
     * @param notify Received notify
     */
    public void receiveNotification(SipRequest notify) {
    	if (logger.isActivated()) {
			logger.debug("Anonymous fetch notification received");
		}
    	
		try {
			// Parse XML part
		    String content = notify.getContent();
		    if (content != null) {
		    	InputSource pidfInput = new InputSource(new ByteArrayInputStream(content.getBytes()));
		    	PidfParser pidfParser = new PidfParser(pidfInput);
		    	PidfDocument presence = pidfParser.getPresence();
		    	if (presence != null) {
			    	if (logger.isActivated()) {
			    		logger.debug("Anonymous fetch notification with PIDF document");
			    	}
			    	Capabilities capabilities =  new Capabilities(); 
		    		String contact = presence.getEntity();
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

		    		// Reset timestamp
		    		capabilities.resetTimestamp();

		    		// Update capabilities in database
		    		ContactsManager.getInstance().setContactCapabilities(contact, capabilities, true);

		    		// Notify listener
		    		imsModule.getCore().getListener().handleAnonymousFetchNotification(
		    				contact, capabilities);
		    	}
		    } else {
		    	if (logger.isActivated()) {
		    		logger.debug("Anonymous fetch notification is empty");
		    	}
		    	String contact = PhoneUtils.extractNumberFromUri(notify.getFromUri());

		    	// Notify content was empty 
		    	Capabilities capabilities = new Capabilities();

		    	// Update capabilities in database
		    	ContactsManager.getInstance().setContactCapabilities(contact, capabilities, true);

		    	// Notify listener
		    	imsModule.getCore().getListener().handleAnonymousFetchNotification(contact, capabilities);
		    }
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Can't parse XML notification", e);
    		}
    	}    	
    }	
}
