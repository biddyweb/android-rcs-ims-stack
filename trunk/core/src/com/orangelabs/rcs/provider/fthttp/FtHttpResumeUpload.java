/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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
package com.orangelabs.rcs.provider.fthttp;

import com.orangelabs.rcs.core.ims.service.im.filetransfer.FileSharingSession;

/**
 * @author YPLO6403
 * 
 *         Class to handle FtHttpResumeUpload data objects
 * 
 */
public class FtHttpResumeUpload extends FtHttpResume {

	/**
	 * The FT HTTP Transfer Id
	 */
	final protected String tid;

	/**
	 * Creates a FT HTTP resume upload data object
	 * 
	 * 
	 * @param session
	 *            the {@code session} value.
	 * @param tid
	 *            the {@code tid} value.
	 * @param thumbnail
	 *            the {@code thumbnail} value.
	 */
	public FtHttpResumeUpload(FileSharingSession session, String tid, byte[] thumbnail) {
		super(Direction.OUTGOING, session.getContent().getName(), thumbnail, session.getRemoteContact(), session
				.getRemoteDisplayName(), session.getContributionID(), session.getSessionID(), session.getParticipants().toString());
		if (tid == null)
			throw new IllegalArgumentException("Null tid");
		this.tid = tid;
	}

	/**
	 * Creates a FT HTTP resume upload data object
	 * 
	 * @param cursor
	 *            the {@code cursor} value.
	 */
	public FtHttpResumeUpload(FthttpCursor cursor) {
		super(cursor);
		this.tid = cursor.getOuTid();
		if (this.tid == null)
			throw new IllegalArgumentException("Null TID");
	}

	public String getTid() {
		return tid;
	}

	@Override
	public String toString() {
		return "FtHttpResumeUpload [tid=" + tid + "]";
	}

}
