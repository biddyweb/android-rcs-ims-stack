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

package com.orangelabs.rcs.core.ims.protocol.rtp.core;

import java.io.DataOutputStream;
import java.io.IOException;

import com.orangelabs.rcs.core.ims.protocol.rtp.util.Packet;

/**
 * Abstract RCTP packet
 * 
 * @author jexa7410
 */
public abstract class RtcpPacket extends Packet {
	public static final int SR = 200;
	public static final int RR = 201;
	public static final int SDES = 202;
	public static final int BYE = 203;
	public static final int APP = 204;
	public static final int COMPOUND = -1;

	public Packet base;
	
	public int type;

	public RtcpPacket() {
	}

	public RtcpPacket(RtcpPacket rtcppacket) {
		super((Packet)rtcppacket);
		
		base = rtcppacket.base;
	}

	public RtcpPacket(Packet packet) {
		super(packet);
		
		base = packet;
	}

	public abstract void assemble(DataOutputStream dataoutputstream) throws IOException;

	public abstract int calcLength();
}
