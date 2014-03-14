/*******************************************************************************
w * Software Name : RCS IMS Stack
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
package com.orangelabs.rcs.core.ims.service.im.filetransfer.http;

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.im.filetransfer.FileSharingError;
import com.orangelabs.rcs.provider.fthttp.FtHttpResumeDaoImpl;
import com.orangelabs.rcs.provider.fthttp.FtHttpResumeDownload;
import com.orangelabs.rcs.provider.fthttp.FtHttpStatus;
import com.orangelabs.rcs.utils.logger.Logger;

public class ResumeDownloadFileSharingSession extends TerminatingHttpFileSharingSession {

	/**
	 * the instance of Download resume (Reflects the content of the DB)
	 */
	private FtHttpResumeDownload resumeDownload;

	/**
	 * The logger
	 */
	private final static Logger logger = Logger.getLogger(ResumeDownloadFileSharingSession.class.getSimpleName());

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param content
	 * @param contact
	 * @param thumbnail
	 * @param chatSessionID
	 * @param chatContributionId
	 * @param download
	 */
	public ResumeDownloadFileSharingSession(ImsService parent, MmContent content, String contact, byte[] thumbnail,
			String chatSessionID, String chatContributionId, FtHttpResumeDownload download) {
		super(parent, content, contact, thumbnail, download.getSessionId(), download.getChatId());
		setRemoteDisplayName(download.getDisplayName());
		// this.msgId = msgId; TODO
		this.resumeDownload = download;
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
			if (logger.isActivated()) {
				logger.info("Resume a HTTP file transfer session as terminating");
			}
			// Notify listeners
			for (int j = 0; j < getListeners().size(); j++) {
				getListeners().get(j).handleSessionStarted();
			}
			// Instantiate the download manager
			downloadManager = new HttpDownloadManager(getContent(), this, resumeDownload.getFilename());
			// Resume download file from the HTTP server
			if (downloadManager.streamForFile != null && downloadManager.resumeDownload()) {
				if (logger.isActivated()) {
					logger.debug("Resume download success for " + resumeDownload);
				}
				// Set filename
				getContent().setUrl(downloadManager.getLocalUrl());
				// File transfered
				handleFileTransfered();
				// Send delivery report "displayed"
				// sendDeliveryReport(ImdnDocument.DELIVERY_STATUS_DISPLAYED);
			} else {
				if (downloadManager.isCancelled()) {
					return;
				}
				if (logger.isActivated()) {
					logger.warn("Resume download has failed");
				}
				FtHttpResumeDaoImpl.getInstance().setStatus(resumeDownload, FtHttpStatus.FAILURE);
				// Upload error
				handleError(new FileSharingError(FileSharingError.MEDIA_DOWNLOAD_FAILED));
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("Transfer has failed", e);
			}
			// Unexpected error
			handleError(new FileSharingError(FileSharingError.UNEXPECTED_EXCEPTION, e.getMessage()));
		}
	}

}
