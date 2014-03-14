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

import java.util.List;

import android.net.Uri;

/**
 * @author YPLO6403
 * 
 *         Interface to get access to FT HTTP data objects
 * 
 */
public interface FtHttpResumeDao {

	/**
	 * Query all entries sorted in _ID ascending order
	 * 
	 * @return the list of entries
	 */
	public List<FtHttpResume> queryAll();

	/**
	 * Query entries with status sorted in _ID ascending order
	 * 
	 * @param status
	 *            the {@code status} value.
	 * @return the list of entries
	 */
	public List<FtHttpResume> queryAll(Status status);
	
	/**
	 * Query the oldest entry
	 * 
	 * @param status
	 *            the {@code status} value.
	 * @return the entry (Can be {@code null}).
	 */
	public FtHttpResume queryOldest(Status status);

	/**
	 * Query the upload entry with TID
	 * 
	 * @param tid
	 *            the {@code tid} value.
	 * @return the entry (Can be {@code null}).
	 */
	public FtHttpResumeUpload queryUpload(String tid);

	/**
	 * Query the download entry with url
	 * 
	 * @param url
	 *            the {@code url} value.
	 * @return the entry (Can be {@code null}).
	 */
	public FtHttpResumeDownload queryDownload(String url);

	/**
	 * Add entry in the fthttp table
	 * 
	 * @param ftHttpResume
	 *            the {@code ftHttpResume} value.
	 * @param status
	 *            the {@code status} value.
	 * @return the URI or null if add operation failed
	 */
	public Uri insert(FtHttpResume ftHttpResume, Status status);

	/**
	 * Works just like insert(FtHttpResume, Status) except the status is always STARTED
	 * 
	 * @see #insert(FtHttpResume, Status)
	 */
	public Uri insert(FtHttpResume ftHttpResume);

	/**
	 * Delete entry in fthttp table
	 * 
	 * @param ftHttpResume
	 *            the {@code ftHttpResume} value.
	 * @return number of rows deleted
	 */
	public int delete(FtHttpResume ftHttpResume);

	/**
	 * Clean fthttp table for non started sessions
	 * 
	 * @return number of rows deleted
	 */
	public int clean();

	/**
	 * Update the status of an entry in the fthttp table
	 * 
	 * @param ftHttpResume
	 *            the {@code ftHttpResume} value.
	 * @param status
	 *            the New {@code status} value.
	 */
	public void setStatus(FtHttpResume ftHttpResume, Status status);

	/**
	 * Get the status entry in the fthttp table
	 * 
	 * @param ftHttpResume
	 *            the {@code ftHttpResume} value.
	 * 
	 * 
	 * @return the status (Can be {@code null}).
	 */
	public Status getStatus(FtHttpResume ftHttpResume);

	/**
	 * Delete all entries in fthttp table
	 * 
	 * @return number of rows deleted
	 */
	int deleteAll();
}
