package com.orangelabs.rcs.database.fthttp;

import java.util.List;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.net.Uri;
import android.test.InstrumentationTestCase;

import com.orangelabs.rcs.core.content.ContentManager;
import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.provider.fthttp.FtHttpResume;
import com.orangelabs.rcs.provider.fthttp.FtHttpResumeDao;
import com.orangelabs.rcs.provider.fthttp.FtHttpResumeDaoImpl;
import com.orangelabs.rcs.provider.fthttp.FtHttpResumeDownload;
import com.orangelabs.rcs.provider.fthttp.FtHttpResumeUpload;
import com.orangelabs.rcs.provider.fthttp.FthttpColumns;
import com.orangelabs.rcs.provider.fthttp.Status;
import com.orangelabs.rcs.utils.logger.Logger;

public class FtHttpTest extends InstrumentationTestCase {

	static final private Logger logger = Logger.getLogger(FtHttpTest.class.getSimpleName());
	private String contact = "contact";
	private String file = "filename";
	private String displayName="displayName";
	private String tid = "tid";
	private String chatId="chatId";
	private String sessionId="sessionId";
	private String participants="participant1;participant2";
	private FtHttpResumeDao fthttp;
	private ContentResolver mContentResolver;
	private ContentProviderClient mProvider;
	private byte[] thumbnail = new byte[] { 0x00, 0x01 };
	private MmContent content;

	protected void setUp() throws Exception {
		super.setUp();
		mContentResolver = getInstrumentation().getTargetContext().getContentResolver();
		mProvider = mContentResolver.acquireContentProviderClient(FthttpColumns.CONTENT_URI);
		fthttp = FtHttpResumeDaoImpl.createInstance(getInstrumentation().getTargetContext());
		content = ContentManager.createMmContentFromFilename("filename", "url", 1023);
		if (logger.isActivated()) {
			logger.debug("SetUp mProvider = " + mProvider + " " + FthttpColumns.CONTENT_URI);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFtHttpProvider() {
		fthttp.deleteAll();
		FtHttpResumeUpload upload = new FtHttpResumeUpload(file,thumbnail,tid,contact,displayName,chatId,sessionId, participants);
		FtHttpResumeDownload download = new FtHttpResumeDownload(file, thumbnail, content, contact, displayName, chatId, sessionId, participants);
		Uri uri = null;
		try {
			uri = fthttp.insert(upload, Status.CREATED);
			if (logger.isActivated()) {
				logger.debug("addUploadForContact result URI = " + uri);
			}
			uri = null;
			uri = fthttp.insert(download);
			if (logger.isActivated()) {
				logger.debug("addUploadForContact result URI = " + uri);
			}
		} catch (Exception e) {
			if (logger.isActivated()) {
				logger.error("addUploadForContact failed" + e);
			}
		}
		assertNotNull("insert failed", uri);

		Status status = fthttp.getStatus(upload);
		assertEquals("getStatus failed", Status.CREATED, status);

		status = fthttp.getStatus(download);
		assertEquals("getStatus failed", Status.STARTED, status);

		fthttp.setStatus(upload, Status.FAILURE);
		status = fthttp.getStatus(upload);
		assertEquals("getStatus failed", Status.FAILURE, status);

		fthttp.setStatus(download, Status.FAILURE);
		status = fthttp.getStatus(download);
		assertEquals("getStatus failed", Status.FAILURE, status);

		FtHttpResume ftHttpResume = fthttp.queryOldest(Status.FAILURE);
		assertTrue("queryOldest failed", ftHttpResume instanceof FtHttpResumeUpload
				& ((FtHttpResumeUpload) ftHttpResume).getTid().equals("tid"));

		List<FtHttpResume> list = fthttp.queryAll();
		assertTrue("queryAll failed", list != null && !list.isEmpty());
		if (logger.isActivated()) {
			for (FtHttpResume item : list) {
				logger.debug(item.toString() + " status=" + fthttp.getStatus(item));
			}
		}

		int rowDeleted = fthttp.delete(upload);
		assertEquals("delete failed", rowDeleted, 1);
		rowDeleted = fthttp.delete(download);
		assertEquals("delete failed", rowDeleted, 1);
	}
}
