package com.orangelabs.rcs.ri.messaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.ri.R;

/**
 * Geoloc info editor 
 */
public class EditGeoloc extends Activity {
	/**
	 * Location label editor
	 */
	private EditText locationEdit;
	
	/**
	 * Latitude editor
	 */
	private EditText latitudeEdit;
	
	/**
	 * Longitude editor
	 */
	private EditText longitudeEdit;
	
	/**
	 * Altitude editor
	 */
	private EditText altitudeEdit;

	/**
	 * Activity result constant
	 */
	public final static int SELECT_GEOLOCATION = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set title
		setTitle(R.string.menu_send_geoloc);
		
		// Set layout
		setContentView(R.layout.messaging_edit_geoloc);

		// Create editors
		locationEdit = (EditText)findViewById(R.id.location);
		InputFilter maxLengthFilter = new InputFilter.LengthFilter(RcsSettings.getInstance().getMaxGeolocLabelLength());
		locationEdit.setFilters(new InputFilter[]{ maxLengthFilter });
		latitudeEdit = (EditText)findViewById(R.id.latitude);
		longitudeEdit = (EditText)findViewById(R.id.longitude);
		altitudeEdit = (EditText)findViewById(R.id.altitude);
		
        // Set button callback
        Button sendBtn = (Button)findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(btnSendListener);	
        
        // Set button callback
        Button selectBtn = (Button)findViewById(R.id.select_geoloc_btn);
        selectBtn.setOnClickListener(btnSelectListener);
        
        setMyLocation();
	}
	
	/**
	 * Set the location of the device
	 */
	protected void setMyLocation() {
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String bestProvider = lm.getBestProvider(criteria, false);
		
		Location lastKnownLoc = lm.getLastKnownLocation(bestProvider);
		if (lastKnownLoc!= null) {
			latitudeEdit.setText(String.valueOf(lastKnownLoc.getLatitude()));
			longitudeEdit.setText(String.valueOf(lastKnownLoc.getLongitude()));
			altitudeEdit.setText(String.valueOf(lastKnownLoc.getAltitude()));
		}
		super.onResume();
	}
	
    /**
     * Invite button listener
     */
    private OnClickListener btnSendListener = new OnClickListener() {
        public void onClick(View v) {
        	String lat = latitudeEdit.getText().toString().trim();
    		if (lat.length() == 0) { 	
    			latitudeEdit.setText("0.0");
    		}
    		
        	String lon = longitudeEdit.getText().toString().trim();
    		if (lon.length() == 0) { 	
    			longitudeEdit.setText("0.0");
    		}
    		
        	String alt = altitudeEdit.getText().toString().trim();
    		if (alt.length() == 0) { 	
    			altitudeEdit.setText("0.0");
    		}
    		
    		Intent in = new Intent();
    		in.putExtra("label", locationEdit.getText().toString());
    		in.putExtra("latitude", Double.parseDouble(lat));
    		in.putExtra("longitude", Double.parseDouble(lon));
    		in.putExtra("altitude", Double.parseDouble(alt));
    		setResult(-1, in);
    		finish();
        }
    };
    
    /**
     * Select geolocation button listener
     */
    private OnClickListener btnSelectListener = new OnClickListener() {
        public void onClick(View v) {
        	// Start a new activity to send a geolocation
        	Intent geolocSelectIntent = new Intent(EditGeoloc.this, SelectGeoloc.class);        	
        	startActivityForResult(geolocSelectIntent, SELECT_GEOLOCATION);    		
        }
    };
    
    /**
     * On activity result
     * 
     * @param requestCode Request code
     * @param resultCode Result code
     * @param data Data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode != RESULT_OK) {
    		return;
    	}

    	switch(requestCode) {
	    	case SELECT_GEOLOCATION: {
	    		
	    		double latitude = data.getDoubleExtra("latitude", 0.0);
	    		double longitude = data.getDoubleExtra("longitude", 0.0);
	    		double altitude = data.getDoubleExtra("altitude", 0.0);

	    		latitudeEdit.setText(String.valueOf(latitude));
	    		longitudeEdit.setText(String.valueOf(longitude));
	    		altitudeEdit.setText(String.valueOf(altitude));
	    	}
	    	
	    	break;
    	}
    }
}
