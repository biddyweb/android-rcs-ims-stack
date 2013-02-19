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

/**
 * Display a geoloc on a Google map
 */
public class DisplayGeoloc extends MapActivity {

	/**
	 * Map view
	 */
	private MapView mapView;

	/**
	 * Map overlay
	 */
	private List<Overlay> mapOverlays;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set title
		setTitle(R.string.title_display_geoloc);

		// Set layout
		setContentView(R.layout.messaging_display_geoloc);

		// Set map
		mapView = (MapView)findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapOverlays = mapView.getOverlays();	
		mapView.getController().setZoom(4);
		
		// Get geoloc value
		String label = getIntent().getStringExtra("label");
		double latitude = getIntent().getDoubleExtra("latitude", 0.0);
		double longitude = getIntent().getDoubleExtra("longitude", 0.0);
		GeoPoint geoPoint = new GeoPoint((int)(latitude * 1E6),	(int)(longitude * 1E6));
		
		// Create an overlay item
		Drawable drawable = getResources().getDrawable(R.drawable.ri_map_icon);
		MyItemizedOverlay itemizedoverlay = new MyItemizedOverlay(drawable, this);
		OverlayItem overlayitem = new OverlayItem(geoPoint, getString(R.string.label_marker), label);
		itemizedoverlay.addOverlay(overlayitem);

		// Add the overlays to the map
		mapOverlays.add(itemizedoverlay);
		mapView.getController().setCenter(geoPoint);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * Overlay
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
