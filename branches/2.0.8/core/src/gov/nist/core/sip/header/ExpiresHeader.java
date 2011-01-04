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

/**
 * Expires SIP Header.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public class ExpiresHeader extends Header {

	/**
	 * expires field
	 */
	protected Integer expires;

	public static final String NAME = Header.EXPIRES;

	public static Class clazz;

	static {
		clazz = new ExpiresHeader().getClass();
	}

	/**
	 * default constructor
	 */
	public ExpiresHeader() {
		super(EXPIRES);
	}

	/**
	 * Return canonical form.
	 * 
	 * @return String
	 */
	public String encodeBody() {
		return expires.toString();
	}

	/**
	 * Gets the expires value of the ExpiresHeader. This expires value is
	 * 
	 * relative time.
	 * 
	 * 
	 * 
	 * @return the expires value of the ExpiresHeader.
	 * 
	 * @since JAIN SIP v1.1
	 * 
	 */
	public int getExpires() {
		return expires.intValue();
	}

	/**
	 * Sets the relative expires value of the ExpiresHeader. The expires value
	 * MUST be greater than zero and MUST be less than 2**31.
	 * 
	 * @param expires -
	 *            the new expires value of this ExpiresHeader
	 * 
	 * @throws InvalidArgumentException
	 *             if supplied value is less than zero.
	 * 
	 * @since JAIN SIP v1.1
	 * 
	 */
	public void setExpires(int expires) throws IllegalArgumentException {
		if (expires < 0)
			throw new IllegalArgumentException("bad argument " + expires);
		this.expires = new Integer(expires);
	}

	/**
	 * Get the parameters for the header as a nameValue list.
	 */
	public NameValueList getParameters() {
		return null;
	}

	/**
	 * Get the value for the header as opaque object (returned value will depend
	 * upon the header. Note that this is not the same as the getHeaderValue
	 * above.
	 */
	public Object getValue() {
		return expires;
	}

}
