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

import gov.nist.core.GenericObject;
import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;

/**
 * Telephone number class.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public class TelephoneNumber extends GenericObject {
	public static final String POSTDIAL = ParameterNames.POSTDIAL;

	public static final String PHONE_CONTEXT_TAG = ParameterNames.PHONE_CONTEXT_TAG;

	public static final String ISUB = ParameterNames.ISUB;

	public static final String PROVIDER_TAG = ParameterNames.PROVIDER_TAG;

	/**
	 * isglobal field
	 */
	protected boolean isglobal;

	/**
	 * phoneNumber field
	 */
	protected String phoneNumber;

	/**
	 * parmeters list
	 */
	protected NameValueList parms;

	/** Creates new TelephoneNumber */
	public TelephoneNumber() {
		parms = new NameValueList("telparms");
	}

	/**
	 * delete the specified parameter.
	 * 
	 * @param name
	 *            String to set
	 */
	public void deleteParm(String name) {
		parms.delete(name);
	}

	/**
	 * get the PhoneNumber field
	 * 
	 * @return String
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * get the PostDial field
	 * 
	 * @return String
	 */
	public String getPostDial() {
		return (String) parms.getValue(POSTDIAL);
	}

	/**
	 * Get the isdn subaddress for this number.
	 * 
	 * @return String
	 */
	public String getIsdnSubaddress() {
		return (String) parms.getValue(ISUB);
	}

	/**
	 * returns true if th PostDial field exists
	 * 
	 * @return boolean
	 */
	public boolean hasPostDial() {
		return parms.getValue(POSTDIAL) != null;
	}

	/**
	 * return true if this header has parameters.
	 * 
	 * @param pname
	 *            String to set
	 * @return boolean
	 */
	public boolean hasParm(String pname) {
		return parms.hasNameValue(pname);
	}

	/**
	 * return true if the isdn subaddress exists.
	 * 
	 * @return boolean
	 */
	public boolean hasIsdnSubaddress() {
		return hasParm(ISUB);
	}

	/**
	 * is a global telephone number.
	 * 
	 * @return boolean
	 */
	public boolean isGlobal() {
		return isglobal;
	}

	/**
	 * remove the PostDial field
	 */
	public void removePostDial() {
		parms.delete(POSTDIAL);
	}

	/**
	 * Remove the isdn subaddress (if it exists).
	 */
	public void removeIsdnSubaddress() {
		deleteParm(ISUB);
	}

	/**
	 * Set the list of parameters.
	 * 
	 * @param p
	 *            NameValueList to set
	 */
	public void setParameters(NameValueList p) {
		parms = p;
	}

	/**
	 * set the Global field
	 * 
	 * @param g
	 *            boolean to set
	 */
	public void setGlobal(boolean g) {
		isglobal = g;
	}

	/**
	 * set the PostDial field
	 * 
	 * @param p
	 *            String to set
	 */
	public void setPostDial(String p) {
		NameValue nv = new NameValue(POSTDIAL, p);
		parms.add(nv);
	}

	/**
	 * set the specified parameter
	 * 
	 * @param name
	 *            String to set
	 * @param value
	 *            Object to set
	 */
	public void setParm(String name, Object value) {
		NameValue nv = new NameValue(name, value);
		parms.add(nv);
	}

	/**
	 * set the isdn subaddress for this structure.
	 * 
	 * @param isub
	 *            String to set
	 */
	public void setIsdnSubaddress(String isub) {
		setParm(ISUB, isub);
	}

	/**
	 * set the PhoneNumber field
	 * 
	 * @param num
	 *            String to set
	 */
	public void setPhoneNumber(String num) {
		phoneNumber = num;
	}

	public String encode() {
		String retval = "";
		if (isglobal)
			retval += "+";
		retval += phoneNumber;
		if (!parms.isEmpty()) {
			retval += Separators.SEMICOLON;
			retval += parms.encode();
		}
		return retval;
	}

	public Object clone() {
		TelephoneNumber retval = new TelephoneNumber();
		retval.isglobal = this.isglobal;
		retval.phoneNumber = new String(this.phoneNumber);
		retval.parms = (NameValueList) this.parms.clone();
		return retval;
	}

}
