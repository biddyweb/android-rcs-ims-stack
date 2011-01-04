package com.orangelabs.rcs.service.api.client.management;

/**
 * Management API
 */
interface IManagementApi {
	// Set trace activation flag
	void setTraceActivation(in boolean state);

	// Set trace level
	void setTraceLevel(in int level);

	// Is trace activated
	boolean isTraceActivated();

	// Get trace level
	int getTraceLevel();
}
