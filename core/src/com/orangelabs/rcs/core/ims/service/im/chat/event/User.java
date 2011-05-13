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
package com.orangelabs.rcs.core.ims.service.im.chat.event;

public class User {
	public final static String STATE_UNKNOWN = "unknown";
	public final static String STATE_CONNECTED = "connected";
	public final static String STATE_DISCONNECTED = "disconnected";
	public final static String STATE_ONHOLD = "on-hold";
	public final static String STATE_MUTED = "muted-via-focus";
	public final static String STATE_PENDING = "pending";
	public final static String STATE_ALERTING = "alerting";
	public final static String STATE_DIALING_IN = "dialing-in";
	public final static String STATE_DIALING_OUT = "dialing-out";
	public final static String STATE_DISCONNECTING = "disconnecting";
	
	private String entity;
	
	private boolean me;
	
	private String state = STATE_UNKNOWN;
	
	public User(String entity, boolean me) {
		this.entity = entity;
		this.me = me;
	}
	
	public String getEntity() {
		return entity;
	}
	
	public boolean isMe() {
		return me;
	}
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}	
}
