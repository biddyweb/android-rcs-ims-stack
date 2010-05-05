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

/**
 * CSeqHeader SIP Header.
 * 
 * @author M. Ranganathan <mranga@nist.gov> NIST/ITL/ANTD <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 * @version JAIN-SIP-1.1
 * 
 */

public class CSeqHeader extends Header {
	public static Class clazz;

	public static final String NAME = Header.CSEQ;

	static {
		clazz = new CSeqHeader().getClass();
	}

	/**
	 * seqno field
	 */
	protected Integer seqno;

	/**
	 * method field
	 */
	protected String method;

	/**
	 * Constructor.
	 */
	public CSeqHeader() {
		super(CSEQ);
	}

	/**
	 * Constructor given the sequence number and method.
	 * 
	 * @param seqno
	 *            is the sequence number to assign.
	 * @param method
	 *            is the method string.
	 */
	public CSeqHeader(int seqno, String method) {
		this();
		this.seqno = new Integer(seqno);
		this.method = method;
	}

	/**
	 * Compare two cseq headers for equality.
	 * 
	 * @param other
	 *            Object to compare against.
	 * @return true if the two cseq headers are equals, false otherwise.
	 */
	public boolean equals(Object other) {
		if (!other.getClass().equals(this.getClass())) {
			return false;
		}
		CSeqHeader that = (CSeqHeader) other;
		if (!this.seqno.equals(that.seqno)) {
			return false;
		}
		if (!equalsIgnoreCase(this.method, that.method)) {
			return false;
		}
		return true;
	}

	/**
	 * Return canonical header content. (encoded header except headerName:)
	 * 
	 * @return encoded string.
	 */
	public String encodeBody() {
		return seqno + Separators.SP + method.toUpperCase();
	}

	/**
	 * Get the method.
	 * 
	 * @return String the method.
	 */
	public String getMethod() {
		return method.toUpperCase();
	}

	/**
	 * Sets the sequence number of this CSeqHeaderHeader. The sequence number
	 * MUST be expressible as a 32-bit unsigned integer and MUST be less than
	 * 2**31.
	 * 
	 * @param sequenceNumber -
	 *            the sequence number to set.
	 * @throws InvalidArgumentException --
	 *             if the seq number is <= 0
	 */
	public void setSequenceNumber(int sequenceNumber) {
		if (sequenceNumber < 0)
			throw new IllegalArgumentException(
					"the sequence number parameter is < 0");
		seqno = new Integer(sequenceNumber);
	}

	/**
	 * Set the method member
	 * 
	 * @param meth --
	 *            String to set
	 */
	public void setMethod(String meth) {
		if (meth == null)
			throw new NullPointerException("parameter is null");
		method = meth;
	}

	/**
	 * Gets the sequence number of this CSeqHeaderHeader.
	 * 
	 * @return sequence number of the CSeqHeaderHeader
	 */

	public int getSequenceNumber() {
		if (this.seqno == null)
			return 0;
		else
			return this.seqno.intValue();
	}

	public Object clone() {
		CSeqHeader retval = new CSeqHeader();
		if (this.seqno != null)
			retval.seqno = new Integer(this.seqno.intValue());
		retval.method = this.method;
		return retval;
	}

	public Object getValue() {
		return this.seqno;
	}

	public NameValueList getParameters() {
		return null;
	}

}
