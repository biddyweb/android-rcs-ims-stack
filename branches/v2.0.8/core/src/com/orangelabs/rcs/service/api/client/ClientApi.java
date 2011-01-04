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
package com.orangelabs.rcs.service.api.client;

import java.util.Vector;

/**
 * Client API
 * 
 * @author jexa7410
 */
public class ClientApi {
	/**
	 * API event listeners
	 */
	private Vector<ClientApiListener> listeners = new Vector<ClientApiListener>();
	
	/**
	 * Constructor
	 */
	public ClientApi() {		
	}
	
	/**
	 * Add an event listener
	 * 
	 * @param listener Listener
	 */
	public void addApiEventListener(ClientApiListener listener) {
		listeners.addElement(listener);
	}

	/**
	 * Remove an event listener
	 * 
	 * @param listener Listener
	 */
	public void removeApiEventListener(ClientApiListener listener) {
		listeners.removeElement(listener);
	}

	/**
	 * Remove all event listeners
	 */
	public void removeAllApiEventListeners() {
		listeners.removeAllElements();
	}
	
	/**
	 * Notify listeners when API is connected to the server
	 */
	public void notifyEventApiConnected() {
		for(int i=0; i < listeners.size(); i++) {
			ClientApiListener listener = (ClientApiListener)listeners.elementAt(i);
			listener.handleApiConnected();
		}
	}

	/**
	 * Notify listeners when API is disconnected from the server
	 */
	public void notifyEventApiDisconnected() {
		for(int i=0; i < listeners.size(); i++) {
			ClientApiListener listener = (ClientApiListener)listeners.elementAt(i);
			listener.handleApiDisconnected();
		}
	}
}
