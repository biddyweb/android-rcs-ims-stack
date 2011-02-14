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

/** Use this class when there is no parser for parsing the given header. */
public class ExtensionHeader extends ParametersHeader {

	public static Class clazz;

	protected String headerValue;

	public ExtensionHeader() {
	}

	static {
		clazz = new ExtensionHeader().getClass();
	}

	/**
	 * A generic header.
	 */
	public ExtensionHeader(String headerName, String headerValue) {
		super(headerName);
		this.headerValue = headerValue;
	}

	/**
	 * set the value for a generic header.
	 */
	public void setValue(String value) {
		this.headerValue = value;
	}

	public void setName(String name) {
		this.headerName = name;
	}

	public NameValueList getParameters() {
		return parameters;
	}

	public String encodeBody() {
		if (parameters != null && !this.parameters.isEmpty())
			return this.headerValue + Separators.SEMICOLON
					+ this.parameters.encode();
		else
			return this.headerValue;

	}

	/**
	 * Get the value of the header.
	 */
	public Object getValue() {
		return this.headerValue;
	}

}
