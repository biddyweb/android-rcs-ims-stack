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
package gov.nist.core.sip.header;

import java.util.Hashtable;

/**
 * A mapping class that returns the Header for a given header name.
 */
public class NameMap {
	static Hashtable nameMap;

	static {
		initializeNameMap();
	}

	protected static void putNameMap(String headerName, Class clazz) {
		nameMap.put(headerName.toLowerCase(), clazz);
	}

	public static Class getClassFromName(String headerName) {
		return (Class) nameMap.get(headerName.toLowerCase());
	}

	public static boolean isHeaderSupported(String headerName) {
		return nameMap.containsKey(headerName);
	}

	private static void initializeNameMap() {
		nameMap = new Hashtable();

		putNameMap(Header.CSEQ, CSeqHeader.clazz); // 1

		putNameMap(Header.RECORD_ROUTE, RecordRouteHeader.clazz); // 2

		putNameMap(Header.VIA, ViaHeader.clazz); // 3

		putNameMap(Header.FROM, FromHeader.clazz); // 4

		putNameMap(Header.CALL_ID, CallIdHeader.clazz); // 5

		putNameMap(Header.MAX_FORWARDS, MaxForwardsHeader.clazz); // 6

		putNameMap(Header.PROXY_AUTHENTICATE, ProxyAuthenticateHeader.clazz); // 7

		putNameMap(Header.CONTENT_TYPE, ContentTypeHeader.clazz); // 8

		putNameMap(Header.CONTENT_LENGTH, ContentLengthHeader.clazz); // 9

		putNameMap(Header.ROUTE, RouteHeader.clazz); // 10

		putNameMap(Header.CONTACT, ContactHeader.clazz); // 11

		putNameMap(Header.WWW_AUTHENTICATE, WWWAuthenticateHeader.clazz); // 12

		putNameMap(Header.PROXY_AUTHORIZATION, ProxyAuthorizationHeader.clazz); // 13

		putNameMap(Header.DATE, DateHeader.clazz); // 14

		putNameMap(Header.EXPIRES, ExpiresHeader.clazz); // 15

		putNameMap(Header.AUTHORIZATION, AuthorizationHeader.clazz); // 6

	}

}
