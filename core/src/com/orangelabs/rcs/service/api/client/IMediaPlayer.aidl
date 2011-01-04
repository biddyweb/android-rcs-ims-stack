package com.orangelabs.rcs.service.api.client;

import com.orangelabs.rcs.service.api.client.MediaSample;
import com.orangelabs.rcs.service.api.client.IMediaEventListener;

/**
 * Media player
 */
interface IMediaPlayer {
	// Open the player
	void open();
	
	// Close the player
	void close();

	// Start the player
	void start();
	
	// Stop the player
	void stop();

	// Read a media sample (blocking method)
	MediaSample readSample();

	// Add a media listener
	void addListener(in IMediaEventListener listener);

	// Remove media listeners
	void removeAllListeners();
}