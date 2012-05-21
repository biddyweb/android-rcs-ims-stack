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

import android.content.Context;
import android.content.Intent;

import com.orangelabs.rcs.addressbook.AccountChangedReceiver;
import com.orangelabs.rcs.addressbook.AuthenticationService;
import com.orangelabs.rcs.provider.eab.ContactsManager;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.ClientApiUtils;
import com.orangelabs.rcs.utils.logger.Logger;

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
     * Launch the RCS service
     *
     * @param context application context
     * @param boot indicates if RCS is launched from the device boot
     */
    public static void launchRcsService(Context context, boolean boot) {
        if (logger.isActivated()) {
            logger.debug("Launch RCS service");
        }
        Intent intent = new Intent(ClientApiUtils.STARTUP_SERVICE_NAME);
        intent.putExtra("boot", boot);
        context.startService(intent);
    }    
    
    /**
     * Launch the RCS core service
     *
     * @param context Application context
     */
    public static void launchRcsCoreService(Context context) {
        if (logger.isActivated()) {
            logger.debug("Launch RCS core service");
        }
        if (RcsSettings.getInstance().isServiceActivated() && RcsSettings.getInstance().checkUserProfile()) {
        	context.startService(new Intent(ClientApiUtils.RCS_SERVICE_NAME));
        }
    }

    /**
     * Force a launch the RCS core service
     *
     * @param context application context
     */
    public static void forceLaunchRcsCoreService(Context context) {
        if (logger.isActivated()) {
            logger.debug("Force launch RCS core service");
        }
        if (RcsSettings.getInstance().checkUserProfile()) {
            RcsSettings.getInstance().setServiceActivationState(true);
            context.startService(new Intent(ClientApiUtils.RCS_SERVICE_NAME));
        }
    }

    /**
     * Stop the RCS service
     *
     * @param context Application context
     */
    public static void stopRcsService(Context context) {
        if (logger.isActivated()) {
            logger.debug("Stop RCS service");
        }
        context.stopService(new Intent(ClientApiUtils.STARTUP_SERVICE_NAME));
        context.stopService(new Intent(ClientApiUtils.PROVISIONING_SERVICE_NAME));
        context.stopService(new Intent(ClientApiUtils.RCS_SERVICE_NAME));
    }
    
    /**
     * Reset RCS config
     *
     * @param context Application context
     */
    public static void resetRcsConfig(Context context) {
        if (logger.isActivated()) {
            logger.debug("Reset RCS config");
        }

        // Stop the Core service
        context.stopService(new Intent(ClientApiUtils.RCS_SERVICE_NAME));

        // Clean the RCS user profile
        RcsSettings.createInstance(context);
        RcsSettings.getInstance().removeUserProfile();

        // Clean the RCS database
        ContactsManager.createInstance(context);
        ContactsManager.getInstance().deleteRCSEntries();

        // Remove the RCS account 
        AuthenticationService.removeRcsAccount(context, null);
        AccountChangedReceiver.setAccountResetByEndUser(false);

        // Clean terms status
        RcsSettings.getInstance().setProvisioningTermsAccepted(false);
    }
}
