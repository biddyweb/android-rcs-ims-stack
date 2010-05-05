package com.orangelabs.rcs.core.ims.service.presence;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Favorite link
 * 
 * @author jexa7410
 */
public class FavoriteLink implements Parcelable {	
	/**
	 * Link
	 */
	private String link = null;
	
	/**
	 * Name
	 */
	private String name = null;

	/**
	 * Constructor
	 * 
	 * @param link Web link
	 */
	public FavoriteLink(String link) {
		this.link = link;
	}

	/**
	 * Constructor
	 * 
	 * @param name Name associated to the link
	 * @param link Web link
	 */
	public FavoriteLink(String name, String link) {
		this.name = name;
		this.link = link;
	}
	
	/**
	 * Constructor
	 * 
	 * @param source Parcelable source
	 */
	public FavoriteLink(Parcel source) {
		this.name = source.readString();
    	this.link = source.readString();
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
    	dest.writeString(name);
    	dest.writeString(link);    	
    }

    /**
     * Parcelable creator
     */
    public static final Parcelable.Creator<FavoriteLink> CREATOR
            = new Parcelable.Creator<FavoriteLink>() {
        public FavoriteLink createFromParcel(Parcel source) {
            return new FavoriteLink(source);
        }

        public FavoriteLink[] newArray(int size) {
            return new FavoriteLink[size];
        }
    };	
	
	/**
	 * Returns the name associated to the link
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name associated to the link
	 * 
	 * @param name Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the web link
	 * 
	 * @return
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Set the web link
	 * 
	 * @param link Web link
	 */	
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * Returns a string representation of the object
	 * 
	 * @return String
	 */
	public String toString() {
		return "link=" + link + ", name=" + name;
	}
}
