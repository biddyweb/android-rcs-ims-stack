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

import java.util.Date;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Instant message
 * 
 * @author jexa7410
 * @author yplo6403
 *
 */
public class InstantMessage implements Parcelable {
	/**
	 * MIME type
	 */
	public static final String MIME_TYPE = "text/plain";
	
	/**
	 * Remote user
	 */
	private String remote;
	
	/**
	 * Remote user display name
	 */
	private String displayName;
	
	/**
	 * Text message
	 */
	private String message;
	
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
	 * @param messageId
	 *            Message Id
	 * @param remote
	 *            Remote user
	 * @param message
	 *            Text message
	 * @param imdnDisplayedRequested
	 *            Flag indicating that an IMDN "displayed" is requested
	 * @param displayName
	 *            the name to display or null if unknown
	 */
	public InstantMessage(String messageId, String remote, String message, boolean imdnDisplayedRequested, String displayName) {
		this.msgId = messageId;
		this.remote = remote;
		this.message = message;
		this.imdnDisplayedRequested = imdnDisplayedRequested;
		Date date = new Date();
		this.receiptAt = date;
		this.serverReceiptAt = date;	
		this.displayName = displayName;
	}
	
	/**
	 * Constructor for incoming message
	 * 
	 * @param messageId
	 *            Message Id
	 * @param remote
	 *            Remote user
	 * @param message
	 *            Text message
	 * @param imdnDisplayedRequested
	 *            Flag indicating that an IMDN "displayed" is requested
	 * @param serverReceiptAt
	 *            Receipt date of the message on the server
	 * @param displayName
	 *            the name to display or null if unknown
	 */
	public InstantMessage(String messageId, String remote, String message, boolean imdnDisplayedRequested, Date serverReceiptAt, String displayName) {
		this.msgId = messageId;
		this.remote = remote;
		this.message = message;
		this.imdnDisplayedRequested = imdnDisplayedRequested;
		this.receiptAt = new Date();
		this.serverReceiptAt = serverReceiptAt;
		this.displayName = displayName;
	}

	/**
	 * Constructor
	 * 
	 * @param source Parcelable source
	 */
	public InstantMessage(Parcel source) {
		this.remote = source.readString();
		this.message = source.readString();
		this.msgId = source.readString();
		this.imdnDisplayedRequested = source.readInt() != 0;
		this.receiptAt = new Date(source.readLong());
		this.serverReceiptAt = new Date(source.readLong());
		this.displayName = source.readString();
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
    	dest.writeString(message);
    	dest.writeString(msgId);
    	dest.writeInt(imdnDisplayedRequested ? 1 : 0);
    	dest.writeLong(receiptAt.getTime());
    	dest.writeLong(serverReceiptAt.getTime());
    	dest.writeString(displayName);
    }

    /**
     * Parcelable creator
     */
    public static final Parcelable.Creator<InstantMessage> CREATOR
            = new Parcelable.Creator<InstantMessage>() {
        public InstantMessage createFromParcel(Parcel source) {
            return new InstantMessage(source);
        }

        public InstantMessage[] newArray(int size) {
            return new InstantMessage[size];
        }
    };	
	
	/**
	 * Returns the text message
	 * 
	 * @return String
	 */
	public String getTextMessage() {
		return message;
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
	 * Get the Remote display name
	 * @return the Display Name
	 */
	public String getDisplayName() {
		return displayName;
	}
}
