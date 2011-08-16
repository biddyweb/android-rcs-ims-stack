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

package com.orangelabs.rcs.core.ims.service.sip;

import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.ImsServiceSession;

/**
 * Generic SIP session 
 * 
 * @author jexa7410
 */
public abstract class GenericSipSession extends ImsServiceSession {
	/**
	 * Feature tag
	 */
	private String featureTag;	
	
	/**
	 * SDP offer
	 */
	private String sdpOffer = null;

	/**
	 * SDP answer
	 */
	private String sdpAnswer = null;
	
    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param contact Remote contact
	 * @param featureTag Feature tag
	 */
	public GenericSipSession(ImsService parent, String contact, String featureTag) {
		super(parent, contact);
		
		this.featureTag = featureTag;
	}
	
	/**
	 * Returns the event listener
	 * 
	 * @return Listener
	 */
	public SipSessionListener getListener() {
		return (SipSessionListener)super.getListener();
	}	
	
	/**
	 * Returns feature tag of the service
	 * 
	 * @return Feature tag
	 */
	public String getFeatureTag() {
		return featureTag;
	}

	/**
	 * Get SDP offer
	 * 
	 * @return SDP offer
	 */
	public String getSdpOffer() {
		return sdpOffer;
	}

	/**
	 * Set SDP offer
	 * 
	 * @param offer SDP offer
	 */
	public void setSdpOffer(String offer) {
		this.sdpOffer = offer;
	}

	/**
	 * Get SDP answer
	 * 
	 * @return SDP answer
	 */
	public String getSdpAnswer() {
		return sdpAnswer;
	}

	/**
	 * Set SDP answer
	 * 
	 * @param answer SDP answer
	 */
	public void setSdpAnswer(String answer) {
		this.sdpAnswer = answer;
	}
}
