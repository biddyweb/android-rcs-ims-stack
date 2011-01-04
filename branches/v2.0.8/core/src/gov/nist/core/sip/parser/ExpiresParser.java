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
import gov.nist.core.sip.header.ExpiresHeader;
import gov.nist.core.sip.header.Header;



/**
 * Parser for SIP Expires Parser. Converts from SIP Date to the internal storage
 * (Calendar).
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public class ExpiresParser extends HeaderParser {

	public ExpiresParser() {
	}

	/**
	 * protected constructor.
	 * 
	 * @param text
	 *            is the text of the header to parse
	 */
	public ExpiresParser(String text) {
		super(text);
	}

	/**
	 * constructor.
	 * 
	 * @param lexer
	 *            is the lexer passed in from the enclosing parser.
	 */
	protected ExpiresParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * Parse the header.
	 */
	public Header parse() throws ParseException {
		ExpiresHeader expires = new ExpiresHeader();
		lexer.match(TokenTypes.EXPIRES);
		lexer.SPorHT();
		lexer.match(':');
		lexer.SPorHT();
		String nextId = lexer.getNextId();
		lexer.match('\n');
		try {
			int delta = Integer.parseInt(nextId);
			expires.setExpires(delta);
			return expires;
		} catch (NumberFormatException ex) {
			throw createParseException("bad integer format");
		}
	}
}
