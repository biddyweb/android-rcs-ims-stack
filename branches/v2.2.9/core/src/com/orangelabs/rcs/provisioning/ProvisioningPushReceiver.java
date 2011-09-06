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

package com.orangelabs.rcs.provisioning;

import java.io.ByteArrayInputStream;

import org.xml.sax.InputSource;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.platform.logger.AndroidAppender;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.logger.Appender;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Provisioning WAP PUSH event receiver
 *  
 * @author jexa7410
 */
public class ProvisioningPushReceiver extends BroadcastReceiver {
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override  
	public void onReceive(Context context, Intent intent) {
		// Set logger appenders
		Appender[] appenders = new Appender[] { 
				new AndroidAppender()
			};
		Logger.setAppenders(appenders);

		if (logger.isActivated()) {
			logger.info("WAP PUSH event received");
		}

		// Set Android factory
		// TODO: to be removed
        AndroidFactory.setApplicationContext(context);
        
        // Set RSC settings
        RcsSettings.createInstance(context);
		
		// Process event in background
        new ReceivePushTask().execute(intent);
	}
	
	/**
	 * Background processing
	 * 
	 * TODO: ignore event if roaming?
	 * TODO: security management?
	 * TODO: change WAP PUSH MIME-type in manifest
	 */
    private class ReceivePushTask extends AsyncTask<Intent,Void,Void> {
        @Override
        protected Void doInBackground(Intent... intents) {
        	try {
        		// Get intent data
	            Intent intent = intents[0];
	            byte[] data = intent.getByteArrayExtra("data");
	            
	            // Instanciate the provisioning manager
				ProvisioningManager mgr = new ProvisioningManager();

				// Download the received URL
				String url = ProvisioningManager.DEFAULT_CONFIG_SERVER;
	    		if (data != null) {
	    			url = new String(data);
	    		}
    			byte[] content = mgr.downloadConfigFile(url);
	    		if (logger.isActivated()) {
	    			logger.debug("Downloaded " + content.length + " bytes: " + new String(content));
	    		}
	            
	    		// Parse the received document
				InputSource contentInput = new InputSource(new ByteArrayInputStream(content));
				ProvisioningParser configParser = new ProvisioningParser(contentInput);
	    		
	    		// Check parameters
				mgr.checkParams(configParser.getParams());
        	} catch(Exception e) {
	    		if (logger.isActivated()) {
	    			logger.error("Can't download URL", e);
	    		}
        	}
            return null;
        }
    }
    
    /**
     * Only for debug
     */
    public static void test() {
    	try {
            // Instanciate the provisioning manager
			ProvisioningManager mgr = new ProvisioningManager();

			// Download the received URL
			String url = "http://172.20.14.41/rcse_orange_ota/rcse_config.xml";
			byte[] content = mgr.downloadConfigFile(url);

    		/*
			InputStream is = FileFactory.getFactory().openConfigFile("rcse_config.xml");
			if (is == null) {
				throw new CoreException("XML file not found");
			}
			String txt = new String();
			int c = -1;
			while ((c = is.read()) != -1) {
				txt += (char)c;
			}
			byte[] content = txt.getBytes();*/
            
    		// Parse the received document
			InputSource contentInput = new InputSource(new ByteArrayInputStream(content));
			ProvisioningParser configParser = new ProvisioningParser(contentInput);
    		
    		// Check parameters
			mgr.checkParams(configParser.getParams());
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}