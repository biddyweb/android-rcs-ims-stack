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
import gov.nist.core.Token;
import gov.nist.core.sip.header.EventHeader;
import gov.nist.core.sip.header.Header;



/**
 * Parser for Event header.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author Olivier Deruelle <deruelle@nist.gov>
 * @author M. Ranganathan <mranga@nist.gov> <a href="{@docRoot}/uncopyright.html">This
 *         code is in the public domain.</a>
 * 
 * @version 1.0
 */
public class EventParser extends ParametersParser {

	EventParser() {
	}

	/**
	 * Creates a new instance of EventParser
	 * 
	 * @param event
	 *            the header to parse
	 */
	public EventParser(String event) {
		super(event);
	}

	/**
	 * Cosntructor
	 * 
	 * @param lexer
	 *            the lexer to use to parse the header
	 */
	protected EventParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * 
	 * @return Header (Event object)
	 * @throws SIPParseException
	 *             if the message does not respect the spec.
	 */
	public Header parse() throws ParseException {
		try {
			headerName(TokenTypes.EVENT);
			this.lexer.SPorHT();

			EventHeader event = new EventHeader();
			this.lexer.match(TokenTypes.ID);
			Token token = lexer.getNextToken();
			String value = token.getTokenValue();

			event.setEventType(value);
			super.parse(event);

			this.lexer.SPorHT();
			this.lexer.match('\n');

			return event;

		} catch (ParseException ex) {
			throw createParseException(ex.getMessage());
		}
	}
}
