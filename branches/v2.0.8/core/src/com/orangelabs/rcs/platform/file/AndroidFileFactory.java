/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright © 2010 France Telecom S.A.
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
package com.orangelabs.rcs.platform.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import android.os.Environment;

import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.R;

/**
 * Android file factory
 * 
 * @author jexa7410
 */
public class AndroidFileFactory extends FileFactory {
	/**
	 * Open a configuration file input stream 
	 * 
	 * @param filename Configuration filename
	 * @return Input stream
	 * @throws IOException
	 */
	public InputStream openConfigFile(String filename) throws IOException {
		// Extract file name without extension
		int index = filename.indexOf(".");
		String name = filename.substring(0, index);		
		
		// Get the ressource associated to the filename
		try {
			Field field = R.raw.class.getField(name);
			Integer id = (Integer)field.getInt(field);
			InputStream is = AndroidFactory.getApplicationContext().getResources().openRawResource(id.intValue());
			return is;
		} catch(Exception e) {
			return null;
		}
	}

	/**
	 * Open a file input stream
	 * 
	 * @param url URL
	 * @return Input stream
	 * @throws IOException
	 */
	public InputStream openFileInputStream(String url) throws IOException {
		File file = new File(url);
		return new FileInputStream(file);
	}

	/**
	 * Open a file output stream
	 * 
	 * @param url URL
	 * @return Output stream
	 * @throws IOException
	 */
	public OutputStream openFileOutputStream(String url) throws IOException {
		File file = new File(url);
		return new FileOutputStream(file);
	}
	
	/**
	 * Returns the description of a file
	 * 
	 * @param url URL of the file
	 * @return File description
	 * @throws IOException
	 */
	public FileDescription getFileDescription(String url) throws IOException {
		File file = new File(url);
		if (file.isDirectory()) {
			return new FileDescription(url, -1L, true);
		} else {
			return new FileDescription(url, file.length(), false);
		}
	}
	
	/**
	 * Returns the root directory for photos
	 * 
	 *  @return Directory path
	 */
	public String getPhotoRootDirectory() {
		String directory = AndroidFactory.getApplicationContext().getString(R.string.rcs_images_directory);
		return Environment.getExternalStorageDirectory() + directory;
	}

	/**
	 * Returns the root directory for videos
	 * 
	 *  @return Directory path
	 */
	public String getVideoRootDirectory() {
		String directory = AndroidFactory.getApplicationContext().getString(R.string.rcs_videos_directory);
		return Environment.getExternalStorageDirectory() + directory;
	}
	
	/**
	 * Returns the root directory for files
	 * 
	 *  @return Directory path
	 */
	public String getFileRootDirectory() {	
		String directory = AndroidFactory.getApplicationContext().getString(R.string.rcs_files_directory);
		return Environment.getExternalStorageDirectory() + directory;
	}	
}
