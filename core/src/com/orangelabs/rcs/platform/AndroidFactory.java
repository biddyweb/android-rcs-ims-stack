package com.orangelabs.rcs.platform;

import android.content.Context;

import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.platform.registry.RegistryFactory;

/**
 * Android platform
 * 
 * @author jexa7410
 */
public class AndroidFactory {
	/**
	 * Android application context
	 */
	private static Context context = null;

	/**
	 * Returns the application context
	 * 
	 * @return Context
	 */
	public static Context getApplicationContext() {
		return context;
	}

	/**
	 * Load factory
	 * 
	 * @param context Context
	 * @throws FactoryException
	 */
	public static void loadFactory(Context context) throws FactoryException {
		AndroidFactory.context = context;
		NetworkFactory.loadFactory("com.orangelabs.rcs.platform.network.AndroidNetworkFactory");
		RegistryFactory.loadFactory("com.orangelabs.rcs.platform.registry.AndroidRegistryFactory");
		FileFactory.loadFactory("com.orangelabs.rcs.platform.file.AndroidFileFactory");
	}
}
