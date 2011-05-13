/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright © 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.core.ims.protocol.msrp;

/**
 * MSRP contants
 * 
 * @author jexa7410
 */
public interface MsrpConstants {
	public static final String MSRP_PROTOCOL = "MSRP";
	public static final String NEW_LINE = "\r\n";
	public static final String END_MSRP_MSG = "-------";
	
	public static final int FLAG_LAST_CHUNK = '$';
	public static final int FLAG_MORE_CHUNK = '+';
	public static final int FLAG_ABORT_CHUNK = '#';
	
	public static final byte CHAR_SP = ' ';
	public static final byte CHAR_LF = '\r';
	public static final byte CHAR_MIN = '-';
	public static final byte CHAR_DOUBLE_POINT = ':';
	
	public static final String HEADER_BYTE_RANGE = "Byte-Range";
	public static final String HEADER_STATUS = "Status";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_MESSAGE_ID = "Message-ID";
	public static final String HEADER_TO_PATH = "To-Path";
	public static final String HEADER_FROM_PATH = "From-Path";
	public static final String HEADER_FAILURE_REPORT = "Failure-Report";
	public static final String HEADER_SUCCESS_REPORT = "Success-Report";
	
	public static final String METHOD_SEND = "SEND";
	public static final String METHOD_REPORT = "REPORT";
	
	public static final int RESPONSE_OK = 200;
	public static final int RESPONSE_UNINTELLIGIBLE = 400;
	public static final int RESPONSE_STOP_SEND = 413;
	public static final int RESPONSE_NOT_UNDERSTOOD = 415;
	public static final int RESPONSE_OUT_OF_BOUND = 423;
	public static final int RESPONSE_NOT_ALLOWED = 403;
	public static final int RESPONSE_NOT_COMPLETED = 408;
	public static final int RESPONSE_SESSION_NOT_EXIST = 481;
	public static final int RESPONSE_METHOD_NOT_UNDERSTOOD = 501;
	public static final int RESPONSE_ALREADY_BOUND_OTHER_CONNECTION = 506;
	
	public static final int CHUNK_MAX_SIZE = 2048;
	public static final String COMMENT_OK = "OK";
}
