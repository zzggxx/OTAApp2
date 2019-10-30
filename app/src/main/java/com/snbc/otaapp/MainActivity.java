package com.snbc.otaapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = MainActivity.class.getName();
    //    size:1.4G
    private static final String url = "http://pztmwtcsl.bkt.clouddn.com/android_r7.1.2_rk3399_snbc_lvds_box_191022.img";
    //    size:690M
    private static final String url1 = "http://pztmwtcsl.bkt.clouddn.com/system.img";
    //    company internet,size 1.4G
    private static final String url2 = "http://192.168.188.4/test/snbc_rk3399.img";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
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
            default:
                break;
        }
    }

    private BaseDownloadTask createDownloadTask() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SNBCUpdate.img";
        return FileDownloader.getImpl().create(url2)
                .setPath(path)
                //update download progress
                .setCallbackProgressTimes(1500)
                //update download speed;
                .setMinIntervalUpdateSpeed(1500)
                //auto retry times 20
                .setAutoRetryTimes(20)
                .setListener(mListener);
    }

    private void reboot() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(process.getOutputStream());
            out.writeBytes("reboot recovery\n");
            out.writeBytes("exit\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //    ViewHolder, the point is weakreference.
    private static class ViewHolder {

        private Button btnControl;
        private TextView totalLength;
        private TextView currentOffset;
        private ProgressBar progressBar;
        private TextView downloadSpeed;
        private Context context;
        private WeakReference<MainActivity> weakReferenceContext;

        public ViewHolder(WeakReference<MainActivity> weakReferenceContext, Button btnControl, TextView totalLength,
                          TextView currentOffset, ProgressBar progressBar, TextView downloadSpeed, Context context) {
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
        }

        public void updateTotalLength(int totalM) {
            totalLength.setText(context.getString(R.string.total_length) + "   " + totalM + "M");
        }
    }

}
