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
package gov.nist.core.sip.address;

/**
 * Common parameter names.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public interface ParameterNames {
	public static final String SIP_URI_SCHEME = "sip";

	public static final String SIPS_URI_SCHEME = "sips";

	public static final String TEL_URI_SCHEME = "tel";

	public static final String POSTDIAL = "postdial";

	public static final String PHONE_CONTEXT_TAG = "context-tag";

	public static final String ISUB = "isub";

	public static final String PROVIDER_TAG = "provider-tag";

	public static final String UDP = "udp";

	public static final String TCP = "tcp";

	public static final String USER = "user";

	public static final String TRANSPORT = "transport";

	public static final String METHOD = "method";

	public static final String TTL = "ttl";

	public static final String MADDR = "maddr";

	public static final String LR = "lr";
}
