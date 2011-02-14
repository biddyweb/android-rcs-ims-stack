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
import java.util.Vector;

import org.xml.sax.InputSource;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.presence.pidf.PidfDocument;
import com.orangelabs.rcs.core.ims.service.presence.pidf.PidfParser;
import com.orangelabs.rcs.core.ims.service.presence.rlmi.ResourceInstance;
import com.orangelabs.rcs.core.ims.service.presence.rlmi.RlmiDocument;
import com.orangelabs.rcs.core.ims.service.presence.rlmi.RlmiParser;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Subscribe manager for presence event
 * 
 * @author jexa7410
 */
public class PresenceSubscribeManager extends SubscribeManager {
	/**
     * The log4j logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param parent IMS module
     * @param defaultExpirePeriod Default expiration period in seconds
     */
    public PresenceSubscribeManager(ImsModule parent, int defaultExpirePeriod) {
    	super(parent, defaultExpirePeriod);
    }

    /**
     * Returns the presentity
     * 
     * @return Presentity
     */
    public String getPresentity() {
    	return ImsModule.IMS_USER_PROFILE.getPublicUri()+";pres-list=rcs";
    }    
    
    /**
     * Create a SUBSCRIBE request
     * 
	 * @param dialog SIP dialog path
	 * @param expirePeriod Expiration period
	 * @param accessInfo Access info
	 * @return SIP request
	 * @throws SipException
     */
    public SipRequest createSubscribe(SipDialogPath dialog, int expirePeriod, String accessInfo) throws SipException {
    	// Create SUBSCRIBE message
    	SipRequest subscribe = SipMessageFactory.createSubscribe(dialog, expirePeriod, accessInfo);

    	// Set the Event header
    	subscribe.addHeader("Event: presence");

    	// Set the Accept header
    	subscribe.addHeader("Accept: application/pidf+xml,application/rlmi+xml,multipart/related");

    	// Set the Supported header
    	subscribe.addHeader("Supported: eventlist");

    	return subscribe;
    }

    /**
     * Receive a notification
     * 
     * @param notify Received notify
     */
    public void receiveNotification(SipRequest notify) {
    	// Check notification
    	if (!isNotifyForThisSubscriber(notify)) {
    		return;
    	}    	
    	
		if (logger.isActivated()) {
			logger.debug("New presence notification received");
		}    	

		// Parse XML part
	    String content = notify.getContent();
		if (content != null) {
	    	try {
				// Extract parts from multipart
	    		String boundary = "--" + SipUtils.extractBoundary(notify.getContentType());
	    		int index  = 0;
	    		while(index != -1) {
	    			int begin = content.indexOf(boundary, index);
	    			int end = content.indexOf(boundary, begin+boundary.length());
	    			if ((begin != -1) && (end != -1)) {
	    				String part = content.substring(begin+boundary.length(), end);
	    				String contentPart = part.substring(part.indexOf("<?xml"));
	    				
	    				if (part.indexOf("application/rlmi+xml") != -1) {
	    	    			// Parse RLMI part
	    					try {
		    					InputSource rlmiInput = new InputSource(new ByteArrayInputStream(contentPart.getBytes()));
		    					RlmiParser rlmiParser = new RlmiParser(rlmiInput);
		    					RlmiDocument rlmiInfo = rlmiParser.getResourceInfo();
		    					Vector<ResourceInstance> list = rlmiInfo.getResourceList();
		    					for(int i=0; i < list.size(); i++) {
		    						ResourceInstance res = (ResourceInstance)list.elementAt(i);
		    						String contact = res.getUri();
		    						String state = res.getState();
		    						String reason = res.getReason();
		    						
		    						if ((contact != null) && (state != null) && (reason != null)) {
		    							if (state.equalsIgnoreCase("terminated") && 
		    									reason.equalsIgnoreCase("rejected")) {
		    								// It's a "terminated" event with status "rejected" the contact
		    								// should be removed from the "rcs" list
		    								getImsModule().getPresenceService().getXdmManager().removeContactFromGrantedList(contact);
		    							}				
		    							
		    							// Notify listener
		    					    	getImsModule().getCore().getListener().handlePresenceSharingNotification(contact, state, reason);
		    						}
		    					}
	    			    	} catch(Exception e) {
	    			    		if (logger.isActivated()) {
	    			    			logger.error("Can't parse RLMI notification", e);
	    			    		}
	    			    	}
	    				} else
	    				if (part.indexOf("application/pidf+xml") != -1) {
	    					try {
		    	    			// Parse PIDF part
	    						InputSource pidfInput = new InputSource(new ByteArrayInputStream(contentPart.getBytes()));
		    					PidfParser pidfParser = new PidfParser(pidfInput);
		    					PidfDocument presenceInfo = pidfParser.getPresence();
		    					
		    					// Notify listener
		    			    	getImsModule().getCore().getListener().handlePresenceInfoNotification(
		    			    			presenceInfo.getEntity(), presenceInfo);
	    			    	} catch(Exception e) {
	    			    		if (logger.isActivated()) {
	    			    			logger.error("Can't parse PIDF notification", e);
	    			    		}
	    			    	}
	    				}
	    			}
	    			index = end; 
	    		}
	    	} catch(Exception e) {
	    		if (logger.isActivated()) {
	    			logger.error("Can't parse presence notification", e);
	    		}
	    	}
	    	
			// Check subscription state
			String state = notify.getHeader("Subscription-State");
			if ((state != null) && (state.indexOf("terminated") != -1)) {
				if (logger.isActivated()) {
					logger.info("Presence subscription has been terminated by server");
				}
				terminatedByServer();
			}
		}
    }   
}
