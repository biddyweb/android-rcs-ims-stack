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

import java.util.Vector;

/**
 * Conference-Info document
 * 
 * @author jexa7410
 */
public class ConferenceInfoDocument {
	public final static String STATE_PARTIAL = "partial";
	public final static String STATE_FULL = "full";
	public final static String STATE_DELETED = "deleted";
	
	private String entity;
	
	private String state;
	
	private Vector<User> users = new Vector<User>();
	
	public ConferenceInfoDocument(String entity, String state) {
		this.entity = entity;
		this.state = state;
	}
	
	public String getEntity() {
		return entity;
	}
	
	public String getState() {
		return state;
	}
	
	public void addUser(User user) {
		users.addElement(user);
	}
	
	public Vector<User> getUsers() {
		return users;
	}
}
