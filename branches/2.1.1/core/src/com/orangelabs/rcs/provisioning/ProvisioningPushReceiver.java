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
package com.orangelabs.rcs.provisioning;

import java.io.ByteArrayInputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

/**
 * Provisioning WAP PUSH event receiver
 *  
 * @author jexa7410
 */
public class ProvisioningPushReceiver extends BroadcastReceiver {
	@Override  
	public void onReceive(Context context, Intent intent) {
		// TODO: ignore event if roaming
		// TODO: security mgt?
		// TODO: document parser
		// TODO: filter the WAP PUSH event. How?
		// TODO: change MIME-type in manifest
        new ReceivePushTask(context).execute(intent);
	}
	
	/**
	 * Background processing
	 */
    private class ReceivePushTask extends AsyncTask<Intent,Void,Void> {
        private Context mContext;
        
        public ReceivePushTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Intent... intents) {
        	try {
        		// Get intent data
	            Intent intent = intents[0];
	            byte[] wbxml = intent.getByteArrayExtra("data");
	
	            // Parse WBXML document
	    		// TODO
        	} catch(Exception e) {
        		// TODO
        	}
            return null;
        }
    }
}