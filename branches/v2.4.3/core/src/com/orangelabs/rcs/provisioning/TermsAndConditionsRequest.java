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

package com.orangelabs.rcs.provisioning;

import com.orangelabs.rcs.R;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provisioning.https.HttpsProvisioningService;
import com.orangelabs.rcs.service.LauncherUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * Show the request for terms and conditions
 *
 * @author hlxn7157
 */
public class TermsAndConditionsRequest extends Activity {

    /**
     * Intent keys
     */
    public static final String TERMS_OBJECT = "com.orangelabs.rcs.provisioning.TERMS_OBJECT";
    public static final String MESSAGE_KEY = "message_key";
    public static final String TITLE_KEY = "title_key";
    public static final String ACCEPT_BTN_KEY = "accept_btn_key";
    public static final String REJECT_BTN_KEY = "reject_btn_key";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Bundle bundle = getIntent().getBundleExtra(TERMS_OBJECT);

        if (bundle != null) {
            String title = bundle.getString(TITLE_KEY);
            String message = bundle.getString(MESSAGE_KEY);
            boolean accept_btn = bundle.getBoolean(ACCEPT_BTN_KEY);
            boolean reject_btn = bundle.getBoolean(REJECT_BTN_KEY);

            if (!TextUtils.isEmpty(message)) {
                // Add text
                TextView textView = new TextView(this);
                textView.setAutoLinkMask(Linkify.ALL);
                textView.setText(message);
                textView.setPadding(10, 10, 10, 10);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(title).setView(textView);

                // If accept and reject is enabled, then create Alert dialog
                // with two buttons else with neutral button
                if (accept_btn && reject_btn) {
                    builder.setPositiveButton(R.string.rcs_core_terms_accept,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Set terms and conditions accepted
                                    RcsSettings.createInstance(getApplicationContext());
                                    RcsSettings.getInstance().setProvisioningTermsAccepted(true);
                                    finish();
                                }
                            });

                    builder.setNegativeButton(R.string.rcs_core_terms_decline,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // If the user declines the terms, the RCS service is stopped and the RCS config is reset
                                    LauncherUtils.stopRcsService(getApplicationContext());
                                    LauncherUtils.resetRcsConfig(getApplicationContext());
                                    HttpsProvisioningService.setProvisioningVersion(getApplicationContext(), "0");
                                    finish();
                                }
                            });
                } else {
                    builder.setNeutralButton(R.string.rcs_core_terms_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Set terms and conditions accepted
                                    RcsSettings.createInstance(getApplicationContext());
                                    RcsSettings.getInstance().setProvisioningTermsAccepted(true);
                                    finish();
                                }
                            });
                }

                AlertDialog alert = builder.create(); 
                alert.setCanceledOnTouchOutside(false);
                alert.setCancelable(false);
                alert.show();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }
}
