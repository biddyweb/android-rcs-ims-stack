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

import gov.nist.core.ParseException;
import gov.nist.core.Separators;

/**
 * Event SIP Header.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/> <a href="{@docRoot}/uncopyright.html">This
 *         code is in the public domain.</a>
 * 
 */
public class EventHeader extends ParametersHeader {

	protected String eventType;

	public final static String ID = "id";

	public final static String NAME = Header.EVENT;

	public static Class clazz;

	static {
		clazz = new EventHeader().getClass();
	}

	/** Creates a new instance of Event */
	public EventHeader() {
		super(EVENT);
	}

	/**
	 * Sets the eventType to the newly supplied eventType string.
	 * 
	 * @param eventType -
	 *            the new string defining the eventType supported in this
	 *            EventHeader
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the eventType value.
	 */
	public void setEventType(String eventType) throws ParseException {
		if (eventType == null)
			throw new NullPointerException(" the eventType is null");
		this.eventType = eventType;
	}

	/**
	 * Gets the eventType of the EventHeader.
	 * 
	 * @return the string object identifing the eventType of EventHeader.
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * Sets the id to the newly supplied <var>eventId</var> string.
	 * 
	 * @param eventId -
	 *            the new string defining the eventId of this EventHeader
	 * @throws ParseException
	 *             which signals that an error has been reached unexpectedly
	 *             while parsing the eventId value.
	 */
	public void setEventId(String eventId) throws ParseException {
		if (eventId == null)
			throw new NullPointerException("the eventId parameter is null");
		setParameter(ID, eventId);
	}

	/**
	 * Gets the id of the EventHeader. This method may return null if the
	 * "eventId" is not set.
	 * 
	 * @return the string object identifing the eventId of EventHeader.
	 */
	public String getEventId() {
		return getParameter(ID);
	}

	/**
	 * Encode in canonical form.
	 * 
	 * @return String
	 */
	public String encodeBody() {
		StringBuffer retval = new StringBuffer();

		if (eventType != null)
			retval.append(Separators.SP + eventType + Separators.SP);

		if (!parameters.isEmpty())
			retval.append(Separators.SEMICOLON + this.parameters.encode());
		return retval.toString();
	}

	/**
	 * Return true if the given event header matches the supplied one.
	 * 
	 * @param matchTarget --
	 *            event header to match against.
	 */
	public boolean match(EventHeader matchTarget) {
		if (matchTarget.eventType == null && this.eventType != null)
			return false;
		else if (matchTarget.eventType != null && this.eventType == null)
			return false;
		else if (this.eventType == null && matchTarget.eventType == null)
			return false;
		else if (getEventId() == null && matchTarget.getEventId() != null)
			return false;
		else if (getEventId() != null && matchTarget.getEventId() == null)
			return false;
		return equalsIgnoreCase(matchTarget.eventType, this.eventType)
				&& ((this.getEventId() == matchTarget.getEventId()) || equalsIgnoreCase(
						this.getEventId(), matchTarget.getEventId()));
	}

	public Object getValue() {
		return this.eventType;
	}

}
