package com.orangelabs.rcs.service.api.client.toip;

import com.orangelabs.rcs.service.api.client.toip.IToIpSession;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.IMediaRenderer;

/**
 * ToIP API
 */
interface IToIpApi {
	// Initiate a ToIP session
	IToIpSession initiateToIpCall(in String contact, in IMediaPlayer player, in IMediaRenderer renderer);

	// Get a ToIP session from its session ID
	IToIpSession getToIpSession(in String id);
}

