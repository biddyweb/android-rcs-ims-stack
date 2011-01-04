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
import gov.nist.core.sip.address.Address;
import gov.nist.core.sip.address.URI;
import gov.nist.core.sip.header.CSeqHeader;
import gov.nist.core.sip.header.CallIdHeader;
import gov.nist.core.sip.header.ContactHeader;
import gov.nist.core.sip.header.ContentLengthHeader;
import gov.nist.core.sip.header.ContentTypeHeader;
import gov.nist.core.sip.header.ExpiresHeader;
import gov.nist.core.sip.header.FromHeader;
import gov.nist.core.sip.header.Header;
import gov.nist.core.sip.header.ToHeader;
import gov.nist.core.sip.message.Request;
import gov.nist.core.sip.message.Response;

import java.util.Vector;

import com.orangelabs.rcs.core.ims.protocol.sip.IdGenerator;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * SIP message factory
 * 
 * @author jexa7410
 */
public class SipMessageFactory {
	/**
     * The logger
     */
    private static Logger logger = Logger.getLogger(SipMessageFactory.class.getName());

    /**
	 * Create a SIP REGISTER request
	 * 
	 * @param dialog SIP dialog path
	 * @param expirePeriod Expiration period
	 * @param accessInfo Access info
	 * @return SIP request
	 * @throws SipException
	 */
    public static SipRequest createRegister(SipDialogPath dialog, int expirePeriod, String accessInfo) throws SipException {
     	Request register = null;
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	        
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallId(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.REGISTER);
	
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, null);
	        
	        // Create the REGISTER request
	        register = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.REGISTER,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					SipUtils.buildViaHeader(dialog),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set Contact header
	        ContactHeader contact = SipUtils.buildContactHeader(dialog.getSipStack());
	        contact.setParameter(new NameValue(SipUtils.FEATURE_3GPP_CS_VOICE, null));
	        contact.setParameter(new NameValue(SipUtils.FEATURE_GSMA_CS_IMAGE, null));
	        contact.setParameter(new NameValue(SipUtils.FEATURE_OMA_IM, null));
	        register.addHeader(contact);
	
	        // Set Accept-Contact header
	        Header accept = SipUtils.HEADER_FACTORY.createHeader("Accept-Contact",	"*;" +
	        		SipUtils.FEATURE_OMA_IM + ";" +
	        		SipUtils.FEATURE_3GPP_CS_VOICE + ";" +
	        		SipUtils.FEATURE_GSMA_CS_IMAGE + ";explicit");
	        register.addHeader(accept);

			// Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader("Route", route.elementAt(i));
	        	register.addHeader(routeHeader);
	        }
	        
	        // Set the Expires header
	        ExpiresHeader expHeader = SipUtils.HEADER_FACTORY.createExpiresHeader(expirePeriod);
	        register.addHeader(expHeader);
	        
	        // Set User-Agent header
	        register.addHeader(SipUtils.buildUserAgentHeader());
	        
	        // Set the P-Access-Network-Info header
	        if (accessInfo != null) {
		        Header accessInfoHeader = SipUtils.buildAccessNetworkInfo(accessInfo);
		        register.addHeader(accessInfoHeader);
	    	}
	        
	        // Set Allow header
	        register.addHeader(SipUtils.buildAllowHeader());

		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP REGISTER message");
		}
		return new SipRequest(register);
    }    

    /**
	 * Create a SIP OPTIONS request
	 * 
	 * @param dialog SIP dialog path
	 * @return SIP request
	 * @throws SipException
	 */
    public static SipRequest createOptions(SipDialogPath dialog) throws SipException {
     	Request options = null;
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	        
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallId(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.OPTIONS);
	
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, null);
	
	        // Create the REGISTER request
	        options = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.OPTIONS,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					SipUtils.buildViaHeader(dialog),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set Contact header
	        ContactHeader contact = SipUtils.buildContactHeader(dialog.getSipStack());
	        contact.setParameter(new NameValue(SipUtils.FEATURE_3GPP_CS_VOICE, null));
	        contact.setParameter(new NameValue(SipUtils.FEATURE_GSMA_CS_IMAGE, null));
	        options.addHeader(contact);
	
	        // Set Accept-Contact header
	        Header accept = SipUtils.HEADER_FACTORY.createHeader("Accept-Contact",	"*;" +
	        		SipUtils.FEATURE_3GPP_CS_VOICE + ";" +
	        		SipUtils.FEATURE_GSMA_CS_IMAGE + ";explicit");
			options.addHeader(accept);

	        // Set Accept header
	    	Header acceptHeader = SipUtils.HEADER_FACTORY.createHeader("Accept", "application/sdp");
			options.addHeader(acceptHeader);
			
			// Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader("Route", route.elementAt(i));
	        	options.addHeader(routeHeader);
	        }
	        
	        // Set User-Agent header
	        options.addHeader(SipUtils.buildUserAgentHeader());
	        
	        // Set Allow header
	        options.addHeader(SipUtils.buildAllowHeader());

		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP OPTIONS message");
		}
		return new SipRequest(options);
    }    
    
    /**
	 * Create a SIP SUBSCRIBE request
	 * 
	 * @param dialog SIP dialog path
	 * @param expirePeriod Expiration period
	 * @param accessInfo Access info
	 * @return SIP request
	 * @throws SipException
	 */
    public static SipRequest createSubscribe(SipDialogPath dialog,
    		int expirePeriod,
    		String accessInfo) throws SipException {
     	Request subscribe = null;
     	
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	        
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallId(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.SUBSCRIBE);
	
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, dialog.getRemoteTag());
	
	        // Create the SUBSCRIBE request
	        subscribe = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.SUBSCRIBE,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					SipUtils.buildViaHeader(dialog),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader("Route", route.elementAt(i));
	        	subscribe.addHeader(routeHeader);
	        }
	        
	        // Set the Expires header
	        ExpiresHeader expHeader = SipUtils.HEADER_FACTORY.createExpiresHeader(expirePeriod);
	        subscribe.addHeader(expHeader);
	        
	        // Set User-Agent header
	        subscribe.addHeader(SipUtils.buildUserAgentHeader());
	        
	        // Set Contact header
	        subscribe.addHeader(SipUtils.buildContactHeader(dialog.getSipStack()));

	        // Set the P-Access-Network-Info header
	    	if (accessInfo != null) {
		        Header accessInfoHeader = SipUtils.buildAccessNetworkInfo(accessInfo);
		        subscribe.addHeader(accessInfoHeader);
	    	}
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP SUBSCRIBE message");
		}
		return new SipRequest(subscribe);
    }	

    /**
	 * Create a SIP MESSAGE request
	 * 
	 * @param dialog SIP dialog path
	 * @param text Message to be sent
	 * @return SIP request
	 * @throws SipException
	 */
	public static SipRequest createInstantMessage(SipDialogPath dialog, String text) throws SipException {
    	Request message = null;
		try {			
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallId(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.MESSAGE);
	        
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, dialog.getRemoteTag());
			
	        // Create the MESSAGE request
	        message = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.MESSAGE,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					SipUtils.buildViaHeader(dialog),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader("Route", route.elementAt(i));
	        	message.addHeader(routeHeader);
	        }
	                
	        // Set Contact header
	        ContactHeader contact = SipUtils.buildContactHeader(dialog.getSipStack());
	        contact.setParameter(new NameValue(SipUtils.FEATURE_OMA_IM, null));
	        message.addHeader(contact);

	        // Set User-Agent header
	        message.addHeader(SipUtils.buildUserAgentHeader());
	
	        // Set Accept-Contact header
	    	Header accept = SipUtils.HEADER_FACTORY.createHeader("Accept-Contact",	"*;" +
	    			SipUtils.FEATURE_OMA_IM + ";explicit");
			message.addHeader(accept);

			// Set the message content
			ContentTypeHeader contentTypeHeader = SipUtils.HEADER_FACTORY.createContentTypeHeader("text", "plain");
	        message.setContent(text, contentTypeHeader);
	        
	        // Set the message content length
			ContentLengthHeader contentLengthHeader = SipUtils.HEADER_FACTORY.createContentLengthHeader(text.length());
			message.setContentLength(contentLengthHeader);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP MESSAGE message");
		}
		return new SipRequest(message);
    }    

    /**
	 * Create a SIP PUBLISH request
	 * 
	 * @param dialog SIP dialog path
	 * @param expirePeriod Expiration period
	 * @param entityTag Entity tag
	 * @param accessInfo Access info
	 * @param sdp SDP part
	 * @return SIP request
	 * @throws SipException
	 */
    public static SipRequest createPublish(SipDialogPath dialog,
    		int expirePeriod,
    		String entityTag,
    		String accessInfo,
    		String sdp) throws SipException {
     	Request publish = null;
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	        
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallId(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.PUBLISH);
	
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, dialog.getRemoteTag());
	
	        // Create the PUBLISH request
	        publish = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.PUBLISH,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					SipUtils.buildViaHeader(dialog),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader("Route", route.elementAt(i));
	        	publish.addHeader(routeHeader);
	        }
	        
	        // Set the Expires header
	        ExpiresHeader expHeader = SipUtils.HEADER_FACTORY.createExpiresHeader(expirePeriod);
	        publish.addHeader(expHeader);

        	// Set the SIP-If-Match header
	        if (entityTag != null) {
	        	Header sipIfMatchHeader = SipUtils.HEADER_FACTORY.createHeader("SIP-If-Match", entityTag);
	        	publish.addHeader(sipIfMatchHeader);
	        }

	        // Set User-Agent header
	        publish.addHeader(SipUtils.buildUserAgentHeader());
	        
	        // Set the P-Access-Network-Info header
	    	if (accessInfo != null) {
		        Header accessInfoHeader = SipUtils.buildAccessNetworkInfo(accessInfo);
		        publish.addHeader(accessInfoHeader);
	    	}
	        
	    	// Set the Event header
	    	publish.addHeader(SipUtils.HEADER_FACTORY.createHeader("Event", "presence"));
        	
	        // Set the message content
	    	if (sdp != null) {
	    		ContentTypeHeader contentTypeHeader = SipUtils.HEADER_FACTORY.createContentTypeHeader("application", "pidf+xml");
	    		publish.setContent(sdp, contentTypeHeader);
	    	}
	    	
    		// Set the message content length
	    	int length = 0;
	    	if (sdp != null) {
	    		length = sdp.length();
	    	}
    		ContentLengthHeader contentLengthHeader = SipUtils.HEADER_FACTORY.createContentLengthHeader(length);
    		publish.setContentLength(contentLengthHeader);
			
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP PUBLISH message");
		}
		return new SipRequest(publish);
    }	

    /**
     * Create a SIP INVITE request
     * 
     * @param dialog SIP dialog path
     * @param expirePeriod Session expiration period
     * @param sdp SDP part
	 * @return SIP request
     * @throws SipException
     */
    public static SipRequest createInvite(SipDialogPath dialog,
    		int expirePeriod,
			String sdp)
            throws SipException {
		try {
			ContentTypeHeader contentType = SipUtils.HEADER_FACTORY.createContentTypeHeader("application", "sdp");
			return createInvite(dialog, expirePeriod, sdp, contentType);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP INVITE message");
		}
    }
    
    /**
     * Create a SIP INVITE request
     * 
     * @param dialog SIP dialog path
     * @param expirePeriod Session expiration period
     * @param multipart Multipart
     * @param boudary Boundary tag
	 * @return SIP request
     * @throws SipException
     */
    public static SipRequest createMultipartInvite(SipDialogPath dialog,
    		int expirePeriod,
			String multipart,
			String boundary)
            throws SipException {
		try {
			ContentTypeHeader contentType = SipUtils.HEADER_FACTORY.createContentTypeHeader("multipart", "mixed");
			contentType.setQuotedParameter("boundary", "boundary1");
			
			return createInvite(dialog, expirePeriod, multipart, contentType);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP INVITE message");
		}
    }

    /**
     * Create a SIP INVITE request
     * 
     * @param dialog SIP dialog path
     * @param expirePeriod Session expiration period
     * @param content Content
     * @param contentType Content type
	 * @return SIP request
     * @throws SipException
     */
    public static SipRequest createInvite(SipDialogPath dialog,
    		int expirePeriod,
			String content,
			ContentTypeHeader contentType)
            throws SipException {
       	Request invite = null;
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	        
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallId(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.INVITE);
	
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, null);

	        // Create the INVITE request
	        invite = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.INVITE,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					SipUtils.buildViaHeader(dialog),
					SipUtils.buildMaxForwardsHeader());       

	        // Set Contact header
	        ContactHeader contact = SipUtils.buildContactHeader(dialog.getSipStack());
	        invite.addHeader(contact);
	
			// Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader("Route", route.elementAt(i));
	        	invite.addHeader(routeHeader);
	        }
	        
	        // Set User-Agent header
	        invite.addHeader(SipUtils.buildUserAgentHeader());
	        
	        // Set Allow header
	        invite.addHeader(SipUtils.buildAllowHeader());

	        // Set the Supported header
			Header supportedHeader = SipUtils.HEADER_FACTORY.createHeader("Supported", "timer");
			invite.addHeader(supportedHeader);
	
	        // Set the message content
	        invite.setContent(content, contentType);

	        // Set the content length
			ContentLengthHeader contentLengthHeader = SipUtils.HEADER_FACTORY.createContentLengthHeader(content.length());
			invite.setContentLength(contentLengthHeader);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP INVITE message");
		}
		return new SipRequest(invite);
    }

    /**
	 * Create a 200 OK response for INVITE request
	 * 
     * @param dialog SIP dialog path
	 * @param sdp SDP part
	 * @return SIP response
	 * @throws SipException
	 */
	public static SipResponse create200OkInviteResponse(SipDialogPath dialog, String sdp) throws SipException {
		Response response = null;
		try {
			// Create the RESPONSE object
			response = SipUtils.MSG_FACTORY.createResponse(200, (Request)dialog.getInvite().getStackMessage());
	
			// Set the local tag
			ToHeader to = (ToHeader)response.getHeader(ToHeader.NAME);
			to.setTag(dialog.getLocalTag());	
	
	        // Set Contact header
	        ContactHeader contact = SipUtils.buildContactHeader(dialog.getSipStack());
	        response.addHeader(contact);
	
			// Set the Server header
			response.addHeader(SipUtils.buildServerHeader());
	
			// Set the Require header
	    	Header requireHeader = SipUtils.HEADER_FACTORY.createHeader("Require", "timer");
			response.addHeader(requireHeader);			
			
	        // Set the message content
			ContentTypeHeader contentTypeHeader = SipUtils.HEADER_FACTORY.createContentTypeHeader("application", "sdp");
			response.setContent(sdp, contentTypeHeader);

	        // Set the message content length
			ContentLengthHeader contentLengthHeader = SipUtils.HEADER_FACTORY.createContentLengthHeader(sdp.length());
			response.setContentLength(contentLengthHeader);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP response");
		}
		return new SipResponse(response);
	}
	
    /**
	 * Create a 200 OK response for OPTIONS request
	 * 
     * @param dialog SIP dialog path
	 * @param sdp SDP part
	 * @return SIP response
	 * @throws SipException
	 */
	public static SipResponse create200OkOptionsResponse(SipRequest options, String sdp) throws SipException {
		Response response = null;
		try {
			// Create the RESPONSE object
			response = SipUtils.MSG_FACTORY.createResponse(200, (Request)options.getStackMessage());
	
			// Set the local tag
			ToHeader to = (ToHeader)response.getHeader(ToHeader.NAME);
			to.setTag(IdGenerator.getIdentifier());
	
	        // Set Accept-Contact header
	    	Header accept = SipUtils.HEADER_FACTORY.createHeader("Accept-Contact",	"*;" +
		    			SipUtils.FEATURE_3GPP_CS_VOICE + ";" +
		    			SipUtils.FEATURE_GSMA_CS_IMAGE + ";explicit");
			response.addHeader(accept);
	
			// Set the Server header
			response.addHeader(SipUtils.buildServerHeader());
	
	        // Set the message content
			ContentTypeHeader contentTypeHeader = SipUtils.HEADER_FACTORY.createContentTypeHeader("application", "sdp");
			response.setContent(sdp, contentTypeHeader);
			
	        // Set the message content length
			ContentLengthHeader contentLengthHeader = SipUtils.HEADER_FACTORY.createContentLengthHeader(sdp.length());
			response.setContentLength(contentLengthHeader);
			
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP response");
		}
		return new SipResponse(response);
	}

	/**
	 * Create a SIP response
	 * 
	 * @param request SIP request
	 * @param code Response code
	 * @return SIP response
	 * @throws SipException
	 */
	public static SipResponse createResponse(SipRequest request, int code) throws SipException {
		Response response = null;
		try {
			// Create the RESPONSE object
			response = SipUtils.MSG_FACTORY.createResponse(code, (Request)request.getStackMessage());

			// Set the local tag
			ToHeader to = (ToHeader)response.getHeader(ToHeader.NAME);
			if (request.getTo().indexOf("tag") == -1) {
				// Add a local tag if needed
				to.setTag(IdGenerator.getIdentifier());
			}
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP response");
		}
		return new SipResponse(response);
	}    

	/**
	 * Create a SIP response with a specific local tag
	 * 
	 * @param request SIP request
	 * @param localTag Local tag
	 * @param code Response code
	 * @return SIP response
	 */
	public static SipResponse createResponse(SipRequest request, String localTag, int code) throws SipException {
		Response response = null;
		try {
			// Create the RESPONSE object
			response = SipUtils.MSG_FACTORY.createResponse(code, (Request)request.getStackMessage());
	
			if (localTag != null) {
				// Set the local tag
				ToHeader to = (ToHeader)response.getHeader(ToHeader.NAME);
				to.setTag(localTag);
			}
			
			// Set the Record-Route header
			// Set by NIST
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message: ", e);
			}
			throw new SipException("Can't create SIP response");
		}
		return new SipResponse(response);
	}

	/**
	 * Create a ringing provisionnal response
	 * 
	 * @param dialog SIP dialog path
	 * @return SIP response
	 * @throws SipException
	 */
	public static SipResponse createRinging(SipDialogPath dialog) throws SipException {
		Response response = null;
		try {
			// Create the RESPONSE object
			response = SipUtils.MSG_FACTORY.createResponse(180, (Request)dialog.getInvite().getStackMessage());
	
			// Set the local tag
			ToHeader to = (ToHeader)response.getHeader(ToHeader.NAME);
			to.setTag(dialog.getLocalTag());
	
			// Set the Server header
			response.addHeader(SipUtils.buildServerHeader());
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP response");
		}
		return new SipResponse(response);
	}

	/**
	 * Create a SIP ACK request
	 * 
	 * @param dialog SIP dialog path
	 * @return SIP request
	 * @throws SipException
	 */
	public static SipRequest createAck(SipDialogPath dialog) throws SipException {
      	Request ack = null;
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallId(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.ACK);
	        
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, dialog.getRemoteTag());
	        
	        // Create the ACK request
	        ack = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.ACK,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					SipUtils.buildViaHeader(dialog, dialog.getInvite().getBranchId()),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader("Route", route.elementAt(i));
	        	ack.addHeader(routeHeader);
	        }
	                
	        // Set Contact header
	        ack.addHeader(SipUtils.buildContactHeader(dialog.getSipStack()));

	        // Set User-Agent header
	        ack.addHeader(SipUtils.buildUserAgentHeader());
	
	        // Set Allow header
	        ack.addHeader(SipUtils.buildAllowHeader());

		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP ACK message");
		}
		return new SipRequest(ack);
	}
	
	/**
	 * Create a SIP CANCEL request
	 * 
	 * @param dialog SIP dialog path
	 * @return SIP request
	 * @throws SipException
	 */
	public static SipRequest createCancel(SipDialogPath dialog) throws SipException {
      	Request cancel = null;
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallId(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.CANCEL);
	        
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, null);

	        // Create the CANCEL request
	        cancel = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.CANCEL,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					dialog.getInvite().getViaHeaders(),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader("Route", route.elementAt(i));
	        	cancel.addHeader(routeHeader);
	        }
	                
	        // Set Contact header
	        cancel.addHeader(SipUtils.buildContactHeader(dialog.getSipStack()));

	        // Set User-Agent header
	        cancel.addHeader(SipUtils.buildUserAgentHeader());
	
	        // Set Allow header
	        cancel.addHeader(SipUtils.buildAllowHeader());
	        
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP CANCEL message");
		}
		return new SipRequest(cancel);
	}
	
	/**
	 * Create a SIP BYE request
	 * 
	 * @param dialog SIP dialog path
	 * @return SIP request
	 * @throws SipException
	 */
	public static SipRequest createBye(SipDialogPath dialog) throws SipException {
       	Request bye = null;
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallId(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.BYE);
	        
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, dialog.getRemoteTag());
			
	        // Create the BYE request
	        bye = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.BYE,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					SipUtils.buildViaHeader(dialog),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader("Route", route.elementAt(i));
	        	bye.addHeader(routeHeader);
	        }
	                
	        // Set Contact header
	        bye.addHeader(SipUtils.buildContactHeader(dialog.getSipStack()));

	        // Set User-Agent header
        	bye.addHeader(SipUtils.buildUserAgentHeader());
	
	        // Set Allow header
	        bye.addHeader(SipUtils.buildAllowHeader());
	        
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP BYE message");
		}
		return new SipRequest(bye);
	}	
}
