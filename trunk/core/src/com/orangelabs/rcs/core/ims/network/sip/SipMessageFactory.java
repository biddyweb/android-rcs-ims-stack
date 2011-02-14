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

import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.Transaction;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.header.AcceptHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.RequireHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SIPIfMatchHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

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
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	        
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallIdHeader(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.REGISTER);
	
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress,
	        		IdGenerator.getIdentifier());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, null);
	        
	        // Create the request
	        Request register = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.REGISTER,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					dialog.getSipStack().getViaHeaders(),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set Contact header
	        ContactHeader contact = dialog.getSipStack().getContactHeader();
	        contact.setParameter(SipUtils.FEATURE_3GPP_CS_VOICE, null);
	        contact.setParameter(SipUtils.FEATURE_GSMA_CS_IMAGE, null);
	        contact.setParameter(SipUtils.FEATURE_OMA_IM, null);
	        register.addHeader(contact);
	
	        // Set Accept-Contact header
	        Header acceptContactHeader = SipUtils.HEADER_FACTORY.createHeader(SipUtils.HEADER_ACCEPT_CONTACT,	"*;" +
	        		SipUtils.FEATURE_OMA_IM + ";" +
	        		SipUtils.FEATURE_3GPP_CS_VOICE + ";" +
	        		SipUtils.FEATURE_GSMA_CS_IMAGE + ";explicit");
	        register.addHeader(acceptContactHeader);

	        // Set Supported header
	        Header supportedHeader = SipUtils.HEADER_FACTORY.createHeader(SupportedHeader.NAME, "path");
	        register.addHeader(supportedHeader);
	        
	        // Set the Route header
        	Vector<String> route = dialog.getSipStack().getDefaultRoutePath();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader(RouteHeader.NAME, route.elementAt(i));
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
	        SipUtils.buildAllowHeader(register);

			return new SipRequest(register);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP REGISTER message");
		}
    }    

    /**
	 * Create a SIP OPTIONS request
	 * 
	 * @param dialog SIP dialog path
	 * @return SIP request
	 * @throws SipException
	 */
    public static SipRequest createOptions(SipDialogPath dialog) throws SipException {
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	        
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallIdHeader(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.OPTIONS);
	
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, null);
	
			// Create the request
	        Request options = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.OPTIONS,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					dialog.getSipStack().getViaHeaders(),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set Contact header
	        ContactHeader contact = dialog.getSipStack().getContactHeader();
	        contact.setParameter(SipUtils.FEATURE_3GPP_CS_VOICE, null);
	        contact.setParameter(SipUtils.FEATURE_GSMA_CS_IMAGE, null);
	        options.addHeader(contact);
	
	        // Set Accept-Contact header
	        Header acceptContactHeader = SipUtils.HEADER_FACTORY.createHeader(SipUtils.HEADER_ACCEPT_CONTACT,	"*;" + 
	        		SipUtils.FEATURE_3GPP_CS_VOICE + ";" +
	        		SipUtils.FEATURE_GSMA_CS_IMAGE + ";explicit");
			options.addHeader(acceptContactHeader);

	        // Set Accept header
	    	Header acceptHeader = SipUtils.HEADER_FACTORY.createHeader(AcceptHeader.NAME, "application/sdp");
			options.addHeader(acceptHeader);
			
			// Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader(RouteHeader.NAME, route.elementAt(i));
	        	options.addHeader(routeHeader);
	        }
	        
	        // Set the P-Preferred-Identity header
			Header prefHeader = SipUtils.HEADER_FACTORY.createHeader(SipUtils.HEADER_P_PREFERRED_IDENTITY, dialog.getLocalParty());
			options.addHeader(prefHeader);

			// Set User-Agent header
	        options.addHeader(SipUtils.buildUserAgentHeader());
	        
	        // Set Allow header
	        SipUtils.buildAllowHeader(options);
	        
			return new SipRequest(options);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP OPTIONS message");
		}
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
    public static SipRequest createSubscribe(SipDialogPath dialog, int expirePeriod, String accessInfo) throws SipException {
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	        
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallIdHeader(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.SUBSCRIBE);
	
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, dialog.getRemoteTag());

	        // Create the request
	        Request subscribe = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.SUBSCRIBE,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					dialog.getSipStack().getViaHeaders(),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader(RouteHeader.NAME, route.elementAt(i));
	        	subscribe.addHeader(routeHeader);
	        }
	        
	        // Set the Expires header
	        ExpiresHeader expHeader = SipUtils.HEADER_FACTORY.createExpiresHeader(expirePeriod);
	        subscribe.addHeader(expHeader);
	        
	        // Set User-Agent header
	        subscribe.addHeader(SipUtils.buildUserAgentHeader());
	        
	        // Set Contact header
	        subscribe.addHeader(dialog.getSipStack().getContactHeader());

	        // Set the P-Access-Network-Info header
	    	if (accessInfo != null) {
		        Header accessInfoHeader = SipUtils.buildAccessNetworkInfo(accessInfo);
		        subscribe.addHeader(accessInfoHeader);
	    	}
	    	
			return new SipRequest(subscribe);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP SUBSCRIBE message");
		}
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
		try {			
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallIdHeader(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.MESSAGE);
	        
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, dialog.getRemoteTag());
			
	        // Create the request
	        Request message = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.MESSAGE,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					dialog.getSipStack().getViaHeaders(),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader(RouteHeader.NAME, route.elementAt(i));
	        	message.addHeader(routeHeader);
	        }
	                
	        // Set the P-Preferred-Identity header
			Header prefHeader = SipUtils.HEADER_FACTORY.createHeader(SipUtils.HEADER_P_PREFERRED_IDENTITY, dialog.getLocalParty());
			message.addHeader(prefHeader);

			// Set Contact header
	        ContactHeader contact = dialog.getSipStack().getContactHeader();
	        contact.setParameter(SipUtils.FEATURE_OMA_IM, null);
	        message.addHeader(contact);

	        // Set User-Agent header
	        message.addHeader(SipUtils.buildUserAgentHeader());
	
	        // Set Accept-Contact header
	    	Header acceptContactHeader = SipUtils.HEADER_FACTORY.createHeader(SipUtils.HEADER_ACCEPT_CONTACT,	"*;" +
	    			SipUtils.FEATURE_OMA_IM + ";explicit");
			message.addHeader(acceptContactHeader);

			// Set the message content
			ContentTypeHeader contentTypeHeader = SipUtils.HEADER_FACTORY.createContentTypeHeader("text", "plain");
	        message.setContent(text, contentTypeHeader);
	        
	        // Set the message content length
			ContentLengthHeader contentLengthHeader = SipUtils.HEADER_FACTORY.createContentLengthHeader(text.length());
			message.setContentLength(contentLengthHeader);
			
			return new SipRequest(message);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP MESSAGE message");
		}
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
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	        
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallIdHeader(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.PUBLISH);
	
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, dialog.getRemoteTag());
	
	        // Create the request
	        Request publish = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.PUBLISH,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					dialog.getSipStack().getViaHeaders(),
					SipUtils.buildMaxForwardsHeader());       
	        
	        // Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader(RouteHeader.NAME, route.elementAt(i));
	        	publish.addHeader(routeHeader);
	        }
	        
	        // Set the Expires header
	        ExpiresHeader expHeader = SipUtils.HEADER_FACTORY.createExpiresHeader(expirePeriod);
	        publish.addHeader(expHeader);

        	// Set the SIP-If-Match header
	        if (entityTag != null) {
	        	Header sipIfMatchHeader = SipUtils.HEADER_FACTORY.createHeader(SIPIfMatchHeader.NAME, entityTag);
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
	    	publish.addHeader(SipUtils.HEADER_FACTORY.createHeader(EventHeader.NAME, "presence"));
        	
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

    		return new SipRequest(publish);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP PUBLISH message");
		}
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
    public static SipRequest createInvite(SipDialogPath dialog,	int expirePeriod, String sdp) throws SipException {
		try {
			// Create the content type
			ContentTypeHeader contentType = SipUtils.HEADER_FACTORY.createContentTypeHeader("application", "sdp");
			
	        // Create the request
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
			// Create the content type
			ContentTypeHeader contentType = SipUtils.HEADER_FACTORY.createContentTypeHeader("multipart", "mixed");
			contentType.setParameter("boundary", boundary);
			
	        // Create the request
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
		try {
	        // Set request line header
	        URI requestURI = SipUtils.ADDR_FACTORY.createURI(dialog.getTarget());
	        
	        // Set Call-Id header
	        CallIdHeader callIdHeader = SipUtils.HEADER_FACTORY.createCallIdHeader(dialog.getCallId()); 
	        
	        // Set the CSeq header
	        CSeqHeader cseqHeader = SipUtils.HEADER_FACTORY.createCSeqHeader(dialog.getCseq(), Request.INVITE);
	
	        // Set the From header
	        Address fromAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getLocalParty());
	        FromHeader fromHeader = SipUtils.HEADER_FACTORY.createFromHeader(fromAddress, dialog.getLocalTag());
	
	        // Set the To header
	        Address toAddress = SipUtils.ADDR_FACTORY.createAddress(dialog.getRemoteParty());
	        ToHeader toHeader = SipUtils.HEADER_FACTORY.createToHeader(toAddress, null);

	        // Create the request
	        Request invite = SipUtils.MSG_FACTORY.createRequest(requestURI,
	                Request.INVITE,
	                callIdHeader,
	                cseqHeader,
					fromHeader,
					toHeader,
					dialog.getSipStack().getViaHeaders(),
					SipUtils.buildMaxForwardsHeader());       

	        // Set Contact header
	        ContactHeader contact = dialog.getSipStack().getContactHeader();
	        invite.addHeader(contact);
	
			// Set the Route header
	        Vector<String> route = dialog.getRoute();
	        for(int i=0; i < route.size(); i++) {
	        	Header routeHeader = SipUtils.HEADER_FACTORY.createHeader(RouteHeader.NAME, route.elementAt(i));
	        	invite.addHeader(routeHeader);
	        }
	        
	        // Set the P-Preferred-Identity header
			Header prefHeader = SipUtils.HEADER_FACTORY.createHeader(SipUtils.HEADER_P_PREFERRED_IDENTITY, dialog.getLocalParty());
			invite.addHeader(prefHeader);

			// Set User-Agent header
	        invite.addHeader(SipUtils.buildUserAgentHeader());
	        
	        // Set Allow header
	        SipUtils.buildAllowHeader(invite);

	        // Set the Supported header
			Header supportedHeader = SipUtils.HEADER_FACTORY.createHeader(SupportedHeader.NAME, "timer");
			invite.addHeader(supportedHeader);
	
			// Set the message content
	        invite.setContent(content, contentType);

	        // Set the content length
			ContentLengthHeader contentLengthHeader = SipUtils.HEADER_FACTORY.createContentLengthHeader(content.length());
			invite.setContentLength(contentLengthHeader);
			
			return new SipRequest(invite);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP INVITE message");
		}
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
		try {
			// Create the response
			Response response = SipUtils.MSG_FACTORY.createResponse(200, (Request)dialog.getInvite().getStackMessage());
	
			// Set the local tag
			ToHeader to = (ToHeader)response.getHeader(ToHeader.NAME);
			to.setTag(dialog.getLocalTag());	
	
	        // Set Contact header
	        ContactHeader contact = dialog.getSipStack().getContactHeader();
	        response.addHeader(contact);
	
			// Set the Server header
			response.addHeader(SipUtils.buildServerHeader());
	
			// Set the Require header
	    	Header requireHeader = SipUtils.HEADER_FACTORY.createHeader(RequireHeader.NAME, "timer");
			response.addHeader(requireHeader);			
			
	        // Set the message content
			ContentTypeHeader contentTypeHeader = SipUtils.HEADER_FACTORY.createContentTypeHeader("application", "sdp");
			response.setContent(sdp, contentTypeHeader);

	        // Set the message content length
			ContentLengthHeader contentLengthHeader = SipUtils.HEADER_FACTORY.createContentLengthHeader(sdp.length());
			response.setContentLength(contentLengthHeader);
			
			SipResponse resp = new SipResponse(response);
			resp.setStackTransaction(dialog.getInvite().getStackTransaction());
			return resp;
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP response");
		}
	}
	
    /**
	 * Create a 200 OK response for OPTIONS request
	 * 
     * @param options SIP options
	 * @param sdp SDP part
	 * @return SIP response
	 * @throws SipException
	 */
	public static SipResponse create200OkOptionsResponse(SipRequest options, String sdp) throws SipException {
		try {
			// Create the response
			Response response = SipUtils.MSG_FACTORY.createResponse(200, (Request)options.getStackMessage());
	
			// Set the local tag
			ToHeader to = (ToHeader)response.getHeader(ToHeader.NAME);
			to.setTag(IdGenerator.getIdentifier());
	
	        // Set Accept-Contact header
	    	Header acceptContactHeader = SipUtils.HEADER_FACTORY.createHeader(SipUtils.HEADER_ACCEPT_CONTACT,	"*;" +
	    				SipUtils.FEATURE_3GPP_CS_VOICE + ";" +
		    			SipUtils.FEATURE_GSMA_CS_IMAGE + ";explicit");
			response.addHeader(acceptContactHeader);
	
			// Set the Server header
			response.addHeader(SipUtils.buildServerHeader());
	
	        // Set the message content
			ContentTypeHeader contentTypeHeader = SipUtils.HEADER_FACTORY.createContentTypeHeader("application", "sdp");
			response.setContent(sdp, contentTypeHeader);
			
	        // Set the message content length
			ContentLengthHeader contentLengthHeader = SipUtils.HEADER_FACTORY.createContentLengthHeader(sdp.length());
			response.setContentLength(contentLengthHeader);

			SipResponse resp = new SipResponse(response);
			resp.setStackTransaction(options.getStackTransaction());
			return resp;
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP response");
		}
	}

	/**
	 * Create a SIP ACK request
	 * 
	 * @param dialog SIP dialog path
	 * @return SIP request
	 * @throws SipException
	 */
	public static SipRequest createAck(SipDialogPath dialog) throws SipException {
		try {
	        long cseq = dialog.getCseq();
	        Request ack = dialog.getInvite().getStackTransaction().getDialog().createAck(cseq);
			return new SipRequest(ack);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP ACK message");
		}
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
		try {
			Response response = SipUtils.MSG_FACTORY.createResponse(code, (Request)request.getStackMessage());
			SipResponse resp = new SipResponse(response);
			resp.setStackTransaction(request.getStackTransaction());
			return resp;
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP response");
		}
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
		try {
			// Create the response
			Response response = SipUtils.MSG_FACTORY.createResponse(code, (Request)request.getStackMessage());
	
			if (localTag != null) {
				// Set the local tag
				ToHeader to = (ToHeader)response.getHeader(ToHeader.NAME);
				to.setTag(localTag);
			}
			
			SipResponse resp = new SipResponse(response);
			resp.setStackTransaction(request.getStackTransaction());
			return resp;
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message: ", e);
			}
			throw new SipException("Can't create SIP response");
		}
	}
	
	/**
	 * Create a SIP BYE request
	 * 
	 * @param dialog SIP dialog path
	 * @return SIP request
	 * @throws SipException
	 */
	public static SipRequest createBye(SipDialogPath dialog) throws SipException {
		try {
	        // Create the request
			Transaction transaction = dialog.getInvite().getStackTransaction();
		    Request bye = transaction.getDialog().createRequest(Request.BYE);
		    
	        return new SipRequest(bye);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP BYE message");
		}
	}	

	/**
	 * Create a SIP CANCEL request
	 * 
	 * @param dialog SIP dialog path
	 * @return SIP request
	 * @throws SipException
	 */
	public static SipRequest createCancel(SipDialogPath dialog) throws SipException {
		try {
	        // Create the request
		    ClientTransaction transaction = (ClientTransaction)dialog.getInvite().getStackTransaction();
		    Request cancel = transaction.createCancel();
		    
			return new SipRequest(cancel);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't create SIP message", e);
			}
			throw new SipException("Can't create SIP BYE message");
		}
	}
}
