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

import gov.nist.core.ParseException;
import gov.nist.core.StringTokenizer;
import gov.nist.core.Utils;

/**
 * 
 * @author M. Ranganathan
 * @version
 */

/**
 * Routing algorithms return a list of hops to which the request is routed.
 */
public class Hop extends Object {
	protected String host;

	protected int port;

	protected String transport;

	protected boolean explicitRoute; // this is generated from a ROUTE
										// header.

	protected boolean defaultRoute; // This is generated from the proxy addr

	protected boolean uriRoute; // This is extracted from the requestURI.

	public String toString() {
		return host + ":" + port + "/" + transport;
	}

	public boolean equals(Object other) {
		if (other.getClass().equals(this.getClass())) {
			Hop otherhop = (Hop) other;
			return (otherhop.host.equals(this.host) && otherhop.port == this.port);
		} else
			return false;
	}

	/**
	 * Create new hop given host, port and transport.
	 * 
	 * @param hostName
	 *            hostname
	 * @param portNumber
	 *            port
	 * @param trans
	 *            transport
	 */
	public Hop(String hostName, int portNumber, String trans) {
		host = hostName;
		port = portNumber;
		if (trans == null)
			transport = "UDP";
		else if (trans == "")
			transport = "UDP";
		else
			transport = trans;
	}

	/**
	 * Creates new Hop
	 * 
	 * @param hop
	 *            is a hop string in the form of host:port/Transport
	 * @throws IllegalArgument
	 *             exception if string is not properly formatted or null.
	 */
	public Hop(String hop) throws IllegalArgumentException {
		if (hop == null)
			throw new IllegalArgumentException("Null arg!");
		try {
			StringTokenizer stringTokenizer = new StringTokenizer(hop + "/");
			String hostPort = stringTokenizer.getNextToken('/');
			// Skip over the slash.
			stringTokenizer.getNextChar();
			// get the transport string.
			transport = stringTokenizer.getNextToken('/').trim();
			if (transport == null)
				transport = "UDP";
			else if (transport == "")
				transport = "UDP";
			if (Utils.compareToIgnoreCase(transport, "UDP") != 0
					&& Utils.compareToIgnoreCase(transport, "TCP") != 0) {
				throw new IllegalArgumentException(hop);
			}
			stringTokenizer = new StringTokenizer(hostPort + ":");
			host = stringTokenizer.getNextToken(':');
			if (host == null || host.equals(""))
				throw new IllegalArgumentException("no host!");
			stringTokenizer.consume(1);
			String portString = null;
			portString = stringTokenizer.getNextToken(':');

			if (portString == null || portString.equals("")) {
				port = 5060;
			} else {
				try {
					port = Integer.parseInt(portString);
				} catch (NumberFormatException ex) {
					throw new IllegalArgumentException("Bad port spec");
				}
			}
			defaultRoute = true;
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad hop");
		}

	}

	/**
	 * Retruns the host string.
	 * 
	 * @return host String
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port.
	 * 
	 * @return port integer.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * returns the transport string.
	 */
	public String getTransport() {
		return transport;
	}

	/**
	 * Return true if this is an explicit route (extacted from a ROUTE Header)
	 */
	public boolean isExplicitRoute() {
		return explicitRoute;
	}

	/**
	 * Return true if this is a default route (next hop proxy address).
	 */
	public boolean isDefaultRoute() {
		return defaultRoute;
	}

	/**
	 * Return true if this is uriRoute
	 */
	public boolean isURIRoute() {
		return uriRoute;
	}

	/**
	 * Set the URIRoute flag.
	 */
	public void setURIRouteFlag() {
		uriRoute = true;
	}

	/**
	 * Set the defaultRouteFlag.
	 */
	public void setDefaultRouteFlag() {
		defaultRoute = true;
	}

	/**
	 * Set the explicitRoute flag.
	 */
	public void setExplicitRouteFlag() {
		explicitRoute = true;
	}

}
