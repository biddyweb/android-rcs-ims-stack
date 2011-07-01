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

package com.orangelabs.rcs.core.ims.service;

import java.util.List;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * SIP intent manager
 * 
 * @author jexa7410
 */
public class SipIntentManager {
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 */
	public SipIntentManager() {
	}
	
	/**
	 * Broadcast request as intent
	 * 
	 * @param request SIP request
	 * @return Returns true if the intent has been resolved, else returns false
	 */
	public boolean broadcastRequest(SipRequest request) {
		boolean result = false;
		
		// Create the intent
		String action = "com.orangelabs.rcs.intent." + request.getMethod();
		Intent intent = new Intent(action);
		intent.addCategory(Intent.CATEGORY_DEFAULT);

		// Set intent parameters
		String mime = request.getContentType();
		if (mime != null) {
			intent.setType(mime.toLowerCase());
		}
        
		String callId = request.getCallId();
		if (callId != null) {
			intent.putExtra("CallId", callId);
		}
        
		String from = request.getFromUri();
		if (from != null) {
			intent.putExtra("From", from);
		}

		String content = request.getContent();
		if (content != null) {
			intent.putExtra("Content", content);
		}
        
		if (logger.isActivated()) {
			logger.debug("Create SIP intent " + action + ", call-Id=" + callId + ", mime-type=" + mime);
		}
		
		try {
			if (isSipIntentResolvedByBroadcastReceiver(intent)) {
	  			// Broadcast intent to intent receivers
	    		if (logger.isActivated()) {
	    			logger.debug("Broadcast intent to broadcast receivers");
	    		}
	    		AndroidFactory.getApplicationContext().sendBroadcast(intent);
	    		result = true;
			}

			if (isSipIntentResolvedByActivity(intent)) {
	    		// Broadcast intent to activities
	    		if (logger.isActivated()) {
	    			logger.debug("Broadcast intent to activities");
	    		}
	    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    		AndroidFactory.getApplicationContext().startActivity(intent);
	    		result = true;
			}
	    } catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't send SIP intent " + action, e);
			}
	    }
	    
	    return result;
	}
	
	/**
	 * Is the SIP intent action may be resolved by at least an activity
	 * 
	 * @param intent The Intent to resolve
	 * @return Returns true if the intent has been resolved, else returns false
	 */
	private boolean isSipIntentResolvedByActivity(Intent intent) {
		PackageManager packageManager = AndroidFactory.getApplicationContext().getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return (list.size() > 0);
	}	

	/**
	 * Is the SIP intent may be resolved by at least broadcast receiver
	 * 
	 * @param intent The Intent to resolve
	 * @return Returns true if the intent has been resolved, else returns false
	 */
	private boolean isSipIntentResolvedByBroadcastReceiver(Intent intent) {
		PackageManager packageManager = AndroidFactory.getApplicationContext().getPackageManager();
		List<ResolveInfo> list = packageManager.queryBroadcastReceivers(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return (list.size() > 0);
	}	
	
	/**
	 * Is the SIP intent may be resolved
	 * 
	 * @param intent The Intent to check
	 * @return Returns true if the intent has been resolved, else returns false
	 */
	public boolean isSipIntentResolved(Intent intent) {
		return (isSipIntentResolvedByActivity(intent) || isSipIntentResolvedByBroadcastReceiver(intent));
	}	
}
