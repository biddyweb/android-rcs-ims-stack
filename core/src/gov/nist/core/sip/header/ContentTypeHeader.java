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
import gov.nist.core.Separators;

/**
 * ContentTypeHeader SIP Header
 * 
 * <pre>
 * 14.17 Content-Type
 *  
 *    The Content-Type entity-header field indicates the media type of the
 *    entity-body sent to the recipient or, in the case of the HEAD method,
 *    the media type that would have been sent had the request been a GET.
 *  
 *    Content-Type   = &quot;Content-Type&quot; &quot;:&quot; media-type
 *  
 *    Media types are defined in section 3.7. An example of the field is
 *  
 *        Content-Type: text/html; charset=ISO-8859-4
 *  
 *    Further discussion of methods for identifying the media type of an
 *    entity is provided in section 7.2.1.   
 * 
 *  FromHeader  HTTP RFC 2616
 * </pre>
 * 
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov>
 * @author Olivier Deruelle <deruelle@nist.gov> <a href="{@docRoot}/uncopyright.html">This
 *         code is in the public domain.</a>
 * 
 */
public class ContentTypeHeader extends ParametersHeader {

	/**
	 * mediaRange field.
	 */
	protected MediaRange mediaRange;

	public static Class clazz;

	public static final String NAME = Header.CONTENT_TYPE;

	static {
		clazz = new ContentTypeHeader().getClass();
	}

	/**
	 * Default constructor.
	 */
	public ContentTypeHeader() {
		super(CONTENT_TYPE);
	}

	/**
	 * Constructor given a content type and subtype.
	 * 
	 * @param contentType
	 *            is the content type.
	 * @param contentSubtype
	 *            is the content subtype
	 */
	public ContentTypeHeader(String contentType, String contentSubtype) {
		this();
		this.setContentType(contentType, contentSubtype);
	}

	/**
	 * compare two MediaRange headers.
	 * 
	 * @param media
	 *            String to set
	 * @return int.
	 */
	public int compareMediaRange(String media) {
		return compareToIgnoreCase(mediaRange.type + "/" + mediaRange.subtype,
				media);
	}

	/**
	 * Encode into a canonical string.
	 * 
	 * @return String.
	 */
	public String encodeBody() {
		if (hasParameters())
			return new StringBuffer(mediaRange.encode()).append(
					Separators.SEMICOLON).append(parameters.encode())
					.toString();
		else
			return mediaRange.encode();
	}

	/**
	 * get the mediaRange field.
	 * 
	 * @return MediaRange.
	 */
	public MediaRange getMediaRange() {
		return mediaRange;
	}

	/**
	 * get the Media Type.
	 * 
	 * @return String.
	 */
	public String getMediaType() {
		return mediaRange.type;
	}

	/**
	 * get the MediaSubType field.
	 * 
	 * @return String.
	 */
	public String getMediaSubType() {
		return mediaRange.subtype;
	}

	/**
	 * Get the content subtype.
	 * 
	 * @return the content subtype string (or null if not set).
	 */
	public String getContentSubType() {
		return mediaRange == null ? null : mediaRange.getSubtype();
	}

	/**
	 * Get the content subtype.
	 * 
	 * @return the content tyep string (or null if not set).
	 */

	public String getContentTypeHeader() {
		return mediaRange == null ? null : mediaRange.getType();
	}

	/**
	 * Get the charset parameter.
	 */
	public String getCharset() {
		return this.getParameter("charset");
	}

	/**
	 * Set the mediaRange member
	 * 
	 * @param m
	 *            mediaRange field.
	 */
	public void setMediaRange(MediaRange m) {
		mediaRange = m;
	}

	/**
	 * set the content type and subtype.
	 * 
	 * @param contentType
	 *            Content type string.
	 * @param contentSubType
	 *            content subtype string
	 */
	public void setContentType(String contentType, String contentSubType) {
		if (mediaRange == null)
			mediaRange = new MediaRange();
		mediaRange.setType(contentType);
		mediaRange.setSubtype(contentSubType);
	}

	/**
	 * set the content type.
	 * 
	 * @param contentType
	 *            Content type string.
	 */

	public void setContentType(String contentType) {
		if (contentType == null)
			throw new NullPointerException("null arg");
		if (mediaRange == null)
			mediaRange = new MediaRange();
		mediaRange.setType(contentType);
	}

	/**
	 * Set the content subtype.
	 * 
	 * @param contentType
	 *            String to set
	 */
	public void setContentSubType(String contentType) {
		if (contentType == null)
			throw new NullPointerException("null arg");
		if (mediaRange == null)
			mediaRange = new MediaRange();
		mediaRange.setSubtype(contentType);
	}

	public Object getValue() {
		return this.mediaRange;
	}

	public Object clone() {
		ContentTypeHeader retval = new ContentTypeHeader();
		retval.parameters = (NameValueList) this.parameters.clone();
		retval.mediaRange = (MediaRange) this.mediaRange.clone();
		return retval;
	}

}
