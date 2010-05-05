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
package gov.nist.core;

/**
 * Stores hostname.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov>
 * @author Emil Ivov <emil_ivov@yahoo.com> IPV6 Support. <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * IPv6 Support added by Emil Ivov (emil_ivov@yahoo.com)<br/>
 * Network Research Team (http://www-r2.u-strasbg.fr))<br/>
 * Louis Pasteur University - Strasbourg - France<br/>
 *
 * Marc Bednarek <bednarek@nist.gov> (Bugfixes).<br/>
 *
 */
public class Host {
	protected static final int HOSTNAME = 1;

	protected static final int IPV4ADDRESS = 2;

	protected static final int IPV6ADDRESS = 3;

	/**
	 * hostName field
	 */
	protected String hostname;

	/**
	 * address field
	 */

	protected int addressType;

	/**
	 * default constructor
	 */
	public Host() {
		addressType = HOSTNAME;
	}

	/**
	 * Constructor given host name or IP address.
	 */
	public Host(String hostName) throws IllegalArgumentException {
		if (hostName == null)
			throw new IllegalArgumentException("null host name");
		this.hostname = hostName;
		if (isIPv6Address(hostName))
			this.addressType = IPV6ADDRESS;
		this.addressType = IPV4ADDRESS;
	}

	/**
	 * constructor
	 * 
	 * @param name
	 *            String to set
	 * @param addrType
	 *            int to set
	 */
	public Host(String name, int addrType) {
		addressType = addrType;
		hostname = name.trim().toLowerCase();
	}

	/**
	 * Return the host name in encoded form.
	 * 
	 * @return String
	 */
	public String encode() {
		if (addressType == IPV6ADDRESS && !isIPv6Reference(hostname))
			return "[" + hostname + "]";
		return hostname;
	}

	/**
	 * Compare for equality of hosts. Host names are compared by textual
	 * equality. No dns lookup is performed.
	 * 
	 * @param obj
	 *            Object to set
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		if (!this.getClass().equals(obj.getClass())) {
			return false;
		}
		Host otherHost = (Host) obj;
		return otherHost.hostname.equals(hostname);

	}

	/**
	 * get the HostName field
	 * 
	 * @return String
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * get the Address field
	 * 
	 * @return String
	 */
	public String getAddress() {
		return hostname;
	}

	/**
	 * Set the hostname member.
	 * 
	 * @param h
	 *            String to set
	 */
	public void setHostname(String h) {
		if (isIPv6Address(h))
			addressType = IPV6ADDRESS;
		else
			addressType = HOSTNAME;
		// Null check bug fix sent in by jpaulo@ipb.pt
		if (h != null)
			hostname = h.trim().toLowerCase();

	}

	/**
	 * Set the IP Address.
	 * 
	 * @param address
	 *            is the address string to set.
	 */
	public void setHostAddress(String address) {
		if (isIPv6Address(address))
			addressType = IPV6ADDRESS;
		else
			addressType = IPV4ADDRESS;
		if (address != null)
			this.hostname = address.trim();
	}

	/**
	 * Set the address member
	 * 
	 * @param address
	 *            address String to set
	 */
	public void setAddress(String address) {
		this.setHostAddress(address);
	}

	/**
	 * Return true if the address is a DNS host name (and not an IPV4 address)
	 * 
	 * @return true if the hostname is a DNS name
	 */
	public boolean isHostname() {
		return addressType == HOSTNAME;
	}

	/**
	 * Return true if the address is a DNS host name (and not an IPV4 address)
	 * 
	 * @return true if the hostname is host address.
	 */
	public boolean isIPAddress() {
		return addressType != HOSTNAME;
	}

	// ----- IPv6
	/**
	 * Verifies whether the <code>address</code> could be an IPv6 address
	 */
	private boolean isIPv6Address(String address) {
		return (address != null && address.indexOf(':') != -1);
	}

	/**
	 * Verifies whether the ipv6reference, i.e. whether it enclosed in square
	 * brackets
	 */
	private boolean isIPv6Reference(String address) {
		return address.charAt(0) == '['
				&& address.charAt(address.length() - 1) == ']';
	}

	public Object clone() {
		Host retval = new Host();
		retval.addressType = this.addressType;
		retval.hostname = new String(this.hostname);
		return retval;
	}
}
