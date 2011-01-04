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
import gov.nist.core.sip.SIPConstants;

/**
 * Status Line (for SIPReply) messages.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */

public final class StatusLine extends GenericObject {

	/**
	 * sipVersion field
	 */
	protected String sipVersion;

	/**
	 * status code field
	 */
	protected int statusCode;

	/**
	 * reasonPhrase field
	 */
	protected String reasonPhrase;

	/**
	 * Default Constructor
	 */
	public StatusLine() {
		reasonPhrase = null;
		sipVersion = SIPConstants.SIP_VERSION_STRING;
	}

	public static Class clazz;

	static {
		clazz = new StatusLine().getClass();
	}

	/**
	 * Encode into a canonical form.
	 * 
	 * @return String
	 */
	public String encode() {
		String encoding = SIPConstants.SIP_VERSION_STRING + Separators.SP
				+ statusCode;
		if (reasonPhrase != null)
			encoding += Separators.SP + reasonPhrase;
		encoding += Separators.NEWLINE;
		return encoding;
	}

	/**
	 * get the Sip Version
	 * 
	 * @return SipVersion
	 */
	public String getSipVersion() {
		return sipVersion;
	}

	/**
	 * get the Status Code
	 * 
	 * @return StatusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * get the ReasonPhrase field
	 * 
	 * @return ReasonPhrase field
	 */
	public String getReasonPhrase() {
		return reasonPhrase;
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
	 * Set the statusCode member
	 * 
	 * @param statusCode
	 *            int to set
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * Set the reasonPhrase member
	 * 
	 * @param reasonPhrase
	 *            String to set
	 */
	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	/**
	 * Get the major version number.
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
				slash = false;
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

	public Object clone() {
		StatusLine retval = new StatusLine();

		if (this.sipVersion != null)
			retval.sipVersion = new String(this.sipVersion);

		retval.statusCode = this.statusCode;

		if (this.reasonPhrase != null)
			retval.reasonPhrase = new String(this.reasonPhrase);

		return retval;

	}

	public boolean equals(Object that) {
		if (that instanceof StatusLine)
			return this.statusCode == ((StatusLine) that).statusCode;
		else
			return false;
	}

}
