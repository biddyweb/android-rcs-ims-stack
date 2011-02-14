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
 * Call ID Header
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public class CallIdHeader extends Header {

	public static final String NAME = Header.CALL_ID;

	public static Class clazz;

	/**
	 * callIdentifier field
	 */
	protected CallIdentifier callIdentifier;

	static {
		clazz = new CallIdHeader().getClass();
	}

	/**
	 * Default constructor
	 */
	public CallIdHeader() {
		super(CALL_ID);
	}

	/**
	 * Compare two call ids for equality.
	 * 
	 * @param other
	 *            Object to set
	 * @return true if the two call ids are equals, false otherwise
	 */
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		CallIdHeader that = (CallIdHeader) other;

		return this.callIdentifier.equals(that.callIdentifier);
	}

	/**
	 * Get the encoded version of this id.
	 * 
	 * @return String.
	 */
	public String encode() {
		return headerName + Separators.COLON + Separators.SP
				+ callIdentifier.encode() + Separators.NEWLINE;
	}

	/**
	 * Encode the body part of this header (leave out the hdrName).
	 * 
	 * @return String encoded body part of the header.
	 */
	public String encodeBody() {
		if (callIdentifier == null)
			return "";
		else
			return callIdentifier.encode();
	}

	/**
	 * get the CallId field. This does the same thing as encodeBody
	 * 
	 * @return String the encoded body part of the
	 */
	public String getCallId() {
		return encodeBody();
	}

	/**
	 * get the call Identifer member.
	 * 
	 * @return CallIdentifier
	 */
	public CallIdentifier getCallIdentifer() {
		return callIdentifier;
	}

	/**
	 * set the CallId field
	 * 
	 * @param cid
	 *            String to set. This is the body part of the Call-Id header. It
	 *            must have the form localId@host or localId.
	 * @throws IllegalArgumentException
	 *             if cid is null, not a token, or is not a token@token.
	 */
	public void setCallId(String cid) throws IllegalArgumentException {
		callIdentifier = new CallIdentifier(cid);
	}

	/**
	 * Set the callIdentifier member.
	 * 
	 * @param cid
	 *            CallIdentifier to set (localId@host).
	 */
	public void setCallIdentifier(CallIdentifier cid) {
		callIdentifier = cid;
	}

	/**
	 * Clone - do a deep copy.
	 * 
	 * @return Object CallIdHeader
	 */
	public Object clone() {
		CallIdHeader retval = new CallIdHeader();
		if (this.callIdentifier != null)
			retval.callIdentifier = (CallIdentifier) this.callIdentifier
					.clone();
		return retval;
	}

	public Object getValue() {
		return callIdentifier;

	}

	public NameValueList getParameters() {
		return null;
	}

}
