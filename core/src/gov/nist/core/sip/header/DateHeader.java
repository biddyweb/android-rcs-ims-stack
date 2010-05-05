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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Date sip header.
 * 
 * @author M. Ranganathan.
 */

public class DateHeader extends Header {

	private Calendar date;

	public static final String NAME = Header.DATE;

	protected static Class clazz;

	static {
		clazz = new DateHeader().getClass();
	}

	public DateHeader() {
		super(Header.DATE);
		date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Set the expiry date.
	 * 
	 * @param date
	 *            is the date to set.
	 */
	public void setDate(Date date) {
		this.date.setTime(date);
	}

	/**
	 * Set the date.
	 */
	public void setDate(Calendar date) {
		this.date = date;

	}

	/**
	 * Get the expiry date.
	 * 
	 * @return get the expiry date.
	 */
	public Date getDate() {
		return this.date.getTime();
	}

	/**
	 * Get the calendar date.
	 */
	public Object getValue() {
		return this.date;
	}

	/**
	 * Encode into canonical form.
	 */
	public String encodeBody() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(encodeCalendar(date));
		return sbuf.toString();
	}

	/**
	 * Get the parameters for the header.
	 */
	public NameValueList getParameters() {
		return null;
	}

	public Object clone() {
		DateHeader retval = new DateHeader();
		retval.setDate(this.getDate());
		return retval;
	}

}
