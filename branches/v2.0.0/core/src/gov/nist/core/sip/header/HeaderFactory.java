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
package gov.nist.core.sip.header;

import gov.nist.core.ParseException;
import gov.nist.core.sip.address.Address;
import gov.nist.core.sip.parser.StringMsgParser;

import java.util.Calendar;
import java.util.Vector;


/**
 * Implementation of the JAIN SIP HeaderFactory
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov>
 * @author Olivier Deruelle <deruelle@nist.gov> <a href="{@docRoot}/uncopyright.html">This
 *         code is in the public domain.</a>
 * 
 */
public class HeaderFactory {

	/**
	 * Creates a new AuthorizationHeader based on the newly supplied scheme
	 * value.
	 * 
	 * @param scheme -
	 *            the new string value of the scheme.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the scheme value.
	 * @return the newly created AuthorizationHeader object.
	 */
	public AuthorizationHeader createAuthorizationHeader(String scheme)
			throws ParseException {
		if (scheme == null)
			throw new NullPointerException("null arg scheme ");
		AuthorizationHeader auth = new AuthorizationHeader();
		auth.setScheme(scheme);

		return auth;
	}

	/**
	 * Creates a new CSeqHeader based on the newly supplied sequence number and
	 * method values.
	 * 
	 * @param sequenceNumber -
	 *            the new integer value of the sequence number.
	 * @param method -
	 *            the new string value of the method.
	 * @throws IllegalArgumentException
	 *             if supplied sequence number is less than zero.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the method value.
	 * @return the newly created CSeqHeader object.
	 */
	public CSeqHeader createCSeqHeader(int sequenceNumber, String method)
			throws ParseException, IllegalArgumentException {
		if (sequenceNumber < 0)
			throw new IllegalArgumentException("bad arg " + sequenceNumber);
		if (method == null)
			throw new NullPointerException("null arg method");
		CSeqHeader cseq = new CSeqHeader();
		cseq.setMethod(method);
		cseq.setSequenceNumber(sequenceNumber);

		return cseq;
	}

	/**
	 * Creates a new CallIdHeader based on the newly supplied callId value.
	 * 
	 * @param callId -
	 *            the new string value of the call-id.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the callId value.
	 * @return the newly created CallIdHeader object.
	 */
	public CallIdHeader createCallId(String callId) throws ParseException {
		if (callId == null)
			throw new NullPointerException("null arg callId");
		CallIdHeader c = new CallIdHeader();
		c.setCallId(callId);
		return c;
	}

	/**
	 * Creates a new ContactHeader based on the newly supplied address value.
	 * 
	 * @param address -
	 *            the new Address value of the address.
	 * @return the newly created ContactHeader object.
	 */
	public ContactHeader createContactHeader(Address address) {
		if (address == null)
			throw new NullPointerException("null arg address");
		ContactHeader contact = new ContactHeader();
		contact.setAddress(address);

		return contact;
	}

	/**
	 * Creates a new wildcard ContactHeader. This is used in Register requests
	 * to indicate to the server that it should remove all locations the at
	 * which the user is currently available. This implies that the following
	 * conditions are met:
	 * <ul>
	 * <li><code>ContactHeader.getAddress.getAddress.getUserInfo() == *;</code>
	 * <li><code>ContactHeader.getAddress.getAddress.isWildCard() == true;</code>
	 * <li><code>ContactHeader.getExpiresHeader() == 0;</code>
	 * </ul>
	 * 
	 * @return the newly created wildcard ContactHeader.
	 */
	public ContactHeader createContactHeader() {
		ContactHeader contact = new ContactHeader();
		contact.setWildCardFlag(true);
		contact.setExpires(0);

		return contact;
	}

	/**
	 * Creates a new CSeqHeader based on the newly supplied contentLength value.
	 * 
	 * @param contentLength -
	 *            the new integer value of the contentLength.
	 * @throws IllegalArgumentException
	 *             if supplied contentLength is less than zero.
	 * @return the newly created ContentLengthHeader object.
	 */
	public ContentLengthHeader createContentLengthHeader(int contentLength)
			throws IllegalArgumentException {
		if (contentLength < 0)
			throw new IllegalArgumentException("bad contentLength");
		ContentLengthHeader c = new ContentLengthHeader();
		c.setContentLength(contentLength);

		return c;
	}

	/**
	 * Creates a new ContentTypeHeader based on the newly supplied contentType
	 * and contentSubType values.
	 * 
	 * @param type -
	 *            the new string content type value.
	 * @param subType -
	 *            the new string content sub-type value.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the content type or content subtype value.
	 * @return the newly created ContentTypeHeader object.
	 */
	public ContentTypeHeader createContentTypeHeader(String contentType,
			String contentSubType) throws ParseException {
		if (contentType == null || contentSubType == null)
			throw new NullPointerException("null contentType or subType");
		ContentTypeHeader c = new ContentTypeHeader();
		c.setContentType(contentType);
		c.setContentSubType(contentSubType);
		return c;
	}

	/**
	 * Creates a new DateHeader based on the newly supplied date value.
	 * 
	 * @param date -
	 *            the new Calender value of the date.
	 * @return the newly created DateHeader object.
	 */
	public DateHeader createDateHeader(Calendar date) {
		DateHeader d = new DateHeader();
		if (date == null)
			throw new NullPointerException("null date");
		d.setDate(date);

		return d;
	}

	/**
	 * Creates a new EventHeader based on the newly supplied eventType value.
	 * 
	 * @param eventType -
	 *            the new string value of the eventType.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the eventType value.
	 * @return the newly created EventHeader object.
	 * @since v1.1
	 */
	public EventHeader createEventHeader(String eventType)
			throws ParseException {
		if (eventType == null)
			throw new NullPointerException("null eventType");
		EventHeader event = new EventHeader();
		event.setEventType(eventType);

		return event;
	}

	/**
	 * Creates a new ExpiresHeader based on the newly supplied expires value.
	 * 
	 * @param expires -
	 *            the new integer value of the expires.
	 * @throws IllegalArgumentException
	 *             if supplied expires is less than zero.
	 * @return the newly created ExpiresHeader object.
	 */
	public ExpiresHeader createExpiresHeader(int expires)
			throws IllegalArgumentException {
		if (expires < 0)
			throw new IllegalArgumentException("bad value " + expires);
		ExpiresHeader e = new ExpiresHeader();
		e.setExpires(expires);

		return e;
	}

	/**
	 * Creates a new ExtensionHeader based on the newly supplied name and value
	 * values.
	 * 
	 * @param name -
	 *            the new string name of the ExtensionHeader value.
	 * @param value -
	 *            the new string value of the ExtensionHeader.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the name or value values.
	 * @return the newly created ExtensionHeader object.
	 */
	public ExtensionHeader createExtensionHeader(String name, String value)
			throws ParseException {
		if (name == null)
			throw new NullPointerException("bad name");

		ExtensionHeader ext = new ExtensionHeader();
		ext.setHeaderName(name);
		ext.setValue(value);

		return ext;
	}

	/**
	 * Creates a new FromHeader based on the newly supplied address and tag
	 * values.
	 * 
	 * @param address -
	 *            the new Address object of the address.
	 * @param tag -
	 *            the new string value of the tag.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the tag value.
	 * @return the newly created FromHeader object.
	 */
	public FromHeader createFromHeader(Address address, String tag)
			throws ParseException {
		if (address == null)
			throw new NullPointerException("null address arg");
		FromHeader from = new FromHeader();
		from.setAddress(address);
		if (tag != null)
			from.setTag(tag);

		return from;
	}

	/**
	 * Creates a new MaxForwardsHeader based on the newly supplied maxForwards
	 * value.
	 * 
	 * @param expires -
	 *            the new integer value of the maxForwards.
	 * @throws IllegalArgumentException
	 *             if supplied maxForwards is less than zero or greater than
	 *             255.
	 * @return the newly created MaxForwardsHeader object.
	 */
	public MaxForwardsHeader createMaxForwardsHeader(int maxForwards)
			throws IllegalArgumentException {
		if (maxForwards < 0 || maxForwards > 255)
			throw new IllegalArgumentException("bad maxForwards arg "
					+ maxForwards);
		MaxForwardsHeader m = new MaxForwardsHeader();
		m.setMaxForwards(maxForwards);

		return m;
	}

	/**
	 * Creates a new ProxyAuthenticateHeader based on the newly supplied scheme
	 * value.
	 * 
	 * @param scheme -
	 *            the new string value of the scheme.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the scheme value.
	 * @return the newly created ProxyAuthenticateHeader object.
	 */
	public ProxyAuthenticateHeader createProxyAuthenticateHeader(String scheme)
			throws ParseException {
		if (scheme == null)
			throw new NullPointerException("bad scheme arg");
		ProxyAuthenticateHeader p = new ProxyAuthenticateHeader();
		p.setScheme(scheme);

		return p;
	}

	/**
	 * Creates a new ProxyAuthorizationHeader based on the newly supplied scheme
	 * value.
	 * 
	 * @param scheme -
	 *            the new string value of the scheme.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the scheme value.
	 * @return the newly created ProxyAuthorizationHeader object.
	 */
	public ProxyAuthorizationHeader createProxyAuthorizationHeader(String scheme)
			throws ParseException {
		if (scheme == null)
			throw new NullPointerException("bad scheme arg");
		ProxyAuthorizationHeader p = new ProxyAuthorizationHeader();
		p.setScheme(scheme);

		return p;
	}

	/**
	 * Creates a new RecordRouteHeader based on the newly supplied address
	 * value.
	 * 
	 * @param address -
	 *            the new Address object of the address.
	 * @return the newly created RecordRouteHeader object.
	 */
	public RecordRouteHeader createRecordRouteHeader(Address address) {
		RecordRouteHeader recordRouteHeader = new RecordRouteHeader();
		recordRouteHeader.setAddress(address);

		return recordRouteHeader;
	}

	/**
	 * Creates a new RouteHeader based on the newly supplied address value.
	 * 
	 * @param address -
	 *            the new Address object of the address.
	 * @return the newly created RouteHeader object.
	 */
	public RouteHeader createRouteHeader(Address address) {
		if (address == null)
			throw new NullPointerException("null address arg");
		RouteHeader route = new RouteHeader();
		route.setAddress(address);

		return route;
	}

	/**
	 * Creates a new ToHeader based on the newly supplied address and tag
	 * values.
	 * 
	 * @param address -
	 *            the new Address object of the address.
	 * @param tag -
	 *            the new string value of the tag.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the tag value.
	 * @return the newly created ToHeader object.
	 */
	public ToHeader createToHeader(Address address, String tag)
			throws ParseException {
		if (address == null)
			throw new NullPointerException("null address");
		ToHeader to = new ToHeader();
		to.setAddress(address);
		if (tag != null)
			to.setTag(tag);

		return to;
	}

	/**
	 * Creates a new ViaHeader based on the newly supplied uri and branch
	 * values.
	 * 
	 * @param uri -
	 *            the new URI object of the uri.
	 * @param branch -
	 *            the new string value of the branch.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the branch value.
	 * @return the newly created ViaHeader object.
	 */
	public ViaHeader createViaHeader(String host, int port, String transport,
			String branch) throws ParseException {
		// This should be changed.
		if (host == null || transport == null)
			throw new NullPointerException("null arg");
		ViaHeader via = new ViaHeader();
		if (branch != null)
			via.setBranch(branch);
		via.setHost(host);
		via.setPort(port);
		via.setTransport(transport);

		return via;
	}

	/**
	 * Creates a new WWWAuthenticateHeader based on the newly supplied scheme
	 * value.
	 * 
	 * @param scheme -
	 *            the new string value of the scheme.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the scheme values.
	 * @return the newly created WWWAuthenticateHeader object.
	 */
	public WWWAuthenticateHeader createWWWAuthenticateHeader(String scheme)
			throws ParseException {
		if (scheme == null)
			throw new NullPointerException("null scheme");
		WWWAuthenticateHeader www = new WWWAuthenticateHeader();
		www.setScheme(scheme);

		return www;
	}

	/**
	 * Create and parse a header.
	 * 
	 * @param headerName --
	 *            header name for the header to parse.
	 * @param headerValue --
	 *            header value for the header to parse.
	 * @throws ParseException
	 * @return the parsed sip header
	 */
	public Header createHeader(String headerName, String headerValue)
			throws ParseException {
		if (headerName == null)
			throw new NullPointerException("header name is null");
		String hdrText = new StringBuffer().append(headerName).append(":")
				.append(headerValue).toString();
		Class clazz = NameMap.getClassFromName(headerName);
		if (clazz == null) {
			ExtensionHeader retval = new ExtensionHeader();
			retval.setHeaderName(headerName);
			retval.setValue(headerValue);
			return retval;
		}

		try {
			if (headerValue == null) {
				Header retval = (Header) clazz.newInstance();
				retval.setHeaderName(headerName);
			}
		} catch (Exception ex) {
			return null;
		}

		StringMsgParser smp = new StringMsgParser();
		Header sipHeader = smp.parseHeader(hdrText);
		if (sipHeader instanceof HeaderList) {
			if (((HeaderList) sipHeader).size() > 1)
				throw new ParseException("Only signleton allowed !", 0);
			else
				return (Header) ((HeaderList) sipHeader).first();
		} else {
			return sipHeader;
		}
	}

	/**
	 * Create and return a list of headers.
	 * 
	 * @param headers --
	 *            list of headers.
	 * @throws ParseException --
	 *             if a parse exception occurs or a List of that type of header
	 *             is not alowed.
	 * @return a List containing the headers.
	 */
	public Vector createHeaders(String headers) throws ParseException {
		if (headers == null)
			throw new NullPointerException("null arg!");
		StringMsgParser smp = new StringMsgParser();
		Header shdr = smp.parseHeader(headers);
		if (shdr instanceof HeaderList)
			return ((HeaderList) shdr).getHeaders();
		else
			throw new ParseException(
					"List of headers of this type is not allowed in a message",
					0);
	}

	/**
	 * Default constructor.
	 */
	public HeaderFactory() {

	}

}
