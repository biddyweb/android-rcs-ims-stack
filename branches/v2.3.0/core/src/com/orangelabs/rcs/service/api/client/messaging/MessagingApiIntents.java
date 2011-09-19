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

package com.orangelabs.rcs.service.api.client.messaging;

/**
 * Messaging API intents 
 * 
 * @author jexa7410
 */
public interface MessagingApiIntents {
    /**
     * Intent broadcasted when a new file transfer invitation has been received
     */
	public final static String FILE_TRANSFER_INVITATION = "com.orangelabs.rcs.messaging.FILE_TRANSFER_INVITATION";
	
    /**
     * Intent broadcasted when a new chat invitation has been received
     */
	public final static String CHAT_INVITATION = "com.orangelabs.rcs.messaging.CHAT_INVITATION";

    /**
     * Intent broadcasted when a 1-1 chat session has been replaced by a chat group session
     */
	public final static String CHAT_SESSION_REPLACED = "com.orangelabs.rcs.messaging.CHAT_SESSION_REPLACED";
}
