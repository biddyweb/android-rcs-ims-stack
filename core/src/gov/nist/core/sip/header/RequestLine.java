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

import gov.nist.core.GenericObject;
import gov.nist.core.Separators;
import gov.nist.core.sip.address.URI;



/**
 * RequestLine of SIP Request.
 * 
 * @author M. Ranganathan
 */
public class RequestLine extends GenericObject {

	/**
	 * uri field. Note that this can be a SIP URI or a generic URI like tel URI.
	 */
	protected URI uri;

	/**
	 * method field.
	 */
	protected String method;

	/**
	 * sipVersion field
	 */
	protected String sipVersion;

	public static Class clazz;

	static {
		clazz = new RequestLine().getClass();
	}

	/**
	 * Default constructor
	 */
	public RequestLine() {
		sipVersion = "SIP/2.0";
	}

	/**
	 * Set the SIP version.
	 * 
	 * @param sipVersion --
	 *            the SIP version to set.
	 */
	public void setSIPVersion(String sipVersion) {
		this.sipVersion = sipVersion;
	}

	/**
	 * Encode the request line as a String.
	 * 
	 * @return requestLine encoded as a string.
	 */
	public String encode() {
		StringBuffer encoding = new StringBuffer();
		if (method != null) {
			encoding.append(method);
			encoding.append(Separators.SP);
		}
		if (uri != null) {
			encoding.append(uri.encode());
			encoding.append(Separators.SP);
		}
		encoding.append(sipVersion + Separators.NEWLINE);
		return encoding.toString();
	}

	/**
	 * get the Request-URI.
	 * 
	 * @return the request URI
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * Constructor given the request URI and the method.
	 */
	public RequestLine(URI requestURI, String method) {
		this.uri = requestURI;
		this.method = method;
		this.sipVersion = "SIP/2.0";
	}

	/**
	 * Get the Method
	 * 
	 * @return method string.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Get the SIP version.
	 * 
	 * @return String
	 */
	public String getSipVersion() {
		return sipVersion;
	}

	/**
	 * Set the uri member.
	 * 
	 * @param uri
	 *            URI to set.
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}

	/**
	 * Set the method member
	 * 
	 * @param method
	 *            String to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Set the sipVersion member
	 * 
	 * @param s
	 *            String to set
	 */
	public void setSipVersion(String s) {
		sipVersion = s;
	}

	/**
	 * Get the major verrsion number.
	 * 
	 * @return String major version number
	 */
	public String getVersionMajor() {
		if (sipVersion == null)
			return null;
		String major = null;
		boolean slash = false;
		for (int i = 0; i < sipVersion.length(); i++) {
			if (sipVersion.charAt(i) == '.')
				break;
			if (slash) {
				if (major == null)
					major = "" + sipVersion.charAt(i);
				else
					major += sipVersion.charAt(i);
			}
			if (sipVersion.charAt(i) == '/')
				slash = true;
		}
		return major;
	}

	/**
	 * Get the minor version number.
	 * 
	 * @return String minor version number
	 * 
	 */
	public String getVersionMinor() {
		if (sipVersion == null)
			return null;
		String minor = null;
		boolean dot = false;
		for (int i = 0; i < sipVersion.length(); i++) {
			if (dot) {
				if (minor == null)
					minor = "" + sipVersion.charAt(i);
				else
					minor += sipVersion.charAt(i);
			}
			if (sipVersion.charAt(i) == '.')
				dot = true;
		}
		return minor;
	}

	/**
	 * Compare for equality.
	 * 
	 * @param other
	 *            object to compare with. We assume that all fields are set.
	 */
	public boolean equals(Object other) {
		boolean retval;
		if (!other.getClass().equals(this.getClass())) {
			return false;
		}
		RequestLine that = (RequestLine) other;
		try {
			retval = this.method.equals(that.method)
					&& this.uri.equals(that.uri)
					&& this.sipVersion.equals(that.sipVersion);
		} catch (NullPointerException ex) {
			retval = false;
		}
		return retval;

	}

	/**
	 * Clone this request.
	 */
	public Object clone() {
		RequestLine retval = new RequestLine();
		if (this.uri != null)
			retval.uri = (URI) this.uri.clone();
		if (this.method != null)
			retval.method = new String(this.method);
		if (this.sipVersion != null)
			retval.sipVersion = new String(this.sipVersion);
		return (Object) retval;
	}

}
