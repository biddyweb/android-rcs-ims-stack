/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

import gov2.nist.javax2.sip.header.extensions.SessionExpiresHeader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import javax2.sip.Transaction;
import javax2.sip.header.AcceptHeader;
import javax2.sip.header.CSeqHeader;
import javax2.sip.header.CallIdHeader;
import javax2.sip.header.ContactHeader;
import javax2.sip.header.ContentTypeHeader;
import javax2.sip.header.ExtensionHeader;
import javax2.sip.header.FromHeader;
import javax2.sip.header.Header;
import javax2.sip.header.SubjectHeader;
import javax2.sip.header.ToHeader;
import javax2.sip.header.ViaHeader;
import javax2.sip.message.Message;

import com.orangelabs.rcs.core.ims.network.sip.SipUtils;

/**
 * SIP message
 * 
 * @author jexa7410
 */
public abstract class SipMessage {
	
	/**
	 * SIP stack API object
	 */
	protected Message stackMessage;
	
	/**
	 * SIP stack transaction
	 */
	private Transaction stackTransaction = null;
	
	/**
	 * Constructor
	 *
	 * @param message SIP stack message
	 */
	public SipMessage(Message message) {
		this.stackMessage = message;
	}
	
	/**
	 * Return the SIP stack message
	 * 
	 * @return SIP message
	 */
	public abstract Message getStackMessage();

	/**
	 * Return the SIP stack transaction
	 * 
	 * @return SIP transaction
	 */
	public Transaction getStackTransaction() {
		return stackTransaction;
	}
	
	/**
	 * Set the SIP stack transaction
	 * 
	 * @param transaction SIP transaction
	 */
	public void setStackTransaction(Transaction transaction) {
		stackTransaction = transaction;
	}

	/**
	 * Add a SIP header
	 * 
	 * @param name Header name
	 * @param value Header value
	 */	
	public void addHeader(String name, String value) {
		try {
			Header header = SipUtils.HEADER_FACTORY.createHeader(name, value);
			stackMessage.setHeader(header);
		} catch(ParseException e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * Return a header value
	 * 
	 * @param name Header name
	 * @return Header
	 */
	public Header getHeader(String name) {
		return stackMessage.getHeader(name);
	}

	/**
	 * Return values of an header
	 * 
	 * @param name Header name
	 * @return List of headers
	 */
	public ListIterator<Header> getHeaders(String name) {
		return stackMessage.getHeaders(name);
	}

	/**
	 * Get Via headers list
	 * 
	 * @return List of headers
	 */
	public ListIterator<ViaHeader> getViaHeaders() {
		return stackMessage.getHeaders(ViaHeader.NAME);
	}	
	
	/**
	 * Return the From
	 * 
	 * @return String
	 */
	public String getFrom() {
		FromHeader header = (FromHeader)stackMessage.getHeader(FromHeader.NAME);
		return header.getAddress().toString();
	}

	/**
	 * Return the From tag
	 * 
	 * @return String
	 */
	public String getFromTag() {
		FromHeader header = (FromHeader)stackMessage.getHeader(FromHeader.NAME);
		return header.getTag();
	}

	/**
	 * Return the From URI
	 * 
	 * @return String
	 */
	public String getFromUri() {
		FromHeader header = (FromHeader)stackMessage.getHeader(FromHeader.NAME);
		return header.getAddress().getURI().toString();
	}
	
	/**
	 * Return the To
	 * 
	 * @return String
	 */
	public String getTo() {
		ToHeader header = (ToHeader)stackMessage.getHeader(ToHeader.NAME);
		return header.getAddress().toString();
	}

	/**
	 * Return the To tag
	 * 
	 * @return String
	 */
	public String getToTag() {
		ToHeader header = (ToHeader)stackMessage.getHeader(ToHeader.NAME);
		return header.getTag();
	}

	/**
	 * Return the To URI
	 * 
	 * @return String
	 */
	public String getToUri() {
		ToHeader header = (ToHeader)stackMessage.getHeader(ToHeader.NAME);
		return header.getAddress().getURI().toString();
	}
	
	/**
	 * Return the CSeq value
	 * 
	 * @return Number
	 */
	public long getCSeq() {
		CSeqHeader header = (CSeqHeader)stackMessage.getHeader(CSeqHeader.NAME);
		return header.getSeqNumber();
	}
	
	/**
	 * Return the contact URI
	 * 
	 * @return String or null
	 */
	public String getContactURI() {
        ContactHeader header = (ContactHeader)stackMessage.getHeader(ContactHeader.NAME);
		if (header != null) {
			return header.getAddress().getURI().toString();
		} else {
			return null;
		}
	}
	
	/**
	 * Return the content part
	 * 
	 * @return String or null
	 */
	public String getContent() {
		byte[] content = stackMessage.getRawContent();
		if (content != null) {
			return new String(content);
		} else {
			return null;
		}
	}
	
	/**
	 * Return the content part as bytes
	 * 
	 * @return String or null
	 */
	public byte[] getContentBytes() {
		return stackMessage.getRawContent();
	}

	/**
	 * Return the content type
	 * 
	 * @return String or null
	 */
	public String getContentType() {
		ContentTypeHeader header = (ContentTypeHeader)stackMessage.getHeader(ContentTypeHeader.NAME);
		if (header != null) {
			return header.getContentType() + "/" + header.getContentSubType();
		} else {
			return null;
		}
	}

	/**
	 * Return the boudary parameter of the content type
	 * 
	 * @return String or null
	 */
	public String getBoundaryContentType() {
		ContentTypeHeader header = (ContentTypeHeader)stackMessage.getHeader(ContentTypeHeader.NAME);
		if (header != null) {
			return header.getParameter("boundary");
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the call-ID value
	 * 
	 * @return String or null
	 */
	public String getCallId() {
		CallIdHeader header = (CallIdHeader)stackMessage.getHeader(CallIdHeader.NAME);
		if (header != null) {
			return header.getCallId();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the subject value
	 * 
	 * @return String or empty
	 */
	public String getSubject() {
		SubjectHeader header = (SubjectHeader)getHeader(SubjectHeader.NAME);
		if (header != null) {
			return header.getSubject();
		} else {
			return "";
		}
	}
	
	/**
	 * Return the accept type
	 * 
	 * @return String or null
	 */
	public String getAcceptType() {
    	AcceptHeader header = (AcceptHeader)getHeader(AcceptHeader.NAME);
		if (header != null) {
			return header.getContentType() + "/" + header.getContentSubType();
		} else {
			return null;
		}
	}
	
	/**
	 * Get the features tags from Contact header
	 * 
	 * @return Array of strings
	 */
	public ArrayList<String> getFeatureTags() {
		ArrayList<String> tags = new ArrayList<String>();
		
		// Read Contact header
		ContactHeader contactHeader = (ContactHeader)stackMessage.getHeader(ContactHeader.NAME);
		if (contactHeader != null) {
	        for(Iterator i = contactHeader.getParameterNames(); i.hasNext();) {
	        	String pname = (String)i.next();
	        	String value = contactHeader.getParameter(pname);
        		if ((value == null) || (value.length() == 0)) {
        			tags.add(pname);	        		
	        	} else {
		        	String[] values = value.split(",");
		        	for(int j=0; j < values.length; j++) {
		        		String tag = values[j].trim();
		        		if (!tags.contains(tag)){
		        			tags.add(tag);
		        		}
		        	}
	        	}
	        }
		}
		
		// Read Accept-Contact header
		ExtensionHeader acceptHeader = (ExtensionHeader)stackMessage.getHeader(SipUtils.HEADER_ACCEPT_CONTACT);
		if (acceptHeader == null) {
			// Check contracted form
			acceptHeader = (ExtensionHeader)stackMessage.getHeader(SipUtils.HEADER_ACCEPT_CONTACT_C);
		}
		if (acceptHeader != null) {
			String[] pnames = acceptHeader.getValue().split(";");
			if (pnames.length > 1) {
				// Start at index 1 to bypass the address
				for(int i=1; i < pnames.length; i++) {
					if (!tags.contains(pnames[i])){
						tags.add(pnames[i]);
					}
				}
			}
		}		
		
		return tags;
	}
	
	/**
	 * Get session timer expire
	 * 
	 * @return Expire time or -1 if no session timer
	 */
	public int getSessionTimerExpire() {
		SessionExpiresHeader sessionExpiresHeader = (SessionExpiresHeader)getHeader(SessionExpiresHeader.NAME);
		if (sessionExpiresHeader != null) {
			return sessionExpiresHeader.getExpires();
		} else {
			return -1;
		}
	}
	
	/**
	 * Get session timer refresher role
	 * 
	 * @return "uac" or "uas"
	 */
	public String getSessionTimerRefresher() {
		String role = "uac";
		SessionExpiresHeader sessionExpiresHeader = (SessionExpiresHeader)getHeader(SessionExpiresHeader.NAME);
		if (sessionExpiresHeader != null) {
			role = sessionExpiresHeader.getRefresher();
		}
		return role;
	}
}
