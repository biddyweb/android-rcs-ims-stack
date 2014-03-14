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

import java.util.List;

import com.orangelabs.rcs.core.ims.service.im.InstantMessagingService;
import com.orangelabs.rcs.core.ims.service.im.filetransfer.FileSharingSessionListener;
import com.orangelabs.rcs.provider.fthttp.FtHttpResume;
import com.orangelabs.rcs.provider.fthttp.FtHttpResumeDaoImpl;
import com.orangelabs.rcs.provider.fthttp.FtHttpStatus;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * File Transfer HTTP resume manager
 */
public class FtHttpResumeManager {
	private FtHttpResumeDaoImpl dao = FtHttpResumeDaoImpl.getInstance();
	private boolean terminate = false;
	private InstantMessagingService imsService;
	private List<FtHttpResume> ftHttp2Resume;
	FileSharingSessionListener listener;
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
		imsService = instantMessagingService;
		try {
			// delete entries in FT HTTP which are no more useful
			dao.clean();
			ftHttp2Resume = dao.queryAll(FtHttpStatus.STARTED);
			processNext();
		} catch (Exception e) {
			// handle exception
			if (logger.isActivated()) {
				logger.error("Exception occurred", e);
			}
		}

	}

	private void processNext() {
		// if (ftHttp2Resume.isEmpty())
		// return;
		// FtHttpResume ftHttpResume = ftHttp2Resume.ftHttp2Resume
		// if (ftHttpResume != null) {
		// switch (ftHttpResume.getDirection()) {
		// case INCOMING:
		// FtHttpResumeDownload download = (FtHttpResumeDownload) ftHttpResume;
		// MmContent content = ContentManager.createMmContentFromMime(download.getFilename(), download.getUrl(),
		// download.getMimeType(), download.getSize());
		// final ResumeDownloadFileSharingSession resumeDownload = new ResumeDownloadFileSharingSession(imsService, content,
		// ftHttpResume.getContact(), ftHttpResume.getThumbnail(), ftHttpResume.getSessionId(),
		// ftHttpResume.getChatId(), download);
		// // resumeDownload.addListener(this);
		// new Thread() {
		// public void run() {
		// resumeDownload.start();
		// }
		// }.start();
		// break;
		// case OUTGOING:
		// // TODO
		// break;
		// }
		// }
	}

	public void terminate() {
		this.terminate = true;

	}

}
