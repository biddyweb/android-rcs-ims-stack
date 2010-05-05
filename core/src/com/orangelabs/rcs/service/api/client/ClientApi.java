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
