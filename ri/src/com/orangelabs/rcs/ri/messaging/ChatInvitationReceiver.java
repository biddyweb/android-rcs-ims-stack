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

package com.orangelabs.rcs.ri.messaging;

import com.orangelabs.rcs.utils.logger.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Chat invitation receiver
 * 
 * @author jexa7410
 */
public class ChatInvitationReceiver extends BroadcastReceiver {
	/**
	 * The logger
	 */
	private final static Logger logger = Logger.getLogger(ChatInvitationReceiver.class.getSimpleName());
	
	@Override
	public void onReceive(Context context, Intent intent) {
        boolean autoAccept = intent.getBooleanExtra("autoAccept", false);
        boolean isGroupChat = intent.getBooleanExtra("isGroupChat",false);
        if (logger.isActivated()) {
        	logger.debug("onReceive autoAccept="+autoAccept+" isGroupChat="+isGroupChat);
        }
        if (autoAccept) {
        	if (isGroupChat) {
        		// Display GroupChatView
                Intent intentGC = new Intent(intent);
                intentGC.setClass(context, GroupChatView.class);
                intentGC.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentGC.setAction(intent.getStringExtra("sessionId"));
                context.startActivity(intentGC);
        	} else {
        		// Display OneToOneChatView
                Intent intentOneToOne = new Intent(intent);
                intentOneToOne.setClass(context, OneToOneChatView.class);
                intentOneToOne.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentOneToOne.setAction(intent.getStringExtra("sessionId"));
                context.startActivity(intentOneToOne);
        	}
        } else {
            // Display invitation notification
            ReceiveChat.addChatInvitationNotification(context, intent);
        }
    }
}
