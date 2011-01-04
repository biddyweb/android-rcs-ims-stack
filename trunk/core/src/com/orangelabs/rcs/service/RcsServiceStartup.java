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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * RCS service startup on device boot
 * 
 * @author jexa7410
 */
public class RcsServiceStartup extends BroadcastReceiver {
    @Override
	public void onReceive(Context context, Intent intent) {
		// Instanciate the settings manager if needed
    	RcsSettings.createInstance(context);
    	
    	// Start the RCS core
    	if (RcsSettings.getInstance().isServiceActivated()){
			// We do not start the service if it is deactivated in settings (check needed for autoboot at device startup)
			context.startService(new Intent("com.orangelabs.rcs.SERVICE"));
		}
	}
}
