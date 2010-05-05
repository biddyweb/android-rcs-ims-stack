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
	public static final String PRESENCE_INVITATION_RINGTONE = "PresenceInvitationRingtone";
	public static final String PRESENCE_INVITATION_VIBRATE = "PresenceInvitationVibrate";
	
	public static final String CSH_INVITATION_RINGTONE = "CShInvitationRingtone";
	public static final String CSH_INVITATION_VIBRATE = "CShInvitationVibrate";

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
	
	// Boolean value
	public static final String TRUE_VALUE = Boolean.toString(true);
	public static final String FALSE_VALUE = Boolean.toString(false);
}
