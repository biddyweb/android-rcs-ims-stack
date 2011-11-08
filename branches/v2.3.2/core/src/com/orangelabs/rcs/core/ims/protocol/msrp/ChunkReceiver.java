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
import java.io.InputStream;
import java.util.Hashtable;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Chunks receiver
 * 
 * @author jexa7410
 */
public class ChunkReceiver extends Thread {
	/**
	 * MSRP connection
	 */
	private MsrpConnection connection;
	
	/**
	 * MSRP input stream
	 */
	private InputStream stream;
	
	/**
	 * Termination flag
	 */
	private boolean terminated = false;
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param connection MSRP connection
	 * @param stream TCP input stream
	 */
	public ChunkReceiver(MsrpConnection connection, InputStream stream) {
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
	 * Terminate the receiver
	 */
	public void terminate() {
		terminated = true;
		try {
			interrupt();
		} catch(Exception e) {}
		if (logger.isActivated()) {
			logger.debug("Receiver is terminated");
		}
	}
	
	/**
	 * Background processing
	 */
/*	public void run() {
		try {
			if (logger.isActivated()) {
				logger.debug("Receiver is started");
			}

			// Background processing
			while(!terminated) {
				// Start reading a new chunk
				StringBuffer firstLine = readLine();
				String[] firstLineTags = firstLine.toString().split(" ");
				if (logger.isActivated()) {
					logger.debug("Read a new chunk");
				}
				
				// Check the MSRP tag
				if ((firstLineTags.length < 3) || !firstLineTags[0].equals(MsrpConstants.MSRP_PROTOCOL)) {
					if (logger.isActivated()) {
						logger.debug("Not a MSRP message");
					}
					return;
				}

				// Get the transaction ID from the first line
				String txId = firstLineTags[1];

				// Get response code or method name from the first line
				int responseCode = -1;
				String method = null;
				try {
					responseCode = Integer.parseInt(firstLineTags[2]);
				} catch(NumberFormatException e) {
					method = firstLineTags[2];
				}

				// Data chunk
				byte[] data = null;

				// Read next lines
				Hashtable<String, String> headers = new Hashtable<String, String>();
				char continuationFlag = '\0';
				long totalSize = 0;
				while(true) {
					String line = readLine().toString();
					if (logger.isActivated()) {
						logger.debug("Read line: " + line);
					}						
					
					if (line.length() == 0) {
						// Empty line detected: read data
						String byteRange = headers.get(MsrpConstants.HEADER_BYTE_RANGE);
						if (byteRange != null) { 
							int chunkSize = getChunkSize(byteRange);
							totalSize = getTotalSize(byteRange);
							if (chunkSize == -1) {
								chunkSize = (int)totalSize;
							}
							data = readChunkedData(chunkSize);
						} else {
							data = readData();
							totalSize = data.length;
						}
						if (logger.isActivated()) {
							logger.debug("Read data: " + data.length);
						}						
					} else
					if (line.startsWith(MsrpConstants.END_MSRP_MSG)) {
						// End of message
						continuationFlag = line.charAt(line.length()-1);
						if (logger.isActivated()) {
							logger.debug("Continuous flag: " + continuationFlag);
						}						
						break;
					} else {
						// It's an header
						String[] header = line.split(":");
						
						// Add the header in the list
						headers.put(header[0].trim(), header[1].trim());
						if (logger.isActivated()) {
							logger.debug("Header: " + header[0]);
						}						
					}
				}
				
				// Process the received MSRP message
				if (responseCode != -1) {
					// Process MSRP response
					connection.getSession().receiveMsrpResponse(responseCode, txId.toString(), headers);
				} else {
					// Process MSRP request
					if (method.toString().equals(MsrpConstants.METHOD_SEND)) {
						// Process a SEND request
						connection.getSession().receiveMsrpSend(txId.toString(), headers, continuationFlag, data, totalSize);
					} else 
					if (method.toString().equals(MsrpConstants.METHOD_REPORT)) {
						// Process a REPORT request
						connection.getSession().receiveMsrpReport(txId.toString(), headers);					
					} else {
						// Unknown request
						if (logger.isActivated()) {
							logger.debug("Unknown request received: " + method.toString());
						}
					}
				}
			}
		} catch(Exception e) {
			if (terminated) { 
				if (logger.isActivated()) {
					logger.debug("Chunk receiver thread terminated");
				}
			} else {
				if (logger.isActivated()) {
					logger.error("Chunk receiver has failed", e);
				}
				
				// Notify the session listener that an error has occured
				connection.getSession().getMsrpEventListener().msrpTransferError(e.getMessage());
			}
			terminated = true;
		}
	}*/
	public void run() {
		try {
			if (logger.isActivated()) {
				logger.debug("Receiver is started");
			}

			// Background processing
			while(!terminated) {
				// Read a chunk (blocking method)
				int i = stream.read();

				if (logger.isActivated()) {
					logger.debug("Read a new chunk");
				}
				StringBuffer chunk = new StringBuffer();

				// Read MSRP tag
				for (; (i != MsrpConstants.CHAR_SP) && (i != -1); i = stream.read()) {
					chunk.append((char)i);
				}
				chunk.append((char)i);

				if (i == -1) {
					// End of stream
					return;
				}
				
				// Read the transaction ID
				StringBuffer txId = new StringBuffer();
				do {
					i = stream.read();
					chunk.append((char)i);
					if (i != MsrpConstants.CHAR_SP) {
						txId.append((char)i);
					}
				} while((i != MsrpConstants.CHAR_SP) && (i != -1));

				if (i == -1) {
					// End of stream
					return;
				}

				// Read response code or method name
				int responseCode = -1;
				StringBuffer method = new StringBuffer();
				for (i = stream.read(); (i != MsrpConstants.CHAR_LF) && (i != -1); i = stream.read()) {
					chunk.append((char)i);

					if (i == MsrpConstants.CHAR_SP) {
						// There is a space -> it's a response
						responseCode = Integer.parseInt(method.toString());
					}
					method.append((char)i);
				}
				chunk.append(MsrpConstants.NEW_LINE);
				stream.read();

				if (i == -1) {
					// End of stream
					return;
				}

				// Read MSRP headers
				Hashtable<String, String> headers = new Hashtable<String, String>();
				for (i = stream.read(); (i != MsrpConstants.CHAR_LF) && (i != -1);) {
					StringBuffer headerName = new StringBuffer();
					StringBuffer headerValue = new StringBuffer();

					for (; (i != MsrpConstants.CHAR_DOUBLE_POINT) && (i != -1); i = stream.read()) {
						headerName.append((char) i);
						chunk.append((char) i);
					}
					chunk.append((char)i);

					for (i = stream.read(); (i != MsrpConstants.CHAR_LF) && (i != -1); i = stream.read()) {
						chunk.append((char) i);
						headerValue.append((char) i);
					}
					chunk.append(MsrpConstants.NEW_LINE);
					stream.read();

					headers.put(headerName.toString().trim(), headerValue.toString().trim());
					
					// It's the end of the header part
					i = stream.read();
					if (i == MsrpConstants.CHAR_MIN) {
						// For response
						for (; (i != MsrpConstants.CHAR_LF) && (i != -1); i = stream.read()) {
							chunk.append((char) i);
						}
						chunk.append(MsrpConstants.NEW_LINE);
						break;
					}					
				}
				stream.read();

				if (i == -1) {
					// End of stream
					return;
				}

				// Process the received MSRP message
				if (responseCode != -1) {
					// Process MSRP response
					if (MsrpConnection.MSRP_TRACE_ENABLED) {
						System.out.println("<<< Receive MSRP response:\n" + chunk.toString());
					}
					connection.getSession().receiveMsrpResponse(responseCode, txId.toString(), headers);
				} else {
					// Process MSRP request
					if (method.toString().equals(MsrpConstants.METHOD_SEND)) {
						// Process a SEND request
	
						// Extract the byte range header
						String byteRangeHeader = headers.get(MsrpConstants.HEADER_BYTE_RANGE);
						
						byte data[] = null;
						long totalSize = 0;
						// The byte range header may be not present (empty packet for example)
						if (byteRangeHeader!=null){
							int chunkSize = getChunkSize(byteRangeHeader);
							totalSize = getTotalSize(byteRangeHeader);
							if (chunkSize == -1) {
								chunkSize = (int)totalSize;
							}

							if (logger.isActivated()) {
								logger.debug("Prepare a data array of size " + chunkSize);
							}

							// Read the data
							data = new byte[chunkSize];
							int nbRead = 0;
							int nbData = -1;
							while((nbRead < chunkSize) && ((nbData = stream.read(data, nbRead, chunkSize-nbRead)) != -1)) {
								nbRead += nbData;
								if (logger.isActivated()) {
									logger.debug("Data chunk read: chunk size=" + nbData + ", total=" + nbRead);
								}
							}
						}

						int flag;
						if (data != null) {
							// Read until the end line
							int length = 9 + txId.length();
							byte endline[] = new byte[256];			
							stream.read(endline, 0, length);
							if (logger.isActivated()) {
								logger.debug("End of line read");
							}

							// Read continuation flag
							flag = stream.read();
							stream.read();
							stream.read();
						} else {
							// In case of no data, continuation flag and newline have already been read, it is at the end of the chunk. 
							flag = chunk.charAt(chunk.length()-3);
							if (logger.isActivated()) {
								logger.debug("SEND request received (no data)");
							}
						}
						if (logger.isActivated()) {
							logger.debug("Continuous flag read: " + (char)flag);
						}
						
						if (MsrpConnection.MSRP_TRACE_ENABLED) {
							System.out.println("<<< Receive MSRP SEND request:\n" + chunk.toString() + new String(data));
						}
						connection.getSession().receiveMsrpSend(txId.toString(), headers, flag, data, totalSize);
					} else 
					if (method.toString().equals(MsrpConstants.METHOD_REPORT)) {
						// Process a REPORT request
						if (MsrpConnection.MSRP_TRACE_ENABLED) {
							System.out.println("<<< Receive MSRP REPORT request:\n" + chunk.toString());
						}
						connection.getSession().receiveMsrpReport(txId.toString(), headers);					
					} else {
						// Unknown request
						if (logger.isActivated()) {
							logger.debug("Unknown request received: " + method.toString());
						}
					}
				}
			}
		} catch(Exception e) {
			if (terminated) { 
				if (logger.isActivated()) {
					logger.debug("Chunk receiver thread terminated");
				}
			} else {
				if (logger.isActivated()) {
					logger.error("Chunk receiver has failed", e);
				}
				
				// Notify the msrp session listener that an error has occured
				connection.getSession().getMsrpEventListener().msrpTransferError("Chunk receiver has failed : " +e);
			}
			terminated = true;
		}
	}

	/**
	 * Read a line
	 * 
	 * @return Line
	 * @throws IOException
	 */
	private StringBuffer readLine() throws IOException {
		StringBuffer line = new StringBuffer();
		for (int i = stream.read(); (i != MsrpConstants.CHAR_LF) && (i != -1); i = stream.read()) {
			line.append((char)i);
		}
		stream.read(); // Read CR
		return line;
	}

	/**
	 * Read chunked data
	 *
	 * @param chunkSize Chunk size
	 * @return Data
	 * @throws IOException
	 */
	private byte[] readChunkedData(int chunkSize) throws IOException {
		// Read data until chunk size is reached
		byte[] result = null;
		result = new byte[chunkSize];
		int nbRead = 0;
		int nbData = -1;
		while((nbRead < chunkSize) && ((nbData = stream.read(result, nbRead, chunkSize-nbRead)) != -1)) {
			nbRead += nbData;
		}		
		stream.read(); // Read LF
		stream.read(); // Read CR
		return result;
	}

	/**
	 * Read data
	 *
	 * @return Data
	 * @throws IOException
	 */
	private byte[] readData() throws IOException {
		// Read data
		byte[] data = new byte[2048];
		int size = 0;
		for (int i = stream.read(); (i != MsrpConstants.CHAR_LF) && (i != -1); i = stream.read()) {
			data[size] = (byte)i;
		}
		stream.read(); // Read CR
		
		byte[] result = new byte[size];
		System.arraycopy(data, 0, result, 0, size);		
		return result;
	}

	/**
	 * Get the chunk size
	 * 
	 * @param header MSRP header
	 * @return Size in bytes
	 */
	private int getChunkSize(String header) {
		if (header == null) {
			return -1;
		}
		int index1 = header.indexOf("-");
		int index2 = header.indexOf("/");
		if ((index1 != -1) && (index2 != -1)) {
			try {
				int lowByte = Integer.parseInt(header.substring(0, index1));
				int highByte = Integer.parseInt(header.substring(index1+1, index2));
				return (highByte - lowByte) + 1;
			} catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Get the total size
	 * 
	 * @param header MSRP header
	 * @return Size in bytes
	 */
	private long getTotalSize(String header) {
		if (header == null) {
			return -1;
		}
		int index = header.indexOf("/");
		if (index != -1) {
			try {
				return Long.parseLong(header.substring(index+1));
			} catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}
}
