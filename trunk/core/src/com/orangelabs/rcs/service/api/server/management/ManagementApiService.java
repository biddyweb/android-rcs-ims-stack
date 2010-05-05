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
