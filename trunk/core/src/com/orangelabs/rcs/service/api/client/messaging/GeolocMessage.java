package com.orangelabs.rcs.service.api.client.messaging;

import java.util.Date;
import java.util.StringTokenizer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Geoloc message
 * 
 * @author jexa7410
 */
public class GeolocMessage implements Parcelable {
	/**
	 * MIME type
	 */
	public static final String MIME_TYPE = "text/geoloc";

	/**
	 * Remote user
	 */
	private String remote;
	
	/**
	 * Geoloc info
	 */
	private GeolocPush geoloc = null;
	
	/**
	 * Receipt date of the message
	 */
	private Date receiptAt;
	
	/**
	 * Receipt date of the message on the server (i.e. CPIM date)
	 */
	private Date serverReceiptAt;

	/**
	 * Message Id
	 */
	private String msgId;
	
	/**
	 * Flag indicating that an IMDN "displayed" is requested for this message
	 */
	private boolean imdnDisplayedRequested = false;
		
    /**
     * Constructor for outgoing message
     * 
     * @param messageId Message Id
     * @param remote Remote user
     * @param geoloc Geoloc info
     * @param imdnDisplayedRequested Flag indicating that an IMDN "displayed" is requested
	 */
	public GeolocMessage(String messageId, String remote, GeolocPush geoloc, boolean imdnDisplayedRequested) {
		this.msgId = messageId;
		this.remote = remote;
		this.geoloc = geoloc;
		this.imdnDisplayedRequested = imdnDisplayedRequested;
		Date date = new Date();
		this.receiptAt = date;
		this.serverReceiptAt = date;		
	}
	
	/**
     * Constructor for incoming message
     * 
     * @param messageId Message Id
     * @param remote Remote user
     * @param geoloc Geoloc info
     * @param imdnDisplayedRequested Flag indicating that an IMDN "displayed" is requested
	 * @param serverReceiptAt Receipt date of the message on the server
	 */
	public GeolocMessage(String messageId, String remote, GeolocPush geoloc, boolean imdnDisplayedRequested, Date serverReceiptAt) {
		this.msgId = messageId;
		this.remote = remote;
		this.geoloc = geoloc;
		this.imdnDisplayedRequested = imdnDisplayedRequested;
		this.receiptAt = new Date();
		this.serverReceiptAt = serverReceiptAt;
	}
	
	/**
	 * Constructor
	 * 
	 * @param source Parcelable source
	 */
	public GeolocMessage(Parcel source) {
		this.remote = source.readString();
		this.geoloc = new GeolocPush(source);
		this.msgId = source.readString();
		this.imdnDisplayedRequested = source.readInt() != 0;
		this.receiptAt = new Date(source.readLong());
		this.serverReceiptAt = new Date(source.readLong());
    }
	
	/**
	 * Describe the kinds of special objects contained in this Parcelable's
	 * marshalled representation
	 * 
	 * @return Integer
	 */
	public int describeContents() {
        return 0;
    }

	/**
	 * Write parcelable object
	 * 
	 * @param dest The Parcel in which the object should be written
	 * @param flags Additional flags about how the object should be written
	 */
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeString(remote);
    	geoloc.writeToParcel(dest, flags);
    	dest.writeString(msgId);
    	dest.writeInt(imdnDisplayedRequested ? 1 : 0);
    	dest.writeLong(receiptAt.getTime());
    	dest.writeLong(serverReceiptAt.getTime());
    }

    /**
     * Parcelable creator
     */
    public static final Parcelable.Creator<GeolocMessage> CREATOR
            = new Parcelable.Creator<GeolocMessage>() {
        public GeolocMessage createFromParcel(Parcel source) {
            return new GeolocMessage(source);
        }

        public GeolocMessage[] newArray(int size) {
            return new GeolocMessage[size];
        }
    };	

    /**
	 * Get geoloc info
	 * 
	 * @return Geoloc info
	 */
	public GeolocPush getGeoloc() {
		return geoloc;
	}
	
	/**
	 * Returns the message Id
	 * 
	 * @return message Id
	 */
    public String getMessageId(){
    	return msgId;
    }
	
	/**
	 * Returns the remote user
	 * 
	 * @return Remote user
	 */
	public String getRemote() {
		return remote;
	}
	
	/**
	 * Returns true if the IMDN "displayed" has been requested 
	 * 
	 * @return Boolean
	 */
	public boolean isImdnDisplayedRequested() {
		return imdnDisplayedRequested;
	}
	
	/**
	 * Returns the receipt date of the message
	 * 
	 * @return Date
	 */
	public Date getDate() {
		return receiptAt;
	}

	/**
	 * Returns the receipt date of the message on the server
	 * 
	 * @return Date
	 */
	public Date getServerDate() {
		return serverReceiptAt;
	}

	/** 
     * Format geoloc object to string
     * 
     * @param geoloc Geoloc object
     * @return String
     */
    public static String formatGeolocToStr(GeolocPush geoloc) {
    	String result = geoloc.getLatitude() + "," +
    		   geoloc.getLongitude() + "," +
    		   geoloc.getAltitude() + "," +
    		   geoloc.getExpiration();
    	String label = geoloc.getLabel();
    	if ((label != null) && (label.length() > 0)) {
    		result = label + "," + result;
    	}
    	return result;
    	
    }

    /** 
     * Format string to geoloc object
     * 
     * @param str String
     * @return Geoloc object
     */
    public static GeolocPush formatStrToGeoloc(String str) {
    	StringTokenizer items = new StringTokenizer(str, ",");
    	String label = "";
    	if (items.countTokens() > 4) {
    		label = items.nextToken();
    	}
		double latitude = Double.valueOf(items.nextToken());					
		double longitude = Double.valueOf(items.nextToken());
		double altitude = Double.valueOf(items.nextToken());
		long expiration = Long.valueOf(items.nextToken());
    	return new GeolocPush(label, latitude, longitude, altitude, expiration);
    }	
}
