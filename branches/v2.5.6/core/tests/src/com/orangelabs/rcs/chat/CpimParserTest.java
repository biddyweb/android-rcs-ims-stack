package com.orangelabs.rcs.chat;

import android.test.AndroidTestCase;

import com.orangelabs.rcs.core.ims.service.im.chat.cpim.CpimMessage;
import com.orangelabs.rcs.core.ims.service.im.chat.cpim.CpimParser;

public class CpimParserTest extends AndroidTestCase {
	/**
	 * CRLF constant
	 */
	private static final String CRLF = "\r\n";

	/**
	 * Double CRLF constant
	 */
	private static final String DOUBLE_CRLF = CRLF + CRLF;

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public final void testCpimParserString() {
		/*
		 * CPIM sample: From: MR SANDERS <im:piglet@100akerwood.com> To:
		 * Depressed Donkey <im:eeyore@100akerwood.com> DateTime:
		 * 2000-12-13T13:40:00-08:00 Subject: the weather will be fine today
		 * 
		 * Content-type: text/plain Content-ID: <1234567890@foo.com>
		 * 
		 * Here is the text of my message.
		 */
		StringBuffer sb = new StringBuffer();
		sb.append("From: MR SANDERS <im:piglet@100akerwood.com>");
		sb.append(CRLF);
		sb.append("To: Depressed Donkey <im:eeyore@100akerwood.com>");
		sb.append(CRLF);
		sb.append("DateTime: 2000-12-13T13:40:00-08:00");
		sb.append(CRLF);
		sb.append("Subject: the weather will be fine today");
		sb.append(DOUBLE_CRLF);
		sb.append("Content-type: text/plain");
		sb.append(CRLF);
		sb.append("Content-ID: <1234567890@foo.com>");
		sb.append(DOUBLE_CRLF);
		sb.append("Here is the text of my message.");
		String text = sb.toString();
		CpimMessage msg = null;
		try {
			msg = (new CpimParser(text)).getCpimMessage();
		} catch (Exception e) {
			fail("no message parsed");
			e.printStackTrace();
		}
		if (msg != null) {
			assertEquals(msg.getHeader("From"),
					"MR SANDERS <im:piglet@100akerwood.com>");
			assertEquals(msg.getHeader("To"),
					"Depressed Donkey <im:eeyore@100akerwood.com>");
			assertEquals(msg.getHeader("DateTime"), "2000-12-13T13:40:00-08:00");
			assertEquals(msg.getHeader("Subject"),
					"the weather will be fine today");
			assertEquals(msg.getContentHeader("Content-ID"),
					"<1234567890@foo.com>");
			assertEquals(msg.getContentType(), "text/plain");
			assertEquals(msg.getMessageContent(),
					"Here is the text of my message.");
		}
	}

}
