package com.orangelabs.rcs.ri.messaging;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.orangelabs.rcs.ri.R;

/**
 * Select a geoloc from a Google map
 */
public class SelectGeoloc extends MapActivity implements OnTouchListener {

	/**
	 * MapView
	 */
	private MapView mapView;

	/**
	 * GestureDetector
	 */
	private GestureDetector gestureDectector;

	/**
	 * Map Overlay
	 */
	private List<Overlay> mapOverlays;

	/**
	 * GeoPoint
	 */
	private GeoPoint geoPoint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set title
		setTitle(R.string.label_select_geoloc);

		// Set layout
		setContentView(R.layout.messaging_select_geoloc);

		// Set map
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setOnTouchListener(this);
		gestureDectector = new GestureDetector(this, new LearnGestureListener());

		// Clear the list of overlay
		mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
		
		// Invalidate the map in order to show changes
		mapView.invalidate();

		// Set button callback
		Button selectBtn = (Button)findViewById(R.id.select_btn);
		selectBtn.setOnClickListener(btnSelectListener);	
	}

	/**
	 * Select button listener
	 */
	private OnClickListener btnSelectListener = new OnClickListener() {
		public void onClick(View v) {
			Intent in = new Intent();    		
			in.putExtra("latitude", (geoPoint.getLatitudeE6() / 1E6));
			in.putExtra("longitude", (geoPoint.getLongitudeE6() / 1E6));
			in.putExtra("altitude", 0.0);
			setResult(-1, in);
			finish();
		}
	};
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		gestureDectector.onTouchEvent(arg1);
		return false;
	}

	/**
	 * Intercepts some events
	 * @author vfml3370
	 *
	 */
	private class LearnGestureListener extends GestureDetector.SimpleOnGestureListener{ 

		@Override 
		public boolean onDoubleTap(MotionEvent ev) { 	     
			mapView.getController().zoomIn();
			return true; 
		} 

		@Override 
		public void onLongPress(MotionEvent e) {
			// Get the latitude and the longitude
			geoPoint = mapView.getProjection().fromPixels((int) e.getX(),(int) e.getY());
			
			// Remove all overlay from the list, only one marker on the map.
			mapOverlays.removeAll(mapOverlays);		
			
			// The Pin that will be displayed on the map
			Drawable drawable = getResources().getDrawable(R.drawable.ri_map_icon);
			
			// Create and add an OverlayItem to the MyItemizedOverlay
			OverlayItem overlayitem1 = new OverlayItem(geoPoint, "", "");
			MyItemizedOverlay itemizedoverlay = new MyItemizedOverlay(drawable);
			itemizedoverlay.addOverlay(overlayitem1);
			itemizedoverlay.setGestureDetector(gestureDectector);
			
			// Add the overlays to the map
			mapOverlays.add(itemizedoverlay);
		} 
	}

	/**
	 * Overlay item
	 */
	private class MyItemizedOverlay extends ItemizedOverlay<OverlayItem>{

		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		private GestureDetector gestureDetector;

		public MyItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}
		
		public void setGestureDetector(GestureDetector gestureDetector) {
			this.gestureDetector = gestureDetector;
		}
				
		public boolean onTouchEvent(MotionEvent e, MapView mapView) {
			if (gestureDetector.onTouchEvent(e)) {
				return true;
			}
			return false;
		}
	}
}
