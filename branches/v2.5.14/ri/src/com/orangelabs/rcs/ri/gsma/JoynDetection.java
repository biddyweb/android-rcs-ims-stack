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

package com.orangelabs.rcs.ri.gsma;

import java.util.Vector;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.service.api.client.gsma.GsmaClientConnector;

/**
 * Joyn client detection
 *
 * @author Beno”t JOGUET
 */
public class JoynDetection extends ListActivity {
    /**
     * List of setting intent
     */
    private Intent[] settingIntents = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set UI title
        setTitle(getString(R.string.title_joyn_detection));

        // Update list
        getJoynClient();
    }

    /**
     * Update list of Joyn clients
     */
    private void getJoynClient() {
        Vector<ApplicationInfo> rcsClients = GsmaClientConnector.getRcsClients(this);
        String[] items = new String[rcsClients.size()];
        settingIntents = new Intent[rcsClients.size()];
        String packageName = "com.orangelabs.rcs";

        for (int i = 0; i < rcsClients.size(); i++) {
            packageName = rcsClients.get(i).packageName;
            items[i] = packageName + " / started " + GsmaClientConnector.isRcsClientActivated(getApplicationContext(), packageName);
            settingIntents[i] = GsmaClientConnector.getRcsSettingsActivityIntent(this, packageName);
        }
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (settingIntents[position] != null) {
            startActivity(settingIntents[position]);
        }
    }
}
