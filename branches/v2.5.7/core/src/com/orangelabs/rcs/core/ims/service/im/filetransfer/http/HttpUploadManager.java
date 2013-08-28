package com.orangelabs.rcs.core.ims.service.im.filetransfer.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.protocol.http.HttpAuthenticationAgent;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * HTTP upload manager
 * 
 * @author jexa7410
 * @author hhff3235
 */
public class HttpUploadManager extends HttpTransferManager {
    /**
     * Boundary tag
     */
    private final static String BOUNDARY_TAG = "boundary1";

    /**
     * Two hyphens
     */
    private final static String twoHyphens = "--";

    /**
     * End of line
     */
    private final static String lineEnd = "\r\n";

    /**
     * Maximum value of retry
     */
    private final static int RETRY_MAX = 3;

    /**
     * File content to upload
     */
    private MmContent content;

    /**
     * Thumbnail to upload
     */
    private byte[] thumbnail;

    /**
     * Thumbnail flag
     */
    private boolean thumbnailFlag = true;

    /**
     * TID of the upload
     */
    private String tid;

    /**
     * TID flag
     */
    private boolean tidFlag = true;

    /**
     * Authentication flag
     */
    private boolean authenticationFlag = true;

    /**
     * The targeted URL
     */
    private URL url;

    /**
     * Retry counter
     */
    private int retryCount = 0;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     *
     * @param content File content to upload
     * @param thumbnail Thumbnail of the file
     * @param listener HTTP transfer event listener
     */
    public HttpUploadManager(MmContent content, byte[] thumbnail, HttpTransferEventListener listener) {
        super(listener);
        
        this.content = content;
        this.thumbnail = thumbnail;
    }

    /**
     * Upload a file
     *
     * @return XML result or null if fails
     */
    public byte[] uploadFile() {
        try {
            if (logger.isActivated()) {
                logger.debug("Upload file " + content.getUrl());
            }

            // Send a first POST request
            HttpPost post = generatePost();
            HttpResponse resp = executeRequest(post);

            // Check response status code
            int statusCode = resp.getStatusLine().getStatusCode();
            if (logger.isActivated()) {
                logger.debug("First POST response " + statusCode);
            }
            switch (statusCode) {
                case 401 :
                    // AUTHENTICATION REQUIRED
                    authenticationFlag = true;
                    break;
                case 204 :
                    // NO CONTENT
                    authenticationFlag = false;
                    break;
                case 503 :
                    // INTERNAL ERROR - check retry-after header
                    Header[] headers = resp.getHeaders("Retry-After");
                    int retryAfter = 0; 
                    if (headers.length > 0) {
                        try {
                            retryAfter = Integer.parseInt(headers[0].getValue());
                        } catch (NumberFormatException e) {
                            // Nothing to do
                        }
                    }
                    if (retryAfter > 0) {
                        try {
                            Thread.sleep(retryAfter * 1000);
                        } catch (InterruptedException e) {
                            // Nothing to do
                        }
                    }
                    // No break to do the retry
                default :
                    // Retry procedure
                    if (retryCount < RETRY_MAX) {
                        retryCount++;
                        return uploadFile();
                    } else {
                        return null;
                    }
            }

            // Send a second POST request
            return sendMultipartPost(resp);
        } catch (Exception e) {
            if (logger.isActivated()) {
                logger.error("Upload file has failed", e);
            }
            return null;
        }
    }

    /**
     * Generate First POST
     *
     * @return POST request
     * @throws MalformedURLException
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    private HttpPost generatePost() throws MalformedURLException, URISyntaxException,
            UnsupportedEncodingException {
        // Check server address
        url = new URL(getHttpServerAddr());
        String protocol = url.getProtocol();    // TODO : exit if not HTTPS
        String host = url.getHost();
        String serviceRoot = url.getPath();

        // Build POST request
        HttpPost post = new HttpPost(new URI(protocol + "://" + host + serviceRoot));
        if (HTTP_TRACE_ENABLED) {
            String trace = ">>> Send HTTP request:";
            trace += "\n" + post.getMethod() + " " + post.getRequestLine().getUri();
            System.out.println(trace);
        }

        // Add Content-disposition with tid
        if (tidFlag) {
            tid = UUID.randomUUID().toString();
            StringEntity entity = new StringEntity(tid);
            entity.setContentType("text/plain");
            post.addHeader("Content-Disposition", "form-data; name=\"tid\"");
            post.setEntity(entity);
            if (HTTP_TRACE_ENABLED) {
                String trace = "Content-Disposition form-data; name=\"tid\"";
                trace += "\n" + tid;
                System.out.println(trace);
            }
        }

        return post;
    }

    /**
     * Create and Send the second POST
     *
     * @param resp response of the first request
     * @return Content of the response
     * @throws CoreException
     * @throws IOException 
     * @throws Exception
     */
    private byte[] sendMultipartPost(HttpResponse resp) throws CoreException, IOException, Exception {
        DataOutputStream outputStream = null;
        String filepath = content.getUrl();

        // Get the connection
        HttpsURLConnection connection = null;
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        connection = (HttpsURLConnection) url.openConnection();

        try {
            connection.setSSLSocketFactory(FileTransSSLFactory.getFileTransferSSLContext().getSocketFactory());
        } catch(Exception e) {
            if (logger.isActivated()) {
                logger.error("Failed to initiate SSL for connection:", e);
            }
        }

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);

        // POST construction
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent", "Joyn");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="
                + BOUNDARY_TAG);

        // Construct the Body
        String body = "";

        // Add tid
        if (tidFlag) {
            body += generateTidMultipart();
        }
        

        // Update authentication agent from response
        if (authenticationFlag) {
            Header[] authHeaders = resp.getHeaders("www-authenticate");
            if (authHeaders.length == 0) {
                throw new IOException("headers malformed in 401 response");
            }
            HttpAuthenticationAgent auth = new HttpAuthenticationAgent(getHttpServerLogin(),
                    getHttpServerPwd());
            auth.readWwwAuthenticateHeader(authHeaders[0].getValue());

            String authValue = auth.generateAuthorizationHeaderValue("post", url.getPath(), body);
            if (authValue != null) {
                connection.setRequestProperty("Authorization", authValue);
            }
        }

        // Trace
        if (HTTP_TRACE_ENABLED) {
            String trace = ">>> Send HTTP request:";
            trace += "\n" + connection.getRequestMethod() + " " + url.toString();
            Set<String> strs = connection.getRequestProperties().keySet();
            Iterator<String> itr = strs.iterator();
            while (itr.hasNext()) {
                Object element = itr.next();
                trace += "\n" + element + ": " + connection.getRequestProperty((String) element);
            }
            trace += "\n" + body;
            System.out.println(trace);
        }

        // Create the DataOutputStream and start writing its body
        outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(body);

        // Add thumbnail
        if (thumbnailFlag && thumbnail != null) {
            writeThumbnailMultipart(outputStream,filepath);
        }

        // Add File
        writreFileMultipart(outputStream, filepath);
        if(!isCancelled())
        {
        	outputStream.writeBytes(twoHyphens + BOUNDARY_TAG + twoHyphens); // if the upload is cancelled, we don't send the last boundary to get bad request
            
	        // Check response status code
	        int responseCode = connection.getResponseCode();
	        if (logger.isActivated()) {
	            logger.debug("Second POST response " + responseCode);
	        }
	        byte[] result = null;
	        boolean success = false;
	        boolean retry = false;
	        if (HTTP_TRACE_ENABLED) {
	            String trace = "<<< Receive HTTP response:";
	            trace += "\n" + connection.getResponseCode() + " " + connection.getResponseMessage();
	            System.out.println(trace);
	        }
	        switch (responseCode) 
	        {
	            case 200 :
	                // 200 OK
	                success = true;
	                InputStream inputStream = connection.getInputStream();
	                result = convertStreamToString(inputStream);
	                inputStream.close();
	                if (HTTP_TRACE_ENABLED) {
	                    System.out.println("\n" + new String(result));
	                }
	                break;
	            case 503 :
	                // INTERNAL ERROR
	                String header = connection.getHeaderField("Retry-After");
	                int retryAfter = 0;
	                if (header != null) {
	                    try {
	                        retryAfter = Integer.parseInt(header);
	                    } catch (NumberFormatException e) {
	                        // Nothing to do
	                    }
	                }
	                if (retryAfter > 0) {
	                    try {
	                        Thread.sleep(retryAfter * 1000);
	                    } catch (InterruptedException e) {
	                        // Nothing to do
	                    }
	                }
	                // No break to do the retry
	            default :
	                // Retry procedure
	                if (retryCount < RETRY_MAX) {
	                    retryCount++;
	                    retry = true;
	                }
	        }

	        // Close streams
	        outputStream.flush();
	        outputStream.close();
	        connection.disconnect();
	        
	        
	        if (success) {
	            return result;
	        } else
	        if (retry) {
	            return sendMultipartPost(resp);
	        } else {
	            throw new IOException("Received " + responseCode + " from server");
	        } 
        } else {
        	// Close streams
            outputStream.flush();
            outputStream.close();
            connection.disconnect();
            
            if (logger.isActivated()) {
	            logger.debug("File transfer cancelled by user");
	        }
            
            return null;
        }
    }

    /**
     * Write the thumbnail multipart
     *
     * @param outputStream DataOutputStream to write to
     * @param filepath File path
     */
    private void writeThumbnailMultipart(DataOutputStream outputStream, String filepath) throws IOException {
    	if(thumbnail.length > 0) {
	        String[] splittedPath = content.getUrl().split("/");
	        String filename = splittedPath[splittedPath.length - 1];

	        outputStream.writeBytes(twoHyphens + BOUNDARY_TAG + lineEnd);
	        outputStream.writeBytes("Content-Disposition: form-data; name=\"Thumbnail\"; filename=\"thumb_"
	                + filename + "\"" + lineEnd);
	        outputStream.writeBytes("Content-Type: " + content.getEncoding() + lineEnd);
	        outputStream.writeBytes("Content-Length: " + thumbnail.length);
	        outputStream.writeBytes(lineEnd + lineEnd);
	        outputStream.write(thumbnail);
	        outputStream.writeBytes(lineEnd);
    	}
    }

    /**
     * Generate the TID multipart
     *
     * @return tid TID header
     */
    private String generateTidMultipart() {
        String tidPartHeader = twoHyphens + BOUNDARY_TAG + lineEnd;
        tidPartHeader += "Content-Disposition: form-data; name=\"tid\"" + lineEnd;
        tidPartHeader += "Content-Type: text/plain" + lineEnd;
        tidPartHeader += "Content-Length: " + tid.length();

        return tidPartHeader + lineEnd + lineEnd + tid + lineEnd;
    }

    /**
     * Write the file multipart
     *
     * @param outputStream DataOutputStream to write to
     * @param filepath File path
     * @throws IOException
     */
    private void writreFileMultipart(DataOutputStream outputStream, String filepath) throws IOException {
        // Check file path
        String[] splittedPath = content.getUrl().split("/");
        String filename = splittedPath[splittedPath.length - 1];

        // Build and write headers
        String filePartHeader = twoHyphens + BOUNDARY_TAG + lineEnd;
        filePartHeader += "Content-Disposition: form-data; name=\"File\"; filename=\"" + filename
                + "\"" + lineEnd;
        filePartHeader += "Content-Type: " + content.getEncoding() + lineEnd;
        File file = new File(filepath);
        filePartHeader += "Content-Length: " + file.length() + lineEnd + lineEnd;
        outputStream.writeBytes(filePartHeader);

        // Write file content
        FileInputStream fileInputStream = new FileInputStream(file);
        int bytesAvailable = fileInputStream.available();
        int bufferSize = Math.min(bytesAvailable, CHUNK_MAX_SIZE);
        byte[] buffer = new byte[bufferSize];
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        int progress = 0;
        while (bytesRead > 0 && !isCancelled()) {
            progress += bytesRead;
            outputStream.write(buffer, 0, bytesRead);
            bytesAvailable = fileInputStream.available(); 
            getListener().httpTransferProgress(progress, file.length());
            bufferSize = Math.min(bytesAvailable, CHUNK_MAX_SIZE);
            buffer = new byte[bufferSize];
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }
        outputStream.writeBytes(lineEnd);
        fileInputStream.close();
    }

    /**
     * Stream conversion
     *
     * @param is Input stream
     * @return Byte array
     */
    private byte[] convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            // Nothing to do
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Nothing to do
            }
        }
        return sb.toString().getBytes();
    }

    /**
     * Blank host verifier
     */
    public class NullHostNameVerifier implements HostnameVerifier {
        /**
         * Verifies that the specified hostname is allowed within the specified SSL session.
         *
         * @param hostname Hostname to check
         * @param session Current SSL session
         * @return Always returns true
         */
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
