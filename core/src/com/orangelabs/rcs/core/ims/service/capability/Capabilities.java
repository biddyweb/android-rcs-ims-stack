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
package com.orangelabs.rcs.core.ims.service.capability;

import java.util.Vector;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Supported capabilities
 * 
 * @author jexa7410
 */
public class Capabilities implements Parcelable {
	/**
	 * Video sharing capability
	 */
	public final static String VIDEO_SHARING_CAPABILITY = "org.gsma.videoshare";
	
	/**
	 * Image sharing capability
	 */
	public final static String IMAGE_SHARING_CAPABILITY = "org.gsma.imageshare";

	/**
	 * File sharing capability
	 */
	public final static String FILE_SHARING_CAPABILITY = "org.openmobilealliance:File-Transfer";

	/**
	 * IM session capability
	 */
	public final static String IM_SESSION_CAPABILITY = "org.openmobilealliance:IM-session";
	
	/**
	 * CS video telephony capability
	 */
	public final static String CS_VIDEO_CAPABILITY = "org.3gpp.cs-videotelephony";

	/**
	 * Image sharing
	 */
	private boolean imageSharing = false;
	
	/**
	 * Video sharing
	 */
	private boolean videoSharing = false;
	
	/**
	 * IM session
	 */
	private boolean imSession = false;

	/**
	 * File transfer
	 */
	private boolean fileTransfer = false;
	
	/**
	 * CS video
	 */
	private boolean csVideo = false;

	/**
	 * Timestamp of the last anonymous fetch procedure
	 */
	private long timestamp = System.currentTimeMillis();
	
	/**
	 * Constructor
	 */
	public Capabilities() {
	}

	/**
	 * Constructor
	 * 
	 * @param source Parcelable source
	 */
	public Capabilities(Parcel source) {
		this.imageSharing = source.readInt() != 0;
		this.videoSharing = source.readInt() != 0;
		this.imSession = source.readInt() != 0;
		this.fileTransfer = source.readInt() != 0;
		this.csVideo = source.readInt() != 0;
		this.timestamp = source.readLong();
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
    	dest.writeInt(imageSharing ? 1 : 0);
    	dest.writeInt(videoSharing ? 1 : 0);
    	dest.writeInt(imSession ? 1 : 0);
    	dest.writeInt(fileTransfer ? 1 : 0);
    	dest.writeInt(csVideo ? 1 : 0);
    	dest.writeLong(timestamp);
    }

    /**
     * Parcelable creator
     */
    public static final Parcelable.Creator<Capabilities> CREATOR
            = new Parcelable.Creator<Capabilities>() {
        public Capabilities createFromParcel(Parcel source) {
            return new Capabilities(source);
        }

        public Capabilities[] newArray(int size) {
            return new Capabilities[size];
        }
    };	

    /**
	 * Is image sharing supported
	 * 
	 * @return Boolean
	 */
	public boolean isImageSharingSupported() {
		return imageSharing;
	}

	/**
	 * Set the image sharing support
	 * 
	 * @param supported Supported 
	 */
	public void setImageSharingSupport(boolean supported) {
		this.imageSharing = supported;
	}

	/**
	 * Is video sharing supported
	 * 
	 * @return Boolean
	 */
	public boolean isVideoSharingSupported() {
		return videoSharing;
	}

	/**
	 * Set the video sharing support
	 * 
	 * @param supported Supported 
	 */
	public void setVideoSharingSupport(boolean supported) {
		this.videoSharing = supported;
	}

	/**
	 * Is IM session supported
	 * 
	 * @return Boolean
	 */
	public boolean isImSessionSupported() {
		return imSession;
	}

	/**
	 * Set the IM session support
	 * 
	 * @param supported Supported 
	 */
	public void setImSessionSupport(boolean supported) {
		this.imSession = supported;
	}

	/**
	 * Is file transfer supported
	 * 
	 * @return Boolean
	 */
	public boolean isFileTransferSupported() {
		return fileTransfer;
	}
	
	/**
	 * Set the file transfer support
	 * 
	 * @param supported Supported 
	 */
	public void setFileTransferSupport(boolean supported) {
		this.fileTransfer = supported;
	}
	
	/**
	 * Is CS video supported
	 * 
	 * @return Boolean
	 */
	public boolean isCsVideoSupported() {
		return csVideo;
	}

	/**
	 * Set the CS video support
	 * 
	 * @param supported Supported 
	 */
	public void setCsVideoSupport(boolean supported) {
		this.csVideo = supported;
	}

	/**
	 * Get the timestamp of the last anonymous fetch procedure (in milliseconds) 
	 * 
	 * @return Timestamp 
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Set timestamp of the last anonymous fetch procedure
	 * 
	 * @param Timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Reset timestamp of the last anonymous fetch procedure 
	 */
	public void resetTimestamp() {
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Returns the list of supported service ID
	 * 
	 * @return List of service-ID
	 */
	public Vector<String> getSupportedServiceIdList() {
		Vector<String> list = new Vector<String>();
		if (videoSharing) {
			list.addElement(VIDEO_SHARING_CAPABILITY);
		}
		if (imageSharing) {
			list.addElement(IMAGE_SHARING_CAPABILITY);
		}
		if (fileTransfer) {
			list.addElement(FILE_SHARING_CAPABILITY);
		}
		if (imSession) {
			list.addElement(IM_SESSION_CAPABILITY);
		}
		if (csVideo) {
			list.addElement(CS_VIDEO_CAPABILITY);
		}
		return list;
	}

	/**
	 * Returns a string representation of the object
	 * 
	 * @return String
	 */
	public String toString() {
		return "image share=" + imageSharing + ", video share=" + videoSharing +
			", file transfer=" + fileTransfer +
			", IM session=" + imSession +
			", CS video session=" + csVideo +
			", timestamp=" + timestamp;
	}
}
