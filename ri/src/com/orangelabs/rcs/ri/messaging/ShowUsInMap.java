package com.orangelabs.rcs.ri.messaging;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.service.api.client.eventslog.EventsLogApi;
import com.orangelabs.rcs.service.api.client.messaging.GeolocPush;

/**
 * Show us in a map
 * 
 * @author jexa7410
 */
public class ShowUsInMap extends MapActivity {

	/**
	 * Map view
	 */
	private MapView mapView;

	/**
	 * Map overlay
	 */
	private List<Overlay> mapOverlays;

	/**
	 *  Event log API
	 */
	private EventsLogApi eventLog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set title
		setTitle(R.string.menu_showus_map);

		// Set layout
		setContentView(R.layout.messaging_display_geoloc);

		// Set map
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapOverlays = mapView.getOverlays();	
		mapView.getController().setZoom(4);
		
		// Instanciate event log API
		eventLog = new EventsLogApi(this);
		
		// Get list of contact geoloc to display
		Drawable drawable = getResources().getDrawable(R.drawable.ri_map_icon);
		ArrayList<String> contacts = getIntent().getStringArrayListExtra("contacts");
		for (int i=0; i < contacts.size(); i++) {
			// Get geoloc of a contact
			String contact = contacts.get(i);
			GeolocPush geoloc = eventLog.getLastGeoloc(contact);
			if (geoloc != null) {
				GeoPoint geoPoint = new GeoPoint((int)(geoloc.getLatitude() * 1E6),	(int)(geoloc.getLongitude() * 1E6));

				// Create an overlay item
				MyItemizedOverlay itemizedoverlay = new MyItemizedOverlay(drawable,this);
				OverlayItem overlayitem = new OverlayItem(geoPoint,contact, geoloc.getLabel());
				itemizedoverlay.addOverlay(overlayitem);
				
				// Add the overlay to the map
				mapOverlays.add(itemizedoverlay);
			}
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	/**
	 * Overlay item
	 */
	private class MyItemizedOverlay extends ItemizedOverlay<OverlayItem>{
		private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();

		private Context context;

		public MyItemizedOverlay(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
			
			this.context = context;
		}

		@Override
		protected OverlayItem createItem(int i) {
			return overlays.get(i);
		}

		@Override
		public int size() {
			return overlays.size();
		}

		public void addOverlay(OverlayItem overlay) {
			overlays.add(overlay);
			populate();
		}
		
		@Override
		protected boolean onTap(int index) {
		  OverlayItem item = overlays.get(index);
		  AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		  dialog.setTitle(item.getTitle());
		  dialog.setMessage(item.getSnippet());
		  dialog.show();
		  return true;
		}
	}
}
