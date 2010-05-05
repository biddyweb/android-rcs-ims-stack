package com.orangelabs.rcs.settings;

import android.content.Context;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * CSh invitation ringtone
 * 
 * @author jexa7410
 */
public class CShInvitationRingtone extends RingtonePreference {

    public CShInvitationRingtone(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Uri onRestoreRingtone() {
        String uri = RcsSettings.getInstance().getCShInvitationRingtone();
        
        if (TextUtils.isEmpty(uri)) {
            return null;
        }
        
        return Uri.parse(uri);
    }

    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
    	if (ringtoneUri != null) {
    		RcsSettings.getInstance().setCShInvitationRingtone(ringtoneUri.toString());
    	} else {
    		RcsSettings.getInstance().setCShInvitationRingtone("");
    	}
    }
}