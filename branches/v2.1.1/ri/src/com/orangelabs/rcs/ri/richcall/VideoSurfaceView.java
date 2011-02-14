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
package com.orangelabs.rcs.ri.richcall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Video preview
 * 
 * @author jexa7410
 */
public class VideoSurfaceView extends SurfaceView {
    
	/**
	 * Display area aspect ratio
	 */
	private float mAspectRatio;
	
	/**
     * Setting the aspect ratio to this value means to not enforce an aspect ratio.
     */
    public static float DONT_CARE = 0.0f;
	
	/**
	 * Canvas for rendering bitmap images
	 */
    private Canvas c;
    
	/**
	 * Surface properties
	 */
	private boolean surfaceCreated = false;
	private boolean surfaceChanged = false;
	
	/**
	 * Surface holder
	 */
	private SurfaceHolder holder;

	/**
	 * Constructor
	 * 
	 * @param context Context
	 */
    public VideoSurfaceView(Context context) {
        super(context);
        init();
    }
    
    /**
     * Constructor
     * 
     * @param context Context
     * @param attrs Attributes
     */
    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    /**
     * Constructor
     * 
     * @param context Context
     * @param attrs Attributes
     * @param defStyle Style
     */
    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Set aspect ration according to desired width and height
     * 
     * @param width Width
     * @param height Height
     */
    public void setAspectRatio(int width, int height) {
        setAspectRatio(((float) width) / ((float) height));
    }
    
    /**
     * Set aspect ratio
     * 
     * @param aspectRatio Ratio
     */
    public void setAspectRatio(float aspectRatio) {
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Ensure aspect ratio
     * 
     * @param widthMeasureSpec Width
     * @param heightMeasureSpec Heigh
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAspectRatio != DONT_CARE) {
            int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

            int width = widthSpecSize;
            int height = heightSpecSize;

            if (width > 0 && height > 0) {
                float defaultRatio = ((float) width) / ((float) height);
                if (defaultRatio < mAspectRatio) {
                    // Need to reduce height
                    height = (int) (width / mAspectRatio);
                } else if (defaultRatio > mAspectRatio) {
                    width = (int) (height * mAspectRatio);
                }
                width = Math.min(width, widthSpecSize);
                height = Math.min(height, heightSpecSize);
                setMeasuredDimension(width, height);
                return;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    /**
	 * Set bitmap from RGB
	 * 
	 * @param image Bitmap
	 */
	public void setImage(Bitmap image){	
		if (surfaceCreated && surfaceChanged){
			c = null;
			try {				
				synchronized (holder) {
					c = holder.lockCanvas();					
				}							
			} finally {
				if (c != null){
					// First clear screen
					c.drawARGB(255, 0, 0, 0);
					
					// Then draw bmp
					c.drawBitmap(image, null, c.getClipBounds(), null);
					holder.unlockCanvasAndPost(c);
				}
			}
		}
	}
	
	/**
	 * Init routine
	 */
	private void init() {
		holder = this.getHolder();
        holder.addCallback(mCallback);
	}
	
	/**
	 * Surface holder callback
	 */
	SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder _holder, int format, int w,int h) {
			surfaceChanged = true;
		}

		public void surfaceCreated(SurfaceHolder _holder) {
			surfaceCreated = true;
		}

		public void surfaceDestroyed(SurfaceHolder _holder) {
			surfaceCreated = false;
		}
	};
}
