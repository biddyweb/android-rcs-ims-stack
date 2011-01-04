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

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;

/**
 * Via Header (these are strung together in a ViaList).
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan and Olivier Deruelle
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */

public class ViaHeader extends gov.nist.core.sip.header.ParametersHeader {

	public static Class clazz;

	public static final String NAME = Header.VIA;

	/**
	 * The branch parameter is included by every forking proxy.
	 */
	public static final String BRANCH = "branch";

	/**
	 * The "hidden" paramter is included if this header field was hidden by the
	 * upstream proxy.
	 */
	public static final String HIDDEN = "hidden";

	/**
	 * The "received" parameter is added only for receiver-added Via Fields.
	 */
	public static final String RECEIVED = "received";

	/**
	 * The "maddr" paramter is designating the multicast address.
	 */
	public static final String MADDR = "maddr";

	/**
	 * The "TTL" parameter is designating the time-to-live value.
	 */
	public static final String TTL = "ttl";

	/**
	 * sentProtocol field.
	 */
	protected Protocol sentProtocol;

	/**
	 * sentBy field.
	 */
	protected HostPort sentBy;

	/**
	 * comment field
	 */
	protected String comment;

	static {
		clazz = new ViaHeader().getClass();
	}

	/**
	 * Default constructor
	 */
	public ViaHeader() {
		super(VIA);
		this.sentBy = new HostPort();

		sentProtocol = new Protocol();
	}

	/**
	 * Compare two via headers for equaltiy.
	 * 
	 * @param other
	 *            Object to set.
	 * @return true if the two via headers are the same.
	 */
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		ViaHeader that = (ViaHeader) other;

		if (!this.sentProtocol.equals(that.sentProtocol)) {
			return false;
		}
		if (!this.parameters.equals(that.parameters)) {
			return false;
		}
		if (!this.sentBy.equals(that.sentBy)) {
			return false;
		}
		return true;
	}

	/**
	 * Encode the via header into a cannonical string.
	 * 
	 * @return String containing cannonical encoding of via header.
	 */
	public String encodeBody() {
		String encoding = "";
		encoding += sentProtocol.encode() + Separators.SP + sentBy.encode();
		// Add the default port if there is no port specified.
		if (!sentBy.hasPort())
			encoding += Separators.COLON + "5060";
		if (comment != null) {
			encoding += Separators.LPAREN + comment + Separators.RPAREN;
		}
		if (!parameters.isEmpty()) {
			encoding += Separators.SEMICOLON + parameters.encode();
		}
		return encoding;
	}

	/**
	 * get the Protocol Version
	 * 
	 * @return String
	 */
	public String getProtocolVersion() {
		if (sentProtocol == null)
			return null;
		else
			return sentProtocol.getProtocolVersion();
	}

	/**
	 * Accessor for the sentProtocol field.
	 * 
	 * @return Protocol field
	 */
	public Protocol getSentProtocol() {

		return sentProtocol;
	}

	/**
	 * Accessor for the sentBy field
	 * 
	 * @return SentBy field
	 */
	public HostPort getSentBy() {
		return sentBy;
	}

	/**
	 * Get the host name. (null if not yet set).
	 * 
	 * @return host name from the via header.
	 */
	public String getHost() {
		if (sentBy == null)
			return null;
		else {
			Host host = sentBy.getHost();
			if (host == null)
				return null;
			else
				return host.getHostname();
		}
	}

	/**
	 * port of the Via header.
	 * 
	 * @return port field.
	 */
	public int getPort() {
		if (sentBy == null)
			return -1;
		return sentBy.getPort();
	}

	/**
	 * port of the Via Header.
	 * 
	 * @return true if Port exists.
	 */
	public boolean hasPort() {
		if (sentBy == null)
			return false;
		return (getSentBy()).hasPort();
	}

	/**
	 * Accessor for the comment field.
	 * 
	 * @return comment field.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Get the Branch parameter if it exists.
	 * 
	 * @return Branch field.
	 */
	public String getBranch() {
		return super.getParameter(ViaHeader.BRANCH);
	}

	/**
	 * get the received parameter if it exists
	 * 
	 * @return received parameter.
	 */
	public String getReceived() {
		return super.getParameter(ViaHeader.RECEIVED);

	}

	/**
	 * Get the maddr parameter if it exists.
	 * 
	 * @return maddr parameter.
	 */
	public String getMaddr() {
		return super.getParameter(ViaHeader.MADDR);

	}

	/**
	 * get the ttl parameter if it exists.
	 * 
	 * @return ttl parameter.
	 */
	public String getTTL() {
		return super.getParameter(ViaHeader.TTL);
	}

	/**
	 * comment of the Via Header.
	 * 
	 * @return false if comment does not exist and true otherwise.
	 */
	public boolean hasComment() {
		return comment != null;
	}

	/**
	 * remove the comment field.
	 */
	public void removeComment() {
		comment = null;
	}

	/**
	 * set the Protocol Version
	 * 
	 * @param protocolVersion
	 *            String to set
	 */
	public void setProtocolVersion(String protocolVersion) {
		if (sentProtocol == null)
			sentProtocol = new Protocol();
		sentProtocol.setProtocolVersion(protocolVersion);
	}

	/**
	 * Set the sentProtocol member
	 * 
	 * @param s
	 *            Protocol to set.
	 */
	public void setSentProtocol(Protocol s) {
		sentProtocol = s;
	}

	/**
	 * set the transport string.
	 * 
	 * @param transport
	 *            String to set
	 */
	public void setTransport(String transport) {
		if (sentProtocol == null)
			sentProtocol = new Protocol();
		sentProtocol.setTransport(transport);
	}

	/**
	 * Set the sentBy member
	 * 
	 * @param s
	 *            HostPort to set.
	 */
	public void setSentBy(HostPort s) {
		sentBy = s;
	}

	/**
	 * Set the comment member
	 * 
	 * @param c
	 *            String to set.
	 */
	public void setComment(String c) {
		comment = c;
	}

	/**
	 * Clone - do a deep copy.
	 * 
	 * @return Object Via
	 */
	public Object clone() {
		ViaHeader retval = new ViaHeader();

		if (this.comment != null)
			retval.comment = new String(this.comment);
		if (this.parameters != null)
			retval.parameters = (NameValueList) parameters.clone();
		if (this.sentBy != null)
			retval.sentBy = (HostPort) sentBy.clone();
		if (this.sentProtocol != null)
			retval.sentProtocol = (Protocol) sentProtocol.clone();
		return retval;
	}

	/**
	 * Get the value portion of this header (does nto include the parameters).
	 */
	public Object getValue() {
		return sentProtocol.encode() + " " + sentBy.encode();

	}

	public void setBranch(String branch) {
		super.setParameter(BRANCH, branch);
	}

	public void setHost(String host) {
		this.sentBy.setHost(new Host(host));
	}

	public void setHost(Host host) {
		this.sentBy.setHost(host);
	}

	public void setPort(int port) {
		this.sentBy.setPort(port);
	}

	public String getTransport() {
		if (this.sentProtocol == null)
			return null;
		else
			return this.sentProtocol.getTransport();
	}

}
