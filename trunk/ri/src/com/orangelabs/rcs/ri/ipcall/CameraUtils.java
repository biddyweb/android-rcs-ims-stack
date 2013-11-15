package com.orangelabs.rcs.ri.ipcall;

import java.lang.reflect.Method;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.H264Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.CameraOptions;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.Orientation;
import com.orangelabs.rcs.ri.richcall.VisioSharing;
import com.orangelabs.rcs.utils.logger.Logger;

public class CameraUtils {

	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(IPCallView.class.getName());
	

    /* *****************************************
     *                Camera
     ***************************************** */

    /**
     * Get Camera "open" Method
     *
     * @return Method
     */
    private static Method getCameraOpenMethod() {
        ClassLoader classLoader = VisioSharing.class.getClassLoader();
        Class cameraClass = null;
        try {
            cameraClass = classLoader.loadClass("android.hardware.Camera");
            try {
                return cameraClass.getMethod("open", new Class[] {
                    int.class
                });
            } catch (NoSuchMethodException e) {
                return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Get Camera "numberOfCameras" Method
     *
     * @return Method
     */
    private static Method getCameraNumberOfCamerasMethod() {
        ClassLoader classLoader = VisioSharing.class.getClassLoader();
        Class cameraClass = null;
        try {
            cameraClass = classLoader.loadClass("android.hardware.Camera");
            try {
                return cameraClass.getMethod("getNumberOfCameras", (Class[])null);
            } catch (NoSuchMethodException e) {
                return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Get number of cameras
     *
     * @return number of cameras
     */
    public static int getNumberOfCameras() {
        Method method = getCameraNumberOfCamerasMethod();
        if (method != null) {
            try {
                Integer ret = (Integer)method.invoke(null, (Object[])null);
                return ret.intValue();
            } catch (Exception e) {
                return 1;
            }
        } else {
            return 1;
        }
    }

    /**
     * Open a camera
     *
     * @param cameraId
     */
    public static void openCamera(CameraOptions cameraId, IPCallView ipcallView) {
        Method method = getCameraOpenMethod();
        if (ipcallView.numberOfCameras > 1 && method != null) {
            try {
            	ipcallView.camera = (Camera)method.invoke(ipcallView.camera, new Object[] {
                    cameraId.getValue()
                });
            	ipcallView.openedCameraId = cameraId;
            } catch (Exception e) {
            	ipcallView.camera = Camera.open();
            	ipcallView.openedCameraId = CameraOptions.BACK;
            }
        } else {
        	ipcallView.camera = Camera.open();
        	ipcallView.openedCameraId = CameraOptions.BACK;
        }
        if (ipcallView.outgoingVideoPlayer != null) {
        	ipcallView.outgoingVideoPlayer.setCameraId(ipcallView.openedCameraId.getValue());
        }
    }

    /**
     * Check if good camera sizes are available for encoder.
     * Must be used only before open camera.
     * 
     * @param cameraId
     * @return false if the camera don't have the good preview size for the encoder
     */
    public static boolean checkCameraSize(CameraOptions cameraId, IPCallView ipcallView) {
        boolean sizeAvailable = false;

        // Open the camera
        openCamera(cameraId, ipcallView);

        // Check common sizes
        Parameters param = ipcallView.camera.getParameters();
        List<Camera.Size> sizes = param.getSupportedPreviewSizes();
        for (Camera.Size size:sizes) {
            if (    (size.width == H264Config.QVGA_WIDTH && size.height == H264Config.QVGA_HEIGHT) ||
                    (size.width == H264Config.CIF_WIDTH && size.height == H264Config.CIF_HEIGHT) ||
                    (size.width == H264Config.VGA_WIDTH && size.height == H264Config.VGA_HEIGHT)) {
                sizeAvailable = true;
                break;
            }
        }

        // Release camera
        releaseCamera(ipcallView);

        return sizeAvailable;
    }

    /**
     * Start the camera preview
     */
    @SuppressLint("NewApi")
	public static void startCameraPreview(IPCallView ipcallView) {
		if (logger.isActivated()) {
            logger.debug("startCameraPreview()");  
            logger.debug("openedCameraId ="+ipcallView.openedCameraId);  
            logger.debug("Build.VERSION_CODES.FROYO ="+Build.VERSION_CODES.FROYO); 
            logger.debug("Build.VERSION.SDK_INT ="+Build.VERSION.SDK_INT);            
        }
		
        if (ipcallView.camera != null) {
            // Camera settings
            Camera.Parameters p = ipcallView.camera.getParameters();
            p.setPreviewFormat(PixelFormat.YCbCr_420_SP); //ImageFormat.NV21);

            // Orientation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                Display display = ((WindowManager)ipcallView.getSystemService(ipcallView.WINDOW_SERVICE)).getDefaultDisplay();
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        if (logger.isActivated()) {
                            logger.debug("ROTATION_0");
                        }
                        if (ipcallView.openedCameraId == CameraOptions.FRONT) {
                        	ipcallView.outgoingVideoPlayer.setOrientation(Orientation.ROTATE_90_CCW);
                        } else {
                        	ipcallView.outgoingVideoPlayer.setOrientation(Orientation.ROTATE_90_CW);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                        	ipcallView.camera.setDisplayOrientation(90);
                        } else {
                            p.setRotation(90);
                        }
                        break;
                    case Surface.ROTATION_90:
                        if (logger.isActivated()) {
                            logger.debug("ROTATION_90");
                        }
                        ipcallView.outgoingVideoPlayer.setOrientation(Orientation.NONE);
                        break;
                    case Surface.ROTATION_180:
                        if (logger.isActivated()) {
                            logger.debug("ROTATION_180");
                        }
                        if (ipcallView.openedCameraId == CameraOptions.FRONT) {
                        	ipcallView.outgoingVideoPlayer.setOrientation(Orientation.ROTATE_90_CW);
                        } else {
                        	ipcallView.outgoingVideoPlayer.setOrientation(Orientation.ROTATE_90_CCW);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                        	ipcallView.camera.setDisplayOrientation(270);
                        } else {
                            p.setRotation(270);
                        }
                        break;
                    case Surface.ROTATION_270:
                        if (logger.isActivated()) {
                            logger.debug("ROTATION_270");
                        }
                        if (ipcallView.openedCameraId == CameraOptions.FRONT) {
                            //outgoingVideoPlayer.setOrientation(Orientation.ROTATE_180);
                        	ipcallView.outgoingVideoPlayer.setOrientation(Orientation.ROTATE_90_CCW);
                        } else {
                            //outgoingVideoPlayer.setOrientation(Orientation.ROTATE_180);
                        	ipcallView.outgoingVideoPlayer.setOrientation(Orientation.ROTATE_90_CW);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                            //camera.setDisplayOrientation(180);
                        	ipcallView.camera.setDisplayOrientation(90);
                        } else {
                            p.setRotation(90);

                        }
                        break;
                }
            } else {
                // getRotation not managed under Froyo
            	ipcallView.outgoingVideoPlayer.setOrientation(Orientation.NONE);
            }

            // Camera size
            List<Camera.Size> sizes = p.getSupportedPreviewSizes();
            if (sizeContains(sizes, ipcallView.outgoingWidth, ipcallView.outgoingHeight)) {
                // Use the existing size without resizing
                p.setPreviewSize(ipcallView.outgoingWidth, ipcallView.outgoingHeight);
                ipcallView.outgoingVideoPlayer.activateResizing(ipcallView.outgoingWidth, ipcallView.outgoingHeight); // same size = no resizing
                if (logger.isActivated()) {
                	logger.debug("Use the existing size without resizing");
                    logger.debug("Camera preview initialized with size " + ipcallView.outgoingWidth + "x" + ipcallView.outgoingHeight);
                }
            } else {
                // Check if can use a other known size (QVGA, CIF or VGA)
                int w = 0;
                int h = 0;
                for (Camera.Size size:sizes) {
                    w = size.width;
                    h = size.height;
                    if (    (w == H264Config.QVGA_WIDTH && h == H264Config.QVGA_HEIGHT) ||
                            (w == H264Config.CIF_WIDTH && h == H264Config.CIF_HEIGHT) ||
                            (w == H264Config.VGA_WIDTH && h == H264Config.VGA_HEIGHT)) {
                        break;
                    }
                }

                if (w != 0) {
                    p.setPreviewSize(w, h);
                    ipcallView.outgoingVideoPlayer.activateResizing(w, h);
                    if (logger.isActivated()) {
                        logger.debug("Camera preview initialized with size " + w + "x" + h + " with a resizing to " + ipcallView.outgoingWidth + "x" + ipcallView.outgoingHeight);
                        logger.debug("outGoingVideoView size ="+ipcallView.outgoingVideoView.getWidth()+"x"+ipcallView.outgoingVideoView.getHeight());
                    }
                } else {
                    // The camera don't have known size, we can't use it
                    if (logger.isActivated()) {
                        logger.debug("Camera preview can't be initialized with size " + ipcallView.outgoingWidth + "x" + ipcallView.outgoingHeight);
                    }
                    ipcallView.camera = null;
                    return;
                }
            }
         
            ipcallView.camera.setParameters(p);
            try {
            	ipcallView.camera.setPreviewDisplay(ipcallView.surface);
            	ipcallView.camera.startPreview();
            	ipcallView.cameraPreviewRunning = true;
            } catch (Exception e) {
            	ipcallView.camera = null;
            }
        }
    }

    /**
     * Release the camera
     */
    public static void releaseCamera(IPCallView ipcallView) {
        if (ipcallView.camera != null) {
        	ipcallView.camera.setPreviewCallback(null);
            if (ipcallView.cameraPreviewRunning) {
            	ipcallView.cameraPreviewRunning = false;
            	ipcallView.camera.stopPreview();
            }
            ipcallView.camera.release();
            ipcallView.camera = null;
        }
    }

    /**
     * Test if size is in list.
     * Can't use List.contains because it doesn't work with some devices.
     *
     * @param list
     * @param width
     * @param height
     * @return boolean
     */
    private static boolean sizeContains(List<Camera.Size> list, int width, int height) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).width == width && list.get(i).height == height) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start the camera
     */
    public static void startCamera(IPCallView ipcallView) {
    	if (logger.isActivated()) {
            logger.info("startCamera()");
        }
        if (ipcallView.camera == null) {
            // Open camera
            openCamera(ipcallView.openedCameraId, ipcallView);
            if (logger.isActivated()) {
                logger.debug("camera is null - openedCameraId ="+ipcallView.openedCameraId);
            }
            if (ipcallView.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            	ipcallView.outgoingVideoView.setAspectRatio(ipcallView.outgoingWidth, ipcallView.outgoingHeight);
                if (logger.isActivated()) {
                    logger.debug("Landscape mode - "+ipcallView.outgoingWidth+"x"+ipcallView.outgoingHeight);
                }
            } else {
            	ipcallView.outgoingVideoView.setAspectRatio(ipcallView.outgoingHeight, ipcallView.outgoingWidth);
                if (logger.isActivated()) {
                    logger.debug("Portrait mode - outGoingWidth ="+ipcallView.outgoingWidth+"- outGoingHeith ="+ipcallView.outgoingHeight);
                    logger.debug("outGoingVideoView size ="+ipcallView.outgoingVideoView.getWidth()+"x"+ipcallView.outgoingVideoView.getHeight());
                }
            }
            // Start camera
            ipcallView.camera.setPreviewCallback(ipcallView.outgoingVideoPlayer);
            startCameraPreview(ipcallView);
        } else {
            if (logger.isActivated()) {
                logger.debug("Camera is not null");
            }
        }
    }

    /**
     * ReStart the camera
     */
    public static void reStartCamera(IPCallView ipcallView) {
        if (ipcallView.camera != null) {
            releaseCamera(ipcallView);
        }
        startCamera(ipcallView);
    }
	
}
