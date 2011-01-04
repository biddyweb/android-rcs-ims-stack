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
import gov.nist.core.sip.header.Header;
import gov.nist.core.sip.header.RouteHeader;
import gov.nist.core.sip.header.RouteList;



/**
 * Parser for a list of route headers.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author Olivier Deruelle <deruelle@nist.gov> <br/>
 * @author M. Ranganathan <mranga@nist.gov> <br/> <a href="{@docRoot}/uncopyright.html">This
 *         code is in the public domain.</a>
 * 
 * @version 1.0
 */

public class RouteParser extends AddressParametersParser {

	RouteParser() {
	}

	/**
	 * Constructor
	 * 
	 * @param String
	 *            route message to parse to set
	 */
	public RouteParser(String route) {
		super(route);
	}

	protected RouteParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message and generate the Route List Object
	 * 
	 * @return Header the Route List object
	 * @throws ParseException
	 *             if errors occur during the parsing
	 */
	public Header parse() throws ParseException {
		RouteList routeList = new RouteList();
		this.lexer.match(TokenTypes.ROUTE);
		this.lexer.SPorHT();
		this.lexer.match(':');
		this.lexer.SPorHT();
		while (true) {
			RouteHeader route = new RouteHeader();
			super.parse(route);
			routeList.add(route);
			this.lexer.SPorHT();
			if (lexer.lookAhead(0) == ',') {
				this.lexer.match(',');
				this.lexer.SPorHT();
			} else if (lexer.lookAhead(0) == '\n')
				break;
			else
				throw createParseException("unexpected char");
		}
		return routeList;
	}
}
