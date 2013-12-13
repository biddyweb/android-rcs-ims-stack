package com.orangelabs.rcs.service;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.StartService;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
//import android.test.InstrumentationTestSuite;
import android.test.MoreAsserts;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;



public class StartServiceTest extends ServiceTestCase<StartService> {
//	   private Logger logger = Logger.getLogger(this.getClass().getName());
	   

	public StartServiceTest() {
		super(StartService.class);
	}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
	       RcsSettings.createInstance(getContext());
	//	RcsSettings rcsSet = RcsSettingsUt.setRcsSettings(RcsSettings.getInstance());
    }

    /**
     * Test basic startup/shutdown of Service
     */
    @SmallTest
    public void testStartable() {
       Intent startIntent = new Intent();  // each one of TestStartable and TestBindable start the service
        startIntent.setClass(getContext(), StartService.class);
        startService(startIntent); 
        assertNotNull(getService());

    }
    
    /**
     * Test binding to service
     */
/*    @MediumTest
    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), StartService.class);
        IBinder service = bindService(startIntent); 
		if (logger.isActivated()) {
			if(service != null) {
				try {
				logger.info("service = " + service.getInterfaceDescriptor());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				logger.info("service is null");  // OK service is null because StartService returns Ibinder null
			}
		}
    }
*/
}
