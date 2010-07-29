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


import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.ParseException;
import gov.nist.core.sip.address.URI;

import java.util.Vector;


/**
 * Parameters header. Suitable for extension by headers that have parameters.
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 * @version JAIN-SIP-1.1
 * 
 */

public abstract class ParametersHeader extends Header {
	protected NameValueList parameters;

	protected ParametersHeader() {
		this.parameters = new NameValueList();
	}

	protected ParametersHeader(String hdrName) {
		super(hdrName);
		this.parameters = new NameValueList();
	}

	/**
	 * Returns the value of the named parameter, or null if it is not set. A
	 * zero-length String indicates flag parameter.
	 * 
	 * @param <var>name
	 *            </var> name of parameter to retrieve
	 * @return the value of specified parameter
	 */

	public String getParameter(String name) {
		return this.parameters.getParameter(name);

	}

	/**
	 * Return the parameter as an object (dont convert to string).
	 * 
	 * @param name
	 *            is the name of the parameter to get.
	 * @return the object associated with the name.
	 * 
	 */
	public Object getParameterValue(String name) {
		return this.parameters.getValue(name);
	}

	/**
	 * Returns an Vector over the names (Strings) of all parameters present in
	 * this ParametersHeader.
	 * 
	 * @return an Iterator over all the parameter names
	 */

	public Vector getParameterNames() {
		return parameters.getNames();
	}

	/**
	 * Return true if you have a parameter and false otherwise.
	 * 
	 * @return true if the parameters list is non-empty.
	 */

	public boolean hasParameters() {
		return parameters != null && !parameters.isEmpty();
	}

	/**
	 * Removes the specified parameter from Parameters of this ParametersHeader.
	 * This method returns silently if the parameter is not part of the
	 * ParametersHeader.
	 * 
	 * @param name -
	 *            a String specifying the parameter name
	 */

	public void removeParameter(String name) {
		this.parameters.delete(name);
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 * 
	 * a value it will be overwritten. A zero-length String indicates flag
	 * 
	 * parameter.
	 * 
	 * 
	 * 
	 * @param name -
	 *            a String specifying the parameter name
	 * 
	 * @param value -
	 *            a String specifying the parameter value
	 * 
	 * @throws ParseException
	 *             which signals that an error has been reached
	 * 
	 * unexpectedly while parsing the parameter name or value.
	 * 
	 */
	public void setParameter(String name, String value) {
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(value);
		} else {
			nv = new NameValue(name, value);
			this.parameters.set(nv);
		}
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 * 
	 * a value it will be overwritten. A zero-length String indicates flag
	 * 
	 * parameter.
	 * 
	 * 
	 * 
	 * @param name -
	 *            a String specifying the parameter name
	 * 
	 * @param value -
	 *            a String specifying the parameter value
	 * 
	 * @throws ParseException
	 *             which signals that an error has been reached
	 * 
	 * unexpectedly while parsing the parameter name or value.
	 * 
	 */
	public void setQuotedParameter(String name, String value)
			throws ParseException {
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(value);
			nv.setQuotedValue();
		} else {
			nv = new NameValue(name, value);
			nv.setQuotedValue();
			this.parameters.set(nv);
		}
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 * 
	 * a value it will be overwritten.
	 * 
	 * 
	 * @param name -
	 *            a String specifying the parameter name
	 * 
	 * @param value -
	 *            an int specifying the parameter value
	 * 
	 * @throws ParseException
	 *             which signals that an error has been reached
	 * 
	 * unexpectedly while parsing the parameter name or value.
	 * 
	 */
	protected void setParameter(String name, int value) {
		Integer val = new Integer(value);
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(val);
		} else {
			nv = new NameValue(name, val);
			this.parameters.set(nv);
		}
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 * 
	 * a value it will be overwritten.
	 * 
	 * 
	 * @param name -
	 *            a String specifying the parameter name
	 * 
	 * @param value -
	 *            a boolean specifying the parameter value
	 * 
	 * @throws ParseException
	 *             which signals that an error has been reached
	 * 
	 * unexpectedly while parsing the parameter name or value.
	 * 
	 */
	protected void setParameter(String name, boolean value) {
		Boolean val = new Boolean(value);
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(val);
		} else {
			nv = new NameValue(name, val);
			this.parameters.set(nv);
		}
	}

	/**
	 * Sets the value of the specified parameter. If the parameter already had
	 * 
	 * a value it will be overwritten. A zero-length String indicates flag
	 * 
	 * parameter.
	 * 
	 * 
	 * 
	 * @param name -
	 *            a String specifying the parameter name
	 * 
	 * @param value -
	 *            a String specifying the parameter value
	 * 
	 * @throws ParseException
	 *             which signals that an error has been reached
	 * 
	 * unexpectedly while parsing the parameter name or value.
	 * 
	 */
	protected void setParameter(String name, Object value) {
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(value);
		} else {
			nv = new NameValue(name, value);
			this.parameters.set(nv);
		}
	}

	/**
	 * Return true if has a parameter.
	 * 
	 * @param paramName
	 *            is the name of the parameter.
	 * 
	 * @return true if the parameter exists and false if not.
	 */
	public boolean hasParameter(String parameterName) {
		return this.parameters.hasNameValue(parameterName);
	}

	/**
	 * Remove all parameters.
	 */
	public void removeParameters() {
		this.parameters = new NameValueList();
	}

	/**
	 * get the parameter list.
	 * 
	 * @return parameter list
	 */
	public NameValueList getParameters() {
		return parameters;
	}

	/**
	 * Set the parameter given a name and value.
	 * 
	 * @param nameValue -
	 *            the name value of the parameter to set.
	 */
	public void setParameter(NameValue nameValue) {
		this.parameters.set(nameValue);
	}

	/**
	 * Set the parameter list.
	 * 
	 * @param nameValueList -
	 *            the name value list to set as the parameter list.
	 */
	public void setParameters(NameValueList parameters) {
		this.parameters = parameters;
	}

	/**
	 * Get the parameter as an integer value.
	 * 
	 * @param parameterName --
	 *            the parameter name to fetch.
	 * 
	 * @return -1 if the parameter is not defined in the header.
	 */
	protected int getParameterAsInt(String parameterName) {
		if (this.getParameterValue(parameterName) != null) {
			try {
				if (this.getParameterValue(parameterName) instanceof String) {
					return Integer.parseInt(this.getParameter(parameterName));
				} else {
					return ((Integer) getParameterValue(parameterName))
							.intValue();
				}
			} catch (NumberFormatException ex) {
				return -1;
			}
		} else
			return -1;
	}

	/**
	 * Get the parameter as an integer when it is entered as a hex.
	 * 
	 * @param parameterName --
	 *            The parameter name to fetch.
	 * 
	 * @return -1 if the parameter is not defined in the header.
	 */
	protected int getParameterAsHexInt(String parameterName) {
		if (this.getParameterValue(parameterName) != null) {
			try {
				if (this.getParameterValue(parameterName) instanceof String) {
					return Integer.parseInt(this.getParameter(parameterName),
							16);
				} else {
					return ((Integer) getParameterValue(parameterName))
							.intValue();
				}
			} catch (NumberFormatException ex) {
				return -1;
			}
		} else
			return -1;
	}

	/**
	 * Get the parameter as a long value.
	 * 
	 * @param parameterName --
	 *            the parameter name to fetch.
	 * 
	 * @return -1 if the parameter is not defined or the parameter as a long.
	 */

	protected long getParameterAsLong(String parameterName) {
		if (this.getParameterValue(parameterName) != null) {
			try {
				if (this.getParameterValue(parameterName) instanceof String) {
					return Long.parseLong(this.getParameter(parameterName));
				} else {
					return ((Long) getParameterValue(parameterName))
							.longValue();
				}
			} catch (NumberFormatException ex) {
				return -1;
			}
		} else
			return -1;
	}

	/**
	 * get the parameter value as a URI.
	 * 
	 * @param parameterName --
	 *            the parameter name
	 * 
	 * @return value of the parameter as a URI or null if the parameter not
	 *         present.
	 */
	protected URI getParameterAsURI(String parameterName) {
		Object val = getParameterValue(parameterName);
		if (val instanceof URI)
			return (URI) val;
		else {
			try {
				return new URI((String) val);
			} catch (ParseException ex) {
				return null;
			}
		}
	}

	/**
	 * Get the parameter value as a boolean.
	 * 
	 * @param parameterName --
	 *            the parameter name
	 * @return boolean value of the parameter.
	 */
	protected boolean getParameterAsBoolean(String parameterName) {
		Object val = getParameterValue(parameterName);
		if (val == null) {
			return false;
		} else if (val instanceof Boolean) {
			return ((Boolean) val).booleanValue();
		} else if (val instanceof String) {
			return equalsIgnoreCase((String) val, "true");
		} else
			return false;
	}

	/**
	 * This is for the benifit of the TCK.
	 * 
	 * @return the name value pair for the given parameter name.
	 */
	public NameValue getNameValue(String parameterName) {
		return parameters.getNameValue(parameterName);
	}

	protected abstract String encodeBody();

}
