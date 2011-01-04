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

import gov.nist.core.HostPort;
import gov.nist.core.NameValueList;
import gov.nist.core.sip.address.Address;



/**
 * An abstract class for headers that take an address and parameters.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */

public abstract class AddressParametersHeader extends ParametersHeader {

	protected Address address;

	/**
	 * get the Address field
	 * 
	 * @return the imbedded Address
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * set the Address field
	 * 
	 * @param address
	 *            Address to set
	 */
	public void setAddress(Address address) {
		this.address = (Address) address;
	}

	/**
	 * Constructor given the name of the header.
	 */
	protected AddressParametersHeader(String name) {
		super(name);
	}

	public Object getValue() {
		return address;

	}

	public String getDisplayName() {
		return address.getDisplayName();
	}

	public String getUserAtHostPort() {
		return address.getUserAtHostPort();
	}

	public HostPort getHostPort() {
		return address.getHostPort();
	}

	public boolean equals(Object other) {
		if (!other.getClass().equals(this.getClass()))
			return false;
		Address otherAddress = ((AddressParametersHeader) other).getAddress();
		if (otherAddress == null)
			return false;
		if (!otherAddress.equals(address)) {
			return false;
		}
		if (!parameters.equals(((AddressParametersHeader) other).parameters)) {
			return false;
		} else
			return true;
	}

	/**
	 * Encode the header content into a String.
	 * 
	 * @return String
	 */
	public String encodeBody() {
		if (address == null) {
			throw new RuntimeException("No body!");
		}
		StringBuffer retval = new StringBuffer();
		if (address.getAddressType() != Address.NAME_ADDR) {
			retval.append("<");
		}
		retval.append(address.encode());
		if (address.getAddressType() != Address.NAME_ADDR) {
			retval.append(">");
		}
		if (!parameters.isEmpty()) {
			retval.append(";").append(parameters.encode());
		}
		return retval.toString();
	}

	public Object clone() {
		try {
			AddressParametersHeader retval = (AddressParametersHeader) this
					.getClass().newInstance();
			if (this.address != null)
				retval.address = (Address) this.address.clone();
			if (this.parameters != null)
				retval.parameters = (NameValueList) this.parameters.clone();
			return retval;
		} catch (Exception ex) {
			return null;
		}
	}

}
