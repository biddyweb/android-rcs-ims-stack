package com.orangelabs.rcs.service.api.client;

import com.orangelabs.rcs.service.api.client.MediaSample;
import com.orangelabs.rcs.service.api.client.IMediaEventListener;

/**
 * Media renderer
 */
interface IMediaRenderer {
	// Open the renderer
	void open();
	
	// Close the renderer
	void close();

	// Start the renderer
	void start();
	
	// Stop the renderer
	void stop();

	// Write a media sample
	void writeSample(in MediaSample sample);

	// Add a media listener
	void addListener(in IMediaEventListener listener);

	// Remove media listeners
	void removeAllListeners();
}