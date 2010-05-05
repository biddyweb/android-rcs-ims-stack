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
import gov.nist.core.sip.address.Address;



/**
 * FromHeader SIP Header
 * 
 * @author M. Ranganathan and Olivier Deruelle <a
 *         "href=${docRoot}/uncopyright.html">This code is in the public domain.</a>
 */

public final class FromHeader extends AddressParametersHeader {

	public static final String TAG = "tag";

	public static final String NAME = Header.FROM;

	public static Class clazz;

	static {
		clazz = new FromHeader().getClass();
	}

	/**
	 * Default constructor
	 */
	public FromHeader() {
		super(FROM);
	}

	/**
	 * Generate a FROM header from a TO header
	 */
	public FromHeader(ToHeader to) {
		super(FROM);
		this.address = (Address) to.address.clone();
		this.parameters = (NameValueList) to.parameters.clone();
	}

	/**
	 * Compare two from headers for equality.
	 * 
	 * @param otherHeader
	 *            Object to set
	 * @return true if the two headers are the same, false otherwise.
	 */
	public boolean equals(Object otherHeader) {
		if (otherHeader == null || address == null)
			return false;
		if (!otherHeader.getClass().equals(this.getClass())) {
			return false;
		}

		return super.equals(otherHeader);
	}

	/**
	 * Get the address field.
	 * 
	 * @return Address
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * Get the display name from the address.
	 * 
	 * @return String
	 */
	public String getDisplayName() {
		return address.getDisplayName();
	}

	/**
	 * Get the tag parameter from the address parm list.
	 * 
	 * @return String
	 */
	public String getTag() {
		return super.getParameter(FromHeader.TAG);
	}

	/**
	 * Boolean function
	 * 
	 * @return true if this header has a Tag, false otherwise.
	 */
	public boolean hasTag() {
		return super.hasParameter(TAG);

	}

	/**
	 * remove the Tag field.
	 */
	public void removeTag() {
		super.removeParameter(TAG);

	}

	/**
	 * Set the tag member
	 * 
	 * @param tag
	 *            String to set.
	 */
	public void setTag(String tag) {
		super.setParameter(TAG, tag);

	}

}
