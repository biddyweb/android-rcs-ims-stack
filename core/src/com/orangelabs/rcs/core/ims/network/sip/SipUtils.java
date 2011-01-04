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

import gov.nist.core.NameValue;
import gov.nist.core.ParseException;
import gov.nist.core.sip.address.Address;
import gov.nist.core.sip.address.AddressFactory;
import gov.nist.core.sip.address.SipURI;
import gov.nist.core.sip.address.TelURL;
import gov.nist.core.sip.address.URI;
import gov.nist.core.sip.header.ContactHeader;
import gov.nist.core.sip.header.Header;
import gov.nist.core.sip.header.HeaderFactory;
import gov.nist.core.sip.header.MaxForwardsHeader;
import gov.nist.core.sip.header.RecordRouteHeader;
import gov.nist.core.sip.header.RouteHeader;
import gov.nist.core.sip.header.ViaHeader;
import gov.nist.core.sip.message.MessageFactory;
import gov.nist.core.sip.message.Request;

import java.util.Enumeration;
import java.util.Vector;

import com.orangelabs.rcs.core.TerminalInfo;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipMessage;
import com.orangelabs.rcs.core.ims.protocol.sip.SipStack;

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
	public static HeaderFactory HEADER_FACTORY = new HeaderFactory();
		
	/**
	 * Address factory
	 */
	public static AddressFactory ADDR_FACTORY = new AddressFactory();

	/**
	 * Message factory
	 */
	public static MessageFactory MSG_FACTORY = new MessageFactory();	
	
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
		Enumeration<Header> list = msg.getStackMessage().getHeaders(RecordRouteHeader.NAME); 
		if (list == null) {
			// No route available
			return null;
		}

        while(list.hasMoreElements()) {
        	RecordRouteHeader record = (RecordRouteHeader)list.nextElement();
            RouteHeader route = new RouteHeader(record.getAddress());
            if (invert) {
            	result.insertElementAt(route.getHeaderValue(), 0);
            } else {
            	result.addElement(route.getHeaderValue());
            }
		}

		return result;
	}
	
	/**
	 * Extract a tag from a header
	 * 
	 * @param header Header
	 * @return Tag or null if there is no tag
	 */
	public static String extractTag(String header) {
		String tag = null;
		int index = header.indexOf("tag=");
		if (index > 0) {
			tag = header.substring(index+4);
		}
		return tag;
	}
		
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
     * Generate a new branch identifier
     *
     * @return Unique identifier in space and time
     */
    public static String makeBranchId() {
		return("z9hG4bK" + System.currentTimeMillis());
    }
	
	/**
	 * Extract the session timer period
	 * 
	 * @param message SIP message
	 * @return Expire period in seconds or -1 in case of error
	 */
	public static int extractSessionTimerPeriod(SipMessage message) {
		try {
			// Extract the session expire value from the "Session-Expires"
			// header of the given SIP message
			String value =	message.getStackMessage().getHeader("Session-Expires").getHeaderValue();
			int index = value.indexOf(";");
			if (index != -1) {
				value = value.substring(0, index).trim();
			}
			return Integer.parseInt(value);
		} catch(Exception e) {
			return -1;
		}
	}
	
	/**
	 * Extract a parameter from a SIP header value
	 * 
	 * @param name Parameter name 
	 * @return Parameter
	 */
	public static String extractHeaderParamater(String header, String name) {
		int index1 = header.indexOf(name+"=");
		if (index1 != -1) {
			int index2 = header.indexOf(",", index1);
			if (index2 == -1) {
				// End of line: no comma found
				index2 = header.length();
			}			
			return header.substring(index1+name.length()+2, index2-1);
		} else {
			return null;
		}
	}
	
    /**
     * Build User-Agent header
     * 
     * @param Header
     */
	public static Header buildUserAgentHeader() throws Exception {
	    String value = "IM-client/OMA1.0 " + TerminalInfo.PRODUCT_NAME + "/" + TerminalInfo.PRODUCT_VERSION;
	    Header userAgentHeader = HEADER_FACTORY.createHeader("User-Agent", value);
	    return userAgentHeader;
    }
    
    /**
     * Build Contact header
     * 
     * @param stack SIP stack
     * @return Header
     */
	public static ContactHeader buildContactHeader(SipStack stack) throws Exception {
    	// Set the contact with the terminal IP address and SIP port
		SipURI contactURI = (SipURI)ADDR_FACTORY.createSipURI(
				null,
				stack.getLocalIpAddress());
		contactURI.setPort(stack.getListeningPort());

		// Add display name
		Address contactAddress = ADDR_FACTORY.createAddress(contactURI);

		// Create the Contact header
		ContactHeader contactHeader = HEADER_FACTORY.createContactHeader(contactAddress);		
		return contactHeader;
    }

	/**
	 * Build Allow header
	 * 
	 * @return Header
	 */
	public static Header buildAllowHeader() throws Exception {
    	String value = Request.INVITE + ", " +
			Request.ACK + ", " +
			Request.BYE + ", " +
			Request.CANCEL + ", " +
			Request.NOTIFY + ", " +
			Request.OPTIONS + ", " +
			Request.MESSAGE;		
    	Header allowHeader = HEADER_FACTORY.createHeader("Allow", value);
        return allowHeader;
    }
	
	/**
     * Build Via headers
     * 
     * @param dialog SIP dialog path
     * @param branchId Branch Id
     * @return Header
     */
	public static Vector<ViaHeader> buildViaHeader(SipDialogPath dialog, String branchId) throws Exception {
		ViaHeader via = HEADER_FACTORY.createViaHeader(dialog.getSipStack().getLocalIpAddress(),
				dialog.getSipStack().getListeningPort(),
				dialog.getSipStack().getProxyProtocol(),
				branchId);
		Vector<ViaHeader> list = new Vector<ViaHeader>();
		list.addElement(via);
		return list;
    }   

    /**
     * Build Via headers
     * 
     * @param dialog SIP dialog path
     * @return Header
     */
	public static Vector<ViaHeader> buildViaHeader(SipDialogPath dialog) throws Exception {
    	String branchId = SipUtils.makeBranchId();    	
		return buildViaHeader(dialog, branchId);
    }   

	/**
     * Build Max-Forwards header
     * 
     * @return Header
     */
	public static MaxForwardsHeader buildMaxForwardsHeader() {
    	return HEADER_FACTORY.createMaxForwardsHeader(70);	
	}
    
	/**
     * Build Server header
     * 
     * @return Header
     * @throws Exception
     */
	public static Header buildServerHeader() throws Exception {
		String value = "IM-client/OMA1.0 " + TerminalInfo.PRODUCT_NAME + "/" + TerminalInfo.PRODUCT_VERSION;
		return HEADER_FACTORY.createHeader("Server", value);
    }
    
    /**
	 * Build P-Access-Network-info
	 * 
	 * @param info Access info
	 * @return Header
	 * @throws Exception
	 */
    public static Header buildAccessNetworkInfo(String info) throws Exception {
		Header accessInfo = HEADER_FACTORY.createHeader("P-Access-Network-Info", info);
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
    		return;
    	}
    	
    	// Set Contact header
    	String acceptTags = "";
    	ContactHeader contact = (ContactHeader)message.getStackMessage().getContactHeaders().first();
    	for(int i=0; i < tags.length; i++) {
    		contact.setParameter(new NameValue(tags[i], null));
    		acceptTags += tags[i] + ";";
    	}
    	
    	// Set Accept-Contact header
    	Header acceptHeader = HEADER_FACTORY.createHeader("Accept-Contact",	"*;" + acceptTags + "explicit");
    	message.getStackMessage().addHeader(acceptHeader);
    }
    
    /**
     * Extract the boundary tag from an input text
     * 
     * @param input Input text
     * @return Returns the boundary tag
     */
    public static String extractBoundary(String input) {
    	try {
	    	int begin = input.indexOf("boundary=");
	    	if (begin != -1) {
		    	int end = input.indexOf("\"", begin+10);
		    	if (end != -1) {
		    		return input.substring(begin+10, end);
		    	}
	    	}
			return null;
    	} catch(Exception e) {
    		return null;
    	}
    }

	/**
	 * Extract the Min-Expires period
	 * 
	 * @param message SIP message
	 * @return Expire period in seconds or -1 in case of error
	 */
	public static int extractMinExpiresPeriod(SipMessage message) {
		try {
			// Extract the expire value from the "Min-Expires" header of the given SIP message
			String value =	message.getStackMessage().getHeader("Min-Expires").getHeaderValue();
			return Integer.parseInt(value);
		} catch(Exception e) {
			return -1;
		}
	}
	
	/**
	 * Extract the username from a SIP address
	 * 
	 * @param address SIP address (SIP-URI or Tel-URL)
	 * @return Username
	 */
	public static String extractUsernameFromAddress(String address){
		String username = null;
		try {
			Address addr = SipUtils.ADDR_FACTORY.createAddress(address);
			URI uri = addr.getURI();
			if (uri instanceof SipURI) {
				SipURI sip = (SipURI)addr.getURI();
				username = sip.getAuthority().getUserInfo().getUser();				
			} else
			if (uri instanceof TelURL) {
				TelURL tel = (TelURL)addr.getURI();
				username = tel.getPhoneNumber();
				if (tel.isGlobal()) {
					username = "+"+username;
				}
			} else {
				username = address;
			}
		} catch(ParseException e) {
			username = null;
		}
		return username;
	}

	/**
	 * Extract the domain name from a SIP address
	 * 
	 * @param address SIP address
	 * @return Domain name
	 */
	public static String extractDomainFromAddress(String address){
		try {
			Address addr = SipUtils.ADDR_FACTORY.createAddress(address);
			return addr.getHost();
		} catch (ParseException e) {
			return null;
		}
	}
}
