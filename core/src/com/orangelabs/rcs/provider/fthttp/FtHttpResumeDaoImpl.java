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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * @author YPLO6403
 * 
 *         Implementation of interface to get access to FT HTTP data objects
 * 
 */
public class FtHttpResumeDaoImpl implements FtHttpResumeDao {

	/**
	 * Current instance
	 */
	private static FtHttpResumeDaoImpl instance = null;

	/**
	 * The logger
	 */
	final private static Logger logger = Logger.getLogger(FtHttpResumeDaoImpl.class.getSimpleName());

	/**
	 * Content resolver
	 */
	private ContentResolver cr;

	private FtHttpResumeDaoImpl(Context context) {
		this.cr = context.getContentResolver();
	}

	/**
	 * Creates an interface to get access to Data Object FtHttpResume
	 * 
	 * @param ctx
	 *            the {@code context} value.
	 * @return Instance of FtHttpResumeDaoImpl
	 */
	public static synchronized FtHttpResumeDaoImpl createInstance(Context ctx) {
		if (instance == null) {
			instance = new FtHttpResumeDaoImpl(ctx);
		}
		return instance;
	}

	/**
	 * Returns instance of DAO FtHttpResume
	 * 
	 * @return Instance
	 */
	public static FtHttpResumeDaoImpl getInstance() {
		return instance;
	}

	@Override
	public List<FtHttpResume> queryAll() {
		FthttpSelection where = new FthttpSelection();
		ArrayList<FtHttpResume> result = new ArrayList<FtHttpResume>();
		FthttpCursor cursor = where.query(cr);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				if (cursor.getDirection() == Direction.INCOMING) {
					result.add(new FtHttpResumeDownload(cursor));
				} else {
					result.add(new FtHttpResumeUpload(cursor));
				}
			}
			cursor.close();
		}
		return result;
	}

	@Override
	public List<FtHttpResume> queryAll(Status status) {
		FthttpSelection where = new FthttpSelection();
		where.status(status);
		ArrayList<FtHttpResume> result = new ArrayList<FtHttpResume>();
		FthttpCursor cursor = where.query(cr);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				if (cursor.getDirection() == Direction.INCOMING) {
					result.add(new FtHttpResumeDownload(cursor));
				} else {
					result.add(new FtHttpResumeUpload(cursor));
				}
			}
			cursor.close();
		}
		return result;
	}
	
	@Override
	public FtHttpResume queryOldest(Status status) {
		FthttpSelection where = new FthttpSelection();
		FtHttpResume result = null;
		where.status(status);
		FthttpCursor cursor = where.query(cr, null, "_ID LIMIT 1");
		if (cursor != null) {
			if (cursor.moveToNext()) {
				if (cursor.getDirection() == Direction.INCOMING) {
					result = new FtHttpResumeDownload(cursor);
				} else {
					result = new FtHttpResumeUpload(cursor);
				}
			}
			cursor.close();
		}
		return result;
	}

	@Override
	public Uri insert(FtHttpResume ftHttpResume) {
		return insert( ftHttpResume, Status.STARTED);
	}
	
	@Override
	public Uri insert(FtHttpResume ftHttpResume, Status status) {
		FthttpContentValues values = new FthttpContentValues();
		values.putDate(new Date(System.currentTimeMillis()));
		values.putStatus(status);
		values.putDirection(ftHttpResume.getDirection());
		values.putFilename(ftHttpResume.getFilename());
		values.putThumbnail(ftHttpResume.getThumbnail());
		values.putContact(PhoneUtils.extractNumberFromUri(ftHttpResume.getContact()));
		values.putDisplayName(ftHttpResume.getDisplayName());
		values.putChatid(ftHttpResume.getChatId());
		values.putSessionId(ftHttpResume.getSessionId());
		List<String> participants = ftHttpResume.getParticipants();
		if (participants != null && !participants.isEmpty())
			values.putParticipants(TextUtils.join(";", participants));
		if (ftHttpResume instanceof FtHttpResumeDownload) {
			FtHttpResumeDownload download = (FtHttpResumeDownload) ftHttpResume;
			values.putInUrl(download.getUrl());
			values.putInType(download.getMimeType());
			values.putInSize(download.getSize());
			if (logger.isActivated()) {
				logger.debug("insert " + download + ")");
			}
		} else {
			if (ftHttpResume instanceof FtHttpResumeUpload) {
				FtHttpResumeUpload upload = (FtHttpResumeUpload) ftHttpResume;
				values.putOuTid(upload.getTid());
				logger.debug("insert " + upload + ")");
			} else {
				return null;
			}
		}
		return cr.insert(FthttpColumns.CONTENT_URI, values.values());
	}

	@Override
	public int deleteAll() {
		return cr.delete(FthttpColumns.CONTENT_URI, null, null);
	}

	@Override
	public int delete(FtHttpResume ftHttpResume) {
		if (logger.isActivated()) {
			logger.debug("delete " + ftHttpResume);
		}
		FthttpSelection where = new FthttpSelection();
		if (ftHttpResume instanceof FtHttpResumeDownload) {
			FtHttpResumeDownload download = (FtHttpResumeDownload) ftHttpResume;
			where.inUrl(download.getUrl());
		} else {
			if (ftHttpResume instanceof FtHttpResumeUpload) {
				FtHttpResumeUpload upload = (FtHttpResumeUpload) ftHttpResume;
				where.ouTid(upload.getTid());
			} else
				return 0;
		}
		return where.delete(cr);
	}

	@Override
	public void setStatus(FtHttpResume ftHttpResume, Status status) {
		if (logger.isActivated()) {
			logger.debug("setStatus (ftHttpResume=" + ftHttpResume + ") (status=" + status + ")");
		}
		FthttpContentValues values = new FthttpContentValues();
		values.putStatus(status);
		FthttpSelection where = new FthttpSelection();
		if (ftHttpResume instanceof FtHttpResumeDownload) {
			FtHttpResumeDownload download = (FtHttpResumeDownload) ftHttpResume;
			where.inUrl(download.getUrl()).and().direction(Direction.INCOMING);
		} else {
			if (ftHttpResume instanceof FtHttpResumeUpload) {
				FtHttpResumeUpload upload = (FtHttpResumeUpload) ftHttpResume;
				where.ouTid(upload.getTid()).and().direction(Direction.OUTGOING);
			} else
				return;
		}
		cr.update(FthttpColumns.CONTENT_URI, values.values(), where.sel(), where.args());
	}

	@Override
	public Status getStatus(FtHttpResume ftHttpResume) {
		FthttpSelection where = new FthttpSelection();
		if (ftHttpResume instanceof FtHttpResumeDownload) {
			FtHttpResumeDownload download = (FtHttpResumeDownload) ftHttpResume;
			where.inUrl(download.getUrl()).and().direction(Direction.INCOMING);
		} else {
			if (ftHttpResume instanceof FtHttpResumeUpload) {
				FtHttpResumeUpload upload = (FtHttpResumeUpload) ftHttpResume;
				where.ouTid(upload.getTid()).and().direction(Direction.OUTGOING);
			} else
				return null;
		}
		Status result = null;
		FthttpCursor cursor = where.query(cr, new String[] { FthttpColumns.STATUS }, "_ID LIMIT 1");
		if (cursor != null) {
			if (cursor.moveToNext()) {
				result = cursor.getStatus();
			}
			cursor.close();
		}
		return result;
	}

	@Override
	public FtHttpResumeUpload queryUpload(String tid) {
		FthttpSelection where = new FthttpSelection();
		FtHttpResumeUpload result = null;
		where.ouTid(tid).and().direction(Direction.OUTGOING);
		FthttpCursor cursor = where.query(cr, null, "_ID LIMIT 1");
		if (cursor != null) {
			if (cursor.moveToNext()) {
				result = new FtHttpResumeUpload(cursor);
			}
			cursor.close();
		}
		return result;
	}

	@Override
	public FtHttpResumeDownload queryDownload(String url) {
		FthttpSelection where = new FthttpSelection();
		FtHttpResumeDownload result = null;
		where.inUrl(url).and().direction(Direction.INCOMING);
		FthttpCursor cursor = where.query(cr, null, "_ID LIMIT 1");
		if (cursor != null) {
			if (cursor.moveToNext()) {
				result = new FtHttpResumeDownload(cursor);
			}
			cursor.close();
		}
		return result;
	}

	@Override
	public int clean() {
		if (logger.isActivated()) {
			logger.debug("deleteFinished");
		}
		FthttpSelection where = new FthttpSelection();
		where.statusNot(Status.STARTED);
		return where.delete(cr);
	}

}
