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

import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.ParseException;
import gov.nist.core.Separators;
import gov.nist.core.sip.address.URI;



/**
 * The generic AuthenticationHeader
 * 
 * @author Olivier Deruelle <deruelle@nist.gov>
 * @author M. Ranganathan <mranga@nist.gov><br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */

public abstract class AuthenticationHeader extends ParametersHeader {

	public static String DOMAIN = "domain";

	public static String REALM = "realm";

	public static String OPAQUE = "opaque";

	public static String ALGORITHM = "algorithm";

	public static String QOP = "qop";

	public static String STALE = "stale";

	public static String SIGNATURE = "signature";

	public static String RESPONSE = "response";

	public static String SIGNED_BY = "signed-by";

	public static String NC = "nc";

	public static String URI = "uri";

	public static String USERNAME = "username";

	public static String CNONCE = "cnonce";

	public static String NONCE = "nonce";

	public static String DIGEST = "Digest";

	public static String NEXT_NONCE = "next-nonce";

	protected String scheme;

	public AuthenticationHeader(String name) {
		super(name);
		parameters.setSeparator(Separators.COMMA); // oddball
		this.scheme = DIGEST;
	}

	public AuthenticationHeader() {
		super();
		parameters.setSeparator(Separators.COMMA);
	}

	/**
	 * set the specified parameter.
	 * 
	 * @param name --
	 *            name of the parameter
	 * @param value --
	 *            value of the parameter.
	 */
	public void setParameter(String name, String value)
			throws IllegalArgumentException {
		NameValue nv = super.parameters.getNameValue(name.toLowerCase());
		if (nv == null) {
			nv = new NameValue(name, value);
			if (equalsIgnoreCase(name, QOP) || equalsIgnoreCase(name, REALM)
					|| equalsIgnoreCase(name, CNONCE)
					|| equalsIgnoreCase(name, NONCE)
					|| equalsIgnoreCase(name, USERNAME)
					|| equalsIgnoreCase(name, DOMAIN)
					|| equalsIgnoreCase(name, OPAQUE)
					|| equalsIgnoreCase(name, NEXT_NONCE)
					|| equalsIgnoreCase(name, URI)
					|| equalsIgnoreCase(name, RESPONSE)) {
				nv.setQuotedValue();
				if (value == null)
					throw new NullPointerException("null value");
				if (value.startsWith(Separators.DOUBLE_QUOTE))
					throw new IllegalArgumentException(value
							+ " : Unexpected DOUBLE_QUOTE");
			}
			super.setParameter(nv);
		} else
			nv.setValue(value);

	}

	/**
	 * Encode in canonical form.
	 * 
	 * @return canonical string.
	 */
	public String encodeBody() {
		return this.scheme + Separators.SP + parameters.encode();
	}

	/**
	 * Sets the scheme of the challenge information for this
	 * AuthenticationHeaderHeader. For example, Digest.
	 * 
	 * @param scheme -
	 *            the new string value that identifies the challenge information
	 *            scheme.
	 * @since v1.1
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	/**
	 * Returns the scheme of the challenge information for this
	 * AuthenticationHeaderHeader.
	 * 
	 * @return the string value of the challenge information.
	 * @since v1.1
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * Sets the Realm of the WWWAuthenicateHeader to the <var>realm</var>
	 * parameter value. Realm strings MUST be globally unique. It is RECOMMENDED
	 * that a realm string contain a hostname or domain name. Realm strings
	 * SHOULD present a human-readable identifier that can be rendered to a
	 * user.
	 * 
	 * @param realm
	 *            the new Realm String of this WWWAuthenicateHeader.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the realm.
	 * @since v1.1
	 */
	public void setRealm(String realm) {
		if (realm == null)
			throw new NullPointerException("null realm");
		setParameter(REALM, realm);
	}

	/**
	 * Returns the Realm value of this WWWAuthenicateHeader. This convenience
	 * method returns only the realm of the complete Challenge.
	 * 
	 * @return the String representing the Realm information, null if value is
	 *         not set.
	 * @since v1.1
	 */
	public String getRealm() {
		return getParameter(REALM);
	}

	/**
	 * Sets the Nonce of the WWWAuthenicateHeader to the <var>nonce</var>
	 * parameter value.
	 * 
	 * @param nonce -
	 *            the new nonce String of this WWWAuthenicateHeader.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the nonce value.
	 * @since v1.1
	 */
	public void setNonce(String nonce) {
		if (nonce == null)
			throw new NullPointerException("null nonce");
		setParameter(NONCE, nonce);
	}

	/**
	 * Returns the Nonce value of this WWWAuthenicateHeader.
	 * 
	 * @return the String representing the nonce information, null if value is
	 *         not set.
	 * @since v1.1
	 */
	public String getNonce() {
		return getParameter(NONCE);
	}

	/**
	 * Sets the URI of the WWWAuthenicateHeader to the <var>uri</var> parameter
	 * value.
	 * 
	 * @param uri -
	 *            the new URI of this WWWAuthenicateHeader.
	 * @since v1.1
	 */
	public void setURI(URI uri) {
		if (uri != null) {
			NameValue nv = new NameValue(URI, uri);
			nv.setQuotedValue();
			super.parameters.set(nv);
		} else {
			throw new NullPointerException("Null URI");
		}
	}

	/**
	 * Returns the URI value of this WWWAuthenicateHeader, for example
	 * DigestURI.
	 * 
	 * @return the URI representing the URI information, null if value is not
	 *         set.
	 * @since v1.1
	 */
	public URI getURI() {
		return getParameterAsURI(URI);
	}

	/**
	 * Sets the Algorithm of the WWWAuthenicateHeader to the new <var>algorithm</var>
	 * parameter value.
	 * 
	 * @param algorithm -
	 *            the new algorithm String of this WWWAuthenicateHeader.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the algorithm value.
	 * @since v1.1
	 */
	public void setAlgorithm(String algorithm) throws ParseException {
		if (algorithm == null)
			throw new NullPointerException("null arg");
		setParameter(ALGORITHM, algorithm);
	}

	/**
	 * Returns the Algorithm value of this WWWAuthenicateHeader.
	 * 
	 * @return the String representing the Algorithm information, null if the
	 *         value is not set.
	 * @since v1.1
	 */
	public String getAlgorithm() {
		return getParameter(ALGORITHM);
	}

	/**
	 * Sets the Qop value of the WWWAuthenicateHeader to the new <var>qop</var>
	 * parameter value.
	 * 
	 * @param qop -
	 *            the new Qop string of this WWWAuthenicateHeader.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the Qop value.
	 * @since v1.1
	 */
	public void setQop(String qop) throws ParseException {
		if (qop == null)
			throw new NullPointerException("null arg");
		setParameter(QOP, qop);
	}

	/**
	 * Returns the Qop value of this WWWAuthenicateHeader.
	 * 
	 * @return the string representing the Qop information, null if the value is
	 *         not set.
	 * @since v1.1
	 */
	public String getQop() {
		return getParameter(QOP);
	}

	/**
	 * Sets the Opaque value of the WWWAuthenicateHeader to the new <var>opaque</var>
	 * parameter value.
	 * 
	 * @param opaque -
	 *            the new Opaque string of this WWWAuthenicateHeader.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the opaque value.
	 * @since v1.1
	 */
	public void setOpaque(String opaque) throws ParseException {
		if (opaque == null)
			throw new NullPointerException("null arg");
		setParameter(OPAQUE, opaque);
	}

	/**
	 * Returns the Opaque value of this WWWAuthenicateHeader.
	 * 
	 * @return the String representing the Opaque information, null if the value
	 *         is not set.
	 * @since v1.1
	 */
	public String getOpaque() {
		return getParameter(OPAQUE);
	}

	/**
	 * Sets the Domain of the WWWAuthenicateHeader to the <var>domain</var>
	 * parameter value.
	 * 
	 * @param domain -
	 *            the new Domain string of this WWWAuthenicateHeader.
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the domain.
	 * @since v1.1
	 */
	public void setDomain(String domain) throws ParseException {
		if (domain == null)
			throw new NullPointerException("null arg");
		setParameter(DOMAIN, domain);
	}

	/**
	 * Returns the Domain value of this WWWAuthenicateHeader.
	 * 
	 * @return the String representing the Domain information, null if value is
	 *         not set.
	 * @since v1.1
	 */
	public String getDomain() {
		return getParameter(DOMAIN);
	}

	/**
	 * Sets the value of the stale parameter of the WWWAuthenicateHeader to the
	 * <var>stale</var> parameter value.
	 * 
	 * @param stale -
	 *            the new boolean value of the stale parameter.
	 * @since v1.1
	 */
	public void setStale(boolean stale) {
		setParameter(new NameValue(STALE, new Boolean(stale)));
	}

	/**
	 * Returns the boolean value of the state paramater of this
	 * WWWAuthenicateHeader.
	 * 
	 * @return the boolean representing if the challenge is stale.
	 * @since v1.1
	 */
	public boolean isStale() {
		return this.getParameterAsBoolean(STALE);
	}

	/**
	 * Set the CNonce.
	 * 
	 * @param cnonce --
	 *            a nonce string.
	 */
	public void setCNonce(String cnonce) throws ParseException {
		this.setParameter(CNONCE, cnonce);
	}

	/**
	 * Get the CNonce.
	 * 
	 * @return the cnonce value.
	 */
	public String getCNonce() {
		return getParameter(CNONCE);
	}

	public int getNonceCount() {
		return this.getParameterAsHexInt(NC);

	}

	/**
	 * Set the nonce count parameter. Bug fix sent in by Andreas Byström
	 * 
	 * @param nonceCount --
	 *            nonce count to set.
	 */

	public void setNonceCount(int nonceCount) throws ParseException,
			IllegalArgumentException {
		if (nonceCount < 0)
			throw new IllegalArgumentException("bad value");

		String nc = Integer.toHexString(nonceCount);

		String base = "00000000";
		nc = base.substring(0, 8 - nc.length()) + nc;
		this.setParameter(NC, nc);

	}

	/**
	 * Get the RESPONSE value (or null if it does not exist).
	 * 
	 * @return String response parameter value.
	 */
	public String getResponse() {
		return (String) getParameterValue(RESPONSE);
	}

	/**
	 * Set the Response.
	 * 
	 * @param response
	 *            to set.
	 */
	public void setResponse(String response) throws ParseException {
		if (response == null)
			throw new NullPointerException("Null parameter");
		// Bug fix from Andreas Byström
		this.setParameter(RESPONSE, response);
	}

	/**
	 * Returns the Username value of this AuthorizationHeader. This convenience
	 * method returns only the username of the complete Response.
	 * 
	 * @return the String representing the Username information, null if value
	 *         is not set.
	 * 
	 * @since JAIN SIP v1.1
	 * 
	 */
	public String getUsername() {
		return (String) getParameter(USERNAME);
	}

	/**
	 * Sets the Username of the AuthorizationHeader to the <var>username</var>
	 * parameter value.
	 * 
	 * @param username
	 *            the new Username String of this AuthorizationHeader.
	 * 
	 * @throws ParseException
	 *             which signals that an error has been reached
	 * 
	 * unexpectedly while parsing the username.
	 * 
	 * @since JAIN SIP v1.1
	 * 
	 */
	public void setUsername(String username) {
		this.setParameter(USERNAME, username);
	}

	/**
	 * Clone - do a deep copy.
	 * 
	 * @return Object Authorization
	 */
	public Object clone() {
		try {
			AuthenticationHeader retval = (AuthenticationHeader) this
					.getClass().newInstance();
			if (this.scheme != null)
				retval.scheme = new String(this.scheme);
			if (this.parameters != null)
				retval.parameters = (NameValueList) parameters.clone();
			return retval;
		} catch (Exception ex) {
			return null;
		}
	}

	public boolean equals(Object that) {
		if (!that.getClass().equals(this.getClass())) {
			return false;
		} else {
			AuthenticationHeader other = (AuthenticationHeader) that;
			return (equalsIgnoreCase(this.scheme, other.scheme) && this.parameters
					.equals(other.parameters));
		}
	}

	/**
	 * Get the value of the header (just returns the scheme).
	 * 
	 * @return the scheme object.
	 */
	public Object getValue() {
		return getScheme();

	}

}
