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
package com.orangelabs.rcs.core.ims.protocol.sip;

import gov.nist.core.sip.header.ContactHeader;
import gov.nist.core.sip.header.ContactList;
import gov.nist.core.sip.header.ExpiresHeader;
import gov.nist.core.sip.message.Response;



/**
 * SIP response
 * 
 * @author jexa7410
 */
public class SipResponse extends SipMessage {
	
	/**
	 * Constructor
	 *
	 * @param response SIP stack response
	 */
	public SipResponse(Response response) {
		super(response);
	}

	/**
	 * Return the response object from stack API
	 * 
	 * @return Response object
	 */
	private Response getStackResponse() {
		return (Response)getStackMessage();
	}
	
	/**
	 * Return the expires value
	 * 
	 * @param localIpAddr Local IP address
	 * @return Expire value
	 */
	public int getExpires(String localIpAddr) {
		int expirePeriod = -1;
		
        // Extract expire value from headers "Contact"
        ContactList contacts = getStackResponse().getContactHeaders();
        if (contacts != null) {
	        ContactHeader contact = contacts.getMyContact(localIpAddr);
	    	if (contact != null) {
	    		String value = contact.getExpires();
	            if (value != null) {
	            	// Expire time in header Contact
	            	return Integer.parseInt(value);
	            }
	    	}
        }

        // Extract expire value from header "Expires"
        ExpiresHeader expires = (ExpiresHeader)getStackResponse().getHeader(ExpiresHeader.NAME);
    	if (expires != null) {
        	// Expire time in header Expires
            return expires.getExpires();            
        }

		return expirePeriod;
	}
	
	/**
	 * Returns the status code value
	 * 
	 * @return Status code or -1 if it's a request
	 */
	public int getStatusCode() {
		return getStackResponse().getStatusCode();
	}
	
	/**
	 * Returns the reason phrase of the response
	 * 
	 * @return String
	 */
	public String getReasonPhrase() {
		return getStackResponse().getReasonPhrase();
	}
	
	/**
	 * Determine if the message is a SIP response
	 * 
	 * @return Returns True if it's a SIP response else returns False
	 */
	public boolean isSipResponse() {
		return true;
	}
}
