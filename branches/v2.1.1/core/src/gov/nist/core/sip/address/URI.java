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
package gov.nist.core.sip.address;

import gov.nist.core.GenericObject;
import gov.nist.core.ParseException;

/**
 * Implementation of the URI class.
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class URI extends GenericObject {
	public static final String SIP = ParameterNames.SIP_URI_SCHEME;

	public static final String SIPS = ParameterNames.SIPS_URI_SCHEME;

	public static final String TEL = ParameterNames.TEL_URI_SCHEME;

	public static final String POSTDIAL = ParameterNames.POSTDIAL;

	public static final String PHONE_CONTEXT_TAG = ParameterNames.PHONE_CONTEXT_TAG;

	public static final String ISUB = ParameterNames.ISUB;

	public static final String PROVIDER_TAG = ParameterNames.PROVIDER_TAG;

	public static final String USER = ParameterNames.USER;

	public static final String TRANSPORT = ParameterNames.TRANSPORT;

	public static final String METHOD = ParameterNames.METHOD;

	public static final String TTL = ParameterNames.TTL;

	public static final String MADDR = ParameterNames.MADDR;

	public static final String LR = ParameterNames.LR;

	/**
	 * Imbedded URI
	 */
	protected String uriString;

	protected String scheme;

	/**
	 * Consturctor
	 */
	protected URI() {
	}

	/**
	 * Constructor given the URI string
	 * 
	 * @param uriString
	 *            The imbedded URI string.
	 * @throws ParseException
	 */
	public URI(String uriString) throws ParseException {
		try {
			this.uriString = uriString;
			int i = uriString.indexOf(":");
			scheme = uriString.substring(0, i);
		} catch (Exception e) {
			throw new ParseException("URI, Bad URI format", 0);
		}
	}

	/**
	 * Encode the URI.
	 * 
	 * @return The encoded URI
	 */
	public String encode() {
		return uriString;

	}

	/**
	 * Encode this URI.
	 * 
	 * @return The encoded URI
	 */
	public String toString() {
		return this.encode();

	}

	/**
	 * Overrides the base clone method
	 * 
	 * @return The Cloned strucutre,
	 */
	public Object clone() {
		try {
			return new URI(this.uriString);

		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage() + this.uriString);
		}
	}

	/**
	 * Returns the value of the "scheme" of this URI, for example "sip", "sips"
	 * or "tel".
	 * 
	 * @return the scheme paramter of the URI
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * This method determines if this is a URI with a scheme of "sip" or "sips".
	 * 
	 * @return true if the scheme is "sip" or "sips", false otherwise.
	 */
	public boolean isSipURI() {
		return this instanceof SipURI;

	}
}
