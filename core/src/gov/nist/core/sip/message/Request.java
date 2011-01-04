/*******************************************************************************
 * Conditions Of Use
 * 
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 ******************************************************************************/
package gov.nist.core.sip.message;


import gov.nist.core.ParseException;
import gov.nist.core.Separators;
import gov.nist.core.Utils;
import gov.nist.core.sip.address.SipURI;
import gov.nist.core.sip.address.URI;
import gov.nist.core.sip.header.CSeqHeader;
import gov.nist.core.sip.header.CallIdHeader;
import gov.nist.core.sip.header.ContactList;
import gov.nist.core.sip.header.ContentLengthHeader;
import gov.nist.core.sip.header.ContentTypeHeader;
import gov.nist.core.sip.header.FromHeader;
import gov.nist.core.sip.header.Header;
import gov.nist.core.sip.header.MaxForwardsHeader;
import gov.nist.core.sip.header.RecordRouteList;
import gov.nist.core.sip.header.RequestLine;
import gov.nist.core.sip.header.RouteList;
import gov.nist.core.sip.header.ToHeader;
import gov.nist.core.sip.header.ViaHeader;
import gov.nist.core.sip.header.ViaList;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;


/**
 * The SIP Request structure-- this belongs to the parser who fills it up.
 * Acknowledgements: Mark Bednarek made a few fixes to this code. Jeff Keyser
 * added two methods that create responses and generate cancel requests from
 * incoming orignial requests without the additional overhead of encoding and
 * decoding messages.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <a href="{@docRoot}/uncopyright.html">This
 *         code is in the public domain.</a>
 * 
 */
public final class Request extends Message {
	public static final String DEFAULT_USER = "ip";

	public static final int DEFAULT_TTL = 1;

	public static final String DEFAULT_TRANSPORT = "udp";

	public static final String DEFAULT_METHOD = "INVITE";

	public static final String ACK = "ACK";

	public static final String BYE = "BYE";

	public static final String CANCEL = "CANCEL";

	public static final String INVITE = "INVITE";

	public static final String OPTIONS = "OPTIONS";

	public static final String REGISTER = "REGISTER";

	public static final String NOTIFY = "NOTIFY";

	public static final String SUBSCRIBE = "SUBSCRIBE";

	public static final String MESSAGE = "MESSAGE";

	public static final String REFER = "REFER";

	public static final String INFO = "INFO";

	public static final String PRACK = "PRACK";

	public static final String UPDATE = "UPDATE";

	public static final String PUBLISH = "PUBLISH";

	private Object transactionPointer;

	protected RequestLine requestLine;

	/**
	 * Get the Request Line of the Request.
	 * 
	 * @return the request line of the SIP Request.
	 */

	public RequestLine getRequestLine() {
		return requestLine;
	}

	/**
	 * Set the request line of the SIP Request.
	 * 
	 * @param requestLine Request line to set in the SIP Request.
	 */
	public void setRequestLine(RequestLine requestLine) {
		this.requestLine = requestLine;
	}

	/**
	 * Constructor.
	 */
	public Request() {
		super(Message.REQUEST);
	}
	
	/**
	 * Check header for constraints. (1) Invite options and bye requests can
	 * only have SIP URIs in the contact headers. (2) Request must have cseq, to
	 * and from and via headers. (3) Method in request URI must match that in
	 * CSEQ.
	 */
	protected void checkHeaders() throws ParseException {
		String prefix = "Missing Header ";

		/* Check for required headers */

		if (getCSeqHeader() == null) {
			throw new ParseException(prefix + Header.CSEQ, 0);
		}
		if (getToHeader() == null) {
			throw new ParseException(prefix + Header.TO, 0);
		}
		if (getFromHeader() == null) {
			throw new ParseException(prefix + Header.FROM, 0);
		}
		if (getViaHeaders() == null) {
			throw new ParseException(prefix + Header.VIA, 0);
		}

		/*
		 * BUGBUG Need to revisit this check later... for now we just leave this
		 * to the application to catch.
		 */

		if (requestLine != null
				&& requestLine.getMethod() != null
				&& getCSeqHeader().getMethod() != null
				&& compareToIgnoreCase(requestLine.getMethod(), getCSeqHeader()
						.getMethod()) != 0) {
			throw new ParseException(
					"CSEQ method mismatch with  Request-Line ", 0);

		}

	}

	/**
	 * Set the default values in the request URI if necessary.
	 */
	protected void setDefaults() {
		// The request line may be unparseable (set to null by the
		// exception handler.
		if (requestLine == null)
			return;
		String method = requestLine.getMethod();
		// The requestLine may be malformed!
		if (method == null)
			return;
		URI u = requestLine.getUri();
		if (u == null)
			return;
		if (method.compareTo(REGISTER) == 0 || method.compareTo(INVITE) == 0) {
			if (u instanceof SipURI) {
				SipURI sipUri = (SipURI) u;
				sipUri.setUserParam(DEFAULT_USER);
				try {
					sipUri.setTransportParam(DEFAULT_TRANSPORT);
				} catch (ParseException ex) {
				}
			}
		}
	}

	/**
	 * Patch up the request line as necessary.
	 */
	protected void setRequestLineDefaults() {
		String method = requestLine.getMethod();
		if (method == null) {
			CSeqHeader cseq = (CSeqHeader) this.getCSeqHeader();
			if (cseq != null) {
				method = cseq.getMethod();
				requestLine.setMethod(method);
			}
		}
	}

	/**
	 * A conveniance function to access the Request URI.
	 * 
	 * @return the requestURI if it exists.
	 */
	public URI getRequestURI() {
		if (this.requestLine == null)
			return null;
		else
			return this.requestLine.getUri();
	}

	/**
	 * Sets the RequestURI of Request. The Request-URI is a SIP or SIPS URI or a
	 * general URI. It indicates the user or service to which this request is
	 * being addressed. SIP elements MAY support Request-URIs with schemes other
	 * than "sip" and "sips", for example the "tel" URI scheme. SIP elements MAY
	 * translate non-SIP URIs using any mechanism at their disposal, resulting
	 * in SIP URI, SIPS URI, or some other scheme.
	 * 
	 * @param requestURI -
	 *            the new Request URI of this request message
	 */
	public void setRequestURI(URI uri) {
		if (this.requestLine == null) {
			this.requestLine = new RequestLine();
		}
		this.requestLine.setUri((URI) uri);
	}

	/**
	 * Set the method.
	 * 
	 * @param method
	 *            is the method to set.
	 * @throws IllegalArgumentException
	 *             if the method is null
	 */
	public void setMethod(String method) throws IllegalArgumentException {
		if (method == null)
			throw new IllegalArgumentException("null method");
		if (this.requestLine == null) {
			this.requestLine = new RequestLine();
		}
		this.requestLine.setMethod(method);
		if (this.cSeqHeader != null) {
			this.cSeqHeader.setMethod(method);
		}
	}

	/**
	 * Get the method from the request line.
	 * 
	 * @return the method from the request line if the method exits and null if
	 *         the request line or the method does not exist.
	 */
	public String getMethod() {
		if (requestLine == null)
			return null;
		else
			return requestLine.getMethod();
	}

	/**
	 * Encode the SIP Request as a string.
	 * 
	 * @return an encoded String containing the encoded SIP Message.
	 */

	public String encode() {
		String retval;
		if (requestLine != null) {
			this.setRequestLineDefaults();
			retval = requestLine.encode() + super.encode();
		} else
			retval = super.encode();
		return retval;
	}

	/**
	 * ALias for encode above.
	 */
	public String toString() {
		return this.encode();
	}

	/**
	 * Make a clone (deep copy) of this object. You can use this if you want to
	 * modify a request while preserving the original
	 * 
	 * @return a deep copy of this object.
	 */

	public Object clone() {

		Request retval = (Request) super.clone();
		if (this.requestLine != null) {
			retval.requestLine = (RequestLine) this.requestLine.clone();
			retval.setRequestLineDefaults();
		}
		return retval;
	}

	/**
	 * Compare for equality.
	 * 
	 * @param other
	 *            object to compare ourselves with.
	 */
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass()))
			return false;
		Request that = (Request) other;

		boolean retval = requestLine.equals(that.requestLine) && super.equals(other);
		return retval;
	}

	/**
	 * Get a dialog identifier. Generates a string that can be used as a dialog
	 * identifier.
	 * 
	 * @param isServer
	 *            is set to true if this is the UAS and set to false if this is
	 *            the UAC
	 */
	public String getDialogId(boolean isServer) {
		CallIdHeader cid = (CallIdHeader) this.getCallId();
		StringBuffer retval = new StringBuffer(cid.getCallId());
		FromHeader from = (FromHeader) this.getFromHeader();
		ToHeader to = (ToHeader) this.getToHeader();
		if (!isServer) {
			retval.append(Separators.COLON).append(from.getUserAtHostPort());
			if (from.getTag() != null) {
				retval.append(Separators.COLON);
				retval.append(from.getTag());
			}
			retval.append(Separators.COLON).append(to.getUserAtHostPort());
			if (to.getTag() != null) {
				retval.append(Separators.COLON);
				retval.append(to.getTag());
			}
		} else {
			retval.append(Separators.COLON).append(to.getUserAtHostPort());
			if (to.getTag() != null) {
				retval.append(Separators.COLON);
				retval.append(to.getTag());
			}
			retval.append(Separators.COLON).append(from.getUserAtHostPort());
			if (from.getTag() != null) {
				retval.append(Separators.COLON);
				retval.append(from.getTag());
			}
		}
		return retval.toString().toLowerCase();

	}

	/**
	 * Get a dialog id given the remote tag.
	 */
	public String getDialogId(boolean isServer, String toTag) {
		FromHeader from = (FromHeader) this.getFromHeader();
		ToHeader to = (ToHeader) this.getToHeader();
		CallIdHeader cid = (CallIdHeader) this.getCallId();
		StringBuffer retval = new StringBuffer(cid.getCallId());
		if (!isServer) {
			retval.append(Separators.COLON).append(from.getUserAtHostPort());
			if (from.getTag() != null) {
				retval.append(Separators.COLON);
				retval.append(from.getTag());
			}
			retval.append(Separators.COLON).append(to.getUserAtHostPort());
			if (toTag != null) {
				retval.append(Separators.COLON);
				retval.append(toTag);
			}
		} else {
			retval.append(Separators.COLON).append(to.getUserAtHostPort());
			if (toTag != null) {
				retval.append(Separators.COLON);
				retval.append(toTag);
			}
			retval.append(Separators.COLON).append(from.getUserAtHostPort());
			if (from.getTag() != null) {
				retval.append(Separators.COLON);
				retval.append(from.getTag());
			}
		}
		return retval.toString().toLowerCase();
	}

	/**
	 * Encode this into a byte array. This is used when the body has been set as
	 * a binary array and you want to encode the body as a byte array for
	 * transmission.
	 * 
	 * @return a byte array containing the Request encoded as a byte array.
	 */

	public byte[] encodeAsBytes() {
		byte[] rlbytes = null;
		if (requestLine != null) {
			try {
				rlbytes = requestLine.encode().getBytes("UTF-8");
			} catch (UnsupportedEncodingException ex) {
			}
		}
		byte[] superbytes = super.encodeAsBytes();
		byte[] retval = new byte[rlbytes.length + superbytes.length];
		System.arraycopy(rlbytes, 0, retval, 0, rlbytes.length);
		System.arraycopy(superbytes, 0, retval, rlbytes.length,
				superbytes.length);
		return retval;
	}

	/**
	 * Creates a default Response message for this request. Note You must add
	 * the necessary tags to outgoing responses if need be. For efficiency, this
	 * method does not clone the incoming request. If you want to modify the
	 * outgoing response, be sure to clone the incoming request as the headers
	 * are shared and any modification to the headers of the outgoing response
	 * will result in a modification of the incoming request. Tag fields are
	 * just copied from the incoming request. Contact headers are removed from
	 * the incoming request. Added by Jeff Keyser.
	 * 
	 * @param statusCode
	 *            Status code for the response. Reason phrase is generated.
	 * 
	 * @return A Response with the status and reason supplied, and a copy of all
	 *         the original headers from this request.
	 */

	public Response createResponse(int statusCode) {

		String reasonPhrase = Response.getReasonPhrase(statusCode);
		return this.createResponse(statusCode, reasonPhrase);

	}

	/**
	 * Creates a default Response message for this request. Note You must add
	 * the necessary tags to outgoing responses if need be. For efficiency, this
	 * method does not clone the incoming request. If you want to modify the
	 * outgoing response, be sure to clone the incoming request as the headers
	 * are shared and any modification to the headers of the outgoing response
	 * will result in a modification of the incoming request. Tag fields are
	 * just copied from the incoming request. Contact headers are removed from
	 * the incoming request. Added by Jeff Keyser. Route headers are not added
	 * to the response.
	 * 
	 * @param statusCode
	 *            Status code for the response.
	 * @param reasonPhrase
	 *            Reason phrase for this response.
	 * 
	 * @return A Response with the status and reason supplied
	 */

	public Response createResponse(int statusCode, String reasonPhrase)
			throws IllegalArgumentException {
		Response newResponse;
		Enumeration headerIterator;
		Header nextHeader;

		newResponse = new Response();
		try {
			newResponse.setStatusCode(statusCode);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad code " + statusCode);
		}
		if (reasonPhrase != null)
			newResponse.setReasonPhrase(reasonPhrase);
		else
			newResponse.setReasonPhrase(Response.getReasonPhrase(statusCode));
		headerIterator = super.getHeaders();
		
		while (headerIterator.hasMoreElements()) {
			nextHeader = (Header) headerIterator.nextElement();
			if (nextHeader instanceof FromHeader
					|| nextHeader instanceof ToHeader
					|| nextHeader instanceof ViaList
					|| nextHeader instanceof CallIdHeader
					|| nextHeader instanceof RecordRouteList					
					|| Utils.equalsIgnoreCase(nextHeader.getName(),	"Time-Stamp")) {
				newResponse.attachHeader(nextHeader, false);
			}
		}
		
		if (getCSeqHeader() != null) {
			newResponse.attachHeader(getCSeqHeader(), false);			
		}
		
		return newResponse;
	}

	/**
	 * Creates a default SIPResquest message that would cancel this request.
	 * Note that tag assignment and removal of is left to the caller (we use
	 * whatever tags are present in the original request). Acknowledgement:
	 * Added by Jeff Keyser.
	 * 
	 * @return A CANCEL Request with a copy all the original headers from this
	 *         request except for Require, ProxyRequire.
	 */

	public Request createCancelRequest() {
		Request newRequest;
		Enumeration headerIterator;
		Header nextHeader;

		newRequest = new Request();
		newRequest.setRequestLine((RequestLine) this.requestLine.clone());
		newRequest.setMethod(CANCEL);
		headerIterator = getHeaders();
		while (headerIterator.hasMoreElements()) {
			nextHeader = (Header) headerIterator.nextElement();
			if (nextHeader.getHeaderName().equals("Require"))
				continue;
			if (nextHeader.getHeaderName().equals("Proxy-Require"))
				continue;

			if (nextHeader instanceof ViaList) {
				nextHeader = (ViaList) ((ViaList) nextHeader).clone();
			}

			// CSeqHeader method for a cancel request must be cancel.
			if (nextHeader instanceof CSeqHeader) {
				CSeqHeader cseq = (CSeqHeader) nextHeader.clone();
				cseq.setMethod(CANCEL);
				nextHeader = cseq;
			}

			newRequest.attachHeader(nextHeader, false);

		}
		return newRequest;
	}

	/**
	 * Creates a default ACK Request message for this original request. Note
	 * that the defaultACK Request does not include the content of the original
	 * Request. If responseToHeader is null then the toHeader of this request is
	 * used to construct the ACK. Note that tag fields are just copied from the
	 * original SIP Request. Added by Jeff Keyser.
	 * 
	 * @param responseToHeader
	 *            To header to use for this request.
	 * 
	 * @return A Request with an ACK method.
	 */
	public Request createAckRequest(ToHeader responseToHeader) {
		Request newRequest;
		Enumeration headerIterator;
		Header nextHeader;

		newRequest = new Request();
		newRequest.setRequestLine((RequestLine) this.requestLine.clone());
		newRequest.setMethod(ACK);
		headerIterator = getHeaders();
		while (headerIterator.hasMoreElements()) {
			nextHeader = (Header) headerIterator.nextElement();
			if (nextHeader.getHeaderName().equals(Header.ROUTE)) {

				// Route header for ACK is assigned by the
				// Dialog if necessary.
				continue;
			} else if (nextHeader.getHeaderName().equals(
					Header.PROXY_AUTHORIZATION)) {
				// Remove proxy auth header.
				// Assigned by the Dialog if necessary.
				continue;
			} else if (nextHeader instanceof ContentLengthHeader) {
				// Adding content is responsibility of user.
				nextHeader = (Header) nextHeader.clone();
				((ContentLengthHeader) nextHeader).setContentLength(0);

			} else if (nextHeader instanceof ContentTypeHeader) {
				// Content type header is removed since
				// content length is 0. Bug fix from
				// Antonis Kyardas.
				continue;
			} else if (nextHeader instanceof CSeqHeader) {
				CSeqHeader cseq = (CSeqHeader) nextHeader.clone();
				cseq.setMethod(ACK);
				nextHeader = cseq;
			} else if (nextHeader instanceof ToHeader) {
				if (responseToHeader != null) {
					nextHeader = responseToHeader;
				} else {
					nextHeader = (Header) nextHeader.clone();
				}
			} else {
				nextHeader = (Header) nextHeader.clone();
			}

			newRequest.attachHeader(nextHeader, false);

		}
		return newRequest;
	}

	/**
	 * Create a new default Request from the original request. Warning: the
	 * newly created Request, shares the headers of this request but we generate
	 * any new headers that we need to modify so the original request is
	 * umodified. However, if you modify the shared headers after this request
	 * is created, then the newly created request will also be modified. If you
	 * want to modify the original request without affecting the returned
	 * Request make sure you clone it before calling this method. Following are
	 * the differences between the original request headers and the generated
	 * request headers.
	 * <ul>
	 * <li> Contact headers are not included in the newly created request.
	 * Setting the appropriate sequence number is the responsibility of the
	 * caller. </li>
	 * <li> RouteList is not copied for ACK and CANCEL </li>
	 * <li> Note that we DO NOT copy the body of the argument into the returned
	 * header. We do not copy the content type header from the original request
	 * either. These have to be added seperately and the content length has to
	 * be correctly set if necessary the content length is set to 0 in the
	 * returned header. </li>
	 * <li>Contact List is not copied from the original request.</li>
	 * <li>RecordRoute List is not included from original request. </li>
	 * <li>Via header is not included from the original request. </li>
	 * </ul>
	 * 
	 * @param requestLine
	 *            is the new request line.
	 * 
	 * @param switchHeaders
	 *            is a boolean flag that causes to and from headers to switch
	 *            (set this to true if you are the server of the transaction and
	 *            are generating a BYE request). If the headers are switched, we
	 *            generate new FromHeader and To headers otherwise we just use
	 *            the incoming headers.
	 * 
	 * @return a new Default SIP Request which has the requestLine specified.
	 * 
	 */
	public Request createRequest(RequestLine requestLine, boolean switchHeaders) {
		Request newRequest = new Request();
		newRequest.requestLine = requestLine;
		Enumeration headerIterator = this.getHeaders();
		while (headerIterator.hasMoreElements()) {
			Header nextHeader = (Header) headerIterator.nextElement();
			// For BYE and cancel set the CSeqHeader header to the
			// appropriate method.
			if (nextHeader instanceof CSeqHeader) {
				CSeqHeader newCseq = (CSeqHeader) nextHeader.clone();
				nextHeader = newCseq;
				newCseq.setMethod(requestLine.getMethod());
			} else if (requestLine.getMethod().equals(ACK)
					&& nextHeader instanceof ContactList) {
				// ACKS never get Contact headers.
				continue;
			} else if (nextHeader instanceof ViaList) {
				ViaHeader via = (ViaHeader) (((ViaList) nextHeader).getFirst()
						.clone());
				via.removeParameter("branch");
				nextHeader = via;
				// Cancel and ACK preserve the branch ID.
			} else if (nextHeader instanceof RouteList) {
				continue; // Route is kept by dialog.
			} else if (nextHeader instanceof RecordRouteList) {
				continue; // RR is added by the caller.
			} else if (nextHeader instanceof ContactList) {
				continue;
			} else if (nextHeader instanceof ToHeader) {
				ToHeader to = (ToHeader) nextHeader;
				if (switchHeaders) {
					nextHeader = new FromHeader(to);
					((FromHeader) nextHeader).removeTag();
				} else {
					nextHeader = (Header) to.clone();
					((ToHeader) nextHeader).removeTag();
				}
			} else if (nextHeader instanceof FromHeader) {
				FromHeader from = (FromHeader) nextHeader;
				if (switchHeaders) {
					nextHeader = new ToHeader(from);
					((ToHeader) nextHeader).removeTag();
				} else {
					nextHeader = (Header) from.clone();
					((FromHeader) nextHeader).removeTag();
				}
			} else if (nextHeader instanceof ContentLengthHeader) {
				ContentLengthHeader cl = (ContentLengthHeader) nextHeader
						.clone();
				cl.setContentLength(0);
				nextHeader = cl;
			} else if (nextHeader instanceof ContentTypeHeader) {
				continue;
			} else if (nextHeader instanceof MaxForwardsHeader) {
				// Header is regenerated if the request is to be switched
				if (switchHeaders) {
					MaxForwardsHeader mf = (MaxForwardsHeader) nextHeader
							.clone();
					mf.setMaxForwards(70);
					nextHeader = mf;
				}
			} else if (!(nextHeader instanceof CallIdHeader)) {
				// Route is kept by dialog.
				// RR is added by the caller.
				// Contact is added by the Caller
				// Any extension headers must be added
				// by the caller.
				continue;
			}

			newRequest.attachHeader(nextHeader, false);

		}
		return newRequest;

	}

	/**
	 * Create a BYE request from this request.
	 * 
	 * @param switchHeaders
	 *            is a boolean flag that causes from and isServerTransaction to
	 *            headers to be swapped. Set this to true if you are the server
	 *            of the dialog and are generating a BYE request for the dialog.
	 * @return a new default BYE request.
	 */
	public Request createBYERequest(boolean switchHeaders) {
		RequestLine requestLine = (RequestLine) this.requestLine.clone();
		requestLine.setMethod("BYE");
		return this.createRequest(requestLine, switchHeaders);
	}

	/**
	 * Create an ACK request from this request. This is suitable for generating
	 * an ACK for an INVITE client transaction.
	 * 
	 * @return an ACK request that is generated from this request.
	 * 
	 */
	public Request createACKRequest() {
		RequestLine requestLine = (RequestLine) this.requestLine.clone();
		requestLine.setMethod(Request.ACK);
		return this.createRequest(requestLine, false);
	}

	/**
	 * Get the host from the topmost via header.
	 * 
	 * @return the string representation of the host from the topmost via
	 *         header.
	 */
	public String getViaHost() {
		ViaHeader via = (ViaHeader) this.getViaHeaders().getFirst();
		return via.getHost();

	}

	/**
	 * Get the port from the topmost via header.
	 * 
	 * @return the port from the topmost via header (5060 if there is no port
	 *         indicated).
	 */
	public int getViaPort() {
		ViaHeader via = (ViaHeader) this.getViaHeaders().getFirst();
		if (via.hasPort())
			return via.getPort();
		else
			return 5060;
	}

	/**
	 * Get the first line encoded.
	 * 
	 * @return a string containing the encoded request line.
	 */
	public String getFirstLine() {
		if (requestLine == null)
			return null;
		else
			return this.requestLine.encode();
	}

	/**
	 * set the sip version.
	 * 
	 * @param sipVerison --
	 *            the sip version to set.
	 */

	public void setSIPVersion(String sipVersion) throws ParseException {
		if (sipVersion == null || !sipVersion.equals("SIP/2.0"))
			throw new ParseException("sipVersion", 0);
		this.requestLine.setSIPVersion(sipVersion);
	}

	/**
	 * Get the SIP version.
	 * 
	 * @return the SIP version from the request line.
	 */
	public String getSIPVersion() {
		return this.requestLine.getSipVersion();
	}

	public Object getTransaction() {
		// Return an opaque pointer to the transaction object.
		// This is for consistency checking and quick lookup.
		return this.transactionPointer;
	}

	public void setTransaction(Object transaction) {
		this.transactionPointer = transaction;
	}

}
