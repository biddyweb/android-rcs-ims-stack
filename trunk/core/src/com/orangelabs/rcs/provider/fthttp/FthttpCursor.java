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

import java.util.Date;

import android.database.Cursor;

import com.orangelabs.rcs.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code fthttp} table.
 */
public class FthttpCursor extends AbstractCursor {
	public FthttpCursor(Cursor cursor) {
		super(cursor);
	}

	/**
	 * Get the {@code ou_tid} value. Can be {@code null}.
	 */
	public String getOuTid() {
		Integer index = getCachedColumnIndexOrThrow(FthttpColumns.OU_TID);
		return getString(index);
	}

	/**
	 * Get the {@code in_url} value. Can be {@code null}.
	 */
	public String getInUrl() {
		Integer index = getCachedColumnIndexOrThrow(FthttpColumns.IN_URL);
		return getString(index);
	}

	/**
	 * Get the {@code in_size} value. Can be {@code null}.
	 */
	public Long getInSize() {
		return getLongOrNull(FthttpColumns.IN_SIZE);
	}

	/**
	 * Get the {@code in_type} value. Can be {@code null}.
	 */
	public String getInType() {
		Integer index = getCachedColumnIndexOrThrow(FthttpColumns.IN_TYPE);
		return getString(index);
	}

	/**
	 * Get the {@code contact} value. Can be {@code null}.
	 */
	public String getContact() {
		Integer index = getCachedColumnIndexOrThrow(FthttpColumns.CONTACT);
		return getString(index);
	}

	/**
	 * Get the {@code chatid} value. Can be {@code null}.
	 */
	public String getChatid() {
		Integer index = getCachedColumnIndexOrThrow(FthttpColumns.CHATID);
		return getString(index);
	}

	/**
	 * Get the {@code filename} value. Cannot be {@code null}.
	 */
	public String getFilename() {
		Integer index = getCachedColumnIndexOrThrow(FthttpColumns.FILENAME);
		return getString(index);
	}

	/**
	 * Get the {@code direction} value. Cannot be {@code null}.
	 */
	public Direction getDirection() {
		Integer intValue = getIntegerOrNull(FthttpColumns.DIRECTION);
		if (intValue == null)
			return null;
		return Direction.values()[intValue];
	}

	/**
	 * Get the {@code status} value. Cannot be {@code null}.
	 */
	public Status getStatus() {
		Integer intValue = getIntegerOrNull(FthttpColumns.STATUS);
		if (intValue == null)
			return null;
		return Status.values()[intValue];
	}

	/**
	 * Get the {@code date} value. Can be {@code null}.
	 */
	public Date getDate() {
		return getDate(FthttpColumns.DATE);
	}

	/**
	 * Get the {@code participants} value. Can be {@code null}.
	 */
	public String getParticipants() {
		Integer index = getCachedColumnIndexOrThrow(FthttpColumns.PARTICIPANTS);
		return getString(index);
	}

	/**
	 * Get the {@code display_name} value. Can be {@code null}.
	 */
	public String getDisplayName() {
		Integer index = getCachedColumnIndexOrThrow(FthttpColumns.DISPLAY_NAME);
		return getString(index);
	}

	/**
	 * Get the {@code session_id} value. Can be {@code null}.
	 */
	public String getSessionId() {
		Integer index = getCachedColumnIndexOrThrow(FthttpColumns.SESSION_ID);
		return getString(index);
	}

	/**
	 * Get the {@code thumbnail} value. Can be {@code null}.
	 */
	public byte[] getThumbnail() {
		Integer index = getCachedColumnIndexOrThrow(FthttpColumns.THUMBNAIL);
		return getBlob(index);
	}

}
