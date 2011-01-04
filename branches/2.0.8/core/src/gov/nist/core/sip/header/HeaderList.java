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

import java.util.Enumeration;
import java.util.Vector;


/**
 * This is a list for the headers.
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */

public class HeaderList extends Header {

	protected Vector<Header> sipHeaderVector;

	public Object clone() {
		try {
			HeaderList retval = (HeaderList) this.getClass().newInstance();
			if (this.headerName != null)
				retval.headerName = new String(this.headerName);
			if (this.headerValue != null)
				retval.headerValue = new String(this.headerValue);
			retval.sipHeaderVector = new Vector<Header>();
			for (int i = 0; i < sipHeaderVector.size(); i++) {
				Header siphdr = (Header) sipHeaderVector.elementAt(i);
				Header newHdr = (Header) siphdr.clone();
				retval.sipHeaderVector.addElement(newHdr);
			}
			return (Object) retval;
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Default constructor
	 * 
	 */
	public HeaderList() {
		sipHeaderVector = new Vector();
	}

	/**
	 * Concatenate two compatible lists. This appends or prepends the new list
	 * to the end of this list.
	 * 
	 * @param other
	 *            HeaderList to set
	 * @param top
	 *            boolean to set
	 */
	public void concatenate(HeaderList other, boolean top) {
		if (other != null) {
			if (top) {
				for (int i = 0; i < size(); i++) {
					Header sipHeader = (Header) elementAt(i);
					other.add(sipHeader);
				}
			} else {
				for (int i = 0; i < other.size(); i++) {
					Header sipHeader = (Header) other.elementAt(i);
					add(sipHeader);
				}
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param sipHeaderName
	 *            to set
	 */
	public HeaderList(String sipHeaderName) {
		sipHeaderVector = new Vector();
		this.headerName = sipHeaderName;
	}

	/**
	 * Add a new element.
	 * 
	 * @param sipHeader
	 *            to add
	 */
	public void add(Header sipHeader) throws IllegalArgumentException {
		if (!headerName.equals(((Header) sipHeader).getHeaderName()))
			throw new IllegalArgumentException("bad type");
		if (sipHeader != null)
			sipHeaderVector.addElement(sipHeader);
	}

	/**
	 * Add a new element on the top of the list.
	 * 
	 * @param sipHeader to add
	 */
	public void addFirst(Object sipHeader) {
		if (sipHeader != null) {
			Vector vec = new Vector();
			vec.addElement(sipHeader);
			for (int i = 0; i < sipHeaderVector.size(); i++) {
				vec.addElement(sipHeaderVector.elementAt(i));
			}
			sipHeaderVector = vec;
		}
	}

	/**
	 * return true if this is empty.
	 */
	public boolean isEmpty() {
		return sipHeaderVector.isEmpty();
	}

	/**
	 * return the size
	 * 
	 * @return size
	 */
	public int size() {
		return sipHeaderVector.size();
	}

	/**
	 * return the element at the position i
	 * 
	 * @return Object
	 * @param int
	 *            i to set
	 */
	public Object elementAt(int i) {
		return sipHeaderVector.elementAt(i);
	}

	/**
	 * remove the specified element
	 * 
	 * @param the
	 *            element to delete
	 */
	public void removeElement(Object element) {
		sipHeaderVector.removeElement(element);
	}

	/**
	 * Remove the first element of the list.
	 */
	public void removeFirst() {
		if (sipHeaderVector.size() == 0)
			return;
		else
			sipHeaderVector.removeElementAt(0);

	}

	public void removeLast() {
		if (sipHeaderVector.size() != 0) {
			sipHeaderVector.removeElementAt(sipHeaderVector.size() - 1);
		}
	}

	/**
	 * Return a vector of encoded strings (one for each sipheader).
	 * 
	 * @return Vector containing encoded strings in this header list. an empty
	 *         vector is returned if this header list contains no sip headers.
	 */
	public Vector getHeadersAsEncodedStrings() {
		Vector retval = new Vector();

		for (int i = 0; i < size(); i++) {
			Header sipheader = (Header) elementAt(i);
			retval.addElement(sipheader.encode());
		}
		return retval;

	}

	/**
	 * Return an enumeration of the imbedded vector.
	 * 
	 * @return an Enumeration of the elements of the vector.
	 */
	public Enumeration getElements() {
		return this.sipHeaderVector.elements();
	}

	/**
	 * Get the first element of the vector.
	 * 
	 * @return the first element of the vector.
	 */
	public Header getFirst() {
		if (sipHeaderVector.size() == 0)
			return null;
		return (Header) this.sipHeaderVector.elementAt(0);
	}

	public Object first() {
		if (sipHeaderVector.size() == 0)
			return null;
		return this.sipHeaderVector.elementAt(0);
	}

	public Object last() {
		if (sipHeaderVector.size() == 0)
			return null;
		else
			return (Header) this.sipHeaderVector.elementAt(sipHeaderVector
					.size() - 1);

	}

	public Object getValue() {
		Vector retval = new Vector();
		for (int i = 0; i < size(); i++) {
			Header sipheader = (Header) elementAt(i);
			retval.addElement(sipheader);
		}
		return retval;
	}

	public NameValueList getParameters() {
		return null;
	}

	public String encode() {
		if (sipHeaderVector.isEmpty())
			return "";
		StringBuffer encoding = new StringBuffer();
		// The following headers do not have comma separated forms for
		// multiple headers. Thus, they must be encoded separately.
		if (this.headerName.equals(WWW_AUTHENTICATE)
				|| this.headerName.equals(VIA) // Modif IOT: multiple Via list was not supported
				|| this.headerName.equals(PROXY_AUTHENTICATE)
				|| this.headerName.equals(AUTHORIZATION)
				|| this.headerName.equals(PROXY_AUTHORIZATION)) {
			for (int i = 0; i < sipHeaderVector.size(); i++) {
				Header sipheader = (Header) sipHeaderVector.elementAt(i);
				encoding.append(sipheader.encode());
			}
			return encoding.toString();
		} else {
			// These can be concatenated together in an comma separated
			// list.
			return this.headerName + Separators.COLON + Separators.SP
					+ this.encodeBody() + Separators.NEWLINE;
		}

	}

	protected String encodeBody() {
		StringBuffer sbuf = new StringBuffer();
		for (int i = 0; i < sipHeaderVector.size(); i++) {
			Header sipHeader = (Header) sipHeaderVector.elementAt(i);
			sbuf.append(sipHeader.encodeBody());
			if (i + 1 < sipHeaderVector.size())
				sbuf.append(",");
		}
		return sbuf.toString();
	}

	public Vector<Header> getHeaders() {
		return this.sipHeaderVector;
	}

}
