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

import com.orangelabs.rcs.core.UserAccountManager;
import com.orangelabs.rcs.platform.registry.AndroidRegistryFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;
import com.orangelabs.rcs.provisioning.https.HttpsProvisioningService;
import com.orangelabs.rcs.utils.logger.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Launcher utility functions
 *
 * @author hlxn7157
 */
public class LauncherUtils {

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(LauncherUtils.class.getName());

    /**
     * Is the first RCs is launched ?
     *
     * @param context Application context
     * @return true if it's the first time RCS is launched
     */
    public static boolean isFirstLaunch(Context context) {
        // Get the last user account
        SharedPreferences preferences = context.getSharedPreferences(AndroidRegistryFactory.RCS_PREFS, Activity.MODE_PRIVATE);
        String lastUserAccount = preferences.getString(UserAccountManager.REGISTRY_LAST_USER_ACCOUNT, null);
        return (lastUserAccount == null);
    }

    /**
     * Launch the RCS service.
     *
     * @param context application context
     * @param boot indicates if RCS is launched from the device boot
     */
    public static void launchRcsService(Context context, boolean boot) {
        // Instantiate the settings manager
        RcsSettings.createInstance(context);
        if (logger.isActivated()) {
            logger.debug("launchRcsService HTTPS="
                + (RcsSettings.getInstance().getAutoConfigMode() == RcsSettingsData.HTTPS_AUTO_CONFIG)
                + " boot=" + boot);
        }
        if (RcsSettings.getInstance().getAutoConfigMode() == RcsSettingsData.HTTPS_AUTO_CONFIG) {
            // HTTPS auto config
            if (isFirstLaunch(context)) {
                // First launch: start the auto config service with special tag
                Intent intent = new Intent(HttpsProvisioningService.SERVICE_NAME);
                intent.putExtra("first", true);
                context.startService(intent);
            } else {
                if (boot) {
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
}
