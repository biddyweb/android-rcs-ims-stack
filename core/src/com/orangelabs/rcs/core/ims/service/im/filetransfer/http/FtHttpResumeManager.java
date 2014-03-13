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
package com.orangelabs.rcs.core.ims.service.im.filetransfer.http;

import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.im.InstantMessagingService;
import com.orangelabs.rcs.provider.fthttp.FtHttpResume;
import com.orangelabs.rcs.provider.fthttp.FtHttpResumeDaoImpl;
import com.orangelabs.rcs.provider.fthttp.FtHttpResumeDownload;
import com.orangelabs.rcs.provider.fthttp.Status;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * File Transfer HTTP resume manager
 */
public class FtHttpResumeManager implements HttpTransferEventListener {
	private FtHttpResume ftHttpResume;
	private FtHttpResumeDaoImpl dao = FtHttpResumeDaoImpl.getInstance();
	private boolean terminate = false;
	private HttpDownloadManager downloadManager;

	/**
	 * The logger
	 */
	private static final Logger logger = Logger.getLogger(FtHttpResumeManager.class.getSimpleName());

	/**
	 * Constructor
	 * 
	 * @param imsService
	 *            IMS service
	 */
	public FtHttpResumeManager(InstantMessagingService instantMessagingService) {
		if (dao == null) {
			if (logger.isActivated()) {
				logger.error("Cannot resume FT");
			}
			return;
		}
		try {
			// delete entries in FT HTTP which are no more useful
			dao.deleteFinished();
			do {
				ftHttpResume = dao.queryOldest(Status.STARTED);
				if (ftHttpResume != null) {
					switch (ftHttpResume.getDirection()) {
					case INCOMING:
						FtHttpResumeDownload download = (FtHttpResumeDownload) ftHttpResume;
						MmContent content = ContentManager.createMmContentFromUrl(download.getUrl(), download.getSize());
						downloadManager = new HttpDownloadManager(content, this, download.getFilename());
						if (downloadManager.streamForFile != null && downloadManager.resumeDownload()) {
							if (logger.isActivated()) {
								logger.error("Resume success for " + download);
							}
						} else {
							dao.setStatus(download, Status.FAILURE);
						}
						downloadManager = null;
						break;
					case OUTGOING:
						break;
					}
				}
			} while (ftHttpResume != null || terminate);
		} catch (Exception e) {
			// handle exception
			if (logger.isActivated()) {
				logger.error("Exception occurred",e);
			}
		}

	}

	@Override
	public void httpTransferStarted() {
		if (logger.isActivated()) {
			logger.info("FT " + this.ftHttpResume + " is started");
		}
	}

	@Override
	public void httpTransferPaused() {
		if (logger.isActivated()) {
			logger.info("FT " + this.ftHttpResume + " is paused");
		}
	}

	@Override
	public void httpTransferResumed() {
		if (logger.isActivated()) {
			logger.info("FT " + this.ftHttpResume + " is resumed");
		}
	}

	@Override
	public void httpTransferProgress(long currentSize, long totalSize) {
		if (logger.isActivated()) {
			logger.info("FT " + this.ftHttpResume + " progress current size=" + currentSize);
		}
	}

	public void terminate() {
		this.terminate = true;
		if (downloadManager != null)
			downloadManager.interrupt();
	}
}
