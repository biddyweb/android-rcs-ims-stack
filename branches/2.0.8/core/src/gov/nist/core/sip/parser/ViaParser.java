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

import gov.nist.core.HostNameParser;
import gov.nist.core.HostPort;
import gov.nist.core.NameValue;
import gov.nist.core.ParseException;
import gov.nist.core.Token;
import gov.nist.core.sip.header.Header;
import gov.nist.core.sip.header.Protocol;
import gov.nist.core.sip.header.ViaHeader;
import gov.nist.core.sip.header.ViaList;



/**
 * Parser for via headers.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author Olivier Deruelle
 * @author M. Ranganathan <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public class ViaParser extends HeaderParser {
	ViaParser() {
	}

	public ViaParser(String via) {
		super(via);
	}

	public ViaParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * a parser for the essential part of the via header.
	 */
	private void parseVia(ViaHeader v) throws ParseException {
		// The protocol
		lexer.match(TokenTypes.ID);
		Token protocolName = lexer.getNextToken();

		this.lexer.SPorHT();
		// consume the "/"
		lexer.match('/');
		this.lexer.SPorHT();
		lexer.match(TokenTypes.ID);
		this.lexer.SPorHT();
		Token protocolVersion = lexer.getNextToken();

		this.lexer.SPorHT();

		// We consume the "/"
		lexer.match('/');
		this.lexer.SPorHT();
		lexer.match(TokenTypes.ID);
		this.lexer.SPorHT();

		Token transport = lexer.getNextToken();
		this.lexer.SPorHT();

		Protocol protocol = new Protocol();
		protocol.setProtocolName(protocolName.getTokenValue());
		protocol.setProtocolVersion(protocolVersion.getTokenValue());
		protocol.setTransport(transport.getTokenValue());
		v.setSentProtocol(protocol);

		// sent-By
		HostNameParser hnp = new HostNameParser(this.getLexer());
		HostPort hostPort = hnp.hostPort();
		v.setSentBy(hostPort);

		// Ignore blanks
		this.lexer.SPorHT();

		// parameters
		while (lexer.lookAhead(0) == ';') {
			this.lexer.match(';');
			this.lexer.SPorHT();
			NameValue nameValue = this.nameValue();
			String name = nameValue.getName();
			nameValue.setName(name.toLowerCase());
			v.setParameter(nameValue);
			this.lexer.SPorHT();
		}

		if (lexer.lookAhead(0) == '(') {
			this.lexer.selectLexer("charLexer");
			lexer.consume(1);
			StringBuffer comment = new StringBuffer();
			while (true) {
				char ch = lexer.lookAhead(0);
				if (ch == ')') {
					lexer.consume(1);
					break;
				} else if (ch == '\\') {
					// Escaped character
					Token tok = lexer.getNextToken();
					comment.append(tok.getTokenValue());
					lexer.consume(1);
					tok = lexer.getNextToken();
					comment.append(tok.getTokenValue());
					lexer.consume(1);
				} else if (ch == '\n') {
					break;
				} else {
					comment.append(ch);
					lexer.consume(1);
				}
			}
			v.setComment(comment.toString());
		}

	}

	public Header parse() throws ParseException {
			ViaList viaList = new ViaList();
			// The first via header.
			this.lexer.match(TokenTypes.VIA);
			this.lexer.SPorHT(); // ignore blanks
			this.lexer.match(':'); // expect a colon.
			this.lexer.SPorHT(); // ingore blanks.

			while (true) {
				ViaHeader v = new ViaHeader();
				parseVia(v);
				viaList.add(v);
				this.lexer.SPorHT(); // eat whitespace.
				if (this.lexer.lookAhead(0) == ',') {
					this.lexer.consume(1); // Consume the comma
					this.lexer.SPorHT(); // Ignore space after.
				}
				if (this.lexer.lookAhead(0) == '\n')
					break;
			}
			this.lexer.match('\n');
			return viaList;
	}
}
