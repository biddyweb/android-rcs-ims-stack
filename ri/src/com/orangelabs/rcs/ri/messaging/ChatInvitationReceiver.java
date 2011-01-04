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
package com.orangelabs.rcs.ri.messaging;

import com.orangelabs.rcs.provider.settings.RcsSettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Chat invitation receiver
 * 
 * @author jexa7410
 */
public class ChatInvitationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// Get invitation info
		String contact = intent.getStringExtra("contact");
		String sessionId = intent.getStringExtra("sessionId");
		
		// Instanciate settings
        RcsSettings.createInstance(context);
        
        // Display invitation notification
		ReceiveChat.addChatInvitationNotification(context, contact, sessionId);
    }
}
