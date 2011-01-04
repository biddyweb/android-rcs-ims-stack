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
package com.orangelabs.rcs.utils;

import java.util.Hashtable;

/**
 * MIME manager
 * 
 * @author jexa7410
 */
public class MimeManager {
	/**
	 * List of supported MIME
	 */
    private static Hashtable<String, String> mimeTable = new Hashtable<String, String>();
    static {
    	// Photo type
    	mimeTable.put("jpg", "image/jpeg");
    	mimeTable.put("jpeg", "image/jpeg");
    	mimeTable.put("png", "image/png");
    	mimeTable.put("bmp", "image/bmp");

    	// Video type
    	mimeTable.put("3gp", "video/3gpp");    	
    	mimeTable.put("mp4", "video/mpeg4");
    	mimeTable.put("mp4a", "video/mpeg4");
    	mimeTable.put("mpeg", "video/mpeg");
    	mimeTable.put("mpg", "video/mpeg");
    }    
    
    /**
     * Returns the supported MIME types 
     * 
     * @return Table
     */
    public static Hashtable<String, String> getSupportedMimeTypes() {
    	return mimeTable;
    }
    
	/**
	 * Returns the Mime-Type associated to a given file extension
	 * 
	 * @param ext File extension
	 * @return Mime-Type
	 */
    public static String getMimeType(String ext) {
    	return mimeTable.get(ext.toLowerCase());
    }
    
	/**
	 * Returns URL extension
	 * 
	 * @param url URL
	 * @return Extension
	 */
	public static String getFileExtension(String url){
		if ((url != null) && (url.indexOf('.')!=-1)) {
			return url.substring(url.lastIndexOf('.')+1);
		}
		
		return "";
	}	
    
	/**
	 * Returns Mime-Type extension
	 * 
	 * @param mime Mime-Type
	 * @return Extension
	 */
	public static String getMimeExtension(String mime){
		if ((mime != null) && (mime.indexOf('/')!=-1)) {
    		return mime.substring(mime.indexOf('/')+1);
		}
		
		return "";
	}	

	/**
	 * Is a photo file
	 * 
	 * @param url URL
	 * @return Boolean
	 */
    public static boolean isPhotoFile(String url) {
    	String ext = getFileExtension(url);
    	String mime = getMimeType(ext);
    	if ((mime != null) && mime.startsWith("image/")) {
    		return true;
    	} else {
    		return false;
    	}
    }    

	/**
	 * Is a video file
	 * 
	 * @param url URL
	 * @return Boolean
	 */
    public static boolean isVideoFile(String url) {
    	String ext = getFileExtension(url);
    	String mime = getMimeType(ext);
    	if ((mime != null) && mime.startsWith("video/")) {
    		return true;
    	} else {
    		return false;
    	}
    }    

    /**
	 * Is an audio file
	 * 
	 * @param url URL
	 * @return Boolean
	 */
    public static boolean isAudioFile(String url) {
    	String ext = getFileExtension(url);
    	String mime = getMimeType(ext);
    	if ((mime != null) && mime.startsWith("audio/")) {
    		return true;
    	} else {
    		return false;
    	}
    }    

	/**
	 * Is a text file
	 * 
	 * @param url URL
	 * @return Boolean
	 */
    public static boolean isTextFile(String url) {
    	String ext = getFileExtension(url);
    	if ((ext != null) && ext.equals("txt")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Is a picture type
     * 
     * @param mime Mime-Type
     * @return Boolean
     */
    public static boolean isPictureType(String mime){
    	if (mime.startsWith("image/")){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    /**
     * Is a video type
     * 
     * @param mime Mime-Type
     * @return Boolean
     */
    public static boolean isVideoType(String mime){
    	if (mime.startsWith("video/")){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    /**
     * Is an audio type
     * 
     * @param mime Mime-Type
     * @return Boolean
     */
    public static boolean isAudioType(String mime){
    	if (mime.startsWith("audio/")){
    		return true;
    	}else{
    		return false;
    	}
    }

    /**
     * Is a text type
     * 
     * @param mime Mime-Type
     * @return Boolean
     */
    public static boolean isTextType(String mime){
    	if (mime.startsWith("text/")){
    		return true;
    	}else{
    		return false;
    	}
    }
}
