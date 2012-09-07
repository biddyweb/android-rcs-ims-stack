package com.orangelabs.rcs.service.api.client.gsma;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * GSMA client connector based on GSMA Implementation guidelines 3.0
 * 
 * @author jexa7410
 */
public class GsmaClientConnector {
	/**
	 * GSMA client registry name
	 */
	public static final String GSMA_PREFS_NAME = "gsma.joyn.preferences";
	
	/**
	 * GSMA client tag
	 */
	public static final String GSMA_CLIENT = "gsma.joyn.client";
	
	/**
	 * GSMA client enabled tag
	 */
	public static final String GSMA_CLIENT_ENABLED = "gsma.joyn.enabled";
	
	/**
     * Is device RCS compliant
     * 
     * @param ctx Context
     * @return Boolean
     */
    public static boolean isDeviceRcsCompliant(Context ctx) {
    	try {
    	    List<ApplicationInfo> apps = ctx.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    	    for(int i=0; i < apps.size(); i++) {
    	    	ApplicationInfo info = apps.get(i);
    	        if (info.metaData != null) {
    	        	if (info.metaData.getBoolean(GSMA_CLIENT, false)) {
    	        		return true;
    	        	}
    	        }
    	    }
    		return false;
    	} catch(Exception e) {
    		return false;
    	}
    }
    
    /**
     * Get the RCS settings intent
     * 
     * @param ctx Context
     * @return Intent or null
     */
    public static Intent getRcsSettingsActivityIntent(Context ctx) {
    	try {
    	    List<ApplicationInfo> apps = ctx.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    	    for(int i=0; i < apps.size(); i++) {
    	    	ApplicationInfo info = apps.get(i);
    	        if (info.metaData != null) {
    	        	String activity = info.metaData.getString("gsma.joyn.settings.activity");
    	        	if (activity != null) {
    	        		return new Intent(activity);
    	        	}
    	        }
    	    }
    		return null;
    	} catch(Exception e) {
    		return null;
    	}
    }
    
    /**
     * Is RCS client activated
     * 
     * @param ctx Context
     * @param client Client package name
     * @return Boolean
     */
    public static boolean isRcsClientActivated(Context ctx, String client) {
		try {
			Context appContext = ctx.createPackageContext(client, Context.MODE_WORLD_WRITEABLE);
			SharedPreferences prefs = appContext.getSharedPreferences(GsmaClientConnector.GSMA_PREFS_NAME, Context.MODE_WORLD_READABLE);
			return prefs.getBoolean(GsmaClientConnector.GSMA_CLIENT_ENABLED, false);
		} catch(Exception e) {
			return false;
		}
    }    
}
