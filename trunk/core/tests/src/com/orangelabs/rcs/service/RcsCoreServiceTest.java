package com.orangelabs.rcs.service;


import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.RcsCoreService;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class RcsCoreServiceTest extends ServiceTestCase<RcsCoreService> {
	public RcsCoreServiceTest() {
		super(RcsCoreService.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	       RcsSettings.createInstance(getContext());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

    @SmallTest
    public void testStartable() {
     Intent startIntent = new Intent();
        startIntent.setClass(getContext(), RcsCoreService.class);
        startService(startIntent); 
               assertNotNull(getService());
     }
    
    /**
     * Test binding to service
     */
    @MediumTest
    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), RcsCoreService.class);
        startIntent.setAction("com.orangelabs.rcs.service.api.client.IImsApi");
        IBinder service = bindService(startIntent); 
         assertNotNull(service);
    }
 
}
