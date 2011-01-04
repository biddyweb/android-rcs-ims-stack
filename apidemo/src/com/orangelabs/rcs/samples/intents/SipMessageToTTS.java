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
package com.orangelabs.rcs.samples.intents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orangelabs.rcs.samples.utils.PlayTextToSpeech;

/**
 * Play a text-to-speech when a plain/text IM is received by using a SIP Intent (see the
 * matching rule in the manifest.xml)
 * 
 * @author jexa7410
 */
public class SipMessageToTTS extends BroadcastReceiver {
    @Override
	public void onReceive(Context context, Intent intent) {
		// Text to be played via TTS
    	String text = "New message received";

    	// Request the TTS service
		Intent serviceIntent = new Intent(context, PlayTextToSpeech.class);
		serviceIntent.putExtra("text", text);
		context.startService(serviceIntent);        	
    }
}	
