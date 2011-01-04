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
package com.orangelabs.rcs.service.api.client;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class ClientApiUtils {
    /**
     * Intent related to external RCS applications
     */
	public final static String RCS_APPS = "com.orangelabs.rcs.EXT_APPLICATION";

    /**
     * Intent related to Instant Messaging Sessions applications
     */
	public final static String RCS_IM_SESSION_APPS = "com.orangelabs.rcs.IM_SESSION_APPLICATION";
	
    /**
     * Intent related to File Transfer applications
     */
	public final static String RCS_FILE_TRANSFER_APPS = "com.orangelabs.rcs.FILE_TRANSFER_APPLICATION";
	
    /**
     * Intent related to external RCS applications supporting content sharing
     */
	public final static String CONTENT_SHARING = "com.orangelabs.rcs.EXT_SHARING_APPLICATION";

	/**
	 * Returns the list of external RCS applications
	 * 
	 * @param ctx Context
	 * @return List
	 */
	public static List<ResolveInfo> getExternalRcsApplications(Context ctx) {
		final PackageManager packageManager = ctx.getPackageManager();
		final Intent intent = new Intent(RCS_APPS);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list;
	}

	/**
	 * Returns the list of applications supporting the content sharing
	 * 
	 * @param ctx Context
	 * @return List
	 */
	public static List<ResolveInfo> getContentSharingApplications(Context ctx) {
		final PackageManager packageManager = ctx.getPackageManager();
		final Intent intent = new Intent(CONTENT_SHARING);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list;
	}
	
	/**
	 * Returns the list of RCS applications handling Instant Messaging Sessions
	 * 
	 * @param ctx Context
	 * @return List
	 */
	public static List<ResolveInfo> getInstantMessagingSessionApplications(Context ctx) {
		final PackageManager packageManager = ctx.getPackageManager();
		final Intent intent = new Intent(RCS_IM_SESSION_APPS);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list;
	}
	
	/**
	 * Returns the list of RCS applications handling File Transfer
	 * 
	 * @param ctx Context
	 * @return List
	 */
	public static List<ResolveInfo> getFileTransferApplications(Context ctx) {
		final PackageManager packageManager = ctx.getPackageManager();
		final Intent intent = new Intent(RCS_FILE_TRANSFER_APPS);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list;
	}
	
}
