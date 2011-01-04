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

import java.util.Vector;

/**
 * Implements a simple NameValue association with a quick lookup.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */

public class NameValueList extends GenericObject {
	private Vector nvList;

	private String separator;

	public NameValueList(String listName) {
		nvList = new Vector();
		this.separator = ";";
	}

	public Vector getNames() {
		Vector names = new Vector();
		for (int i = 0; i < names.size(); i++) {
			names.addElement(((NameValue) nvList.elementAt(i)).name);
		}
		return names;
	}

	public NameValue elementAt(int i) {
		if (i >= nvList.size())
			throw new IndexOutOfBoundsException("Index out of bounds" + i);
		return (NameValue) nvList.elementAt(i);
	}

	public void add(NameValue nv) {
		if (nv == null)
			throw new NullPointerException("null nv");
		nvList.addElement(nv);
	}

	/**
	 * Set a namevalue object in this list.
	 */

	public void set(NameValue nv) {
		this.delete(nv.name);
		this.add(nv);
	}

	/**
	 * Set a namevalue object in this list.
	 */
	public void set(String name, Object value) {
		NameValue nv = new NameValue(name, value);
		this.set(nv);
	}

	/**
	 * Add a name value record to this list.
	 */

	public void add(String name, Object obj) {
		if (name == null)
			throw new NullPointerException("name in null ! ");
		NameValue nv = new NameValue(name, obj);
		add(nv);
	}

	/**
	 * Compare if two NameValue lists are equal.
	 * 
	 * @param otherObject
	 *            is the object to compare to.
	 * @return true if the two objects compare for equality.
	 */
	public boolean equals(Object otherObject) {
		if (!otherObject.getClass().equals(this.getClass())) {
			return false;
		}
		NameValueList other = (NameValueList) otherObject;

		if (this.nvList.size() != other.nvList.size()) {
			return false;
		}
		for (int i = 0; i < nvList.size(); i++) {
			NameValue nv = (NameValue) nvList.elementAt(i);
			NameValue nv1 = other.getNameValue(nv.name);
			if (nv1 == null) {
				return false;
			}
			if (!nv1.equals(nv))
				return false;
		}
		return true;
	}

	/**
	 * Do a lookup on a given name and return value associated with it.
	 */
	public Object getValue(String name) {
		NameValue nv = this.getNameValue(name);
		if (nv != null)
			return nv.value;
		else
			return null;
	}

	/**
	 * Get the NameValue record given a name.
	 * 
	 * @since 1.0
	 */
	public NameValue getNameValue(String name) {
		if (name == null)
			throw new NullPointerException("null arg!");
		String name1 = name.toLowerCase();
		for (int i = 0; i < nvList.size(); i++) {
			NameValue nv = (NameValue) nvList.elementAt(i);
			if (nv.getName() != null
					&& nv.getName().toLowerCase().equals(name1))
				return nv;
		}
		return null;
	}

	/**
	 * Returns a boolean telling if this NameValueList has a record with this
	 * name
	 * 
	 * @since 1.0
	 */
	public boolean hasNameValue(String name) {
		return getNameValue(name) != null;
	}

	/**
	 * Remove the element corresponding to this name.
	 * 
	 * @since 1.0
	 */
	public boolean delete(String name) {
		int i = 0;
		String name1 = name.toLowerCase();
		for (i = 0; i < nvList.size(); i++) {
			NameValue nv = (NameValue) nvList.elementAt(i);
			if (nv.getName() != null
					&& nv.getName().toLowerCase().equals(name1))
				break;
		}
		if (i < nvList.size())
			nvList.removeElementAt(i);
		return true;
	}

	/**
	 * default constructor.
	 */
	public NameValueList() {
		nvList = new Vector();
		this.separator = ";";
	}

	public Object clone() {
		NameValueList retval = new NameValueList();
		retval.separator = this.separator;
		for (int i = 0; i < nvList.size(); i++) {
			NameValue nv = (NameValue) nvList.elementAt(i);
			NameValue nnv = (NameValue) nv.clone();
			retval.add(nnv);
		}
		return retval;

	}

	/**
	 * Get the parameter as a String.
	 * 
	 * @return the parameter as a string.
	 */
	public String getParameter(String name) {
		Object val = this.getValue(name);
		if (val == null)
			return null;
		if (val instanceof GenericObject)
			return ((GenericObject) val).encode();
		else
			return val.toString();
	}

	/**
	 * Get the first element of the list.
	 */
	public Object first() {
		return nvList.elementAt(0);
	}

	public String encode() {
		if (nvList.size() == 0)
			return "";
		StringBuffer encoding = new StringBuffer();
		for (int i = 0; i < nvList.size(); i++) {
			Object obj = nvList.elementAt(i);
			if (obj instanceof GenericObject) {
				GenericObject gobj = (GenericObject) obj;
				encoding.append(gobj.encode());
			} else {
				encoding.append(obj.toString());
			}
			if (i < nvList.size() - 1)
				encoding.append(separator);
			else
				break;
		}
		return encoding.toString();
	}

	public String toString() {
		return this.encode();
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public boolean isEmpty() {
		return this.nvList.size() == 0;
	}

	public int size() {
		return nvList.size();
	}

}
