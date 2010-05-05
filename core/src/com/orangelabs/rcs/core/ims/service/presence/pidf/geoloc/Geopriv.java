package com.orangelabs.rcs.core.ims.service.presence.pidf.geoloc;

public class Geopriv {
	private String method = null;
	private double latitude = 0;
	private double longitude = 0;
	private double altitude = 0;
	
	public Geopriv() {
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String met) {
		this.method = met;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double l) {
		latitude = l;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double l) {
		longitude = l;
	}

	public double getAltitude() {
		return altitude;
	}	

	public void setAltitude(double a) {
		altitude = a;
	}
}
