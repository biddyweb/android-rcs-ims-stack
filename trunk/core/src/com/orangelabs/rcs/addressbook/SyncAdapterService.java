/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright © 2010 France Telecom S.A.
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

package com.orangelabs.rcs.addressbook;

import com.orangelabs.rcs.utils.logger.Logger;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Service to handle account sync. This is invoked with an intent with action
 * ACTION_AUTHENTICATOR_INTENT. It instantiates the sync adapter and returns its
 * IBinder.
 */
public class SyncAdapterService extends Service {

    /**
     * Contacts sync adapter.
     */
    private RcsContactsSyncAdapter mSyncAdapter;

    /**
     * Android content sync adapter
     */
    public static final String ANDROID_CONTENT_SYNCADPTER = "android.content.SyncAdapter";
    
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate() {
        mSyncAdapter = new RcsContactsSyncAdapter(this);
    }

    /**
     * When binding to the service, return an interface to our sync adpater.
     */
    @Override
    public IBinder onBind(Intent intent) {
        if ((ANDROID_CONTENT_SYNCADPTER).equals(intent.getAction())) {
            return mSyncAdapter.getSyncAdapterBinder();
        }
        if (logger.isActivated()){
        	logger.error("Bound with unknown intent: " + intent);
        }
        return null;
    }

    /**
     * SyncAdapter that spawns a thread to invoke a sync operation.
     */
    private static class RcsContactsSyncAdapter extends AbstractThreadedSyncAdapter {

        public RcsContactsSyncAdapter(Context context) {
            super(context, true /* autoInitialize */);

        }

        /**
         * Perform a sync for this account.
         */
        @Override
        public void onPerformSync(Account account, Bundle extras,
            String authority, ContentProviderClient provider,
            SyncResult syncResult) {
        	
        	//TODO synchro code here 
        	
        	return;
        }

    }
}

