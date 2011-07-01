package com.orangelabs.rcs.service.api.client.sip;

import com.orangelabs.rcs.service.api.client.sip.ISipSession;

/**
 * SIP API
 */
interface ISipApi {

	// Initiate a session
	ISipSession initiateSession(in String contact, in String offer);

	// Get a session from its session ID
	ISipSession getSession(in String id);
}


