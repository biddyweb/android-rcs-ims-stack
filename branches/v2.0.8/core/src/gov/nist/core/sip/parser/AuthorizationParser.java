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
import gov.nist.core.sip.header.AuthorizationHeader;
import gov.nist.core.sip.header.Header;



/**
 * Parser for authorization headers.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author Olivier Deruelle <deruelle@nist.gov>
 * @author M. Ranganathan <mranga@nist.gov> <a href="{@docRoot}/uncopyright.html">This
 *         code is in the public domain.</a>
 * 
 */
public class AuthorizationParser extends ChallengeParser {

	public AuthorizationParser() {
	}

	/**
	 * Constructor
	 * 
	 * @param String
	 *            Authorization message to parse
	 */
	public AuthorizationParser(String authorization) {
		super(authorization);
	}

	/**
	 * Cosntructor
	 * 
	 * @param Lexer
	 *            lexer to set
	 */
	protected AuthorizationParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * 
	 * @return Header (Authorization object)
	 * @throws ParseException
	 *             if the message does not respect the spec.
	 */
	public Header parse() throws ParseException {
		headerName(TokenTypes.AUTHORIZATION);
		AuthorizationHeader auth = new AuthorizationHeader();
		super.parse(auth);
		return auth;
	}
}
