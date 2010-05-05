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

/**
 * Header names that are supported by this parser. These are the canonical names
 * for the headers.
 */
public interface HeaderNames {

	/**
	 * constant ERROR_INFO field.
	 */
	public static final String ERROR_INFO = "Error-Info";

	/**
	 * constant MIME_VERSION field.
	 */
	public static final String MIME_VERSION = "Mime-Version";

	/**
	 * constant IN_REPLY_TO field.
	 */
	public static final String IN_REPLY_TO = "In-Reply-To";

	/**
	 * constant ALLOW field.
	 */
	public static final String ALLOW = "Allow";

	/**
	 * constant CONTENT_LANGUAGE field.
	 */
	public static final String CONTENT_LANGUAGE = "Content-Language";

	/**
	 * constant CALL_INFO field.
	 */
	public static final String CALL_INFO = "Call-Info";

	/**
	 * constant CSEQ field.
	 */
	public static final String CSEQ = "CSeq";

	/**
	 * constant ALERT_INFO field.
	 */
	public static final String ALERT_INFO = "Alert-Info";

	/**
	 * constant ACCEPT_ENCODING field.
	 */
	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	/**
	 * constant ACCEPT field.
	 */
	public static final String ACCEPT = "Accept";

	/**
	 * constant ENCRYPTION field.
	 */
	public static final String ENCRYPTION = "Encryption";

	/**
	 * constant ACCEPT_LANGUAGE field.
	 */
	public static final String ACCEPT_LANGUAGE = "Accept-Language";

	/**
	 * constant RECORD_ROUTE field.
	 */
	public static final String RECORD_ROUTE = "Record-Route";

	/**
	 * constant TIMESTAMP field.
	 */
	public static final String TIMESTAMP = "Timestamp";

	/**
	 * constant TO field.
	 */
	public static final String TO = "To";

	/**
	 * constant VIA field.
	 */
	public static final String VIA = "Via";

	/**
	 * constant FROM field.
	 */
	public static final String FROM = "From";

	/**
	 * constant CALL_ID field.
	 */
	public static final String CALL_ID = "Call-Id";

	/**
	 * constant AUTHORIZATION field.
	 */
	public static final String AUTHORIZATION = "Authorization";

	/**
	 * constant PROXY_AUTHENTICATE field.
	 */
	public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

	/**
	 * constant SERVER field.
	 */
	public static final String SERVER = "Server";

	/**
	 * constant UNSUPPORTED field.
	 */
	public static final String UNSUPPORTED = "Unsupported";

	/**
	 * constant RETRY_AFTER field.
	 */
	public static final String RETRY_AFTER = "Retry-After";

	/**
	 * constant CONTENT_TYP field.
	 */
	public static final String CONTENT_TYPE = "Content-Type";

	/**
	 * constant CONTENT_ENCODING field.
	 */
	public static final String CONTENT_ENCODING = "Content-Encoding";

	/**
	 * constant CONTENT_LENGTH field.
	 */
	public static final String CONTENT_LENGTH = "Content-Length";

	/**
	 * constant HIDE field.
	 */
	public static final String HIDE = "Hide";

	/**
	 * constant ROUTE field.
	 */
	public static final String ROUTE = "Route";

	/**
	 * constant CONTACT field.
	 */
	public static final String CONTACT = "Contact";

	/**
	 * constant WWW_AUTHENTICATE field.
	 */
	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	/**
	 * constant MAX_FORWARDS field.
	 */
	public static final String MAX_FORWARDS = "Max-Forwards";

	/**
	 * constant ORGANIZATION field.
	 */
	public static final String ORGANIZATION = "Organization";

	/**
	 * constant PROXY_AUTHORIZATION field.
	 */
	public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

	/**
	 * constant PROXY_REQUIRE field.
	 */
	public static final String PROXY_REQUIRE = "Proxy-Require";

	/**
	 * constant REQUIRE field.
	 */
	public static final String REQUIRE = "Require";

	/**
	 * constant CONTENT_DISPOSITION field.
	 */
	public static final String CONTENT_DISPOSITION = "Content-Disposition";

	/**
	 * constant SUBJECT field.
	 */
	public static final String SUBJECT = "Subject";

	/**
	 * constant USER_AGENT field.
	 */
	public static final String USER_AGENT = "User-Agent";

	/**
	 * constant WARNING field.
	 */
	public static final String WARNING = "Warning";

	/**
	 * constant PRIORITY field.
	 */
	public static final String PRIORITY = "Priority";

	/**
	 * constant DATE field.
	 */
	public static final String DATE = "Date";

	/**
	 * constant EXPIRES field.
	 */
	public static final String EXPIRES = "Expires";

	/**
	 * constant RESPONSE_KEY field.
	 */
	public static final String RESPONSE_KEY = "Response-Key";

	/**
	 * constant WARN_AGENT field.
	 */
	public static final String WARN_AGENT = "Warn-Agent";

	/**
	 * constant SUPPORTED field.
	 */
	public static final String SUPPORTED = "Supported";

	public static final String EVENT = "Event";

}
