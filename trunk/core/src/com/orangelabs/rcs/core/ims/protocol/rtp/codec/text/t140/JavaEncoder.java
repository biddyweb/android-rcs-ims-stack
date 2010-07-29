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
package com.orangelabs.rcs.core.ims.protocol.rtp.codec.text.t140;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.Codec;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.text.T140Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.text.RedFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;

public class JavaEncoder extends Codec {

	private RtpTextPacketizer packetizer;
	
	public JavaEncoder(int redLevel) {
		packetizer = new RtpTextPacketizer(T140Format.PAYLOAD, RedFormat.PAYLOAD, redLevel);
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
		/* rglt1266 : also copy flags */
		if(src.getMarker()) dst.setFlags(Buffer.FLAG_RTP_MARKER);
    }
	
	public int process(Buffer input, Buffer output) {	
		RtpTextBuffer tmpInBuffer = new RtpTextBuffer();
		RtpTextBuffer tmpOutBuffer = new RtpTextBuffer();

		bufferCopy(input, tmpInBuffer);
		bufferCopy(output, tmpOutBuffer);

		packetizer.encode(tmpInBuffer, tmpOutBuffer);

		bufferCopy(tmpOutBuffer, output);
		// Add missing info
		output.setFormat(input.getFormat());

		return BUFFER_PROCESSED_OK;
	}

}
