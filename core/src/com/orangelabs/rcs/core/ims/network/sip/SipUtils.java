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
package com.orangelabs.rcs.core.ims.network.sip;

import java.text.ParseException;
import java.util.ListIterator;
import java.util.Vector;

import javax.sip.InvalidArgumentException;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.MinExpiresHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ServerHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

import com.orangelabs.rcs.core.TerminalInfo;
import com.orangelabs.rcs.core.ims.protocol.sip.SipMessage;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;

/**
 * SIP utility functions
 * 
 * @author JM. Auffret
 */
public class SipUtils {
	/**
	 * CRLF constant
	 */
	public final static String CRLF = "\r\n";
	
	/**
	 * 3GPP CS voice call feature tag
	 */
	public final static String FEATURE_3GPP_CS_VOICE = "+g.3gpp.cs-voice";

	/**
	 * GSMA image CS feature tag
	 */
	public final static String FEATURE_GSMA_CS_IMAGE = "+g.3gpp.app_ref=\"urn%3Aurn-xxx%3A3gpp-application.ims.iari.gsma-is\"";
	
	/**
	 * OMA IM feature tag
	 */
	public final static String FEATURE_OMA_IM = "+g.oma.sip-im";

	/**
	 * OMA large IM feature tag
	 */
	public final static String FEATURE_OMA_IM_LARGE = "+g.oma.sip-im.large-message";
	
	/**
	 * Header factory
	 */
	public static HeaderFactory HEADER_FACTORY = null;
		
	/**
	 * Address factory
	 */
	public static AddressFactory ADDR_FACTORY = null;

	/**
	 * Message factory
	 */
	public static MessageFactory MSG_FACTORY = null;	
		
	/**
	 * Accept-Contact header
	 */
	public static final String HEADER_ACCEPT_CONTACT = "Accept-Contact";
	
	/**
	 * P-Access-Network-Info header
	 */
	public static final String HEADER_P_ACCESS_NETWORK_INFO = "P-Access-Network-Info";
	
	/**
	 * P-Asserted-Identity header
	 */
	public static final String HEADER_P_ASSERTED_IDENTITY = "P-Asserted-Identity";
	
	/**
	 * P-Preferred-Identity header
	 */
	public static final String HEADER_P_PREFERRED_IDENTITY = "P-Preferred-Identity";
	
	/**
	 * P-Associated-URI header
	 */
	public static final String HEADER_P_ASSOCIATED_URI = "P-Associated-URI";
	
	/**
	 * Service-Route header
	 */
	public static final String HEADER_SERVICE_ROUTE = "Service-Route";
	
	/**
	 * Privacy header
	 */
	public static final String HEADER_PRIVACY = "Privacy";
	
	/**
	 * Construct an NTP time from a date in milliseconds
	 *
	 * @param date Date in milliseconds
	 * @return NTP time in string format
	 */
	public static String constructNTPtime(long date) {
		long ntpTime = 2208988800L;
		long startTime = (date / 1000) + ntpTime;
		return String.valueOf(startTime);
	}

    /**
     * Build User-Agent header
     * 
     * @param Header
     */
	public static Header buildUserAgentHeader() throws Exception {
	    String value = TerminalInfo.PRODUCT_NAME + "/" + TerminalInfo.PRODUCT_VERSION;
	    Header userAgentHeader = HEADER_FACTORY.createHeader(UserAgentHeader.NAME, value);
	    return userAgentHeader;
    }
    
	/**
	 * Build Allow header
	 * 
	 * @param request SIP request
	 */
	public static void buildAllowHeader(Request request) throws Exception {
		request.addHeader(HEADER_FACTORY.createAllowHeader(Request.INVITE));
		request.addHeader(HEADER_FACTORY.createAllowHeader(Request.ACK));
		request.addHeader(HEADER_FACTORY.createAllowHeader(Request.BYE));
		request.addHeader(HEADER_FACTORY.createAllowHeader(Request.NOTIFY));
		request.addHeader(HEADER_FACTORY.createAllowHeader(Request.OPTIONS));
		request.addHeader(HEADER_FACTORY.createAllowHeader(Request.MESSAGE));
    }
	
	/**
     * Build Max-Forwards header
     * 
     * @return Header
     * @throws InvalidArgumentException
     */
	public static MaxForwardsHeader buildMaxForwardsHeader() throws InvalidArgumentException {
    	return HEADER_FACTORY.createMaxForwardsHeader(70);	
	}
    
	/**
     * Build Server header
     * 
     * @return Header
     * @throws Exception
     */
	public static Header buildServerHeader() throws Exception {
		String value = TerminalInfo.PRODUCT_NAME + "/" + TerminalInfo.PRODUCT_VERSION;
		return HEADER_FACTORY.createHeader(ServerHeader.NAME, value);
    }
    
    /**
	 * Build P-Access-Network-info
	 * 
	 * @param info Access info
	 * @return Header
	 * @throws Exception
	 */
    public static Header buildAccessNetworkInfo(String info) throws Exception {
		Header accessInfo = HEADER_FACTORY.createHeader(SipUtils.HEADER_P_ACCESS_NETWORK_INFO, info);
		return accessInfo;
    }
    
    /**
     * Extract a parameter from an input text
     * 
     * @param input Input text
     * @param param Parameter name
     * @param defaultValue Default value
     * @return Returns the parameter value or a default value in case of error
     */
    public static String extractParameter(String input, String param, String defaultValue) {
    	// TODO: use SDP parser
    	try {
	    	int begin = input.indexOf(param);
	    	if (begin != -1) {
		    	int end = input.indexOf(" ", begin+1);
		    	if (end == -1) {
		    		return input.substring(begin+param.length());
		    	} else {
		    		return input.substring(begin+param.length(), end);
		    	}
	    	}
			return defaultValue;
    	} catch(Exception e) {
    		return defaultValue;
    	}
    }

    /**
     * Set feature tags of a message
     * 
     * @param message SIP message
     * @param tags Table of tags
     * @throws ParseException
     */
    public static void setFeatureTags(SipMessage message, String[] tags) throws ParseException {
    	if (tags.length == 0) {
    		// No feature tag to be set
    		return;
    	}
    	
    	// Set Contact header
    	String acceptTags = "";
    	
    	ContactHeader contact = (ContactHeader)message.getHeader(ContactHeader.NAME);
    	for(int i=0; i < tags.length; i++) {
    		contact.setParameter(tags[i], null);
    		acceptTags += tags[i] + ";";
    	}
    	
    	// Set Accept-Contact header
    	message.addHeader(SipUtils.HEADER_ACCEPT_CONTACT, "*;" + acceptTags + "explicit");
    }

    /**
	 * Get Min-Expires period from message
	 * 
	 * @param message SIP message
	 * @return Expire period in seconds or -1 in case of error
	 */
	public static int getMinExpiresPeriod(SipMessage message) {
		MinExpiresHeader minHeader = (MinExpiresHeader)message.getHeader(MinExpiresHeader.NAME);
		if (minHeader != null) {
			return minHeader.getExpires();
		} else {
			return -1;
		}
	}

	/**
	 * Get asserted identity
	 * 
	 * @param request SIP request
	 * @return SIP URI
	 */
	public static String getAssertedIdentity(SipRequest request) {
		ExtensionHeader assertedHeader = (ExtensionHeader)request.getHeader(SipUtils.HEADER_P_ASSERTED_IDENTITY);
		if (assertedHeader == null) {
			return request.getFromUri();
		} else {
			return assertedHeader.getValue();
		}
	}

    /**
	 * Generate a list of route headers. The record route of the incoming message
	 * is used to generate the corresponding route header.
	 * 
	 * @param msg SIP message
	 * @param invert Invert or not the route list
	 * @return List of route headers as string
	 * @throws Exception
	 */
	public static Vector<String> routeProcessing(SipMessage msg, boolean invert) {
		Vector<String> result = new Vector<String>(); 
		ListIterator<Header> list = msg.getHeaders(RecordRouteHeader.NAME); 
		if (list == null) {
			// No route available
			return null;
		}

        while(list.hasNext()) {
        	RecordRouteHeader record = (RecordRouteHeader)list.next();
            RouteHeader route = SipUtils.HEADER_FACTORY.createRouteHeader(record.getAddress());
            if (invert) {
            	result.insertElementAt(route.getAddress().toString(), 0);
            } else {
            	result.addElement(route.getAddress().toString());
            }
		}

		return result;
	}
	
    /**
     * Is a given feature tag present or not in SIP message
     * 
     * @param request Request
     * @param tag Tag to be searched
     * @return Boolean
     */
    public static boolean isFeatureTagPresent(SipRequest request, String tag) {
    	// Check Accept-Contact header firstly
		Header featureTag = request.getHeader(SipUtils.HEADER_ACCEPT_CONTACT);
		if (featureTag == null) {
			featureTag = request.getHeader("a"); 

	    	// Check Contact header secondly		
			if (featureTag == null) {
				featureTag = request.getHeader(ContactHeader.NAME);
				if (featureTag == null) {
					featureTag = request.getHeader("m"); 
				}
			}
		}

		if ((featureTag != null) && (featureTag.toString().indexOf(tag) != -1)) {
			return true;
		} else {
			return false;
		}    	
    }	
}
