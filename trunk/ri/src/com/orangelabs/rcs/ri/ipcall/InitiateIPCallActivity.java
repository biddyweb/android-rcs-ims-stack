package com.orangelabs.rcs.ri.ipcall;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.Utils;
import com.orangelabs.rcs.utils.logger.Logger;

public class InitiateIPCallActivity extends Activity {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RcsSettings.createInstance(getApplicationContext());

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.ipcall_initiate_call);

        // Set title
        setTitle(R.string.menu_initiate_ipcall);

        // Set the contact selector
        Spinner spinner = (Spinner)findViewById(R.id.contact);
        spinner.setAdapter(Utils.createRcsContactListAdapter(this));

        // Set button callback
        Button audioVideoInviteBtn = (Button)findViewById(R.id.audio_video_invite_btn);
        audioVideoInviteBtn.setOnClickListener(audioVideoInviteBtnListener);
        Button audioInviteBtn = (Button)findViewById(R.id.audio_invite_btn);
        audioInviteBtn.setOnClickListener(audioInviteBtnListener);

        // Disable button if no contact available
        if (spinner.getAdapter().getCount() == 0) {
        	logger.debug("no contact available disable buttons");
        	audioInviteBtn.setEnabled(false);
        	audioVideoInviteBtn.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    }

    /**
     * Dial button listener
     */
    private OnClickListener audioInviteBtnListener = new OnClickListener() {
        public void onClick(View v) {
        	// Get the remote contact
            Spinner spinner = (Spinner)findViewById(R.id.contact);
            MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
            final String remote = cursor.getString(1);


            // Initiate an audio IP call
            Intent intent = new Intent(getApplicationContext(), IPCallSessionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("contact", remote);
            intent.putExtra("video", false);
            intent.setAction("outgoing");
            getApplicationContext().startActivity(intent);
        }
    };

    /**
     * Invite button listener
     */
    private OnClickListener audioVideoInviteBtnListener = new OnClickListener() {
        public void onClick(View v) {
        	// Get the remote contact
            Spinner spinner = (Spinner)findViewById(R.id.contact);
            MatrixCursor cursor = (MatrixCursor)spinner.getSelectedItem();
            final String remote = cursor.getString(1);
            
            // Initiate an audio+video IP call
            Intent intent = new Intent(getApplicationContext(), IPCallSessionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("contact", remote);
            intent.putExtra("video", true);
            intent.setAction("outgoing");
            getApplicationContext().startActivity(intent);
        }
    };
}
