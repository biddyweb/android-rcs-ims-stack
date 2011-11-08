/*******************************************************************************
 * Software Name : RCS IMS Stack
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

package com.orangelabs.rcs.core.ims.protocol.rtp.codec.text.t140;

/**
 * Contains information about one RTP text packet.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class RtpTextBuffer {

    private byte[] data;
    private int length;
    private int offset;
    private long timeStamp;
    private long seqNo;
    private long ssrc;
    private byte markerByte;
    
    public void setSsrc(long ssrc) {
	this.ssrc = ssrc;
    }

    public void setData(byte[] data) {
	this.data = data;
    }

    public void setLength(int length) {
	this.length = length;
    }

    public void setOffset(int offset) {
	this.offset = offset;
    }

    public void setTimeStamp(long timeStamp) {
	this.timeStamp = timeStamp;
    }

    public void setSequenceNumber(long seqNo) {
	this.seqNo = seqNo;
    }

    public void setMarker(boolean marker) {
	if(marker) {
	    markerByte=0x1;
	    return;
	}
	markerByte=0;
    }

    public long getSsrc() {
	return ssrc;
    }

    public byte[] getData() {
	return data;
    }

    public int getLength() {
	return length;
    }

    public int getOffset() {
	return offset;
    }

    public long getTimeStamp() {
	return timeStamp;
    }

    public long getSequenceNumber() {
	return seqNo;
    }

    public boolean getMarker() {
	if(markerByte==1) {
	    return true;
	}
	return false;
    }
}