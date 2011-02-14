/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright © 2010 France Telecom S.A.
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
package com.orangelabs.rcs.service.api.client;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Media sample
 * 
 * @author jexa7410
 */
public class MediaSample implements Parcelable {
	/**
	 * Data
	 */
	private byte[] data = null;
	
	/**
	 * Sample size
	 */
    private int dataSize = 0;
	
    /**
     * Timestamp of the sample
     */
	private long timestamp = 0L;
	
	/**
	 * Constructor 
	 * 
	 * @param data Data
	 * @param timestamp Timestamp
	 */
	public MediaSample(byte[] data, long timestamp) {
		this.timestamp = timestamp;
		this.dataSize = data.length;
		this.data = data;
	}

	/**
	 * Constructor
	 * 
	 * @param source Parcelable source
	 */
	public MediaSample(Parcel source) {
    	timestamp = source.readLong();
		dataSize = source.readInt();
		data = new byte[dataSize];
    	source.readByteArray(data);
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
    	dest.writeLong(timestamp);
    	dest.writeInt(data.length);
    	dest.writeByteArray(data);    	
    }

    /**
     * Parcelable creator
     */
    public static final Parcelable.Creator<MediaSample> CREATOR
            = new Parcelable.Creator<MediaSample>() {
        public MediaSample createFromParcel(Parcel source) {
            return new MediaSample(source);
        }

        public MediaSample[] newArray(int size) {
            return new MediaSample[size];
        }
    };	

    /**
	 * Set the timestamp
	 * 
	 * @param timestamp Timestamp
	 */
    public byte[] getData() {
		return data;
	}

	/**
	 * Returns the timestamp
	 * 
	 * @return Timestamp
	 */
    public long getTimestamp() {
		return timestamp;
	}
}
