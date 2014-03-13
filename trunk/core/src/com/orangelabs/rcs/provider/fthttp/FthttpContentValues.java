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

import android.content.ContentResolver;
import android.net.Uri;

import com.orangelabs.rcs.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code fthttp} table.
 */
public class FthttpContentValues extends AbstractContentValues {
	@Override
	public Uri uri() {
		return FthttpColumns.CONTENT_URI;
	}

	/**
	 * Update row(s) using the values stored by this object and the given selection.
	 * 
	 * @param contentResolver
	 *            The content resolver to use.
	 * @param where
	 *            The selection to use (can be {@code null}).
	 * @return the number of rows updated.
	 */
	public int update(ContentResolver contentResolver, FthttpSelection where) {
		return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
	}

	/**
	 * Adds TID to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putOuTid(String value) {
		mContentValues.put(FthttpColumns.OU_TID, value);
		return this;
	}

	/**
	 * Adds URL to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putInUrl(String value) {
		mContentValues.put(FthttpColumns.IN_URL, value);
		return this;
	}

	/**
	 * Adds size to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putInSize(Long value) {
		mContentValues.put(FthttpColumns.IN_SIZE, value);
		return this;
	}

	/**
	 * Adds type (of transferred file) to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putInType(String value) {
		mContentValues.put(FthttpColumns.IN_TYPE, value);
		return this;
	}

	/**
	 * Adds contact to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putContact(String value) {
		mContentValues.put(FthttpColumns.CONTACT, value);
		return this;
	}

	/**
	 * Adds chat ID to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putChatid(String value) {
		mContentValues.put(FthttpColumns.CHATID, value);
		return this;
	}

	/**
	 * Adds file name to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putFilename(String value) {
		if (value == null)
			throw new IllegalArgumentException("value for filename must not be null");
		mContentValues.put(FthttpColumns.FILENAME, value);
		return this;
	}

	/**
	 * Adds direction to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value} (cannot be null).
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putDirection(Direction value) {
		if (value == null)
			throw new IllegalArgumentException("value for direction must not be null");
		mContentValues.put(FthttpColumns.DIRECTION, value.ordinal());
		return this;
	}

	public FthttpContentValues putStatus(Status value) {
		if (value == null)
			throw new IllegalArgumentException("value for status must not be null");
		mContentValues.put(FthttpColumns.STATUS, value.ordinal());
		return this;
	}

	/**
	 * Adds date to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value} (cannot be null).
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putDate(Date value) {
		if (value == null)
			throw new IllegalArgumentException("value for date must not be null");
		mContentValues.put(FthttpColumns.DATE, value.getTime());
		return this;
	}

	public FthttpContentValues putDate(long value) {
		mContentValues.put(FthttpColumns.DATE, value);
		return this;
	}

	/**
	 * Adds participants to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putParticipants(String value) {
		mContentValues.put(FthttpColumns.PARTICIPANTS, value);
		return this;
	}

	/**
	 * Adds display name to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putDisplayName(String value) {
		mContentValues.put(FthttpColumns.DISPLAY_NAME, value);
		return this;
	}

	/**
	 * Adds session ID to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putSessionId(String value) {
		mContentValues.put(FthttpColumns.SESSION_ID, value);
		return this;
	}

	/**
	 * Adds thumbnail byte array to the set of content values.
	 * 
	 * @param value
	 *            The added {@code value}.
	 * @return Returns the {@code FthttpContentValues} wrapped by this object.
	 */
	public FthttpContentValues putThumbnail(byte[] value) {
		mContentValues.put(FthttpColumns.THUMBNAIL, value);
		return this;
	}

}
