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
package com.orangelabs.rcs.core.ims.protocol.sdp;

import java.io.ByteArrayInputStream;
import java.util.Vector;

public class TimeDescription extends Parser {
	// Values:
	public String timeActive;

	public Vector<String> repeatTimes;

	public TimeDescription(ByteArrayInputStream bin) {
		// Time the session is active:
		timeActive = getLine(bin);

		// Repeat Times:
		repeatTimes = new Vector<String>();

		boolean found = getToken(bin, "r=", false);

		while (found) {
			String repeatTime = getLine(bin);

			repeatTimes.addElement(repeatTime);

			found = getToken(bin, "r=", false);
		}
	}
}
