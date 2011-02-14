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
 * MaxForwards Header
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov>
 * @author Olivier Deruelle <deruelle@nist.gov> <a href="{@docRoot}/uncopyright.html">This
 *         code is in the public domain.</a>
 * 
 */
public class MaxForwardsHeader extends Header {

	/**
	 * maxForwards field.
	 */
	protected int maxForwards;

	public static final String NAME = Header.MAX_FORWARDS;

	public final static Class clazz;

	static {
		clazz = new MaxForwardsHeader().getClass();
	}

	/**
	 * Default constructor.
	 */
	public MaxForwardsHeader() {
		super(Header.MAX_FORWARDS);
	}

	/**
	 * get the MaxForwards field.
	 * 
	 * @return the maxForwards member.
	 */
	public int getMaxForwards() {
		return maxForwards;
	}

	/**
	 * Set the maxForwards member
	 * 
	 * @param maxForwards
	 *            maxForwards parameter to set
	 */
	public void setMaxForwards(int maxForwards) throws IllegalArgumentException {
		if (maxForwards < 0 || maxForwards > 255)
			throw new IllegalArgumentException("bad max forwards value "
					+ maxForwards);
		this.maxForwards = maxForwards;
	}

	/**
	 * Encode into a string.
	 * 
	 * @return encoded string.
	 * 
	 */
	public String encodeBody() {
		return new Integer(maxForwards).toString();
	}

	/**
	 * Boolean function
	 * 
	 * @return true if MaxForwards field reached zero.
	 */
	public boolean hasReachedZero() {
		return maxForwards == 0;
	}

	/**
	 * decrement MaxForwards field one by one.
	 */
	public void decrementMaxForwards() {
		if (maxForwards >= 0)
			maxForwards--;
	}

	public Object getValue() {
		return new Integer(maxForwards);
	}

	public NameValueList getParameters() {
		return null;
	}

}
