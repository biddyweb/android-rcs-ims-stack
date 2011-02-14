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
import java.util.HashMap;

//import org.kxml2.io.KXmlSerializer;
//import org.kxml2.wap.WbxmlParser;

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

	            String sec = null;  
	            String mac = null;  
	            if (intent.getExtras() != null) {  
	            	HashMap<String, String> provisioningParameters = (HashMap<String, String>)intent.getExtras().get("contentTypeParameters");
	               System.out.println("Extras " + intent.getExtras().toString());	
	            	if (provisioningParameters != null) {  
		               sec = provisioningParameters.get("SEC");  
		               mac = provisioningParameters.get("MAC");  
		               System.out.println("Sec= " + sec + " Mac=" + mac);	
	            	}  
	            } else {
		               System.out.println("No extras");		            	
	            }
	            
	            
	    		System.out.println("********* WBXML size: "+wbxml.length+" ***********");	
	    		for(int i = 0; i < wbxml.length; i += 16){
	    			for (int j = i; j < Math.min(i + 16, wbxml.length); j ++) {
	    				int b = ((int) wbxml[j]) & 0x0ff;
	    				System.out.print(Integer.toHexString(b / 16));
	    				System.out.print(Integer.toHexString(b % 16));
	    				System.out.print(' ');
	    			}
	    			
	    			for (int j = i; j < Math.min(i + 16, wbxml.length); j ++) {
	    				int b = wbxml[j];
	    				System.out.print(b >= 32 && b <= 127  ? (char) b : '?');
	    			}
	    			
	    			System.out.println();
	    		}
	            
	            // Parse WBXML document
	    		ByteArrayInputStream bis = new ByteArrayInputStream(wbxml);
//	    		WbxmlParser xp = new WbxmlParser();
//	    		xp.setInput(bis, null);
//	    		
//	    		//ByteArrayOuputStream bos = new ByteArrayOuputStream();
//	    		KXmlSerializer xs = new KXmlSerializer();
//	    		xs.setOutput(System.out, null);
//	    		
//	    		new Roundtrip(xp, xs).roundTrip();
	    		
        	} catch(Exception e) {
        		// TODO
        	}
            return null;
        }
    }
}