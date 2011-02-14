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
import gov.nist.core.sip.header.ContentLengthHeader;
import gov.nist.core.sip.header.Header;



/**
 * Parser for Content-Length Header.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author Olivier Deruelle <br/>
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public class ContentLengthParser extends HeaderParser {

	public ContentLengthParser() {
	}

	public ContentLengthParser(String contentLength) {
		super(contentLength);
	}

	protected ContentLengthParser(Lexer lexer) {
		super(lexer);
	}

	public Header parse() throws ParseException {
		try {
			ContentLengthHeader contentLength = new ContentLengthHeader();
			headerName(TokenTypes.CONTENT_LENGTH);
			String number = this.lexer.number();
			contentLength.setContentLength(Integer.parseInt(number));
			this.lexer.SPorHT();
			this.lexer.match('\n');
			return contentLength;
		} catch (Exception ex) {
			throw createParseException(ex.getMessage());
		}
	}
}
