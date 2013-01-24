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

package com.orangelabs.rcs.service.api.client.messaging;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Geolocation push info
 * 
 * @author Orange
 */
public class GeolocPush implements Parcelable {
    /**
     * Latitude
     */
    private double latitude;

    /**
     * Longitude
     */
    private double longitude;

    /**
     * Altitude
     */
    private double altitude;

    /**
     * Label associated to the location
     */
    private String label;

    /**
     * Constructor
     *
     * @param label Label
     * @param latitude Latitude
     * @param longitude Longitude
     * @param altitude Altitude
     */
    public GeolocPush(String label, double latitude, double longitude, double altitude) {
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /**
     * Constructor
     *
     * @param source Parcelable source
     */
    public GeolocPush(Parcel source) {
        this.label = source.readString();
        this.latitude = source.readDouble();
        this.longitude = source.readDouble();
        this.altitude = source.readDouble();
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
        dest.writeString(label);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(altitude);
    }

    /**
     * Parcelable creator
     */
    public static final Parcelable.Creator<GeolocPush> CREATOR = new Parcelable.Creator<GeolocPush>() {
        public GeolocPush createFromParcel(Parcel source) {
            return new GeolocPush(source);
        }

        public GeolocPush[] newArray(int size) {
            return new GeolocPush[size];
        }
    };

    /**
     * Returns the label
     *
     * @return Label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the label
     *
     * @param label Label
     */
    public void getLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the latitude
     *
     * @return Latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude
     *
     * @param latitude Latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns the longitude
     *
     * @return Longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude
     *
     * @param longitude Longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the altitude
     *
     * @return Altitude
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * Set the altitude
     *
     * @param altitude Altitude
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    /**
     * Returns a string representation of the object
     *
     * @return String
     */
    public String toString() {
        return "Label=" + label +
                ", Latitude=" + latitude +
                ", Longitude=" + longitude +
                ", Altitude=" + altitude;
    }
}
