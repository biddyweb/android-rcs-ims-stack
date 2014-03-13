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

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.im.filetransfer.FileSharingSession;

/**
 * @author YPLO6403
 * 
 *         Class to handle FtHttpResumeDownload data objects
 * 
 */
public final class FtHttpResumeDownload extends FtHttpResume {

	/**
	 * The URL to download file
	 */
	final String url;
	/**
	 * The mime type of the file to download
	 */
	final String mimeType;
	/**
	 * The size of the file to download
	 */
	final Long size;

	/**
	 * Creates a FT HTTP resume download data object (immutable)
	 * 
	 * @param session
	 *            the {@code session} instance.
	 * @param filename
	 *            the {@code filename} value.
	 * @param thumbnail
	 *            the {@code thumbnail} value.
	 */
	public FtHttpResumeDownload(FileSharingSession session, String filename, byte[] thumbnail) {
		super(Direction.INCOMING, filename, thumbnail, session.getRemoteContact(), session.getRemoteDisplayName(), session
				.getContributionID(), session.getSessionID(), session.getParticipants().toString());
		MmContent content = session.getContent();
		this.url = content.getUrl();
		this.mimeType = content.getEncoding();
		this.size = content.getSize();
		if (size <= 0 || url == null || mimeType == null)
			throw new IllegalArgumentException("Invalid argument");
	}

	/**
	 * Creates a FT HTTP resume download data object
	 * 
	 * @param cursor
	 *            the {@code cursor} value.
	 */
	public FtHttpResumeDownload(FthttpCursor cursor) {
		super(cursor);
		this.url = cursor.getInUrl();
		this.mimeType = cursor.getInType();
		this.size = cursor.getInSize();
		if (this.size <= 0 || this.url == null || this.mimeType == null)
			throw new IllegalArgumentException("Null argument");
	}

	public String getUrl() {
		return url;
	}

	public String getMimeType() {
		return mimeType;
	}

	public Long getSize() {
		return size;
	}

	@Override
	public String toString() {
		return "FtHttpResumeDownload [url=" + url + ", mimeType=" + mimeType + ", size=" + size + "]";
	}

}
