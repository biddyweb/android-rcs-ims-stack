package com.orangelabs.rcs.core.ims.service.im.filetransfer.http;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * HTTP upload manager
 *
 * @author jexa7410
 */
public class HttpDownloadManager extends HttpTransferManager {
    /**
     * Maximum value of retry
     */
    private final static int RETRY_MAX = 3;

    /**
     * File content to Download
     */
    private MmContent content;

    /**
     * File name
     */
    private String contentName;

    /**
     * Retry counter
     */
    private int retry_count = 0;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     *
     * @param content File content to download
     * @param listener HTTP transfer event listener
     * @param string 
     */
    public HttpDownloadManager(MmContent content, HttpTransferEventListener listener, String name) {
        super(listener);
        this.content = content;
        this.contentName = name;
    }

    /**
     * Returns file path
     *
     * @return file path
     */
    public String getFilePath() {
        return RcsSettings.getInstance().getFileRootDirectory() +contentName;
    }

    /**
     * Download file
     * 
     * @return Boolean result. Data are saved during the transfer in the content object.  
     */
    public boolean downloadFile() {
        try {
            if (logger.isActivated()) {
                logger.debug("Download file " + content.getUrl());
            }

            // Send GET request
            HttpGet request = new HttpGet(content.getUrl());
            if (HTTP_TRACE_ENABLED) {
                String trace = ">>> Send HTTP request:";
                trace += "\n" + request.getMethod() + " " + request.getRequestLine().getUri();
                System.out.println(trace);
            }

            // Execute request with retry procedure
            if (!executeRequest(request)) {
                if (retry_count < RETRY_MAX) {
                    retry_count++;
                    return downloadFile();
                } else {
                    if (logger.isActivated()) {
                        logger.debug("Failed to download file");
                    }
                    return false;
                }
            }

            return true;
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * Execute the GET request
     *
     * @param request
     * @return true if 200 Ok
     */
    private boolean executeRequest(HttpGet request) {
        int calclength = 0;
        boolean result = false;

        try {
            // Init file
            if (contentName == null) {
                contentName = "file_"+new Date();
                if (content.getEncoding().startsWith("image/jpeg")) {
                    contentName += ".jpg";
                }
            }
            File file = new File(RcsSettings.getInstance().getFileRootDirectory(), contentName);
            BufferedOutputStream bOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[CHUNK_MAX_SIZE];

            // Execute HTTP request
            HttpResponse response = getHttpClient().execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (HTTP_TRACE_ENABLED) {
                String trace = "<<< Resceive HTTP response:";
                trace += "\n" + statusCode + " " + response.getStatusLine().getReasonPhrase();
                System.out.println(trace);
            }
            if (statusCode == 200) {
                result = true;
                HttpEntity entity = response.getEntity();
                InputStream input = entity.getContent();
                int num;
                while ((num = input.read(buffer)) != -1) {
                    calclength += num;
                    getListener().httpTransferProgress(calclength, content.getSize());
                    bOutputStream.write(buffer, 0, num);
                }
            }

            // Close stream
            bOutputStream.flush();
            bOutputStream.close();
        } catch (Exception e) {
            // Nothing to do
        }
        return result;
    }
}
