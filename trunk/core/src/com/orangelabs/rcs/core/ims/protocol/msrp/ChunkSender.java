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

package com.orangelabs.rcs.core.ims.protocol.msrp;

import java.io.IOException;
import java.io.OutputStream;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Chunks sender
 * 
 * @author jexa7410
 */
public class ChunkSender extends Thread {
	/**
	 * MSRP connection
	 */
	private MsrpConnection connection;

	/**
	 * MSRP output stream
	 */
	private OutputStream stream;
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param connection MSRP connection
	 * @param stream TCP output stream
	 */
	public ChunkSender(MsrpConnection connection, OutputStream stream) {
		this.connection = connection;
		this.stream = stream;
	}	
	
	/**
	 * Returns the MSRP connection
	 * 
	 * @return MSRP connection
	 */
	public MsrpConnection getConnection() {
		return connection;
	}
	
	/**
	 * Terminate the sender
	 */
	public void terminate() {
		if (logger.isActivated()) {
			logger.debug("Sender is terminated");
		}
	}
	
	/**
	 * Send a chunk
	 * 
	 * @param chunk New chunk
	 * @throws IOException
	 */
	public void sendChunk(byte chunk[]) throws IOException {
		if (MsrpConnection.MSRP_TRACE_ENABLED) {
			System.out.println(">>> Send MSRP message:\n" + new String(chunk));
		}
		stream.write(chunk);
		stream.flush();
	}	
}
