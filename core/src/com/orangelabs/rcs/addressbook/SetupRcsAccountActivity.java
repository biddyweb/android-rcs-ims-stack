package com.orangelabs.rcs.addressbook;

import com.orangelabs.rcs.R;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.os.Bundle;

/**
 * Setup RCS account activity
 */
public class SetupRcsAccountActivity extends android.accounts.AccountAuthenticatorActivity {

	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		
		// Instanciate contacts manager
		ContactsManager.createInstance(this);
		
		// Create RCS account
		AuthenticationService.createRcsAccount(this, getString(R.string.rcs_core_account_username), true, true);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			AccountAuthenticatorResponse response = extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
			Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ACCOUNT_NAME, getString(R.string.rcs_core_account_username));
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, AuthenticationService.ACCOUNT_MANAGER_TYPE);
			response.onResult(result);
		}
		finish();
	}
	
}
