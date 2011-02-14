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

import gov.nist.core.NameValueList;
import gov.nist.core.sip.header.ContactHeader;
import gov.nist.core.sip.header.ContentTypeHeader;
import gov.nist.core.sip.header.ExpiresHeader;
import gov.nist.core.sip.header.Header;
import gov.nist.core.sip.header.ViaHeader;
import gov.nist.core.sip.header.ViaList;
import gov.nist.core.sip.message.Message;

import java.util.Enumeration;
import java.util.Vector;


/**
 * SIP message
 * 
 * @author jexa7410
 */
public abstract class SipMessage {
	
	/**
	 * SIP stack object
	 */
	private Message message;

	/**
	 * Constructor
	 *
	 * @param message SIP stack message
	 */
	public SipMessage(Message message) {
		this.message = message;
	}

	/**
	 * Return the message object from stack API
	 * 
	 * @return Message object
	 */
	public Message getStackMessage() {
		return message;
	}

	/**
	 * Return a header value
	 * 
	 * @param name Header name
	 * @return Header value
	 */
	public String getHeader(String name) {
		Header header = message.getHeader(name);
		if (header != null) {
			return header.getHeaderValue();
		} else {
			return null;
		}			
	}

	/**
	 * Return values of an header
	 * 
	 * @param name Header name
	 * @return List of header value
	 */
	public Vector<String> getHeaders(String name) {
		Enumeration<Header> headers = message.getHeaders(name);
		if (headers != null) {
			Vector<String> result = new Vector<String>();
			for(Enumeration<Header> e = headers; e.hasMoreElements(); ) {
				Header h = e.nextElement();
				result.addElement(h.getHeaderValue());
			}
			return result;
		} else {
			return null;
		}			
	}

	/**
	 * Return a header parameter value
	 * 
	 * @param name Header name
	 * @param param Parameter name
	 * @return Parameter value
	 */
	public String getHeaderParameter(String name, String param) {
		String value = null;
		Header header = message.getHeader(name);
		if (header != null) {
			NameValueList list = header.getParameters();
			value = list.getParameter(param);
		}
		return value;		
	}
	
	/**
	 * Return the From
	 * 
	 * @return String
	 */
	public String getFrom() {
		return message.getFromHeader().getHeaderValue();
	}

	/**
	 * Return the From uri
	 * 
	 * @return String
	 */
	public String getFromUri() {
		return message.getFromHeader().getAddress().getURI().toString();
	}

	/**
	 * Return the To
	 * 
	 * @return String
	 */
	public String getTo() {
		return message.getToHeader().getHeaderValue();
	}

	/**
	 * Return the To uri
	 * 
	 * @return String
	 */
	public String getToUri() {
		return message.getToHeader().getAddress().getURI().toString();
	}
	
	/**
	 * Return the CSeq value
	 * 
	 * @return String
	 */
	public int getCSeq() {
		return message.getCSeqHeaderNumber();
	}
	
	/**
	 * Return the contact URI
	 * 
	 * @return String
	 */
	public String getContactURI() {
        ContactHeader contact = (ContactHeader)message.getHeader(ContactHeader.NAME);
        if (contact != null) {
        	return contact.getAddress().getURI().toString();
        } else {
        	return null;
        }
	}
	
	/**
	 * Return the contact value
	 * 
	 * @return String
	 */
	public String getContact() {
        ContactHeader contact = (ContactHeader)message.getHeader(ContactHeader.NAME);
        if (contact != null) {
        	return contact.getHeaderValue().toString();
        } else {
        	return null;
        }
	}
		
	/**
	 * Return the content part
	 * 
	 * @return String
	 */
	public String getContent() {
		byte[] content = message.getRawContent();
		if (content != null) {
			return new String(content);
		} else {
			return null;
		}
	}
	
	/**
	 * Return the content type
	 * 
	 * @return String
	 */
	public String getContentType() {
		ContentTypeHeader header = message.getContentTypeHeaderHeader();
		if (header != null) {
			return header.getHeaderValue();
		} else {
			return null;
		}
	}

	/**
	 * Returns the call-ID value
	 * 
	 * @return Call-ID
	 */
	public String getCallId() {
		return message.getCallIdentifier();
	}

	/**
	 * Get Via headers list
	 * 
	 * @return List
	 */
	public Vector<Header> getViaHeaders() {
		return message.getViaHeaders().getHeaders();
	}
	
	/**
	 * Determine if the message is a SIP response
	 * 
	 * @return Returns True if it's a SIP response else returns False
	 */
	public abstract boolean isSipResponse();
	
	/**
	 * Add a SIP header
	 * 
	 * @param value Header value
	 */	
	public void addHeader(String value) {
		message.addHeader(value);
	}
	
	/**
	 * Get branch Id of the Via header
	 * 
	 * @return Branch ID
	 */
	public String getBranchId() {
		ViaList list = this.getStackMessage().getViaHeaders();
		ViaHeader first = (ViaHeader)list.elementAt(0);
		return first.getBranch();
	}

	/**
	 * Return the expires value
	 * 
	 * @return Expire value
	 */
	public int getExpires() {
		int expirePeriod = -1;
		
        // Extract expire value from header "Expires"
        ExpiresHeader expires = (ExpiresHeader)message.getHeader(ExpiresHeader.NAME);
    	if (expires != null) {
        	// Expire time in header Expires
            return expires.getExpires();            
        }

		return expirePeriod;
	}
}
