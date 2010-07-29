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
package gov.nist.core.sip.message;


import gov.nist.core.sip.header.ContactHeader;
import gov.nist.core.sip.header.ContactList;
import gov.nist.core.sip.header.ExtensionHeader;
import gov.nist.core.sip.header.Header;
import gov.nist.core.sip.header.HeaderList;
import gov.nist.core.sip.header.ProxyAuthenticateHeader;
import gov.nist.core.sip.header.ProxyAuthenticateList;
import gov.nist.core.sip.header.RecordRouteHeader;
import gov.nist.core.sip.header.RecordRouteList;
import gov.nist.core.sip.header.RouteHeader;
import gov.nist.core.sip.header.RouteList;
import gov.nist.core.sip.header.ViaHeader;
import gov.nist.core.sip.header.ViaList;
import gov.nist.core.sip.header.WWWAuthenticateHeader;
import gov.nist.core.sip.header.WWWAuthenticateList;

import java.util.Hashtable;


/**
 * A map of which of the standard headers may appear as a list
 */

class ListMap {
	// A table that indicates whether a header has a list representation or
	// not (to catch adding of the non-list form when a list exists.)
	// Entries in this table allow you to look up the list form of a header
	// (provided it has a list form).
	private static Hashtable headerListTable;
	static {
		initializeListMap();
	}

	/**
	 * Build a table mapping between objects that have a list form and the class
	 * of such objects.
	 */
	static private void initializeListMap() {
		headerListTable = new Hashtable();
		headerListTable.put(ExtensionHeader.clazz, new HeaderList().getClass());

		headerListTable.put(ContactHeader.clazz, new ContactList().getClass());

		headerListTable.put(ViaHeader.clazz, new ViaList().getClass());

		headerListTable.put(WWWAuthenticateHeader.clazz,
				new WWWAuthenticateList().getClass());

		headerListTable.put(RouteHeader.clazz, new RouteList().getClass());

		headerListTable.put(ProxyAuthenticateHeader.clazz,
				new ProxyAuthenticateList().getClass());

		headerListTable.put(RecordRouteHeader.clazz, new RecordRouteList()
				.getClass());

	}

	/**
	 * return true if this has an associated list object.
	 */
	static protected boolean hasList(Header sipHeader) {
		if (sipHeader instanceof HeaderList)
			return false;
		else {
			Class headerClass = sipHeader.getClass();
			return headerListTable.get(headerClass) != null;
		}
	}

	/**
	 * Return true if this has an associated list object.
	 */
	static protected boolean hasList(Class sipHdrClass) {
		return headerListTable.get(sipHdrClass) != null;
	}

	/**
	 * Get the associated list class.
	 */
	static protected Class getListClass(Class sipHdrClass) {
		return (Class) headerListTable.get(sipHdrClass);
	}

	/**
	 * Return a list object for this header if it has an associated list object.
	 */
	static protected HeaderList getList(Header sipHeader) {
		try {
			Class headerClass = sipHeader.getClass();
			Class listClass = (Class) headerListTable.get(headerClass);
			HeaderList shl = (HeaderList) listClass.newInstance();
			shl.setHeaderName(sipHeader.getHeaderName());
			return shl;
		} catch (Exception ex) {
			return null;
		}
	}

}
