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


import gov.nist.core.LexerCore;
import gov.nist.core.sip.header.EventHeader;
import gov.nist.core.sip.header.Header;

import java.util.Hashtable;


/**
 * Lexer class for the parser.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */

public class Lexer extends LexerCore {
	/**
	 * get the header name of the line
	 * 
	 * @return String
	 */
	public static String getHeaderName(String line) {
		if (line == null)
			return null;
		String headerName = null;
		try {
			int begin = line.indexOf(":");
			headerName = null;
			if (begin >= 1)
				headerName = line.substring(0, begin);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return headerName;
	}

	public Lexer(String lexerName, String buffer) {
		super(lexerName, buffer);
		this.selectLexer(lexerName);
	}

	/**
	 * get the header value of the line
	 * 
	 * @return String
	 */
	public static String getHeaderValue(String line) {
		if (line == null)
			return null;
		String headerValue = null;
		try {
			int begin = line.indexOf(":");
			headerValue = line.substring(begin + 1);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return headerValue;
	}

	public void selectLexer(String lexerName) {
		currentLexer = (Hashtable) lexerTables.get(lexerName);
		this.currentLexerName = lexerName;
		if (currentLexer == null) {
			addLexer(lexerName);
			if (lexerName.equals("method_keywordLexer")) {
				addKeyword(TokenNames.REGISTER.toUpperCase(),
						TokenTypes.REGISTER);
				addKeyword(TokenNames.ACK.toUpperCase(), TokenTypes.ACK);
				addKeyword(TokenNames.OPTIONS.toUpperCase(), TokenTypes.OPTIONS);
				addKeyword(TokenNames.BYE.toUpperCase(), TokenTypes.BYE);
				addKeyword(TokenNames.INVITE.toUpperCase(), TokenTypes.INVITE);
				addKeyword(TokenNames.SIP.toUpperCase(), TokenTypes.SIP);
				addKeyword(TokenNames.SUBSCRIBE.toUpperCase(),
						TokenTypes.SUBSCRIBE);
				addKeyword(TokenNames.NOTIFY.toUpperCase(), TokenTypes.NOTIFY);
			} else if (lexerName.equals("command_keywordLexer")) {
				addKeyword(Header.FROM.toUpperCase(), TokenTypes.FROM); // 1
				addKeyword(Header.TO.toUpperCase(), TokenTypes.TO); // 2
				addKeyword(Header.VIA.toUpperCase(), TokenTypes.VIA); // 3
				addKeyword(Header.ROUTE.toUpperCase(), TokenTypes.ROUTE); // 4
				addKeyword(Header.MAX_FORWARDS.toUpperCase(),
						TokenTypes.MAX_FORWARDS); // 5
				addKeyword(Header.AUTHORIZATION.toUpperCase(),
						TokenTypes.AUTHORIZATION); // 6
				addKeyword(Header.PROXY_AUTHORIZATION.toUpperCase(),
						TokenTypes.PROXY_AUTHORIZATION); // 7
				addKeyword(Header.DATE.toUpperCase(), TokenTypes.DATE); // 8
				addKeyword(Header.CONTENT_LENGTH.toUpperCase(),
						TokenTypes.CONTENT_LENGTH); // 9
				addKeyword(Header.CONTENT_TYPE.toUpperCase(),
						TokenTypes.CONTENT_TYPE); // 10
				addKeyword(Header.CONTACT.toUpperCase(), TokenTypes.CONTACT); // 11
				addKeyword(Header.CALL_ID.toUpperCase(), TokenTypes.CALL_ID); // 11
				addKeyword(Header.EXPIRES.toUpperCase(), TokenTypes.EXPIRES); // 12
				addKeyword(Header.RECORD_ROUTE.toUpperCase(),
						TokenTypes.RECORD_ROUTE); // 13
				addKeyword(Header.CSEQ.toUpperCase(), TokenTypes.CSEQ); // 14
				addKeyword(Header.WWW_AUTHENTICATE.toUpperCase(),
						TokenTypes.WWW_AUTHENTICATE); // 15
				addKeyword(Header.PROXY_AUTHENTICATE.toUpperCase(),
						TokenTypes.PROXY_AUTHENTICATE); // 16
				addKeyword(EventHeader.NAME.toUpperCase(), TokenTypes.EVENT); // 17
				// And now the dreaded short forms....
				addKeyword(TokenNames.C.toUpperCase(), TokenTypes.CONTENT_TYPE);
				addKeyword(TokenNames.F.toUpperCase(),	TokenTypes.FROM);
				addKeyword(TokenNames.I.toUpperCase(), TokenTypes.CALL_ID);
				addKeyword(TokenNames.M.toUpperCase(), TokenTypes.CONTACT);
				addKeyword(TokenNames.L.toUpperCase(),	TokenTypes.CONTENT_LENGTH);
				addKeyword(TokenNames.T.toUpperCase(), TokenTypes.TO);
				addKeyword(TokenNames.V.toUpperCase(), TokenTypes.VIA);
				addKeyword(TokenNames.O.toUpperCase(), TokenTypes.EVENT);
			} else if (lexerName.equals("status_lineLexer")) {
				addKeyword(TokenNames.SIP.toUpperCase(), TokenTypes.SIP);
			} else if (lexerName.equals("request_lineLexer")) {
				addKeyword(TokenNames.SIP.toUpperCase(), TokenTypes.SIP);
			} else if (lexerName.equals("sip_urlLexer")) {
				addKeyword(TokenNames.TEL.toUpperCase(), TokenTypes.TEL);
				addKeyword(TokenNames.SIP.toUpperCase(), TokenTypes.SIP);
			}

		}

	}

}
