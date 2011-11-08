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

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.Codec;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.text.T140Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.text.RedFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;

/**
 * Extracts data from incoming RTP-Text packets, also handles missing packets
 * 
 * @author Erik Zetterstrom, Omnitor AB
 * @author Andreas Piirimets, Omnitor AB
 */
public class JavaDecoder extends Codec {

	private RtpTextDePacketizer depacketizer;
	
	public JavaDecoder(boolean red){
		depacketizer = new RtpTextDePacketizer(T140Format.PAYLOAD,RedFormat.PAYLOAD,red);
	}
	
	/**
     * Copies information in buffers.
     *
     * @param src Source buffer
     * @param dst Destination buffer.
     */
    private void bufferCopy(Buffer src, RtpTextBuffer dst) {
		dst.setData((byte[])src.getData());
		dst.setLength(src.getLength());
		dst.setOffset(src.getOffset());
		dst.setTimeStamp(src.getTimeStamp());
		dst.setSequenceNumber(src.getSequenceNumber());
    }

    /**
     * Copies information in buffers.
     *
     * @param src Source buffer
     * @param dst Destination buffer.
     */
    private void bufferCopy(RtpTextBuffer src, Buffer dst) {
		dst.setData(src.getData());
		dst.setLength(src.getLength());
		dst.setOffset(src.getOffset());
		dst.setTimeStamp(src.getTimeStamp());
		dst.setSequenceNumber(src.getSequenceNumber());
    }

    /**
     * Extracts data from received packets. Handles missing packets.
     *
     * @param inputBuffer  The received packet
     * @param outputBuffer The extracted data
     *
     * @return 1 if success
     * @return 0 if parse failure
     * @return -1 packet received out of order.
     */
    public synchronized int process(Buffer inputBuffer, Buffer outputBuffer) {

		RtpTextBuffer tempInBuffer = new RtpTextBuffer();
		RtpTextBuffer tempOutBuffer = new RtpTextBuffer();
	
		bufferCopy(inputBuffer, tempInBuffer);
		bufferCopy(outputBuffer, tempOutBuffer);
	
		depacketizer.decode(tempInBuffer, tempOutBuffer);
	
		bufferCopy(tempInBuffer, inputBuffer);
		bufferCopy(tempOutBuffer, outputBuffer);

        return BUFFER_PROCESSED_OK;
    }


}
