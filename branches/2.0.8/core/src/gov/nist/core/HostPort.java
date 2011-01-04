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
 * Holds the hostname:port.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public final class HostPort {

	// host / ipv4/ ipv6/
	/**
	 * host field
	 */
	protected Host host;

	/**
	 * port field
	 * 
	 */
	protected Integer port;

	/**
	 * Default constructor
	 */
	public HostPort() {

		host = null;
		port = null; // marker for not set.
	}

	/**
	 * Encode this hostport into its string representation. Note that this could
	 * be different from the string that has been parsed if something has been
	 * edited.
	 * 
	 * @return String
	 */
	public String encode() {
		StringBuffer retval = new StringBuffer();
		retval.append(host.encode());
		if (port != null)
			retval.append(':').append(port.toString());
		return retval.toString();
	}

	/**
	 * returns true if the two objects are equals, false otherwise.
	 * 
	 * @param other
	 *            Object to set
	 * @return boolean
	 */
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		HostPort that = (HostPort) other;
		if ((this.port == null && that.port != null)
				|| (this.port != null && that.port == null))
			return false;
		else if (this.port == that.port && this.host.equals(that.host))
			return true;
		else
			return this.host.equals(that.host) && this.port.equals(that.port);
	}

	/**
	 * get the Host field
	 * 
	 * @return host field
	 */
	public Host getHost() {
		return host;
	}

	/**
	 * get the port field
	 * 
	 * @return int
	 */
	public int getPort() {
		if (port == null) {
			return -1;
		} else {
			return port.intValue();
		}
	}

	/**
	 * Returns boolean value indicating if Header has port
	 * 
	 * @return boolean value indicating if Header has port
	 */
	public boolean hasPort() {
		return port != null;
	}

	/**
	 * remove port.
	 */
	public void removePort() {
		port = null;
	}

	/**
	 * Set the host member
	 * 
	 * @param h
	 *            Host to set
	 */
	public void setHost(Host h) {
		host = h;
	}

	/**
	 * Set the port member
	 * 
	 * @param p
	 *            int to set
	 */
	public void setPort(int p) {
		port = new Integer(p);
	}

	public Object clone() {
		HostPort retval = new HostPort();
		if (this.host != null)
			retval.host = (Host) this.host.clone();
		if (this.port != null)
			retval.port = new Integer(this.port.intValue());
		return retval;
	}
}
