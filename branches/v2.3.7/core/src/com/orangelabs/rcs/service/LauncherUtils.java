/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.service;

import com.orangelabs.rcs.platform.registry.AndroidRegistryFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;
import com.orangelabs.rcs.provisioning.https.HttpsProvisioningService;
import com.orangelabs.rcs.utils.logger.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

/**
 * Launcher utility functions
 *
 * @author hlxn7157
 */
public class LauncherUtils {
    /**
     * Key for provisioning version 
     */
    private static final String REGISTRY_PROVISIONING_VERSION = "ProvisioningVersion";

    /**
     * Last user account used
     */
    public static final String REGISTRY_LAST_USER_ACCOUNT = "LastUserAccount";

    /**
     * Last user account used
     */
    public static final String REGISTRY_CURRENT_USER_ACCOUNT = "CurrentUserAccount";

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(LauncherUtils.class.getName());

    /**
     * Launch the RCS service.
     *
     * @param context application context
     * @param boot indicates if RCS is launched from the device boot
     */
    public static void launchRcsService(Context context, boolean boot) {
        // Instantiate the settings manager
        RcsSettings.createInstance(context);
        // Set the current user account
        initCurrentUserAccount(context);
        
        if (logger.isActivated()) {
            logger.debug("launchRcsService HTTPS="
                + (RcsSettings.getInstance().getAutoConfigMode() == RcsSettingsData.HTTPS_AUTO_CONFIG)
                + " boot=" + boot);
        }
        if (RcsSettings.getInstance().getAutoConfigMode() == RcsSettingsData.HTTPS_AUTO_CONFIG) {
            // HTTPS auto config
            if (hasChangedAccount(context)) {
                // Activate service if new account
                RcsSettings.getInstance().setServiceActivationState(true);
            }
            // Check the last provisioning version
            if (getProvisioningVersion(context).equals("-1")) {
                if (hasChangedAccount(context)) {
                    // Reset provisioning version
                    setProvisioningVersion(context, "0");
                    // Start provisioning as a first launch
                    Intent intent = new Intent(HttpsProvisioningService.SERVICE_NAME);
                    intent.putExtra("first", true);
                    context.startService(intent);
                } else {
                    if (logger.isActivated()) {
                        logger.debug("Provisioning is blocked with this account");
                    }
                }
            } else {
                if (isFirstLaunch(context)) {
                    // First launch: start the auto config service with special tag
                    Intent intent = new Intent(HttpsProvisioningService.SERVICE_NAME);
                    intent.putExtra("first", true);
                    context.startService(intent);
                } else if (boot) {
                    // Boot: start the auto config service
                    context.startService(new Intent(HttpsProvisioningService.SERVICE_NAME));
                } else {
                    // Start the RCS service
                    launchRcsCoreService(context);
                }
            }
        } else {
            // No auto config: start the RCS service
            launchRcsCoreService(context);
        }
    }

    /**
     * Launch the RCS Core service.
     *
     * @param context application context
     */
    public static void launchRcsCoreService(Context context) {
        if (logger.isActivated()) {
            logger.debug("launchRcsCoreService");
        }
        if (RcsSettings.getInstance().isServiceActivated()) {
            context.startService(new Intent(RcsCoreService.SERVICE_NAME));
        }
    }

    /**
     * Stop the RCS service.
     *
     * @param context application context
     */
    public static void stopRcsService(Context context) {
        if (logger.isActivated()) {
            logger.debug("stopRcsService");
        }
        context.stopService(new Intent(HttpsProvisioningService.SERVICE_NAME));
        context.stopService(new Intent(RcsCoreService.SERVICE_NAME));
    }

    /**
     * Is the first RCs is launched ?
     *
     * @param context Application context
     * @return true if it's the first time RCS is launched
     */
    public static boolean isFirstLaunch(Context context) {
        return (getLastUserAccount(context) == null);
    }

    /**
     * Check if RCS account has changed since the last time we started the service
     *
     * @param context application context
     * @return true if the active account was changed
     */
    public static boolean hasChangedAccount(Context context) {
        String lastUserAccount = getLastUserAccount(context);
        String currentUserAccount = getCurrentUserAccount(context);
        if (lastUserAccount == null) {
            return true;
        } else if (currentUserAccount == null) {
            return false;
        } else {
            return (!currentUserAccount.equalsIgnoreCase(lastUserAccount));
        }
    }

    /**
     * Get the current user account
     *
     * @param context application context
     * @return current user account
     */
    public static String getCurrentUserAccount(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(AndroidRegistryFactory.RCS_PREFS, Activity.MODE_PRIVATE);
        return preferences.getString(REGISTRY_CURRENT_USER_ACCOUNT, null);
    }

    /**
     * Initiate the current user account from the imsi
     *
     * @param context application context
     */
    private static void initCurrentUserAccount(Context context) {
        TelephonyManager mgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = mgr.getSubscriberId();
        mgr = null;

        SharedPreferences preferences = context.getSharedPreferences(AndroidRegistryFactory.RCS_PREFS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(REGISTRY_CURRENT_USER_ACCOUNT, imsi);
        editor.commit();
    }

    /**
     * Get the last user account
     *
     * @param context application context
     * @return last user account
     */
    public static String getLastUserAccount(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(AndroidRegistryFactory.RCS_PREFS, Activity.MODE_PRIVATE);
        return preferences.getString(REGISTRY_LAST_USER_ACCOUNT, null);
    }

    /**
     * Set the last user account
     *
     * @param context application context
     * @param value last user account
     */
    public static void setLastUserAccount(Context context, String value) {
        SharedPreferences preferences = context.getSharedPreferences(AndroidRegistryFactory.RCS_PREFS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(REGISTRY_LAST_USER_ACCOUNT, value);
        editor.commit();
    }

    /**
     * Get the provisioning version from the registry
     *
     * @return provisioning version
     */
    public static String getProvisioningVersion(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(AndroidRegistryFactory.RCS_PREFS, Activity.MODE_PRIVATE);
        return preferences.getString(REGISTRY_PROVISIONING_VERSION, "0");
    }

    /**
     * Write the provisioning version in the registry
     *
     * @param value provisioning version
     */
    public static void setProvisioningVersion(Context context, String value) {
        SharedPreferences preferences = context.getSharedPreferences(AndroidRegistryFactory.RCS_PREFS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(REGISTRY_PROVISIONING_VERSION, value);
        editor.commit();
    }
}
