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
package com.orangelabs.rcs.samples.im;

import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.widget.TextView;

import com.orangelabs.rcs.samples.R;

/**
 * Display the received IM
 * 
 * @author jexa7410
 */
public class ReceiveIm extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.messaging_receive_im);
        
        // Set UI title
        setTitle(R.string.title_recv_im);
        
        // Dispaly the remote contact
        TextView from = (TextView)findViewById(R.id.from);
        from.setText(getIntent().getStringExtra("contact"));
        
        // display the received message
    	TextView msg = (TextView)findViewById(R.id.message);
        msg.setText(getIntent().getStringExtra("message"));
        
		// Play a tone
		try {
			ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_RING,100);
			toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2);
		} catch (Exception e){
			e.printStackTrace();
		}
    }
}
