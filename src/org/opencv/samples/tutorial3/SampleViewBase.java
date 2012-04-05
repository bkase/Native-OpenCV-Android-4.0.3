package org.opencv.samples.tutorial3;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public abstract class SampleViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "Sample::SurfaceView";

    private Camera              mCamera;
    private SurfaceHolder       mHolder;
    private int                 mFrameWidth;
    private int                 mFrameHeight;
    private byte[]              mFrame;
    private boolean             mThreadRun;

    public SampleViewBase(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    
    public SampleViewBase(Context context, AttributeSet attrs) { 
        super(context, attrs); 
        mHolder = getHolder(); 
        mHolder.addCallback(this); 
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
        Log.i(TAG, "Instantiated new " + this.getClass()); 
    }

    public int getFrameWidth() {
        return mFrameWidth;
    }

    public int getFrameHeight() {
        return mFrameHeight;
    }

    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        Log.i(TAG, "surfaceCreated");
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            mFrameWidth = width;
            mFrameHeight = height;

            // selecting optimal camera preview size
            {
                double minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - height) < minDiff) {
                        mFrameWidth = size.width;
                        mFrameHeight = size.height;
                        minDiff = Math.abs(size.height - height);
                    }
                }
            }

            params.setPreviewSize(getFrameWidth(), getFrameHeight());
            mCamera.setParameters(params);
            try {
				mCamera.setPreviewTexture(new SurfaceTexture(10));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				Log.e(TAG, "mCamera.setPreviewDisplay fails: " + e1);
			} 
            /*try {
				mCamera.setPreviewDisplay(null);
			} catch (IOException e) {
				Log.e(TAG, "mCamera.setPreviewDisplay fails: " + e);
			}*/
            try { 
                Log.i(TAG, "start preview"); 
                mCamera.startPreview(); 
                Log.i(TAG, "started preview"); 
            } catch (RuntimeException e){Log.e(TAG, "mCamera.startPreview() failed");} 
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        mCamera = Camera.open();
        mCamera.setPreviewCallback(new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                synchronized (SampleViewBase.this) {
                    mFrame = data;
                    SampleViewBase.this.notify();
                }
            }
        });
        (new Thread(this)).start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        mThreadRun = false;
        if (mCamera != null) {
            synchronized (this) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        }
    }

    protected abstract Bitmap processFrame(byte[] data);

    public void run() {
        mThreadRun = true;
        Log.i(TAG, "Starting processing thread");
        SurfaceView trueview = (SurfaceView) ((View)getParent()).findViewById(R.id.cvsurface); 
        while (mThreadRun) {
            Bitmap bmp = null;

            synchronized (this) {
                try {
                    this.wait();
                    bmp = processFrame(mFrame);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (bmp != null) {
                Canvas canvas = trueview.getHolder().lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(bmp, (canvas.getWidth() - getFrameWidth()) / 2, (canvas.getHeight() - getFrameHeight()) / 2, null);
                    trueview.getHolder().unlockCanvasAndPost(canvas);
                }
                bmp.recycle();
            }
        }
    }
}