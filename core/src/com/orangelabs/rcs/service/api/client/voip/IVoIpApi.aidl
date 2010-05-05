package com.orangelabs.rcs.service.api.client.voip;

import com.orangelabs.rcs.service.api.client.voip.IVoIpSession;
import com.orangelabs.rcs.service.api.client.IMediaPlayer ;
import com.orangelabs.rcs.service.api.client.IMediaRenderer ;

/**
 * VoIP API
 */
interface IVoIpApi {

	// Initiate a VoIP call
	IVoIpSession initiateVoIpCall(in String contact, in IMediaPlayer player, in IMediaRenderer renderer);

	// Get a VoIp session from its session ID
	IVoIpSession getVoIpSession(in String id);
}


