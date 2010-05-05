package com.orangelabs.rcs.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.provider.settings.RcsSettings;

/**
 * Content sharing settings display
 * 
 * @author jexa7410
 */
public class CShSettingsDisplay extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
	private CheckBoxPreference vibrate;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.rcs_settings_csh_preferences);
        
        vibrate = (CheckBoxPreference)findPreference("csh_invitation_vibration");
        vibrate.setPersistent(false);
        vibrate.setOnPreferenceChangeListener(this);
        vibrate.setChecked(RcsSettings.getInstance().isPhoneVibrateForCShInvitation());
	}

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference.getKey().equals("csh_invitation_vibration")) {
        	Boolean state = (Boolean)objValue;
        	RcsSettings.getInstance().setPhoneVibrateForCShInvitation(state.booleanValue());
        }
        return true;
    }    
}