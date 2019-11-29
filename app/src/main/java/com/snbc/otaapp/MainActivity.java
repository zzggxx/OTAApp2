package com.snbc.otaapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = MainActivity.class.getName();
    //    size:1.4G
    private static final String url = "http://pztmwtcsl.bkt.clouddn.com/android_r7.1.2_rk3399_snbc_lvds_box_191022.img";
    //    size:690M
    private static final String url1 = "http://pztmwtcsl.bkt.clouddn.com/system.img";
    //    company internet,size 1.4G
    private static final String url2 = "http://192.168.188.4/test/update.zip";

    FileDownloadListener mListener = new FileDownloadListener() {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

        }

        @Override
        protected void connected(BaseDownloadTask task, String etag, boolean isContinue,
                                 int soFarBytes, int totalBytes) {

            mTotalM = totalBytes / 1024 / 1024;
//            Log.i(TAG, "connected: ");

            mHolder.updateTotalLength(mTotalM);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            mSoFarM = soFarBytes / 1024 / 1024;
//            Log.i(TAG, "progress: " + soFarM);

            mPercent = (int) (100 * (soFarBytes / (double) totalBytes));
//            Log.i(TAG, "percenter: " + percenter);

            mHolder.updateProgress(mSoFarM, mPercent, task.getSpeed());
        }

        @Override
        protected void blockComplete(BaseDownloadTask task) {
        }

        @Override
        protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes,
                             final int soFarBytes) {
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            mHolder.updateCompleted();
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            mHolder.updatePaused();
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
//            Log.i(TAG, "error: "  + e);
            mHolder.updateError();
        }

        @Override
        protected void warn(BaseDownloadTask task) {
        }
    };
    private Button mBtn;
    private int mDownloadId;
    private ViewHolder mHolder;
    private int mPercent;
    private SharedPreferences mSp;
    private int mTotalM;
    private int mSoFarM;
    private static LinearLayout mLlRestartShow;
    private static TextView mTvSure;
    private static final int[] n = {10};
    private static TimerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String updateurl = Utils.getProperty("updateurl", "");
        if (!TextUtils.isEmpty(updateurl)) {
            initView();
        } else {
            Toast.makeText(this, R.string.please_checkout_update_url, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initView() {
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.progressbar);

        TextView download_speed = (TextView) findViewById(R.id.download_speed);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(this);
        TextView total_length = (TextView) findViewById(R.id.total_length);
        TextView current_offset = (TextView) findViewById(R.id.current_offset);

        mHolder = new ViewHolder(new WeakReference<>(this), mBtn, total_length,
                current_offset, progressbar, download_speed, this);

        mSp = getSharedPreferences("snbc_update", Context.MODE_PRIVATE);
        int percent = mSp.getInt("percent", 0);
        progressbar.setProgress(percent);
        int totalLength = mSp.getInt("totalLength", 0);
        total_length.setText(getString(R.string.total_length) + "   " + totalLength + "M");
        int soFarLength = mSp.getInt("soFarLength", 0);
        current_offset.setText(getString(R.string.current_offset) + "   " + soFarLength + "M");

        findViewById(R.id.tv_cancel).setOnClickListener(this);
        mTvSure = (TextView) findViewById(R.id.tv_sure);
        mTvSure.setOnClickListener(this);
        mLlRestartShow = (LinearLayout) findViewById(R.id.ll_restart_show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSp = getSharedPreferences("snbc_update", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSp.edit();
        editor.putInt("percent", mPercent);
        editor.putInt("totalLength", mTotalM);
        editor.putInt("soFarLength", mSoFarM);
        editor.commit();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn:
                if (getString(R.string.download_file_start).equals(mBtn.getText().toString())) {
                    mDownloadId = createDownloadTask().start();
                    mBtn.setText(R.string.download_file_pause);

                } else if (getString(R.string.download_file_continue).equals(mBtn.getText().toString())) {
                    createDownloadTask().start();
                    mBtn.setText(R.string.download_file_pause);

                } else if (getString(R.string.download_file_pause).equals(mBtn.getText().toString()) ||
                        getString(R.string.download_file_error).equals(mBtn.getText().toString())) {

                    FileDownloader.getImpl().pause(mDownloadId);
                    mBtn.setText(R.string.download_file_continue);

                }
                break;
            case R.id.tv_cancel:
                mLlRestartShow.setVisibility(View.GONE);
                task.cancel();
                break;
            case R.id.tv_sure:
                reboot();
                break;
            default:
                break;
        }
    }

    private BaseDownloadTask createDownloadTask() {
        return FileDownloader.getImpl().create(url2)
                .setPath(getPath())
                //update download progress
                .setCallbackProgressTimes(1500)
                //update download speed;
                .setMinIntervalUpdateSpeed(1500)
                //auto retry times 20
                .setAutoRetryTimes(20)
                .setListener(mListener);
    }

    private String getPath() {
        File mnt = Environment.getExternalStorageDirectory();
        File file1 = new File(mnt, "update");
        file1.mkdir();
        return file1.getAbsoluteFile() + File.separator + "update.zip";
    }

    public void reboot() {
//        try {
//            Process process = Runtime.getRuntime().exec("su");
//            DataOutputStream out = new DataOutputStream(process.getOutputStream());
//            out.writeBytes("reboot recovery\n");
//            out.writeBytes("exit\n");
//            out.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Intent intent = new Intent();
        intent.setAction("com.update.firmware");
        sendBroadcast(intent);

    }

    //    ViewHolder, the point is weakreference.
    private static class ViewHolder {

        private Button btnControl;
        private TextView totalLength;
        private TextView currentOffset;
        private ProgressBar progressBar;
        private TextView downloadSpeed;
        private MainActivity context;
        private WeakReference<MainActivity> weakReferenceContext;

        public ViewHolder(WeakReference<MainActivity> weakReferenceContext, Button btnControl, TextView totalLength,
                          TextView currentOffset, ProgressBar progressBar, TextView downloadSpeed, MainActivity context) {
            this.weakReferenceContext = weakReferenceContext;
            this.btnControl = btnControl;
            this.totalLength = totalLength;
            this.currentOffset = currentOffset;
            this.progressBar = progressBar;
            this.downloadSpeed = downloadSpeed;
            this.context = context;
        }

        private void updateSpeed(int speed) {
            downloadSpeed.setText(String.format("%dKB/s", speed));
        }

        public void updateProgress(int soFarM, int percent, int speed) {

            currentOffset.setText(context.getString(R.string.current_offset) + "   " + soFarM + "M");
            progressBar.setProgress(percent);

            updateSpeed(speed);
        }

        public void updatePaused() {
        }

        public void updateError() {
            btnControl.setText(R.string.download_file_error);
        }

        public void updateCompleted() {
            btnControl.setText(R.string.download_file_complete);
            mLlRestartShow.setVisibility(View.VISIBLE);

            new Timer().schedule(task = new TimerTask() {
                @Override
                public void run() {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvSure.setText(context.getString(R.string.restart_now) + "(" + n[0]-- + ")");
                            if ("0".equals(String.valueOf(n[0]))) {
                                context.reboot();
                            }
                        }
                    });
                }
            }, 1000, 1000);

        }

        public void updateTotalLength(int totalM) {
            totalLength.setText(context.getString(R.string.total_length) + "   " + totalM + "M");
        }
    }

}
