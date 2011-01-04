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
package gov.nist.core.sip.parser;

import gov.nist.core.ParseException;
import gov.nist.core.sip.message.Message;



/**
 * A listener interface that enables customization of parse error handling. An
 * class that implements this interface is registered with the parser and is
 * called back from the parser handle parse errors.
 */

public interface ParseExceptionListener {
	/**
	 * This gets called from the parser when a parse error is generated. The
	 * handler is supposed to introspect on the error class and header name to
	 * handle the error appropriately. The error can be handled by :
	 * <ul>
	 * <li>1. Re-throwing an exception and aborting the parse.
	 * <li>2. Ignoring the header (attach the unparseable header to the Message
	 * being parsed).
	 * <li>3. Re-Parsing the bad header and adding it to the sipMessage
	 * </ul>
	 * 
	 * @param ex -
	 *            parse exception being processed.
	 * @param sipMessage --
	 *            sip message being processed.
	 * @param headerText --
	 *            header/RL/SL text being parsed.
	 * @param messageText --
	 *            message where this header was detected.
	 */
	public void handleException(ParseException ex, Message sipMessage,
			Class headerClass, String headerText, String messageText)
			throws ParseException;
}
