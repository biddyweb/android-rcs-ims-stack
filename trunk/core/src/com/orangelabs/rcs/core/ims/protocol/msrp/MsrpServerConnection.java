package com.orangelabs.rcs.core.ims.protocol.msrp;

import java.io.IOException;

import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.platform.network.SocketConnection;
import com.orangelabs.rcs.platform.network.SocketServerConnection;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * MSRP server connection
 * 
 * @author jexa7410
 */
public class MsrpServerConnection extends MsrpConnection {
	/**
	 * Local TCP port number
	 */
	private int localPort; 

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param session MSRP session
	 * @param localPort Local port number
	 */
	public MsrpServerConnection(MsrpSession session, int localPort) {
		super(session);
		
		this.localPort = localPort;
	}

	/**
	 * Returns the socket connection
	 * 
	 * @return Socket
	 * @throws IOException
	 */
	public SocketConnection getSocketConnection() throws IOException {
		if (logger.isActivated()) {
			logger.debug("Open server socket at " + localPort);
		}
		SocketServerConnection socketServer = NetworkFactory.getFactory().createSocketServerConnection();
		socketServer.open(localPort);
		SocketConnection socket = socketServer.acceptConnection();
		if (logger.isActivated()) {
			logger.debug("Wait client connection");
		}
		if (logger.isActivated()) {
			logger.debug("Socket connected to " + socket.getRemoteAddress() + ":" + socket.getRemotePort());
		}
		return socket;
	}
}
