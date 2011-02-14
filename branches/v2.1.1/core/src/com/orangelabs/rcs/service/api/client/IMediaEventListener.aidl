package com.orangelabs.rcs.service.api.client;

/**
 * Media event listener
 */
interface IMediaEventListener {
	// Media is started
	void mediaStarted();
	
	// Media is stopped
	void mediaStopped();

	// Media has failed
	void mediaError(in String error);
}
