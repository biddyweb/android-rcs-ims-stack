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
package com.orangelabs.rcs.service.api.server.management;

import com.orangelabs.rcs.service.api.client.management.IManagementApi;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Management API service
 * 
 * @author jexa7410
 */
public class ManagementApiService extends IManagementApi.Stub {

    /**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public ManagementApiService() {
		if (logger.isActivated()) {
			logger.info("Management API service is loaded");
		}
	}

	/**
	 * Set trace activation flag
	 * 
	 * @param state State
	 */
	public void setTraceActivation(boolean state) {	
		if (logger.isActivated()) {
			logger.info("Set trace activation to " + state);
		}

		Logger.activationFlag = state;
	}

	/**
	 * Set trace level
	 * 
	 * @param level Level
	 */
	public void setTraceLevel(int level) {	
		if (logger.isActivated()) {
			logger.info("Set trace level to " + level);
		}

		Logger.traceLevel = level;
	}
	
	/**
	 * Is trace activated
	 * 
	 * @return Boolean
	 */
	public boolean isTraceActivated() {	
		return Logger.activationFlag;
	}

	/**
	 * Get trace level
	 * 
	 * @return Level
	 */
	public int getTraceLevel() {	
		return Logger.traceLevel;
	}	
}
