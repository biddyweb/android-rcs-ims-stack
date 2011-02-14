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
package com.orangelabs.rcs.utils;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Date utility functions
 *  
 * @author jexa7410
 */
public class DateUtils {
	/**
	 * Decode a date to string value (see RFC 3339)
	 * 
	 * @param date Date in milliseconds
	 * @return String
	 */
	public static String encodeDate(long date) {
		// Format: 2009-03-19T14:03:54Z
		// Timezone of published date must be in UTC, ie GMT+0
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-0"));
		calendar.setTimeInMillis(date); 

		StringBuffer buff = new StringBuffer();
		int year = calendar.get(Calendar.YEAR);
		buff.append(year);
		buff.append("-");
		
		int month = calendar.get(Calendar.MONTH);
		month++; // API Calendar, months go from 0 to 11, not 1 to 12
		if (month < 10)
			buff.append("0" + month);
		else
			buff.append(month);
		buff.append("-");
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if (day < 10)
			buff.append("0" + day);
		else
			buff.append(day);
		buff.append("T");
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < 10)
			buff.append("0" + hour);
		else
			buff.append(hour);
		buff.append(":");
		
		int min = calendar.get(Calendar.MINUTE);
		if (min < 10)
			buff.append("0" + min);
		else
			buff.append(min);
		buff.append(":");
		
		int sec = calendar.get(Calendar.SECOND);
		if (sec < 10)
			buff.append("0" + sec);
		else
			buff.append(sec);
		buff.append("Z");

		return buff.toString();
	}

	/**
	 * Decode a string date to long value (see RFC 3339)
	 * 
	 * @param date Date as string
	 * @return Milliseconds
	 */
	public static long decodeDate(String date) {
		// Format: 2009-03-19T14:03:54Z
		// Timezone of date is in UTC, ie GMT+0
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-0"));		
		String[] parts = date.split("T");
		
		String[] d = parts[0].split("-");
		int year = Integer.parseInt(d[0]);
		cal.set(Calendar.YEAR, year);
		int month = Integer.parseInt(d[1]);
		cal.set(Calendar.MONTH, month-1);
		int day = Integer.parseInt(d[2]);
		cal.set(Calendar.DAY_OF_MONTH, day);
		
		String[] t = parts[1].split("[:.Z]");
		int hour = Integer.parseInt(t[0]);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		int minute = Integer.parseInt(t[1]);
		cal.set(Calendar.MINUTE, minute);
		int second = Integer.parseInt(t[2]);
		cal.set(Calendar.SECOND, second);
		
		return cal.getTimeInMillis();
	}
}
