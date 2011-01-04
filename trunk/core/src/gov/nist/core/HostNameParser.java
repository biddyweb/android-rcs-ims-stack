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
package gov.nist.core;

/**
 * Parser for host names.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 * IPv6 Support added by Emil Ivov (emil_ivov@yahoo.com)<br/> Network Research
 * Team (http://www-r2.u-strasbg.fr))<br/> Louis Pasteur University -
 * Strasbourg - France<br/>
 */

public class HostNameParser extends ParserCore {

	/**
	 * The lexer is initialized with the buffer.
	 */
	public HostNameParser(LexerCore lexer) {
		this.lexer = lexer;
		lexer.selectLexer("charLexer");
	}

	protected String domainLabel() throws ParseException {
		StringBuffer retval = new StringBuffer();
		while (lexer.hasMoreChars()) {
			char la = lexer.lookAhead(0);
			if (LexerCore.isAlpha(la)) {
				lexer.consume(1);
				retval.append(la);
			} else if (LexerCore.isDigit(la)) {
				lexer.consume(1);
				retval.append(la);
			} else if (la == '-') {
				lexer.consume(1);
				retval.append(la);
			} else
				break;
		}
		return retval.toString();
	}

	protected String ipv6Reference() throws ParseException {
		StringBuffer retval = new StringBuffer();
		while (lexer.hasMoreChars()) {
			char la = lexer.lookAhead(0);
			if (LexerCore.isHexDigit(la)) {
				lexer.consume(1);
				retval.append(la);
			} else if (la == '.' || la == ':' || la == '[') {
				lexer.consume(1);
				retval.append(la);
			} else if (la == ']') {
				lexer.consume(1);
				retval.append(la);
				return retval.toString();
			} else
				break;
		}

		throw new ParseException(lexer.getBuffer() + ": Illegal Host name ", lexer.getPtr());
	}

	public Host host() throws ParseException {
		StringBuffer hname = new StringBuffer();

		// IPv6 referene
		if (lexer.lookAhead(0) == '[') {
			hname.append(ipv6Reference());
		}
		// IPv4 address or hostname
		else {
			String nextTok = domainLabel();
			hname.append(nextTok);
			// Bug reported by Stuart Woodsford (B.T.)
			while (lexer.hasMoreChars()) {
				// Reached the end of the buffer.
				if (lexer.lookAhead(0) == '.') {
					lexer.consume(1);
					nextTok = domainLabel();
					hname.append(".");
					hname.append(nextTok);
				} else
					break;
			}
		}

		String hostname = hname.toString();
		if (hostname.equals(""))
			throw new ParseException(lexer.getBuffer()
					+ ": Illegal Host name ", lexer.getPtr());
		else
			return new Host(hostname);
	}

	public HostPort hostPort() throws ParseException {
		Host host = this.host();
		HostPort hp = new HostPort();
		hp.setHost(host);
		// Has a port?
		if (lexer.hasMoreChars() && lexer.lookAhead(0) == ':') {
			lexer.consume(1);
			try {
				String port = lexer.number();
				hp.setPort(Integer.parseInt(port));
			} catch (NumberFormatException nfe) {
				throw new ParseException(lexer.getBuffer()
						+ " :Error parsing port ", lexer.getPtr());
			}
		}
		return hp;
	}
}
