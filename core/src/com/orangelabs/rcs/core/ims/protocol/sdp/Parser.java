package com.orangelabs.rcs.core.ims.protocol.sdp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import com.orangelabs.rcs.utils.logger.Logger;

public class Parser {
	private static Vector<Integer> buffer;

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void init() {
		buffer = new Vector<Integer>();
	}

	public void ungetToken(String tokenStr) {
		byte token[] = tokenStr.getBytes();

		for (int i = 0; i < token.length; i++) {
			buffer.insertElementAt(new Integer(token[token.length - i - 1]), 0);
		}
	}

	public boolean getToken(ByteArrayInputStream bin, String tokenString) {
		boolean found = false;

		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		skipWhitespace(bin);

		if (bin.available() > 0) {
			int ch = readChar(bin);

			while (ch != '=' && ch != '\n' && ch != '\r' && ch != -1) {
				bout.write(ch);

				ch = readChar(bin);
			}

			bout.write(ch);
		}

		String token = new String(bout.toByteArray());

		if (tokenString.equals(token)) {
			found = true;
		} else {
			ungetToken(token);
		}

		return found;
	}

	public boolean getToken(ByteArrayInputStream bin, String tokenString,
			boolean mandatory) {
		boolean found = getToken(bin, tokenString);

		if (!found) {
			if (mandatory) {
				if (logger.isActivated()) {
					logger.warn("Mandatory token missing: " + tokenString);
				}
			}
		}

		return found;
	}

	public String getLine(ByteArrayInputStream bin) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		if (bin.available() > 0) {
			int ch = readChar(bin);

			while (ch != '\n' && ch != '\r' && ch != -1) {
				bout.write(ch);

				ch = readChar(bin);
			}
		}

		String line = new String(bout.toByteArray());

		return line;
	}

	private void skipWhitespace(ByteArrayInputStream bin) {
		int ch = readChar(bin);

		while (ch == ' ' || ch == '\n' || ch == '\r') {
			ch = readChar(bin);
		}

		buffer.insertElementAt(new Integer(ch), 0);
	}

	public int readChar(ByteArrayInputStream bin) {
		int ch;

		if (buffer.size() > 0) {
			ch = ((Integer) buffer.elementAt(0)).intValue();

			buffer.removeElementAt(0);
		} else {
			ch = bin.read();
		}

		return ch;
	}
}
