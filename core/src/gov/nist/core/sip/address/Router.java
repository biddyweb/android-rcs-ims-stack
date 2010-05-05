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

import gov.nist.core.sip.message.Request;

import java.util.Enumeration;


public interface Router {
	/**
	 * Return a linked list of addresses corresponding to a requestURI. This is
	 * called for sending out outbound messages for which we do not directly
	 * have the request URI. The implementaion function is expected to return a
	 * linked list of addresses to which the request is forwarded. The
	 * implementation may use this method to perform location searches etc.
	 * 
	 * @param sipRequest
	 *            is the message to route.
	 */
	public Enumeration getNextHops(Request sipRequest);

	/**
	 * Set the outbound proxy.
	 */
	public void setOutboundProxy(String outboundProxy);

	/**
	 * Get the outbound proxy.
	 */
	public Hop getOutboundProxy();

}
