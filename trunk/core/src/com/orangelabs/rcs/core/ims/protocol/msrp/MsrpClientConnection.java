package com.orangelabs.rcs.core.ims.protocol.msrp;

import java.io.IOException;

import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.platform.network.SocketConnection;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * MSRP client connection
 * 
 * @author jexa7410
 */
public class MsrpClientConnection extends MsrpConnection {
	/**
	 * Remote IP address 
	 */
	private String remoteAddress;
	
	/**
	 * Remote TCP port number
	 */
	private int remotePort; 
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param session MSRP session
	 * @param remoteAddress Remote IP address
	 * @param remotePort Remote port number
	 */
	public MsrpClientConnection(MsrpSession session, String remoteAddress, int remotePort) {
		super(session);
		
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
	}

	/**
	 * Returns the socket connection
	 * 
	 * @return Socket
	 * @throws IOException
	 */
	public SocketConnection getSocketConnection() throws IOException {
		if (logger.isActivated()) {
			logger.debug("Open client socket to " + remoteAddress + ":" + remotePort);
		}
		SocketConnection socket = NetworkFactory.getFactory().createSocketClientConnection();
		socket.open(remoteAddress, remotePort);		
		if (logger.isActivated()) {
			logger.debug("Socket connected to " + socket.getRemoteAddress() + ":" + socket.getRemotePort());
		}
		return socket;
	}
}
