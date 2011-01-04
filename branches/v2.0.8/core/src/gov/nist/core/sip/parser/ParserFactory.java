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
import gov.nist.core.sip.header.CSeqHeader;
import gov.nist.core.sip.header.CallIdHeader;
import gov.nist.core.sip.header.ContactHeader;
import gov.nist.core.sip.header.ContentLengthHeader;
import gov.nist.core.sip.header.ContentTypeHeader;
import gov.nist.core.sip.header.DateHeader;
import gov.nist.core.sip.header.EventHeader;
import gov.nist.core.sip.header.ExpiresHeader;
import gov.nist.core.sip.header.FromHeader;
import gov.nist.core.sip.header.MaxForwardsHeader;
import gov.nist.core.sip.header.ProxyAuthenticateHeader;
import gov.nist.core.sip.header.ProxyAuthorizationHeader;
import gov.nist.core.sip.header.RecordRouteHeader;
import gov.nist.core.sip.header.RouteHeader;
import gov.nist.core.sip.header.ToHeader;
import gov.nist.core.sip.header.ViaHeader;
import gov.nist.core.sip.header.WWWAuthenticateHeader;

import java.util.Hashtable;



/**
 * A factory class that does a name lookup on a registered parser and returns a
 * header parser for the given name.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * @author Jean Deruelle <jeand@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */
public class ParserFactory {

	private static Hashtable parserTable;

	private static Class[] constructorArgs;

	static {
		parserTable = new Hashtable();
		constructorArgs = new Class[1];
		constructorArgs[0] = new String().getClass();

		parserTable.put("t", new ToParser().getClass());
		parserTable.put(ToHeader.NAME.toLowerCase(), new ToParser().getClass());

		parserTable.put(FromHeader.NAME.toLowerCase(), new FromParser()
				.getClass());
		parserTable.put("f", new FromParser().getClass());

		parserTable.put(CSeqHeader.NAME.toLowerCase(), new CSeqParser()
				.getClass());

		parserTable.put(ViaHeader.NAME.toLowerCase(), new ViaParser()
				.getClass());
		parserTable.put("v", new ViaParser().getClass());

		parserTable.put(ContactHeader.NAME.toLowerCase(), new ContactParser()
				.getClass());
		parserTable.put("m", new ContactParser().getClass());

		parserTable.put(ContentTypeHeader.NAME.toLowerCase(),
				new ContentTypeParser().getClass());
		parserTable.put("c", new ContentTypeParser().getClass());

		parserTable.put(ContentLengthHeader.NAME.toLowerCase(),
				new ContentLengthParser().getClass());
		parserTable.put("l", new ContentLengthParser().getClass());

		parserTable.put(AuthorizationHeader.NAME.toLowerCase(),
				new AuthorizationParser().getClass());

		parserTable.put(WWWAuthenticateHeader.NAME.toLowerCase(),
				new WWWAuthenticateParser().getClass());

		parserTable.put(CallIdHeader.NAME.toLowerCase(), new CallIDParser()
				.getClass());
		parserTable.put("i", new CallIDParser().getClass());

		parserTable.put(RouteHeader.NAME.toLowerCase(), new RouteParser()
				.getClass());

		parserTable.put(RecordRouteHeader.NAME.toLowerCase(),
				new RecordRouteParser().getClass());

		parserTable.put(DateHeader.NAME.toLowerCase(), new DateParser()
				.getClass());

		parserTable.put(ProxyAuthorizationHeader.NAME.toLowerCase(),
				new ProxyAuthorizationParser().getClass());

		parserTable.put(ProxyAuthenticateHeader.NAME.toLowerCase(),
				new ProxyAuthenticateParser().getClass());

		parserTable.put(MaxForwardsHeader.NAME.toLowerCase(),
				new MaxForwardsParser().getClass());

		parserTable.put(ExpiresHeader.NAME.toLowerCase(), new ExpiresParser()
				.getClass());

		parserTable.put(EventHeader.NAME.toLowerCase(), new EventParser()
				.getClass());
		parserTable.put("o", new EventParser().getClass());

		/*
		 * parserTable.put( AuthenticationInfoHeader.NAME.toLowerCase(),
		 * AuthenticationInfoParser.getClass());
		 */

	}

	/**
	 * create a parser for a header. This is the parser factory.
	 */
	public static HeaderParser createParser(String line) throws ParseException {
		String headerName = Lexer.getHeaderName(line);
		String headerValue = Lexer.getHeaderValue(line);
		if (headerName == null || headerValue == null)
			throw new ParseException("The header name or value is null", 0);
		Class parserClass = (Class) parserTable.get(headerName.toLowerCase());

		if (parserClass != null) {

			try {

				HeaderParser retval = (HeaderParser) parserClass.newInstance();
				retval.setHeaderToParse(line);
				return retval;

			} catch (Exception ex) {
				return null;
			}

		} else {
			// Just generate a generic Header. We define
			// parsers only for the above.
			return new HeaderParser(line);
		}

	}

}
