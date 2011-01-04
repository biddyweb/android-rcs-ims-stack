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

import gov.nist.core.NameValue;
import gov.nist.core.ParseException;
import gov.nist.core.Token;
import gov.nist.core.sip.header.AuthenticationHeader;



/**
 * Parser for the challenge portion of the authentication header.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author Olivier Deruelle <deruelle@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 * @version 1.0
 */

public abstract class ChallengeParser extends HeaderParser {

	protected ChallengeParser() {
	}

	/**
	 * Constructor
	 * 
	 * @param String
	 *            challenge message to parse to set
	 */
	protected ChallengeParser(String challenge) {
		super(challenge);
	}

	/**
	 * Constructor
	 * 
	 * @param String
	 *            challenge message to parse to set
	 */
	protected ChallengeParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * Get the parameter of the challenge string
	 * 
	 * @return NameValue containing the parameter
	 */
	protected void parseParameter(AuthenticationHeader header)
			throws ParseException {
		NameValue nv = this.nameValue('=');
		header.setParameter(nv);
	}

	/**
	 * parser the String message
	 * 
	 * @return Challenge object
	 * @throws ParseException
	 *             if the message does not respect the spec.
	 */
	public void parse(AuthenticationHeader header) throws ParseException {

		// the Scheme:
		this.lexer.SPorHT();
		lexer.match(TokenTypes.ID);
		Token type = lexer.getNextToken();
		this.lexer.SPorHT();
		header.setScheme(type.getTokenValue());

		// The parameters:
		try {
			while (lexer.lookAhead(0) != '\n') {
				this.parseParameter(header);
				this.lexer.SPorHT();
				if (lexer.lookAhead(0) == '\n' || lexer.lookAhead(0) == '\0')
					break;
				this.lexer.match(',');
				this.lexer.SPorHT();
			}
		} catch (ParseException ex) {
			throw ex;
		}

	}

}
