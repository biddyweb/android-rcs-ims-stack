package com.orangelabs.rcs.settings;

import android.content.Context;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * File tranfser invitation ringtone
 * 
 * @author jexa7410
 */
public class FileTransferInvitationRingtone extends RingtonePreference {

    public FileTransferInvitationRingtone(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Uri onRestoreRingtone() {
        String uri = RcsSettings.getInstance().getFileTransferInvitationRingtone();
        
        if (TextUtils.isEmpty(uri)) {
            return null;
        }
        
        return Uri.parse(uri);
    }

    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
    	if (ringtoneUri != null) {
    		RcsSettings.getInstance().setFileTransferInvitationRingtone(ringtoneUri.toString());
    	} else {
    		RcsSettings.getInstance().setFileTransferInvitationRingtone("");
    	}
    }
}