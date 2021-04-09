package com.octopus.controlproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * @Author： zh
 * @Date： 08/12/20 7:27 PM
 * @Description：
 */
public class CameraUtil {
    private MediaRecorder mediarecorder;// android MediaRecorder
    //    private SurfaceView, monitor the lifecycle of the Surface
    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    //    private long recordTime;
    private long startTime = Long.MIN_VALUE;
    private long endTime = Long.MIN_VALUE;
    private HashMap<String, String> map = new HashMap<String, String>();
    private static final String TAG = "SEDs508EG";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_AUDIO = 3;

    private Context context;

    public CameraUtil(SurfaceHolder surfaceHolder, Context context) {
//        this.recordTime = recordTime;
//        this.surfaceview = surfaceview;
        this.surfaceHolder = surfaceHolder;
        this.context = context;
    }

    /**
     * Get the camera instance object @return
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (Exception e) {
            // failed to open
            Log.i("info", "failed to open ");
        }
        return c;
    }

    private int cameraId;

    /**
     * Open the camera according to the type
     * @return the object of the currently open camera: frontIndex
     */
    private Camera openCamera() {
        int frontIndex = -1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();

        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontIndex = cameraIndex;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backIndex = cameraIndex;
            }
        }

        if (backIndex != -1) {
            cameraId = backIndex;
            return Camera.open(backIndex);
        } else {
            cameraId = frontIndex;
            return Camera.open(frontIndex);
        }
    }

    /**
     * initCamera
     */
    public void initCamera() {
        Log.e("TAG", "initCamera");
        try {
            //if Camera is null, open it
            if (mCamera == null) {
                mCamera = openCamera();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        Camera.Parameters parameters;
        try {
            //Get various resolutions for preview
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        parameters.setPreviewSize(480, 800);
        // set image format
        parameters.setPictureFormat(PixelFormat.JPEG);
        //set format of previewImage
        parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
        setCameraDisplayOrientation(mCamera);
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            e.printStackTrace();
            return;
        }
        mCamera.startPreview();

        takePhoto();
    }

    /**
     * Realize the camera function
     */
    public void takePhoto() {
        Camera.Parameters parameters;
        try {
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //Get the resolution of camera. Not sure if the camera array is in descending or ascending order
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        int size = 0;
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).width >= 480) {
                //perfect match
                size = i;
                break;
            } else {
                //Find the closest one
                size = i;
            }
        }

        //Set the photo resolution, choose within the range supported by the camera
        parameters.setPictureSize(list.get(size).width, list.get(size).height);
        //Set camera parameters
        mCamera.setParameters(parameters);

        //use takePicture()
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            //Take a picture after auto focus is completed
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success && camera != null) {
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        //Save files FileOutputStream
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            File file = getOutputMediaFile(MEDIA_TYPE_IMAGE, context);
                            FileOutputStream fileOutputStream;
                            try {
                                fileOutputStream = new FileOutputStream(file);
                                fileOutputStream.write(data);
                                fileOutputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "onPictureTaken: save failure " + e);
                            }

                            //stop preview
                            mCamera.stopPreview();
                            mCamera.release();
                            mCamera = null;
                        }
                    });
                }
            }
        });
    }

    /**
     * Set the rotation angle
     * @param camera
     */
    private void setCameraDisplayOrientation(Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * Start to record Video
     */
    public void startRecord() {
//        mCamera = getCameraInstance(); // get camera
        mCamera = openCamera();
        mCamera.unlock();

        CamcorderProfile mProfile;
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
            mProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
//            Toast.makeText(context, "profile 1080", Toast.LENGTH_LONG).show();
        } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_2160P)) {
            mProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_2160P);
//            Toast.makeText(context, "profile 2160", Toast.LENGTH_LONG).show();
        } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
            mProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
//            Toast.makeText(context, "profile 480", Toast.LENGTH_LONG).show();
        } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
            mProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
//            Toast.makeText(context, "profile 720", Toast.LENGTH_LONG).show();
        } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_CIF)) {
            mProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_CIF);
//            Toast.makeText(context, "profile cif", Toast.LENGTH_LONG).show();
        } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH)) {
            mProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
//            Toast.makeText(context, "profile high", Toast.LENGTH_LONG).show();
        } else {
            mProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
//            Toast.makeText(context, "profile QUALITY_QVGA", Toast.LENGTH_LONG).show();
        }

        mediarecorder = new MediaRecorder();// Create media-recorder object
        mediarecorder.setCamera(mCamera); //  from Camera record video

        mediarecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediarecorder.setOutputFormat(mProfile.fileFormat);
        mediarecorder.setAudioEncoder(mProfile.audioCodec);
        mediarecorder.setVideoEncoder(mProfile.videoCodec);
        mediarecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        mediarecorder.setVideoFrameRate(mProfile.videoFrameRate);
        mediarecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
        mediarecorder.setAudioEncodingBitRate(mProfile.audioBitRate);
        mediarecorder.setAudioChannels(mProfile.audioChannels);
        mediarecorder.setAudioSamplingRate(mProfile.audioSampleRate);

        mediarecorder.setPreviewDisplay(surfaceHolder.getSurface()); // Set the output-path of the video file

        mediarecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO, context).toString());
        try { // prepare to record
            mediarecorder.prepare(); // start
            mediarecorder.start();
            // time.setVisibility(View.VISIBLE);// set recordTime view
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void releaseMediaRecorder() {
        if (mediarecorder != null) {
            // Clear recorder configuration
            mediarecorder.reset();
            // release recorder object
            mediarecorder.release();
            mediarecorder = null;
            // lock camera
            mCamera.lock();
        }
    }


    /**
     * Stop to record
     */
    public void stopRecord() {
        System.out.println("--------------");

        if (mediarecorder != null) {
            // 停止录制
            mediarecorder.stop();
            // 释放资源
            mediarecorder.release();
            mediarecorder = null;
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        }
    }


    /**
     * Timer * @author bcaiw *
     */
    public class TimerThread extends TimerTask {
        /**
         * stop
         */
        @Override
        public void run() {
            try {
                stopRecord();
                this.cancel();
            } catch (Exception e) {
                map.clear();
                map.put("recordingFlag", "false");
                String ac_time = getVedioRecordTime();// recordTime
                map.put("recordTime", ac_time);
                // sendMsgToHandle(m_msgHandler, iType, map);
            }

        }
    }

    /**
     *
     * @param handle
     * @param iType
     * @param info
     */
    public void sendMsgToHandle(Handler handle, int iType,
                                Map<String, String> info) {
        Message threadMsg = handle.obtainMessage();
        threadMsg.what = iType;
        Bundle threadbundle = new Bundle();
        threadbundle.clear();
        for (Iterator i = info.keySet().iterator(); i.hasNext(); ) {
            Object obj = i.next();
            threadbundle.putString(obj.toString(), info.get(obj));
        }
        threadMsg.setData(threadbundle);
        handle.sendMessage(threadMsg);

    }

    /**
     * Calculate the current recorded time, the default value returns 0
     *
     * @return
     */
    public String getVedioRecordTime() {
        String result = "0";
        if (startTime != Long.MIN_VALUE && endTime != Long.MIN_VALUE) {
            long tempTime = (endTime - startTime);
            result = String.valueOf(tempTime);
        }
        return result;

    }

    public static File getMediaStorageDir(Context context) {
//        return new File(Environment.getExternalStorageDirectory() + File.separator + "/RVideo/");
//        return Environment.getDataDirectory();
//        return new File("/mnt/external_sd/RVideo");
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }

    public static File getOutputMediaFile(int type, Context context) {
        // whether SDCard exists
        if (!Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            Log.d(TAG, "SDCard is not exist");
            return null;
        }
        // whether file exists
        File mediaStorageDir = getMediaStorageDir(context);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        //Create media file name
        String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timestamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + timestamp + ".mp4");
        } else if (type == MEDIA_TYPE_AUDIO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + timestamp + ".m4a");
        } else {
            Log.d(TAG, "not correct file");
            return null;
        }

        return mediaFile;
    }
}

