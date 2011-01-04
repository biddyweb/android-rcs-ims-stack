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

import gov.nist.core.NameValueList;

/**
 * ContentLengthHeader Header (of which there can be only one in a SIPMessage).
 * 
 * <pre>
 * Fielding, et al.            Standards Track                   [Page 119]
 * RFC 2616                        HTTP/1.1                       June 1999
 * 
 * 
 *       14.13 Content-Length
 * 
 *    The Content-Length entity-header field indicates the size of the
 *    entity-body, in decimal number of OCTETs, sent to the recipient or,
 *    in the case of the HEAD method, the size of the entity-body that
 *    would have been sent had the request been a GET.
 * 
 *        Content-Length    = &quot;Content-Length&quot; &quot;:&quot; 1*DIGIT
 * 
 *    An example is
 * 
 *        Content-Length: 3495
 * 
 *    Applications SHOULD use this field to indicate the transfer-length of
 *    the message-body, unless this is prohibited by the rules in section
 *    4.4.
 * 
 *    Any Content-Length greater than or equal to zero is a valid value.
 *    Section 4.4 describes how to determine the length of a message-body
 *    if a Content-Length is not given.
 * 
 *    Note that the meaning of this field is significantly different from
 *    the corresponding definition in MIME, where it is an optional field
 *    used within the &quot;message/external-body&quot; content-type. In HTTP, it
 *    SHOULD be sent whenever the message's length can be determined prior
 *    to being transferred, unless this is prohibited by the rules in
 *    section 4.4.
 * </pre>
 * 
 * @author M. Ranganathan <mranga@nist.gov>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 */
public class ContentLengthHeader extends Header {

	/**
	 * contentLength field.
	 */
	protected Integer contentLength;

	public static final String NAME = Header.CONTENT_LENGTH;

	protected static Class clazz;

	static {
		clazz = new ContentLengthHeader().getClass();
	}

	/**
	 * Default constructor.
	 */
	public ContentLengthHeader() {
		super(CONTENT_LENGTH);
	}

	/**
	 * Constructor given a length.
	 */
	public ContentLengthHeader(int length) {
		super(CONTENT_LENGTH);
		this.contentLength = new Integer(length);
		this.headerValue = String.valueOf(contentLength.intValue());
	}

	/**
	 * get the ContentLengthHeader field.
	 * 
	 * @return int
	 */
	public int getContentLength() {
		return contentLength.intValue();
	}

	/**
	 * Set the contentLength member
	 * 
	 * @param contentLength
	 *            int to set
	 */
	public void setContentLength(int contentLength)
			throws IllegalArgumentException {
		if (contentLength < 0)
			throw new IllegalArgumentException("parameter is <0");
		this.contentLength = new Integer(contentLength);
		this.headerValue = String.valueOf(contentLength);
	}

	/**
	 * Encode into a canonical string.
	 * 
	 * @return String
	 */
	public String encodeBody() {
		if (contentLength == null)
			return "0";
		else
			return contentLength.toString();
	}

	public Object clone() {
		ContentLengthHeader retval = new ContentLengthHeader();
		if (contentLength != null)
			retval.contentLength = new Integer(contentLength.intValue());
		return retval;
	}

	public Object getValue() {
		return this.contentLength;
	}

	/**
	 * Get the parameters for the header as a nameValue list.
	 */
	public NameValueList getParameters() {
		return null;
	}

}
