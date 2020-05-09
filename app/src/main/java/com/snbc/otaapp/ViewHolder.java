package com.snbc.otaapp;

import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * ViewHolder, the point is weakreference.
 * <p>
 * author: zhougaoxiong
 * date: 2020/5/9,14:06
 * projectName:OTAApp2
 * packageName:com.snbc.otaapp
 */
public class ViewHolder {

    private Button btnControl;
    private TextView totalLength;
    private TextView currentOffset;
    private ProgressBar progressBar;
    //    private TextView downloadSpeed;
    private MainActivity context;
    private WeakReference<MainActivity> weakReferenceContext;

    public ViewHolder(WeakReference<MainActivity> weakReferenceContext, Button btnControl,
                      TextView totalLength, TextView currentOffset, ProgressBar progressBar,
            /*TextView downloadSpeed,*/ MainActivity context) {
        this.weakReferenceContext = weakReferenceContext;
        this.btnControl = btnControl;
        this.totalLength = totalLength;
        this.currentOffset = currentOffset;
        this.progressBar = progressBar;
//        this.downloadSpeed = downloadSpeed;
        this.context = context;
    }

//    private void updateSpeed(int speed) {
//        downloadSpeed.setText(String.format("%dKB/s", speed));
//    }

    public void updateProgress(int soFarM, int percent/*, int speed*/) {

        currentOffset.setText(/*context.getString(R.string.current_offset) + "   " +*/ soFarM + "M");
        progressBar.setProgress(percent);

//        updateSpeed(speed);
    }

    public void setProgress(int percent) {
        progressBar.setProgress(percent);
    }

    public void updateError() {
        btnControl.setText(R.string.download_file_error);
    }

    public void updateCompleted() {
//        btnControl.setText(R.string.download_file_complete);
//        mLlRestartShow.setVisibility(View.VISIBLE);
//
//        new Timer().schedule(task = new TimerTask() {
//            @Override
//            public void run() {
//                context.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTvSure.setText(context.getString(R.string.restart_now) + "(" + n[0]-- + ")");
//                        if ("0".equals(String.valueOf(n[0]))) {
//                            context.reboot();
//                        }
//                    }
//                });
//            }
//        }, 1000, 1000);

    }

    public void updateTotalLength(int totalM) {
        totalLength.setText(/*context.getString(R.string.total_length) + "   " +*/ totalM + "M / ");
    }
}
