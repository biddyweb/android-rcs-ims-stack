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

package com.orangelabs.rcs.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * String utility functions
 */
public class StringUtils {
	/**
	 * Encode string into UTF-8
	 * 
	 * @param text Input text
	 * @return Encoded string
	 */
	public static String encodeUTF8(String text) {
		if (text == null) {
			return null;
		}

		String result;
		try {
			result = URLEncoder.encode(text, "UTF-8");
		} catch(Exception e) {
			result = text;
		}
		return result;		
	}
	
	/**
	 * Decode UTF-8 string
	 * 
	 * @param text Input text
	 * @return Decoded string
	 */
	public static String decodeUTF8(String text) {
		if (text == null) {
			return null;
		}
		
		String result;
		try {
			result = URLDecoder.decode(text, "UTF-8");
		} catch(Exception e) {
			result = text;
		}
		return result;		
	}	
	
	/**
	 * Escape characters for text appearing as XML data, between tags.
	 * 
	 * The following characters are replaced :
	 * <br> <
	 * <br> >
	 * <br> &
	 * <br> "
	 * <br> '
	 * 
	 * @param text Input text
	 * @return Encoded string
	 */
	public static String encodeXML(String text) {
		if (text == null) {
			return null;
		}
		
	    final StringBuilder result = new StringBuilder();
	    final StringCharacterIterator iterator = new StringCharacterIterator(text);
	    char character =  iterator.current();
	    while (character != CharacterIterator.DONE ){
	      if (character == '<') {
	        result.append("&lt;");
	      }
	      else if (character == '>') {
	        result.append("&gt;");
	      }
	      else if (character == '\"') {
	        result.append("&quot;");
	      }
	      else if (character == '\'') {
	        result.append("&#039;");
	      }
	      else if (character == '&') {
	         result.append("&amp;");
	      }
	      else {
	        //the char is not a special one
	        //add it to the result as is
	        result.append(character);
	      }
	      character = iterator.next();
	    }
	    return result.toString();
	}
	
	/**
	 * Decode XML string
	 * 
	 * @param text Input text
	 * @return Decoded string
	 */
	public static String decodeXML(String text) {
		if (text == null) {
			return null;
		}
		
	    text = text.replaceAll("&lt;", "<");
	    text = text.replaceAll("&gt;", ">");
	    text = text.replaceAll("&quot;", "\"");
	    text = text.replaceAll("&#039;", "\'");
	    text = text.replaceAll("&amp;", "&");
		
	    return text;
	}
}
