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
package com.orangelabs.rcs.samples.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.orangelabs.rcs.platform.logger.AndroidAppender;
import com.orangelabs.rcs.utils.logger.Appender;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Play a text-to-speech in background
 * 
 * @author jexa7410
 */
public class PlayTextToSpeech extends Service implements OnInitListener {
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
     * TTS engine 
     */
	private TextToSpeech tts = null;
	
	/**
	 * Text to be played
	 */
	private String text = null;
    
    @Override
    public void onCreate() {
        // Set logger appenders
		Appender[] appenders = new Appender[] { 
				new AndroidAppender()
			};
		Logger.setAppenders(appenders);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
		return null;
    }
    
    @Override
	public void onStart(Intent intent, int startId) {
		// Get parameters
		text = intent.getStringExtra("text");
		
		// Instanciate the TTS engine
		try {
    		if (logger.isActivated()) {
    			logger.debug("Start TTS");
    		}
	        
			// Success, create the TTS instance
			tts = new TextToSpeech(getApplicationContext(), this);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Unexpected exception", e);
			}
		}
	}
    
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Deallocate TTS engine
        if (tts != null) {
    		if (logger.isActivated()) {
    			logger.debug("TTS engine stopped");
    		}
        	tts.shutdown();
        }
    }

    /**
     * TTS engine init
     * 
     * @param status Status
     */
    public void onInit(int status) {
		if ((tts != null) && (status == TextToSpeech.SUCCESS)) {
			if (logger.isActivated()) {
				logger.debug("TTS engine started with success");
			}
			if (text != null) {
	    		if (logger.isActivated()) {
	    			logger.debug("Play text: " + text);
	    		}
	    		
	    		// Speak
		        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		        while(tts.isSpeaking()) {
		        	try {
		        		Thread.sleep(500);
		        	} catch(Exception e) {}
		        }
		        
		        // Stop the service
		        this.stopSelf();
			}
    	}
    }
}
