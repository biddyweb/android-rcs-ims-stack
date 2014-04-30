package com.orangelabs.rcs.database.backup_restore;

import java.io.File;

import javax2.sip.InvalidArgumentException;

import android.os.Environment;
import android.test.AndroidTestCase;

import com.orangelabs.rcs.provider.BackupRestoreDb;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.FileUtils;
import com.orangelabs.rcs.utils.logger.Logger;

public class DbBackupRestoreTest extends AndroidTestCase {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	protected void setUp() throws Exception {
		logger.info("Set up start");
		super.setUp();
		RcsSettings.createInstance(getContext());
		File srcdir = new File(Environment.getDataDirectory() + "/data/com.orangelabs.rcs/databases");
		File[] list = null;
		try {
			list = BackupRestoreDb.listOfSavedAccounts(srcdir);
			for (File file : list) {
				FileUtils.deleteDirectory(file);
			}
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		}
		logger.info("Set up end");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testBackupRestoreDb() {
		logger.info("testBackupRestoreDb start");
		File srcdir = new File(Environment.getDataDirectory() + "/data/com.orangelabs.rcs/databases");
		assertTrue(BackupRestoreDb.backupAccount("1111"));
		assertTrue(BackupRestoreDb.backupAccount("2222"));
		assertTrue(BackupRestoreDb.backupAccount("3333"));
		assertTrue(BackupRestoreDb.backupAccount("4444"));
		File[] list = null;
		try {
			list = BackupRestoreDb.listOfSavedAccounts(srcdir);
			for (File file : list) {
				logger.info("account " + file);
			}
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		}
		assertTrue("listOfSavedAccounts failed", list != null && list.length == 4);
		assertTrue("getOldestFile failed", FileUtils.getOldestFile(list).getName().equals("1111"));
		assertTrue("restoreAccountProviders failed", BackupRestoreDb.restoreAccount("2222"));
		BackupRestoreDb.cleanBackups("3333");
		try {
			FileUtils.deleteDirectory(new File(srcdir, "2222"));
			FileUtils.deleteDirectory(new File(srcdir, "3333"));
			FileUtils.deleteDirectory(new File(srcdir, "4444"));
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		}
		try {
			list = BackupRestoreDb.listOfSavedAccounts(srcdir);
			logger.error( " test backup"+ list.toString());
			assertTrue("listOfSavedAccounts failed", list == null || list.length == 0);
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
		}
		logger.info("testBackupRestoreDb end");
	}

}
