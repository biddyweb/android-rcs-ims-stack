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
package gov.nist.core;

/**
 * Generic structure for storing name-value pairs.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public class NameValue extends GenericObject {
	protected boolean isQuotedString;

	protected String separator;

	protected String quotes;

	protected String name;

	protected Object value;

	public NameValue() {
		name = null;
		value = null;
		separator = Separators.EQUALS;
		this.quotes = "";
	}

	public NameValue(String n, Object v) {
		name = n;
		value = v;
		separator = Separators.EQUALS;
		quotes = "";
	}

	/**
	 * Set the separator for the encoding method below.
	 */
	public void setSeparator(String sep) {
		separator = sep;
	}

	/**
	 * A flag that indicates that doublequotes should be put around the value
	 * when encoded (for example name=value when value is doublequoted).
	 */
	public void setQuotedValue() {
		isQuotedString = true;
		this.quotes = Separators.DOUBLE_QUOTE;
	}

	/**
	 * Return true if the value is quoted in doublequotes.
	 */
	public boolean isValueQuoted() {
		return isQuotedString;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	/**
	 * Set the name member
	 */
	public void setName(String n) {
		name = n;
	}

	/**
	 * Set the value member
	 */
	public void setValue(Object v) {
		value = v;
	}

	/**
	 * Get the encoded representation of this namevalue object. Added
	 * doublequote for encoding doublequoted values (bug reported by Kirby
	 * Kiem).
	 * 
	 * @since 1.0
	 * @return an encoded name value (eg. name=value) string.
	 */
	public String encode() {
		if (name != null && value != null) {
			return name + separator + quotes + value.toString() + quotes;
		} else if (name == null && value != null) {
			return quotes + value.toString() + quotes;
		} else if (name != null && value == null) {
			return name;
		} else
			return "";
	}

	public Object clone() {
		NameValue retval = new NameValue();
		retval.separator = this.separator;
		retval.isQuotedString = this.isQuotedString;
		retval.quotes = this.quotes;
		retval.name = this.name;
		if (value != null && value instanceof GenericObject) {
			retval.value = ((GenericObject) this.value).clone();
		} else
			retval.value = this.value;
		return retval;
	}

	/**
	 * Equality comparison predicate.
	 */
	public boolean equals(Object other) {
		if (!other.getClass().equals(this.getClass()))
			return false;
		NameValue that = (NameValue) other;
		if (this == that)
			return true;
		if (this.name == null && that.name != null || this.name != null
				&& that.name == null)
			return false;
		if (this.name != null
				&& that.name != null
				&& this.name.toLowerCase().compareTo(that.name.toLowerCase()) != 0)
			return false;
		if (this.value != null && that.value == null || this.value == null
				&& that.value != null)
			return false;
		if (this.value == that.value)
			return true;
		if (value instanceof String) {
			// Quoted string comparisions are case sensitive.
			if (isQuotedString)
				return this.value.equals(that.value);
			String val = (String) this.value;
			String val1 = (String) that.value;
			return val.toLowerCase().equals(val1.toLowerCase());
		} else
			return this.value.equals(that.value);
	}

	public String toString() {
		return this.encode();
	}

}
