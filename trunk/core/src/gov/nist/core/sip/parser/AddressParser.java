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
import gov.nist.core.sip.address.Address;
import gov.nist.core.sip.address.URI;



/**
 * Parser for addresses.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */

public class AddressParser extends Parser {

	protected AddressParser(Lexer lexer) {
		this.lexer = lexer;
		this.lexer.selectLexer("charLexer");
	}

	public AddressParser(String address) {
		this.lexer = new Lexer("charLexer", address);
	}

	protected Address nameAddr() throws ParseException {
		if (this.lexer.lookAhead(0) == '<') {
			this.lexer.match('<');
			this.lexer.selectLexer("sip_urlLexer");
			this.lexer.SPorHT();
			URLParser uriParser = new URLParser((Lexer) lexer);
			URI uri = uriParser.uriReference();
			Address retval = new Address();
			retval.setAddressType(Address.NAME_ADDR);
			retval.setURI(uri);
			this.lexer.SPorHT();
			this.lexer.match('>');
			return retval;
		} else {
			Address addr = new Address();
			addr.setAddressType(Address.NAME_ADDR);
			String name = null;
			if (this.lexer.lookAhead(0) == '\"') {
				name = this.lexer.quotedString();
				this.lexer.SPorHT();
			} else
				name = this.lexer.getNextToken('<');
			addr.setDisplayName(name.trim());
			this.lexer.match('<');
			this.lexer.SPorHT();
			URLParser uriParser = new URLParser((Lexer) lexer);
			URI uri = uriParser.uriReference();
			addr.setAddressType(Address.NAME_ADDR);
			addr.setURI(uri);
			this.lexer.SPorHT();
			this.lexer.match('>');
			return addr;
		}
	}

	public Address address() throws ParseException {
		Address retval = null;
		int k = 0;
		while (lexer.hasMoreChars()) {
			if (lexer.lookAhead(k) == '<' || lexer.lookAhead(k) == '\"'
					|| lexer.lookAhead(k) == ':'
					|| lexer.lookAhead(k) == '/')
				break;
			else if (lexer.lookAhead(k) == '\0')
				throw createParseException("unexpected EOL");
			else
				k++;
		}
		if (this.lexer.lookAhead(k) == '<'
				|| this.lexer.lookAhead(k) == '\"') {
			retval = nameAddr();
		} else if (this.lexer.lookAhead(k) == ':'
				|| this.lexer.lookAhead(k) == '/') {
			retval = new Address();
			URLParser uriParser = new URLParser((Lexer) lexer);
			URI uri = uriParser.uriReference();
			retval.setAddressType(Address.ADDRESS_SPEC);
			retval.setURI(uri);
		} else {
			throw createParseException("Bad address spec");
		}
		return retval;
	}
}
