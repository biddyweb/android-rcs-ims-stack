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

package com.orangelabs.rcs.ri.messaging;

import com.orangelabs.rcs.utils.logger.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * File transfer outgoing resume receiver
 * 
 * @author YPLO6403
 *
 */
public class FileTransferResumeReceiver extends BroadcastReceiver {

	/** The logger */
	private final static Logger logger = Logger.getLogger(FileTransferResumeReceiver.class.getSimpleName());

	@Override
	public void onReceive(Context context, Intent intent) {
		if (logger.isActivated()) {
			logger.debug("onReceive");
		}
		// Display progress
		Intent intentLocal = new Intent(intent);
		intentLocal.setClass(context, InitiateFileTransfer.class);
		intentLocal.addFlags(Intent.FLAG_FROM_BACKGROUND);
		intentLocal.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		intentLocal.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentLocal.setAction("FT_OUTGOING_RESUME");
		context.startActivity(intentLocal);
	}
}
