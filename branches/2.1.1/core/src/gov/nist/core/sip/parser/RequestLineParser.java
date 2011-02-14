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
import gov.nist.core.sip.address.URI;
import gov.nist.core.sip.header.RequestLine;



/**
 * Parser for the SIP request line.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */

class RequestLineParser extends Parser {
	public RequestLineParser(String requestLine) {
		this.lexer = new Lexer("method_keywordLexer", requestLine);
	}

	public RequestLineParser(Lexer lexer) {
		this.lexer = lexer;
		this.lexer.selectLexer("method_keywordLexer");
	}

	public RequestLine parse() throws ParseException {
		RequestLine retval = new RequestLine();
		String m = method();
		lexer.SPorHT();
		retval.setMethod(m);
		this.lexer.selectLexer("sip_urlLexer");
		URLParser urlParser = new URLParser(this.getLexer());
		URI url = urlParser.uriReference();
		lexer.SPorHT();
		retval.setUri(url);
		this.lexer.selectLexer("request_lineLexer");
		String v = sipVersion();
		retval.setSipVersion(v);
		lexer.SPorHT();
		lexer.match('\n');
		return retval;
	}
}
