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
import gov.nist.core.sip.header.ContentTypeHeader;
import gov.nist.core.sip.header.Header;



/**
 * Parser for content type header.
 * 
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public class ContentTypeParser extends ParametersParser {

	ContentTypeParser() {
	}

	public ContentTypeParser(String contentType) {
		super(contentType);
	}

	protected ContentTypeParser(Lexer lexer) {
		super(lexer);
	}

	public Header parse() throws ParseException {
		ContentTypeHeader contentType = new ContentTypeHeader();
		this.headerName(TokenTypes.CONTENT_TYPE);

		// The type:
		lexer.match(TokenTypes.ID);
		Token type = lexer.getNextToken();
		this.lexer.SPorHT();
		contentType.setContentType(type.getTokenValue());

		// The sub-type:
		lexer.match('/');
		lexer.match(TokenTypes.ID);
		Token subType = lexer.getNextToken();
		this.lexer.SPorHT();
		contentType.setContentSubType(subType.getTokenValue());
		super.parse(contentType);
		this.lexer.match('\n');
		return contentType;

	}
}
