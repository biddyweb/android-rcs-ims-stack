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

import java.util.Vector;

/**
 * Base string token splitter
 */
public class StringTokenizer {

	protected String buffer;

	protected int ptr;

	protected int savedPtr;

	protected char delimiter;

	public StringTokenizer() {
		this.delimiter = '\n';
		this.ptr = 0;
	}

	public StringTokenizer(String buffer) {
		this.buffer = buffer;
		this.ptr = 0;
		this.delimiter = '\n';

	}

	public StringTokenizer(String buffer, char delimiter) {
		this.buffer = buffer;
		this.delimiter = delimiter;
		this.ptr = 0;
	}

	public String nextToken() {
		StringBuffer retval = new StringBuffer();

		while (ptr < buffer.length()) {
			if (buffer.charAt(ptr) == delimiter) {
				retval.append(buffer.charAt(ptr));
				ptr++;
				break;
			} else {
				retval.append(buffer.charAt(ptr));
				ptr++;
			}
		}

		return retval.toString();
	}

	public boolean hasMoreChars() {
		return ptr < buffer.length();
	}

	public static boolean isHexDigit(char ch) {
		if (isDigit(ch))
			return true;
		else {
			char ch1 = Character.toUpperCase(ch);
			return ch1 == 'A' || ch1 == 'B' || ch1 == 'C' || ch1 == 'D'
					|| ch1 == 'E' || ch1 == 'F';
		}
	}

	public static boolean isAlpha(char ch) {
		boolean retval = Character.isUpperCase(ch) || Character.isLowerCase(ch);
		return retval;
	}

	public static boolean isDigit(char ch) {
		boolean retval = Character.isDigit(ch);
		return retval;
	}

	public String getLine() {
		StringBuffer retval = new StringBuffer();
		while (ptr < buffer.length() && buffer.charAt(ptr) != '\n') {
			retval.append(buffer.charAt(ptr));
			ptr++;
		}
		if (ptr < buffer.length() && buffer.charAt(ptr) == '\n') {
			retval.append('\n');
			ptr++;
		}
		return retval.toString();
	}

	public String peekLine() {
		int curPos = ptr;
		String retval = this.getLine();
		ptr = curPos;
		return retval;
	}

	public char lookAhead() throws ParseException {
		return lookAhead(0);
	}

	public char lookAhead(int k) throws ParseException {
		// Debug.out.println("ptr = " + ptr);
		if (ptr + k < buffer.length())
			return buffer.charAt(ptr + k);
		else
			return '\0';
	}

	public char getNextChar() throws ParseException {
		if (ptr >= buffer.length())
			throw new ParseException(buffer + " getNextChar: End of buffer",
					ptr);
		else
			return buffer.charAt(ptr++);
	}

	public void consume() {
		ptr = savedPtr;
	}

	public void consume(int k) {
		ptr += k;
	}

	/**
	 * Get a Vector of the buffer tokenized by lines
	 */
	public Vector getLines() {
		Vector result = new Vector();
		while (hasMoreChars()) {
			String line = getLine();
			result.addElement(line);
		}
		return result;
	}

	/**
	 * Get the next token from the buffer.
	 */
	public String getNextToken(char delim) throws ParseException {
		StringBuffer retval = new StringBuffer();
		while (true) {
			char la = lookAhead(0);
			if (la == delim)
				break;
			else if (la == '\0')
				throw new ParseException("EOL reached", 0);
			retval.append(buffer.charAt(ptr));
			consume(1);
		}
		return retval.toString();
	}
}
