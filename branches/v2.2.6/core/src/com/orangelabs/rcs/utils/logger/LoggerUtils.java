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

package com.orangelabs.rcs.utils.logger;

import java.util.Calendar;

/**
 * Logger utility functions
 * 
 * @author jexa7410
 */
public class LoggerUtils {
	/**
	 * Classname length
	 */
	private static int MAX_CLASSNAME_LENGTH = 20;

	/**
	 * Format thelevel
	 * 
	 * @param level Level
	 * @return Formatted level
	 */
	public static String formatLevel(int level) {
		if (level == Logger.INFO_LEVEL) {
			return "[ INFO] ";
		} else
		if (level == Logger.WARN_LEVEL) {
			return "[ WARN] ";
		} else
		if (level == Logger.ERROR_LEVEL) {
			return "[ERROR] ";
		} else
		if (level == Logger.FATAL_LEVEL) {
			return "[FATAL] ";
		} else {
			return "[DEBUG] ";
		}
	}
		
	/**
	 * Format the classname
	 * 
	 * @param classname Classname to be formatted
	 * @return Formatted classname
	 */
	public static String formatClassname(String classname) {
		int size = classname.length();
		String txt = classname;
		if (size > MAX_CLASSNAME_LENGTH) {
			txt = classname.substring(0, MAX_CLASSNAME_LENGTH);
		} else {
			for (int i=0; i < (MAX_CLASSNAME_LENGTH-size); i++) {
				txt = txt + " ";
			}
		}
		return txt;
	}
	
	/**
	 * Format the date
	 * 
	 * @return String
	 */
	public static String formatDate() {
		Calendar currentTime = Calendar.getInstance();
		return currentTime.get(Calendar.YEAR) + "-" +
			(currentTime.get(Calendar.MONTH)+1)+ "-" +	// API Calendar, months go from 0 to 11, not 1 to 12
			currentTime.get(Calendar.DAY_OF_MONTH) + " " +
			currentTime.get(Calendar.HOUR_OF_DAY) + "." +
			currentTime.get(Calendar.MINUTE) + "." +
			currentTime.get(Calendar.SECOND) + "," +
			currentTime.get(Calendar.MILLISECOND);
	}	

}
