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
package com.orangelabs.rcs.service.api.server;

import com.orangelabs.rcs.core.Core;

/**
 * Server API utils
 * 
 * @author jexa7410
 */
public class ServerApiUtils {
	/**
	 * Test core
	 * 
	 * @throws ServerApiException
	 */
	public static void testCore() throws ServerApiException {
		if (Core.getInstance() == null) {
			throw new ServerApiException("Core is not instanciated");
		}
	}
	
	/**
	 * Test IMS connection
	 * 
	 * @throws ServerApiException
	 */
	public static void testIms() throws ServerApiException {
		if ((Core.getInstance() == null) ||
			(Core.getInstance().getImsModule().getCurrentNetworkInterface() == null) ||
			(!Core.getInstance().getImsModule().getCurrentNetworkInterface().isRegistered())) { 
			throw new ServerApiException("Core is not connected to IMS"); 
		}
	}
}
