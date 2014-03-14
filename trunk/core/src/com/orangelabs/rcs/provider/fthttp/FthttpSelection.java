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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.orangelabs.rcs.provider.base.AbstractSelection;

/**
 * Selection for the {@code fthttp} table.
 */
public class FthttpSelection extends AbstractSelection<FthttpSelection> {
	@Override
	public Uri uri() {
		return FthttpColumns.CONTENT_URI;
	}

	/**
	 * Query the given content resolver using this selection.
	 * 
	 * @param contentResolver
	 *            The content resolver to query.
	 * @param projection
	 *            A list of which columns to return. Passing null will return all columns, which is inefficient.
	 * @param sortOrder
	 *            How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use
	 *            the default sort order, which may be unordered.
	 * @return A {@code FthttpCursor} object, which is positioned before the first entry, or null.
	 */
	public FthttpCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
		Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
		if (cursor == null)
			return null;
		return new FthttpCursor(cursor);
	}

	/**
	 * Equivalent of calling {@code query(contentResolver, projection, null}.
	 */
	public FthttpCursor query(ContentResolver contentResolver, String[] projection) {
		return query(contentResolver, projection, null);
	}

	/**
	 * Equivalent of calling {@code query(contentResolver, projection, null, null}.
	 */
	public FthttpCursor query(ContentResolver contentResolver) {
		return query(contentResolver, null, null);
	}

	/**
	 * Define selection based on tid for outgoing FT HTTP
	 * 
	 * @param value
	 *            the list of {@code tids}
	 * @return A {@code FthttpSelection} selection instance to query the table
	 */
	public FthttpSelection ouTid(String... tids) {
		addEquals(FthttpColumns.OU_TID, tids);
		return this;
	}

	/**
	 * Define selection based on url for incoming FT HTTP
	 * 
	 * @param value
	 *            the list of {@code urls}
	 * @return A {@code FthttpSelection} selection instance to query the table
	 */
	public FthttpSelection inUrl(String... urls) {
		addEquals(FthttpColumns.IN_URL, urls);
		return this;
	}

	/**
	 * Define selection based on direction
	 * 
	 * @param value
	 *            the list of direction {@code values}
	 * @return A {@code FthttpSelection} selection instance to query the table
	 */
	public FthttpSelection direction(Direction... values) {
		addEquals(FthttpColumns.DIRECTION, values);
		return this;
	}

	/**
	 * Define selection based on status
	 * 
	 * @param value
	 *            the list of status {@code values}
	 * @return A {@code FthttpSelection} selection instance to query the table
	 */
	public FthttpSelection status(Status... values) {
		addEquals(FthttpColumns.STATUS, values);
		return this;
	}

	/**
	 * Define selection based on status not equal
	 * 
	 * @param value
	 *            the list of status {@code values}
	 * @return A {@code FthttpSelection} selection instance to query the table
	 */
    public FthttpSelection statusNot(Status... value) {
        addNotEquals(FthttpColumns.STATUS, value);
        return this;
    }
    
	public FthttpSelection sessionId(String... value) {
		addEquals(FthttpColumns.SESSION_ID, value);
		return this;
	}

}
