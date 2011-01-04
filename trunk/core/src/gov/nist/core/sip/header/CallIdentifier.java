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
import gov.nist.core.Utils;

/**
 * The call identifer that goes into a callID header and a in-reply-to header.
 * 
 * @author Olivier Deruelle and M. Ranganathan
 * @see CallIdHeader
 */
public final class CallIdentifier extends GenericObject {

	/**
	 * localId field
	 */
	protected String localId;

	/**
	 * host field
	 */
	protected String host;

	/**
	 * Default constructor
	 */
	public CallIdentifier() {
	}

	/**
	 * Constructor
	 * 
	 * @param local
	 *            id is the local id.
	 * @param host
	 *            is the host.
	 */
	public CallIdentifier(String localId, String host) {
		this.localId = localId;
		this.host = host;
	}

	/**
	 * constructor
	 * 
	 * @param cid
	 *            String to set
	 * @throws IllegalArgumentException
	 *             if cid is null or is not a token, or token@token
	 */
	public CallIdentifier(String cid) throws IllegalArgumentException {
		setCallIdHeader(cid);
	}

	/**
	 * Get the encoded version of this id.
	 * 
	 * @return String to set
	 */
	public String encode() {
		if (host != null) {
			return localId + Separators.AT + host;
		} else {
			return localId;
		}
	}

	/**
	 * Compare two call identifiers for equality.
	 * 
	 * @param other
	 *            Object to set
	 * @return true if the two call identifiers are equals, false otherwise
	 */
	public boolean equals(Object other) {
		if (!other.getClass().equals(this.getClass())) {
			return false;
		}
		CallIdentifier that = (CallIdentifier) other;
		if (this.localId.compareTo(that.localId) != 0) {
			return false;
		}
		if (this.host == that.host)
			return true;
		if ((this.host == null && that.host != null)
				|| (this.host != null && that.host == null))
			return false;
		if (Utils.compareToIgnoreCase(host, that.host) != 0) {
			return false;
		}
		return true;
	}

	/**
	 * get the LocalId field
	 * 
	 * @return String
	 */
	public String getLocalId() {
		return localId;
	}

	/**
	 * get the host field
	 * 
	 * @return host member String
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Set the localId member
	 * 
	 * @param localId
	 *            String to set
	 */
	public void setLocalId(String localId) {
		this.localId = localId;
	}

	/**
	 * set the callId field
	 * 
	 * @param cid
	 *            Strimg to set
	 * @throws IllegalArgumentException
	 *             if cid is null or is not a token or token@token
	 */
	public void setCallIdHeader(String cid) throws IllegalArgumentException {
		if (cid == null)
			throw new IllegalArgumentException("NULL!");
		int index = cid.indexOf('@');
		if (index == -1) {
			localId = cid;
			host = null;
		} else {
			localId = cid.substring(0, index);
			host = cid.substring(index + 1, cid.length());
			if (localId == null || host == null) {
				throw new IllegalArgumentException(
						"CallIdHeader  must be token@token or token");
			}
		}
	}

	/**
	 * Set the host member
	 * 
	 * @param host
	 *            String to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Clone - do a deep copy.
	 * 
	 * @return Object CallIdentifier
	 */
	public Object clone() {
		CallIdentifier retval = new CallIdentifier();

		if (this.localId != null)
			retval.localId = new String(this.localId);
		if (this.host != null)
			retval.host = new String(this.host);
		return retval;
	}

}
