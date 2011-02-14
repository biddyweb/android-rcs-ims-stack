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
import gov.nist.core.sip.address.Address;



/**
 * ToHeader SIP Header.
 * 
 * @author M. Ranganathan and Olivier Deruelle <a
 *         "href=${docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public final class ToHeader extends AddressParametersHeader {

	public static final String NAME = Header.TO;

	public static final String TAG = "tag";

	public static Class clazz;

	static {
		clazz = new ToHeader().getClass();
	}

	/**
	 * default Constructor.
	 */
	public ToHeader() {
		super(TO);
	}

	/**
	 * Generate a TO header from a FROM header
	 */
	public ToHeader(FromHeader from) {
		super(TO);
		address = (Address) from.address.clone();
		parameters = (NameValueList) from.parameters.clone();
	}

	/**
	 * Compare two ToHeader headers for equality.
	 * 
	 * @param otherHeader
	 *            Object to set
	 * @return true if the two headers are the same.
	 */
	public boolean equals(Object otherHeader) {
		if (!otherHeader.getClass().equals(this.getClass())) {
			return false;
		}
		return super.equals(otherHeader);
	}

	/**
	 * Encode the header content into a String.
	 * 
	 * @return String
	 */
	public String encodeBody() {
		String retval = "";
		if (address.getAddressType() != Address.NAME_ADDR) {
			retval += Separators.LESS_THAN;
		}
		retval += address.encode();
		if (address.getAddressType() != Address.NAME_ADDR) {
			retval += Separators.GREATER_THAN;
		}
		if (!parameters.isEmpty()) {
			retval += Separators.SEMICOLON + parameters.encode();
		}
		return retval;
	}

	/**
	 * Get the tag parameter from the address parm list.
	 * 
	 * @return tag field
	 */
	public String getTag() {
		return getParameter(TAG);
	}

	/**
	 * Boolean function
	 * 
	 * @return true if the Tag exist
	 */
	public boolean hasTag() {
		return hasParameter(TAG);
	}

	/**
	 * Set the tag member
	 * 
	 * @param t
	 *            String to set
	 */
	public void setTag(String t) {
		setParameter(TAG, t);
	}

	public void removeTag() {
		removeParameter(TAG);
	}

}
