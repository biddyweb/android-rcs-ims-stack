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
import gov.nist.core.LexerCore;
import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.ParseException;
import gov.nist.core.Token;
import gov.nist.core.sip.address.SipURI;
import gov.nist.core.sip.address.TelURL;
import gov.nist.core.sip.address.TelephoneNumber;
import gov.nist.core.sip.address.URI;

import java.util.Vector;


/**
 * Parser For SIP and Tel URLs. Other kinds of URL's are handled by the J2SE 1.4
 * URL class.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public class URLParser extends Parser {

	public URLParser(String url) {
		this.lexer = new Lexer("sip_urlLexer", url);

	}

	URLParser(Lexer lexer) {
		this.lexer = lexer;
		this.lexer.selectLexer("sip_urlLexer");
	}

	protected static boolean isMark(char next) {
		return next == '-' || next == '_' || next == '.' || next == '!'
				|| next == '~' || next == '*' || next == '\'' || next == '('
				|| next == ')';
	}

	protected static boolean isUnreserved(char next) {
		return Lexer.isAlpha(next) || Lexer.isDigit(next) || isMark(next);

	}

	protected static boolean isReservedNoSlash(char next) {
		return next == ';' || next == '?' || next == ':' || next == '@'
				|| next == '&' || next == '+' || next == '$' || next == ',';

	}

	// Missing '=' bug in character set - discovered by interop testing
	// at SIPIT 13 by Bob Johnson and Scott Holben.
	// Replace . by ; fixed by Bruno Konik
	protected static boolean isUserUnreserved(char la) {
		return la == '&' || la == '?' || la == '+' || la == '$' || la == '#'
				|| la == '/' || la == ',' || la == ';' || la == '=';
	}

	protected String unreserved() throws ParseException {
		char next = lexer.lookAhead(0);
		if (isUnreserved(next)) {
			lexer.consume(1);
			return new StringBuffer().append(next).toString();
		} else
			throw createParseException("unreserved");

	}

	/**
	 * Name or value of a parameter.
	 */
	protected String paramNameOrValue() throws ParseException {
		StringBuffer retval = new StringBuffer();
		while (lexer.hasMoreChars()) {
			char next = lexer.lookAhead(0);
			if (next == '[' || next == '[' || next == '/' || next == ':'
					|| next == '&' || next == '+' || next == '$'
					|| isUnreserved(next)) {
				retval.append(next);
				lexer.consume(1);
			} else if (isEscaped()) {
				String esc = lexer.charAsString(3);
				lexer.consume(3);
				retval.append(esc);
			} else
				break;
		}
		return retval.toString();
	}

	protected NameValue uriParam() throws ParseException {
		String pvalue = null;
		String pname = paramNameOrValue();
		char next = lexer.lookAhead(0);
		if (next == '=') {
			lexer.consume(1);
			pvalue = paramNameOrValue();
		}
		return new NameValue(pname, pvalue);
	}

	protected static boolean isReserved(char next) {
		return next == ';' || next == '/' || next == '?' || next == ':'
				|| next == '@' || next == '&' || next == '+' || next == '$'
				|| next == '=' || next == ',';
	}

	protected String reserved() throws ParseException {
		char next = lexer.lookAhead(0);
		if (isReserved(next)) {
			lexer.consume(1);
			return new StringBuffer().append(next).toString();
		} else
			throw createParseException("reserved");
	}

	protected boolean isEscaped() {
		try {
			char next = lexer.lookAhead(0);
			char next1 = lexer.lookAhead(1);
			char next2 = lexer.lookAhead(2);
			return (next == '%' && Lexer.isHexDigit(next1) && Lexer
					.isHexDigit(next2));
		} catch (Exception ex) {
			return false;
		}
	}

	protected String escaped() throws ParseException {
		StringBuffer retval = new StringBuffer();
		char next = lexer.lookAhead(0);
		char next1 = lexer.lookAhead(1);
		char next2 = lexer.lookAhead(2);
		if (next == '%' && Lexer.isHexDigit(next1)
				&& Lexer.isHexDigit(next2)) {
			lexer.consume(3);
			retval.append(next);
			retval.append(next1);
			retval.append(next2);
		} else
			throw createParseException("escaped");
		return retval.toString();
	}

	protected String mark() throws ParseException {
		char next = lexer.lookAhead(0);
		if (isMark(next)) {
			lexer.consume(1);
			return new StringBuffer().append(next).toString();
		} else
			throw createParseException("mark");
	}

	protected String uric() {
			try {
				char la = lexer.lookAhead(0);
				if (isUnreserved(la)) {
					lexer.consume(1);
					return Lexer.charAsString(la);
				} else if (isReserved(la)) {
					lexer.consume(1);
					return Lexer.charAsString(la);
				} else if (isEscaped()) {
					String retval = lexer.charAsString(3);
					lexer.consume(3);
					return retval;
				} else
					return null;
			} catch (Exception ex) {
				return null;
			}
	}

	protected String uricNoSlash() {
			try {
				char la = lexer.lookAhead(0);
				if (isEscaped()) {
					String retval = lexer.charAsString(3);
					lexer.consume(3);
					return retval;
				} else if (isUnreserved(la)) {
					lexer.consume(1);
					return Lexer.charAsString(la);
				} else if (isReservedNoSlash(la)) {
					lexer.consume(1);
					return Lexer.charAsString(la);
				} else
					return null;
			} catch (ParseException ex) {
				return null;
			}
	}

	protected String uricString() {
		StringBuffer retval = new StringBuffer();
		while (true) {
			String next = uric();
			if (next == null)
				break;
			retval.append(next);
		}
		return retval.toString();
	}

	/**
	 * Parse and return a structure for a generic URL. Note that non SIP URLs
	 * are just stored as a string (not parsed).
	 * 
	 * @return URI is a URL structure for a SIP url.
	 * @throws ParsException
	 *             if there was a problem parsing.
	 */
	public URI uriReference() throws ParseException {
		URI retval = null;
		Vector vect = lexer.peekNextToken(2);
		Token t1 = (Token) vect.elementAt(0);
		Token t2 = (Token) vect.elementAt(1);
			if (t1.getTokenType() == TokenTypes.SIP) {
				if (t2.getTokenType() == ':')
					retval = sipURL();
				else
					throw createParseException("Expecting \':\'");
			} else if (t1.getTokenType() == TokenTypes.TEL) {
				if (t2.getTokenType() == ':') {
					retval = telURL();
				} else
					throw createParseException("Expecting \':\'");
			} else {
				String urlString = uricString();
				try {
					// retval = new URI(urlString);
					retval = new URI(urlString);

				} catch (ParseException ex) {
					throw createParseException(ex.getMessage());
				}
			}
		return retval;
	}

	/**
	 * Parser for the base phone number.
	 */
	private String base_phone_number() throws ParseException {
		StringBuffer s = new StringBuffer();

			int lc = 0;
			while (lexer.hasMoreChars()) {
				char w = lexer.lookAhead(0);
				if (LexerCore.isDigit(w) || w == '-' || w == '.' || w == '('
						|| w == ')') {
					lexer.consume(1);
					s.append(w);
					lc++;
				} else if (lc > 0)
					break;
				else
					throw createParseException("unexpected " + w);
			}
			return s.toString();
	}

	/**
	 * Parser for the local phone #.
	 */
	private String local_number() throws ParseException {
		StringBuffer s = new StringBuffer();
			int lc = 0;
			while (lexer.hasMoreChars()) {
				char la = lexer.lookAhead(0);
				if (la == '*' || la == '#' || la == '-' || la == '.'
						|| la == '(' || la == ')' || LexerCore.isDigit(la)) {
					lexer.consume(1);
					s.append(la);
					lc++;
				} else if (lc > 0)
					break;
				else
					throw createParseException("unexepcted " + la);
			}
			return s.toString();
	}

	/**
	 * Parser for telephone subscriber.
	 * 
	 * @return the parsed telephone number.
	 */
	public final TelephoneNumber parseTelephoneNumber() throws ParseException {
		TelephoneNumber tn;

		lexer.selectLexer("charLexer");
			char c = lexer.lookAhead(0);
			if (c == '+')
				tn = global_phone_number();
			else if (LexerCore.isAlpha(c) || LexerCore.isDigit(c) || c == '-'
					|| c == '*' || c == '.' || c == '(' || c == ')' || c == '#') {
				tn = local_phone_number();
			} else
				throw createParseException("unexpected char " + c);
			return tn;
	}

	private final TelephoneNumber global_phone_number() throws ParseException {
			TelephoneNumber tn = new TelephoneNumber();
			tn.setGlobal(true);
			NameValueList nv = null;
			this.lexer.match(PLUS);
			String b = base_phone_number();
			tn.setPhoneNumber(b);
			if (lexer.hasMoreChars()) {
				char tok = lexer.lookAhead(0);
				if (tok == ';') {
					this.lexer.consume(1);
					nv = tel_parameters();
					tn.setParameters(nv);
				}
			}
			return tn;
	}

	private TelephoneNumber local_phone_number() throws ParseException {
		TelephoneNumber tn = new TelephoneNumber();
		tn.setGlobal(false);
		NameValueList nv = null;
		String b = null;
			b = local_number();
			tn.setPhoneNumber(b);
			if (lexer.hasMoreChars()) {
				Token tok = this.lexer.peekNextToken();
				switch (tok.getTokenType()) {
				case SEMICOLON: {
					this.lexer.consume(1);
					nv = tel_parameters();
					tn.setParameters(nv);
					break;
				}
				default: {
					break;
				}
				}
			}
		return tn;
	}

	private NameValueList tel_parameters() throws ParseException {
		NameValueList nvList = new NameValueList();
		while (true) {
			NameValue nv = nameValue('=');
			nvList.add(nv);
			char tok = lexer.lookAhead(0);
			if (tok == ';')
				continue;
			else
				break;
		}
		return nvList;
	}

	/**
	 * Parse and return a structure for a Tel URL.
	 * 
	 * @return a parsed tel url structure.
	 */
	public TelURL telURL() throws ParseException {
		lexer.match(TokenTypes.TEL);
		lexer.match(':');
		TelephoneNumber tn = this.parseTelephoneNumber();
		TelURL telUrl = new TelURL();
		telUrl.setTelephoneNumber(tn);
		return telUrl;

	}

	/**
	 * Parse and return a structure for a SIP URL.
	 * 
	 * @return a URL structure for a SIP url.
	 * @throws ParsException
	 *             if there was a problem parsing.
	 */

	public SipURI sipURL() throws ParseException {
		SipURI retval = new SipURI();
			lexer.match(TokenTypes.SIP);
			lexer.match(':');
			retval.setScheme(TokenNames.SIP);
			int m = lexer.markInputPosition();
			try {
				String user = user();
				lexer.lookAhead(0);
				// name:password@hostPort
				lexer.match(':');
				String password = password();
				lexer.match('@');
				HostNameParser hnp = new HostNameParser(this.getLexer());
				HostPort hp = hnp.hostPort();
				retval.setUser(user);
				retval.setUserPassword(password);
				retval.setHostPort(hp);
			} catch (ParseException ex) {
				// name@hostPort
				try {
					lexer.rewindInputPosition(m);
					String user = user();
					lexer.match('@');
					HostNameParser hnp = new HostNameParser(this.getLexer());
					HostPort hp = hnp.hostPort();
					retval.setUser(user);
					retval.setHostPort(hp);
				} catch (ParseException e) {
					// hostPort
					lexer.rewindInputPosition(m);
					HostNameParser hnp = new HostNameParser(this.getLexer());
					HostPort hp = hnp.hostPort();
					retval.setHostPort(hp);
				}
			}
			lexer.selectLexer("charLexer");
			while (lexer.hasMoreChars()) {
				if (lexer.lookAhead(0) != ';')
					break;
				lexer.consume(1);
				NameValue parms = uriParam();
				retval.setUriParameter(parms);
			}

			if (lexer.hasMoreChars() && lexer.lookAhead(0) == '?') {
				lexer.consume(1);
				while (lexer.hasMoreChars()) {
					NameValue parms = qheader();
					retval.setQHeader(parms);
					if (lexer.hasMoreChars() && lexer.lookAhead(0) != '&')
						break;
					else
						lexer.consume(1);
				}
			}
			return retval;
	}

	public String peekScheme() throws ParseException {
		Vector tokens = lexer.peekNextToken(1);
		if (tokens.size() == 0)
			return null;
		String scheme = ((Token) tokens.elementAt(0)).getTokenValue();
		return scheme;
	}

	/**
	 * Get a name value for a given query header (ie one that comes after the
	 * ?).
	 */
	protected NameValue qheader() throws ParseException {

		String name = lexer.getNextToken('=');
		lexer.consume(1);
		String value = hvalue();
		return new NameValue(name, value);

	}

	protected String hvalue() throws ParseException {
		StringBuffer retval = new StringBuffer();
		while (lexer.hasMoreChars()) {
			char la = lexer.lookAhead(0);
			// Look for a character that can terminate a URL.
			if (la == '+' || la == '?' || la == ':' || la == '@' || la == '['
					|| la == ']' || la == '/' || la == '$' || la == '_'
					|| la == '-' || la == '"' || la == '!' || la == '~'
					|| la == '*' || la == '.' || la == '(' || la == ')'
					|| LexerCore.isAlpha(la) || LexerCore.isDigit(la)) {
				lexer.consume(1);
				retval.append(la);
			} else if (la == '%') {
				retval.append(escaped());
			} else
				break;
		}
		return retval.toString();
	}

	/**
	 * Scan forward until you hit a terminating character for a URL. We do not
	 * handle non sip urls in this implementation.
	 * 
	 * @return the string that takes us to the end of this URL (i.e. to the next
	 *         delimiter).
	 */
	protected String urlString() throws ParseException {
		StringBuffer retval = new StringBuffer();
		lexer.selectLexer("charLexer");

		while (lexer.hasMoreChars()) {
			char la = lexer.lookAhead(0);
			// Look for a character that can terminate a URL.
			if (la == ' ' || la == '\t' || la == '\n' || la == '>' || la == '<')
				break;
			lexer.consume(0);
			retval.append(la);
		}
		return retval.toString();
	}

	protected String user() throws ParseException {

			StringBuffer retval = new StringBuffer();
			while (lexer.hasMoreChars()) {
				char la = lexer.lookAhead(0);
				if (isUnreserved(la) || isUserUnreserved(la)) {
					retval.append(la);
					lexer.consume(1);
				} else if (isEscaped()) {
					String esc = lexer.charAsString(3);
					lexer.consume(3);
					retval.append(esc);
				} else
					break;
			}
			return retval.toString();
	}

	protected String password() throws ParseException {
		StringBuffer retval = new StringBuffer();
		while (true) {
			char la = lexer.lookAhead(0);
			if (isUnreserved(la) || la == '&' || la == '=' || la == '+'
					|| la == '$' || la == ',') {
				retval.append(la);
				lexer.consume(1);
			} else if (isEscaped()) {
				String esc = lexer.charAsString(3);
				retval.append(esc);
				// BUG FIX from Jeff Haynie frm JAIN-SIP
				lexer.consume(3);
			} else
				break;

		}
		return retval.toString();

	}

	/**
	 * Default parse method. This method just calls uriReference.
	 */

	public URI parse() throws ParseException {
		return uriReference();
	}

}
