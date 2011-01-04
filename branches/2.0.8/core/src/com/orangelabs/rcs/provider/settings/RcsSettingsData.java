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
package com.orangelabs.rcs.provider.settings;

import android.net.Uri;

/**
 * RCS settings data constants
 * 
 * @author jexa7410
 */
public class RcsSettingsData {
	// Database URI
	public final static Uri CONTENT_URI = Uri.parse("content://com.orangelabs.rcs.settings/settings"); 
	
	// Column names
	public static final String KEY_ID = "_id";
	public static final String KEY_KEY = "key";
	public static final String KEY_VALUE = "value";

	// Data
	public static final String SERVICE_ACTIVATED = "ServiceActivated";
	
	public static final String PRESENCE_INVITATION_RINGTONE = "PresenceInvitationRingtone";
	public static final String PRESENCE_INVITATION_VIBRATE = "PresenceInvitationVibrate";
	public static final String PRESENCE_HYPERAVAILABILITY_BEEP = "PresenceHyperAvailabilityBeep";
	public static final String PRESENCE_HYPERAVAILABILITY_VIBRATE = "PresenceHyperAvailabilityVibrate";
	
	public static final String CSH_INVITATION_RINGTONE = "CShInvitationRingtone";
	public static final String CSH_INVITATION_VIBRATE = "CShInvitationVibrate";
	public static final String CSH_AVAILABLE_BEEP = "CShAvailableBeep";
	public static final String CSH_VIDEO_FORMAT = "CShVideoFormat";
	public static final String CSH_VIDEO_SIZE = "CShVideoSize";
	
	public static final String FILETRANSFER_INVITATION_RINGTONE = "FileTransferInvitationRingtone";
	public static final String FILETRANSFER_INVITATION_VIBRATE = "FileTransferInvitationVibrate";

	public static final String FREETEXT1 = "Freetext1";
	public static final String FREETEXT2 = "Freetext2";
	public static final String FREETEXT3 = "Freetext3";
	public static final String FREETEXT4 = "Freetext4";
	
	public static final String USERPROFILE_USERNAME = "Username";
	public static final String USERPROFILE_DISPLAY_NAME = "DisplayName";
	public static final String USERPROFILE_PRIVATE_ID = "PrivateId";
	public static final String USERPROFILE_PASSWORD = "Password";
	public static final String USERPROFILE_HOME_DOMAIN = "HomeDomain";
	public static final String USERPROFILE_SIP_PROXY = "OutboundProxyAddr";
	public static final String USERPROFILE_XDM_SERVER = "XdmServerAddr";
	public static final String USERPROFILE_XDM_LOGIN= "XdmServerLogin";
	public static final String USERPROFILE_XDM_PASSWORD = "XdmServerPassword";
	public static final String USERPROFILE_IM_CONF_URI = "ImConferenceUri";
	
	// Boolean value
	public static final String TRUE_VALUE = Boolean.toString(true);
	public static final String FALSE_VALUE = Boolean.toString(false);
}
