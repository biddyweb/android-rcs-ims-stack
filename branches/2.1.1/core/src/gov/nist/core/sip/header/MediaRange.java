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

import gov.nist.core.GenericObject;
import gov.nist.core.Separators;

/**
 * Media Range
 * 
 * @see Accept
 * @since 0.9
 * @version 1.0
 * 
 * <pre>
 *  Revisions:
 * 
 *  Version 1.0
 *     1. Added encode method.
 * 
 *  media-range    = ( &quot;STAR/STAR&quot;
 *                         | ( type &quot;/&quot; STAR )
 *                         | ( type &quot;/&quot; subtype )
 *                         ) *( &quot;;&quot; parameter )       
 *  
 *  HTTP RFC 2616 Section 14.1
 * </pre>
 */
public class MediaRange extends GenericObject {

	/**
	 * type field
	 */
	protected String type;

	/**
	 * subtype field
	 */
	protected String subtype;

	public Object clone() {
		MediaRange retval = new MediaRange();
		if (type != null)
			retval.type = new String(this.type);
		if (subtype != null)
			retval.subtype = new String(this.subtype);
		return retval;
	}

	/**
	 * Default constructor
	 */
	public MediaRange() {
	}

	/**
	 * get type field
	 * 
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/**
	 * get the subType field.
	 * 
	 * @return String
	 */
	public String getSubtype() {
		return subtype;
	}

	/**
	 * Set the type member
	 * 
	 * @param t
	 *            String to set
	 */
	public void setType(String t) {
		type = t;
	}

	/**
	 * Set the subtype member
	 * 
	 * @param s
	 *            String to set
	 */
	public void setSubtype(String s) {
		subtype = s;
	}

	/**
	 * Encode the object.
	 * 
	 * @return String
	 */
	public String encode() {
		String encoding = type + Separators.SLASH + subtype;
		return encoding;
	}

}
