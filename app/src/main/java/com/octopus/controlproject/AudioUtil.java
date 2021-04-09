package com.octopus.controlproject;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.FileUtils;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * @Author： zh
 * @Date： 02/12/20
 * @Description： util for audio
 */
public class AudioUtil {

    private MediaRecorder mMediaRecorder;

    public void startRecord(Context context) {

        /* Instantiating MediaRecorder object*/
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            /* ②setAudioSource/set VideoSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// set microphone
            /*
             * ②Set the format of the output file：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP、MPEG-4、RAW_AMR
             * format: MPEG-4
             */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            /*
             * ②Set the encoding of audio files : AAC/AMR_NB/AMR_MB/Default
             */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            /* ③ready */
            File file = CameraUtil.getOutputMediaFile(CameraUtil.MEDIA_TYPE_AUDIO, context);
            //SetOutputFile
            if (Build.VERSION.SDK_INT < 26) {
                //api < 26, use setOutputFile(String path)
                mMediaRecorder.setOutputFile(file.getAbsolutePath());
            } else {
                //API >= 26,  use setOutputFile(File path)
                mMediaRecorder.setOutputFile(file);
            }

            mMediaRecorder.prepare();
            /* ④start to record */
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            Log.e("exception", "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        } catch (IOException e) {
            Log.e("exception", "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        }
    }

    public void stopRecord() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        } catch (RuntimeException e) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

}
