/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
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
package com.orangelabs.rcs.core.ims.protocol.rtp.codec.text.t140;

/**
 * This class contains global constants that need to be available to all classes
 * in the package
 * 
 * @author Erik Zetterstrom, Omnitor AB
 * @author Andreas Piirimets, Omnitor AB
 */
public class T140Constants {

	// Timer constants

	/**
	 * Waiting period for a missing packet in ms
	 */
	public static final int WAIT_FOR_MISSING_PACKET = 500;

	/**
	 * Waiting period for a missing packet in ms when redundancy is used
	 */
	public static final int WAIT_FOR_MISSING_PACKET_RED = 3000;

	// Error codes

	/**
	 * Unsupported encoding of input string
	 */
	public static final int INVALID_INPUT_ENCODING = 66;

	// Special characters

	/**
	 * Special character to indicate lost data, UTF-8 encoded
	 */
	public static final byte[] LOSS_CHAR = { (byte) 0xEF, (byte) 0xBF, (byte) 0xBD };

	/**
	 * Special character to indicate lost data, in character form
	 */
	public static final char LOSS_CHAR_CHAR = 0xFFFD;

	/**
	 * Zero width no break space, transmitted at the beginning of a T.140
	 * session to ensure that the byte order is correct
	 */
	public static final byte[] ZERO_WIDTH_NO_BREAK_SPACE = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
	
	/**
	 * Zero width no break space in character form
	 */
	public static final char ZERO_WIDTH_NO_BREAK_SPACE_CHAR = 0xFEFF;

	// Header constants

	/**
	 * Size of a redundant header
	 */
	public static final int REDUNDANT_HEADER_SIZE = 4;

	/**
	 * Size of the primary header
	 */
	public static final int PRIMARY_HEADER_SIZE = 1;

	// Sequence number constants

	/**
	 * Maximum sequence number
	 */
	public static final int MAX_SEQUENCE_NUMBER = 65535;

	/**
	 * Used to determine when the sequence numbers has wrapped around
	 */
	public static final int WRAP_AROUND_MARGIN = 20;

	// Unicode controls

	/**
	 * Backspace
	 */
	public static final char BACKSPACE = 0x8;

	/**
	 * Line seperator
	 */
	public static final char LINE_SEPERATOR = 0x2028;

	/**
	 * Escape - used in combination with other controls (like intterupt)
	 */
	public static final char ESC = 0x1B;

	/**
	 * Line feed
	 */
	public static final char LINE_FEED = 0xA;

	/**
	 * Carriage return
	 */
	public static final char CARRIAGE_RETURN = 0xD;

	/**
	 * CRLF
	 */
	public static final char CR_LF = 0x0d0a;

	/**
	 * Second char in intterupt message
	 */
	public static final char INTERRUPT2 = 0x61;

	/**
	 * Bell
	 */
	public static final char BELL = 0x7;

	/**
	 * Start of string (used as a general protocol element introducer)
	 */
	public static final char SOS = 0x98;

	/**
	 * String terminator (end of sos string)
	 */
	public static final char ST = 0x9C;

	/**
	 * Start of string used for (graphic rendition)
	 */
	public static final char GRAPHIC_START = 0x9B;

	/**
	 * String terminator (used for grapich rendition)
	 */
	public static final char GRAPHIC_END = 0x6D;

}
