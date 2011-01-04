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
 * Contact Item. There can be several (strung together in a ContactList).
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public final class ContactHeader extends AddressParametersHeader {

	public static Class clazz;

	public static final String NAME = Header.CONTACT;

	static {
		clazz = new ContactHeader().getClass();
	}

	/**
	 * wildCardFlag field.
	 */
	protected boolean wildCardFlag;

	/**
	 * comment field.
	 */
	protected String comment;

	/**
	 * Default constructor.
	 */
	public ContactHeader() {
		super(CONTACT);
		wildCardFlag = false;
	}

	/**
	 * Encode this into a cannonical String.
	 * 
	 * @return String
	 */
	public String encodeBody() {
		String encoding = "";
		if (wildCardFlag) {
			return encoding + "*";
		}
		if (address != null) {
			// Bug report by Joao Paulo
			if (address.getAddressType() == Address.NAME_ADDR) {
				encoding += address.encode();
			} else {
				// Encoding in canonical form must have <> around address.
				encoding += "<" + address.encode() + ">";
			}
		}
		if (parameters != null && !parameters.isEmpty()) {
			encoding += ";" + parameters.encode();
		}
		if (comment != null) {
			encoding += "(" + comment + ")";
		}
		return encoding;
	}

	/**
	 * get the WildCardFlag field
	 * 
	 * @return boolean
	 */
	public boolean getWildCardFlag() {
		return wildCardFlag;
	}

	/**
	 * get the Action field.
	 * 
	 * @return String
	 */
	public String getAction() {
		return getParameter("action");
	}

	/**
	 * get the address field.
	 * 
	 * @return Address
	 */
	public Object getValue() {
		return address;
	}

	/**
	 * get the comment field
	 * 
	 * @return String
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * get Expires field
	 * 
	 * @return String
	 */
	public String getExpires() {
		return getParameter("expires");
	}

	/**
	 * Set the expiry time in seconds.
	 * 
	 * @param String
	 *            expires to set.
	 */

	public void setExpires(String expires) {
		setParameter("expires", expires);
	}

	public void setExpires(int expires) {
		setParameter("expires", new Integer(expires).toString());
	}

	/**
	 * get the Q-value
	 * 
	 * @return String
	 */
	public String getQValue() {
		return getParameter("q");
	}

	/**
	 * Boolean function
	 * 
	 * @return true if this header has a Q-value, false otherwise.
	 */
	public boolean hasQValue() {
		return hasParameter("q");
	}

	/**
	 * Set the wildCardFlag member
	 * 
	 * @param w
	 *            boolean to set
	 */
	public void setWildCardFlag(boolean w) {
		wildCardFlag = w;
	}

	/**
	 * Set the address member
	 * 
	 * @param address
	 *            Address to set
	 */
	public void setAddress(Address address) {
		if (address != null)
			this.address = address;
	}

	/**
	 * Set the comment member
	 * 
	 * @param comment
	 *            String to set
	 */
	public void setComment(String comment) {
		if (comment != null)
			this.comment = comment;
	}

	/**
	 * Clone - do a deep copy.
	 * 
	 * @return Object Contact
	 */
	public Object clone() {
		ContactHeader retval = new ContactHeader();
		retval.wildCardFlag = this.wildCardFlag;
		if (this.comment != null)
			retval.comment = new String(this.comment);
		if (this.parameters != null)
			retval.parameters = (NameValueList) parameters.clone();
		if (this.address != null)
			retval.address = (Address) address.clone();
		return retval;
	}

}
