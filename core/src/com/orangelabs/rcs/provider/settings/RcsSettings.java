package com.orangelabs.rcs.provider.settings;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * RCS settings
 * 
 * @author jexa7410
 */
public class RcsSettings {
	/**
	 * Current instance
	 */
	private static RcsSettings instance = null;

	/**
	 * Content resolver
	 */
	private ContentResolver cr;
	
	/**
	 * Database URI
	 */
	private Uri databaseUri = RcsSettingsData.CONTENT_URI;

	/**
	 * Create instance
	 * 
	 * @param ctx Context
	 */
	public static synchronized void createInstance(Context ctx) {
		if (instance == null) {
			instance = new RcsSettings(ctx);
		}
	}
	
	/**
	 * Returns instance
	 * 
	 * @return Instance
	 */
	public static RcsSettings getInstance() {
		return instance;
	}
	
	/**
     * Constructor
     * 
     * @param ctx Application context
     */
	private RcsSettings(Context ctx) {
		super();
		
        this.cr = ctx.getContentResolver();
	}
	
	/**
	 * Read a parameter
	 * 
	 * @param key Key
	 * @return Value
	 */
	private String readParameter(String key) {
		String result = null;
        Cursor c = cr.query(databaseUri, null, RcsSettingsData.KEY_KEY + "='" + key + "'", null, null);
        if (c != null) {
        	if (c.getCount() > 0) {
		        if (c.moveToFirst()) {
		        	result = c.getString(2);
		        }
        	}
	        c.close();
        }
        return result;
	}
	
	/**
	 * Write a parameter
	 * 
	 * @param key Key
	 * @param value Value
	 */
	private void writeParameter(String key, String value) {
        ContentValues values = new ContentValues();
        values.put(RcsSettingsData.KEY_VALUE, value);
        String where = RcsSettingsData.KEY_KEY + "='" + key + "'";
        cr.update(databaseUri, values, where, null);
	}

	/**
	 * Get the ringtone for presence invitation
	 * 
	 * @return Ringtone URI or null if there is no ringtone
	 */
	public String getPresenceInvitationRingtone() {
		String result = null;
		if (instance != null) {
			result = readParameter(RcsSettingsData.PRESENCE_INVITATION_RINGTONE);
		}
		return result;
	}
	
	/**
	 * Set the presence invitation ringtone
	 * 
	 * @param uri Ringtone URI
	 */
	public void setPresenceInvitationRingtone(String uri) {
		if (instance != null) {
			writeParameter(RcsSettingsData.PRESENCE_INVITATION_RINGTONE, uri);
		}
	}

	/**
	 * Is phone vibrate for presence invitation
	 * 
	 * @return Boolean
	 */
	public boolean isPhoneVibrateForPresenceInvitation() {
		boolean result = false;
		if (instance != null) {
			return Boolean.parseBoolean(readParameter(RcsSettingsData.PRESENCE_INVITATION_VIBRATE));
		}
		return result;
	}	

	/**
	 * Set phone vibrate for presence invitation
	 * 
	 * @param vibrate Vibrate state
	 */
	public void setPhoneVibrateForPresenceInvitation(boolean vibrate) {
		if (instance != null) {
			writeParameter(RcsSettingsData.PRESENCE_INVITATION_VIBRATE, Boolean.toString(vibrate));
		}
	}	
	
	/**
	 * Get the ringtone for CSh invitation
	 * 
	 * @return Ringtone URI or null if there is no ringtone
	 */
	public String getCShInvitationRingtone() {
		String result = null;
		if (instance != null) {
			result = readParameter(RcsSettingsData.CSH_INVITATION_RINGTONE);
		}
		return result;
	}
	
	/**
	 * Set the CSh invitation ringtone
	 * 
	 * @param uri Ringtone URI
	 */
	public void setCShInvitationRingtone(String uri) {
		if (instance != null) {
			writeParameter(RcsSettingsData.CSH_INVITATION_RINGTONE, uri);
		}
	}

	/**
	 * Is phone vibrate for CSh invitation
	 * 
	 * @return Boolean
	 */
	public boolean isPhoneVibrateForCShInvitation() {
		boolean result = false;
		if (instance != null) {
			return Boolean.parseBoolean(readParameter(RcsSettingsData.CSH_INVITATION_VIBRATE));
		}
		return result;
	}	

	/**
	 * Set phone vibrate for CSh invitation
	 * 
	 * @param vibrate Vibrate state
	 */
	public void setPhoneVibrateForCShInvitation(boolean vibrate) {
		if (instance != null) {
			writeParameter(RcsSettingsData.CSH_INVITATION_VIBRATE, Boolean.toString(vibrate));
		}
	}	

	/**
	 * Get the ringtone for file transfer invitation
	 * 
	 * @return Ringtone URI or null if there is no ringtone
	 */
	public String getFileTransferInvitationRingtone() {
		String result = null;
		if (instance != null) {
			result = readParameter(RcsSettingsData.FILETRANSFER_INVITATION_RINGTONE);
		}
		return result;
	}
	
	/**
	 * Set the file transfer invitation ringtone
	 * 
	 * @param uri Ringtone URI
	 */
	public void setFileTransferInvitationRingtone(String uri) {
		if (instance != null) {
			writeParameter(RcsSettingsData.FILETRANSFER_INVITATION_RINGTONE, uri);
		}
	}

	/**
	 * Is phone vibrate for file transfer invitation
	 * 
	 * @return Boolean
	 */
	public boolean isPhoneVibrateForFileTransferInvitation() {
		boolean result = false;
		if (instance != null) {
			return Boolean.parseBoolean(readParameter(RcsSettingsData.FILETRANSFER_INVITATION_VIBRATE));
		}
		return result;
	}	

	/**
	 * Set phone vibrate for file transfer invitation
	 * 
	 * @param vibrate Vibrate state
	 */
	public void setPhoneVibrateForFileTransferInvitation(boolean vibrate) {
		if (instance != null) {
			writeParameter(RcsSettingsData.FILETRANSFER_INVITATION_VIBRATE, Boolean.toString(vibrate));
		}
	}	
		
	/**
	 * Get the pre-defined freetext 1
	 * 
	 * @return String
	 */
	public String getPredefinedFreetext1() {
		String result = null;
		if (instance != null) {
			result = readParameter(RcsSettingsData.FREETEXT1);
		}
		return result;
	}

	/**
	 * Set the pre-defined freetext 1
	 * 
	 * @param txt Text
	 */
	public void setPredefinedFreetext1(String txt) {
		if (instance != null) {
			writeParameter(RcsSettingsData.FREETEXT1, txt);
		}
	}

	/**
	 * Get the pre-defined freetext 2
	 * 
	 * @return String
	 */
	public String getPredefinedFreetext2() {
		String result = null;
		if (instance != null) {
			result = readParameter(RcsSettingsData.FREETEXT2);
		}
		return result;
	}
	
	/**
	 * Set the pre-defined freetext 2
	 * 
	 * @param txt Text
	 */
	public void setPredefinedFreetext2(String txt) {
		if (instance != null) {
			if (instance != null) {
				writeParameter(RcsSettingsData.FREETEXT2, txt);
			}
		}
	}

	/**
	 * Get the pre-defined freetext 3
	 * 
	 * @return String
	 */
	public String getPredefinedFreetext3() {
		String result = null;
		if (instance != null) {
			result = readParameter(RcsSettingsData.FREETEXT3);
		}
		return result;
	}

	/**
	 * Set the pre-defined freetext 3
	 * 
	 * @param txt Text
	 */
	public void setPredefinedFreetext3(String txt) {
		if (instance != null) {
			if (instance != null) {
				writeParameter(RcsSettingsData.FREETEXT3, txt);
			}
		}
	}

	/**
	 * Get the pre-defined freetext 4
	 * 
	 * @return String
	 */
	public String getPredefinedFreetext4() {
		String result = null;
		if (instance != null) {
			result = readParameter(RcsSettingsData.FREETEXT4);
		}
		return result;
	}
	
	/**
	 * Set the pre-defined freetext 4
	 * 
	 * @param txt Text
	 */
	public void setPredefinedFreetext4(String txt) {
		if (instance != null) {
			if (instance != null) {
				writeParameter(RcsSettingsData.FREETEXT4, txt);
			}
		}
	}

	/**
	 * Returns user profile username
	 * 
	 * @return String value
	 */
	public String getUserProfileUserName() {
		String result = null;
		if (instance != null) {
			return readParameter(RcsSettingsData.USERPROFILE_USERNAME);
		}
		return result;
	}	

	/**
	 * Set user profile username
	 * 
	 * @param value Value
	 */
	public void setUserProfileUserName(String value) {
		if (instance != null) {
			writeParameter(RcsSettingsData.USERPROFILE_USERNAME, value);
		}
	}	

	/**
	 * Returns user profile display name
	 * 
	 * @return String value
	 */
	public String getUserProfileDisplayName() {
		String result = null;
		if (instance != null) {
			return readParameter(RcsSettingsData.USERPROFILE_DISPLAY_NAME);
		}
		return result;
	}	

	/**
	 * Set user profile display name
	 * 
	 * @param value Value
	 */
	public void setUserProfileDisplayName(String value) {
		if (instance != null) {
			writeParameter(RcsSettingsData.USERPROFILE_DISPLAY_NAME, value);
		}
	}	

	/**
	 * Returns user profile private Id
	 * 
	 * @return String value
	 */
	public String getUserProfilePrivateId() {
		String result = null;
		if (instance != null) {
			return readParameter(RcsSettingsData.USERPROFILE_PRIVATE_ID);
		}
		return result;
	}	

	/**
	 * Set user profile private Id
	 * 
	 * @param value Value
	 */
	public void setUserProfilePrivateId(String value) {
		if (instance != null) {
			writeParameter(RcsSettingsData.USERPROFILE_PRIVATE_ID, value);
		}
	}	

	/**
	 * Returns user profile password
	 * 
	 * @return String value
	 */
	public String getUserProfilePassword() {
		String result = null;
		if (instance != null) {
			return readParameter(RcsSettingsData.USERPROFILE_PASSWORD);
		}
		return result;
	}	

	/**
	 * Set user profile password
	 * 
	 * @param value Value
	 */
	public void setUserProfilePassword(String value) {
		if (instance != null) {
			writeParameter(RcsSettingsData.USERPROFILE_PASSWORD, value);
		}
	}	

	/**
	 * Returns user profile domain
	 * 
	 * @return String value
	 */
	public String getUserProfileDomain() {
		String result = null;
		if (instance != null) {
			return readParameter(RcsSettingsData.USERPROFILE_HOME_DOMAIN);
		}
		return result;
	}	

	/**
	 * Set user profile domain
	 * 
	 * @param value Value
	 */
	public void setUserProfileDomain(String value) {
		if (instance != null) {
			writeParameter(RcsSettingsData.USERPROFILE_HOME_DOMAIN, value);
		}
	}

	/**
	 * Returns user profile proxy
	 * 
	 * @return String value
	 */
	public String getUserProfileProxy() {
		String result = null;
		if (instance != null) {
			return readParameter(RcsSettingsData.USERPROFILE_SIP_PROXY);
		}
		return result;
	}	

	/**
	 * Set user profile proxy
	 * 
	 * @param value Value
	 */
	public void setUserProfileProxy(String value) {
		if (instance != null) {
			writeParameter(RcsSettingsData.USERPROFILE_SIP_PROXY, value);
		}
	}

	/**
	 * Returns user profile XDM server
	 * 
	 * @return String value
	 */
	public String getUserProfileXdmServer() {
		String result = null;
		if (instance != null) {
			return readParameter(RcsSettingsData.USERPROFILE_XDM_SERVER);
		}
		return result;
	}	

	/**
	 * Set user profile XDM server
	 * 
	 * @param value Value
	 */
	public void setUserProfileXdmServer(String value) {
		if (instance != null) {
			writeParameter(RcsSettingsData.USERPROFILE_XDM_SERVER, value);
		}
	}

	/**
	 * Returns user profile XDM login
	 * 
	 * @return String value
	 */
	public String getUserProfileXdmLogin() {
		String result = null;
		if (instance != null) {
			return readParameter(RcsSettingsData.USERPROFILE_XDM_LOGIN);
		}
		return result;
	}	

	/**
	 * Set user profile XDM login
	 * 
	 * @param value Value
	 */
	public void setUserProfileXdmLogin(String value) {
		if (instance != null) {
			writeParameter(RcsSettingsData.USERPROFILE_XDM_LOGIN, value);
		}
	}

	/**
	 * Returns user profile XDM password
	 * 
	 * @return String value
	 */
	public String getUserProfileXdmPassword() {
		String result = null;
		if (instance != null) {
			return readParameter(RcsSettingsData.USERPROFILE_XDM_PASSWORD);
		}
		return result;
	}	

	/**
	 * Set user profile XDM password
	 * 
	 * @param value Value
	 */
	public void setUserProfileXdmPassword(String value) {
		if (instance != null) {
			writeParameter(RcsSettingsData.USERPROFILE_XDM_PASSWORD, value);
		}
	}
}
