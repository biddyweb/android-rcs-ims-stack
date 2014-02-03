/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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
import com.orangelabs.rcs.provider.settings.RcsSettings;
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
	 * Power lock
	 */
	private PowerManager.WakeLock powerLock = null;

    // Changed by Deutsche Telekom
    /**
     * Power lock
     */
    private static PowerManager.WakeLock tempPowerLock = null;

    // Changed by Deutsche Telekom
    /**
     * Power lock synchronizer object
     */
    private static Object tempPowerLockSynchronizerObject = new Object();

    // Changed by Deutsche Telekom
    /**
     * Power lock counter
     */
    private static long tempPowerLockCounter = 0;

	/**
     * The logger
     */
    private static Logger logger = Logger.getLogger(CpuManager.class.getName());
    
  
    
    /**
	 * Constructor
	 */
	public CpuManager() {
	}

	/**
	 * Init
	 */
	public void init() {
        if (RcsSettings.getInstance().isCpuAlwaysOn()) {
            // Activate the always-on procedure even if the device wakes up
            PowerManager pm = (PowerManager) AndroidFactory.getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            powerLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RcsCore");
            powerLock.acquire();
    		if (logger.isActivated()) {
    			logger.info("Always-on CPU activated");
    		}
        }
	}

	/**
	 * Stop
	 */
	public void close() {
        // Release power manager wave lock
        if (powerLock != null) {
            powerLock.release();
        }
	}

	// Changed by Deutsche Telekom
    /**
     * Set a temporary WakeLock if screen is not active. For each call of this function, one call must be done of the
     * function {@link CpuManager#releaseTempLock()}. To grant the usability, the code should be implemented like this:
     * 
     * <pre>
     * <code>
     *     CpuManager.setTempLock();
     *     try {
     *         // ... Code to be executed ...
     *     } finally {
     *         CpuManager.releaseTempLock();
     *     }
     * </code>
     * </pre>
     */
//	public static void setTempLock(){
//		// Need to syncronize temporary lock and counter
//        synchronized (tempPowerLockSynchronizerObject) {
//	    	// Increment the temporary power lock counter
//            tempPowerLockCounter++;
//    	    // logger.info("CpuManager.setTempLock: counter = " + tempPowerLockCounter);
//            PowerManager pm = (PowerManager) AndroidFactory.getApplicationContext()
//                    .getSystemService(Context.POWER_SERVICE);
//            if (!pm.isScreenOn() && (tempPowerLock == null || (tempPowerLock != null && !tempPowerLock.isHeld()))) {
//                tempPowerLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RcsCoreTemp");
//                tempPowerLock.acquire();
//                if (logger.isActivated()) {
//                    logger.info("CpuManager: tempPowerLock acquired!");
//                }
//            }
//        }
//	}

	// Changed by Deutsche Telekom
    /**
     * Release temporary WakeLock if set before For each call of {@link CpuManager#setTempLock()}, one call of this
     * function must be done. To grant the usability, the code should be implemented like this:
     * 
     * <pre>
     * <code>
     *     CpuManager.setTempLock();
     *     try {
     *         // ... Code to be executed ...
     *     } finally {
     *         CpuManager.releaseTempLock();
     *     }
     * </code>
     * </pre>
     */
//	public static void releaseTempLock(){
//		// Need to syncronize temporary lock and counter
//        synchronized (tempPowerLockSynchronizerObject) {
//        	// Decrement the temporary power lock counter
//            tempPowerLockCounter--;
//            // logger.info("CpuManager.releaseTempLock: counter = " + tempPowerLockCounter);
//            // Validate if the number of calls of "setTempLock", to ensure that no action will release the lock if another
//            // one has requested and not finished yet
//    	    if (tempPowerLockCounter <= 0) {
//                if (tempPowerLock != null && tempPowerLock.isHeld()) {
//                    tempPowerLock.release();
//                    tempPowerLock = null;
//                    if (logger.isActivated()) {
//                        logger.info("CpuManager: tempPowerLock released!");
//                    }
//                }
//                // Just to reset the counter if "releaseTempLock" is called more times than the "setTempLock"
//                tempPowerLockCounter = 0;
//    	    }
//        }
//	}
}
