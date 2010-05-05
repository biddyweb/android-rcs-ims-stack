package com.orangelabs.rcs.utils;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * String utility functions
 */
public class StringUtils {

	/**
	 * Encode a string in UTF-8
	 * 
	 * @param string
	 * @return same string, encoded in UTF-8 format
	 */
	public static String encodeUTF8(String string){
		CharsetEncoder encoder = Charset.forName("iso-8859-1").newEncoder();
		try {
			// Encode in UTF-8
			string = new String(encoder.encode(CharBuffer.wrap(string)).array());
		} catch(CharacterCodingException e) {
			// Could not encode to UTF-8, returned string is unchanged
		}
		return string;
	}
	
	/**
	 * Escape characters for text appearing as XML data, between tags.
	 * 
	 * <P>
	 * The following characters are replaced with corresponding character
	 * entities :
	 * <table border='1' cellpadding='3' cellspacing='0'>
	 * <tr>
	 * <th>Character</th>
	 * <th>Encoding</th>
	 * </tr>
	 * <tr>
	 * <td><</td>
	 * <td>&lt;</td>
	 * </tr>
	 * <tr>
	 * <td>></td>
	 * <td>&gt;</td>
	 * </tr>
	 * <tr>
	 * <td>&</td>
	 * <td>&amp;</td>
	 * </tr>
	 * <tr>
	 * <td>"</td>
	 * <td>&quot;</td>
	 * </tr>
	 * <tr>
	 * <td>'</td>
	 * <td>&#039;</td>
	 * </tr>
	 * </table>
	 * 
	 * <P>
	 * Note that JSTL's {@code <c:out>} escapes the exact same set of characters
	 * as this method. <span class='highlight'>That is, {@code <c:out>} is good
	 * for escaping to produce valid XML, but not for producing safe
	 * HTML.</span>
	 */
	public static String forXML(String aText) {
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(
				aText);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '<') {
				result.append("&lt;");
			} else if (character == '>') {
				result.append("&gt;");
			} else if (character == '\"') {
				result.append("&quot;");
			} else if (character == '\'') {
				result.append("&#039;");
			} else if (character == '&') {
				result.append("&amp;");
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}
}
