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
package com.orangelabs.rcs.service;

import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.utils.logger.Logger;

import android.content.Context;
import android.os.PowerManager;

/**
 * CPU manager
 * 
 * @author jexa7410
 */
public class CpuManager {
	/**
	 * Always-on flag
	 */
	public final static boolean ALWAYS_ON = false; 
	
	/**
	 * Power manager
	 */
	private PowerManager.WakeLock powerManager = null;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
	 * Constructor
	 */
	public CpuManager() {
		if (logger.isActivated()) {
			logger.info("CPU always-on state is " + CpuManager.ALWAYS_ON);
		}
	}
	
	/**
	 * Init
	 */
	public void init() {
		if (CpuManager.ALWAYS_ON) {
			// Activate the always-on procedure even if the device wakes up
			PowerManager pm = (PowerManager)AndroidFactory.getApplicationContext().getSystemService(Context.POWER_SERVICE);
			powerManager = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RcsCore");
			powerManager.acquire();
		}
	}

	/**
	 * Stop
	 */
	public void close() {
		if (CpuManager.ALWAYS_ON) {
			// Release power manager wave lock
			if (powerManager != null) {
	    		powerManager.release();
	    	}
		}		
	}
}
