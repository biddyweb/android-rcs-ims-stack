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
import gov.nist.core.ParserCore;
import gov.nist.core.Token;

import java.util.Vector;


/**
 * Base parser class.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */

public abstract class Parser extends ParserCore implements TokenTypes {

	protected Parser() {
	}

	protected ParseException createParseException(String exceptionString) {
		return new ParseException(lexer.getBuffer() + ":" + exceptionString,
				lexer.getPtr());
	}

	protected Lexer getLexer() {
		return (Lexer) this.lexer;
	}

	protected String sipVersion() throws ParseException {
		Token tok = lexer.match(SIP);
		if (!tok.getTokenValue().equals("SIP"))
			createParseException("Expecting SIP");
		lexer.match('/');
		tok = lexer.match(ID);
		if (!tok.getTokenValue().equals("2.0"))
			createParseException("Expecting SIP/2.0");

		return "SIP/2.0";
	}

	/**
	 * parses a method. Consumes if a valid method has been found.
	 */
	protected String method() throws ParseException {
		Vector tokens = this.lexer.peekNextToken(1);
		Token token = (Token) tokens.elementAt(0);
		if (token.getTokenType() == INVITE || token.getTokenType() == ACK
				|| token.getTokenType() == OPTIONS
				|| token.getTokenType() == BYE
				|| token.getTokenType() == REGISTER
				|| token.getTokenType() == CANCEL
				|| token.getTokenType() == SUBSCRIBE
				|| token.getTokenType() == NOTIFY
				|| token.getTokenType() == ID) {
			lexer.consume();
			return token.getTokenValue();
		} else {
			throw createParseException("Invalid Method");
		}
	}
}
