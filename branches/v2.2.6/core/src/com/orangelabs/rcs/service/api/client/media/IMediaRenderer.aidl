package com.orangelabs.rcs.service.api.client.media;

import com.orangelabs.rcs.service.api.client.media.IMediaEventListener;

/**
 * Media RTP renderer
 */
interface IMediaRenderer {
	// Open the renderer
	void open(in String remoteHost, in int remotePort);
	
	// Close the renderer
	void close();

	// Start the renderer
	void start();

	// Stop the renderer
	void stop();

	// Returns the local RTP port
	int getLocalRtpPort();

	// Add a media listener
	void addListener(in IMediaEventListener listener);

	// Remove media listeners
	void removeAllListeners();
}