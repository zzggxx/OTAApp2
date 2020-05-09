package com.snbc.otaapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;
import java.lang.ref.WeakReference;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    private Button mBtn;
    private ViewHolder mHolder;
    private static LinearLayout mLlRestartShow;
    private static TextView mTvSure;
    private FtpHelper mFtpHelper;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage: " + Thread.currentThread().getName());
            int what = msg.what;
            switch (what) {
                case 0:
                    int obj = (int) msg.obj;
                    mHolder.updateTotalLength(obj);
                    break;
                case 1:
                    Bundle data = msg.getData();
                    long soFarM = data.getLong("soFarM");
                    int percent = data.getInt("percent");
                    int speed = data.getInt("speed");
                    mHolder.updateProgress((int) soFarM, percent, speed);
                    break;
                case 2:
                    long soFarMTotal = (long) msg.obj;
                    mHolder.updateProgress((int) soFarMTotal, 100, 0);
                    mLlRestartShow.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
    //Download listener
    private SftpProgressMonitor mSftpProgressMonitor = new SftpProgressMonitor() {

        private long mTime;
        private long count = 0;     //当前接收的总字节数
        private long max = 0;       //最终文件大小
        private int percent = -1;  //进度

        @Override
        public void init(int i, String s, String s1, final long l) {
            if (i == SftpProgressMonitor.PUT) {
                Log.i(TAG, "init: Upload file begin");
            } else {
                Log.i(TAG, "init: Download file begin");
            }

            this.max = l;
            this.count = 0;
            this.percent = -1;

            Message message = mHandler.obtainMessage();
            message.what = 0;
            message.obj = (int) (l / 1024 / 1024);
            mHandler.sendMessage(message);
        }

        /**
         * 当每次传输了一个数据块后，调用count方法，count方法的参数为这一次传输的数据块大小
         */
        @Override
        public boolean count(long l) {
            this.count += l;
            Log.i(TAG, "count: " + count + "  " + max);
            percent = (int) (this.count * 100 / max);
            Log.i(TAG, "percent: " + percent);
//            mHolder.updateProgress((int) (this.count / 1024 / 1024), percent, 0);

            if (System.currentTimeMillis() - mTime >= 1000) {
                mTime = System.currentTimeMillis();
                Message message = mHandler.obtainMessage();
                message.what = 1;
                Bundle bundle = new Bundle();
                bundle.putLong("soFarM", this.count / 1024 / 1024);
                bundle.putInt("percent", percent);
                bundle.putInt("speed", 0);
                message.setData(bundle);
//                message.obj = percent;
                mHandler.sendMessage(message);
            }

            if (percent <= this.count * 100 / max) {
                return true;
            }

            return false;
        }

        @Override
        public void end() {
            Message message = mHandler.obtainMessage();
            message.what = 2;
            message.obj = this.max / 1024 / 1024;
            mHandler.sendMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        /*------------------download ui-------------------------*/
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.progressbar);
        TextView download_speed = (TextView) findViewById(R.id.download_speed);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(this);
        TextView total_length = (TextView) findViewById(R.id.total_length);
        TextView current_offset = (TextView) findViewById(R.id.current_offset);

        mHolder = new ViewHolder(new WeakReference<>(this), mBtn, total_length,
                current_offset, progressbar, download_speed, this);

        /*------------------restart now?-------------------------*/
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        mTvSure = (TextView) findViewById(R.id.tv_sure);
        mTvSure.setOnClickListener(this);
        mLlRestartShow = (LinearLayout) findViewById(R.id.ll_restart_show);

        mFtpHelper = new FtpHelper("192.168.188.4", "22", "xin", "xin",
                "/file/update.img", getPath(), 160000, 5000,
                mSftpProgressMonitor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mFtpHelper.download();
                    }
                }).start();
                break;
            case R.id.tv_cancel:
                mLlRestartShow.setVisibility(View.GONE);
                break;
            case R.id.tv_sure:
                reboot();
                break;
            default:
                break;
        }
    }

    private String getPath() {
//        return getFilesDir() + File.separator + "update.img";
        File mnt = Environment.getExternalStorageDirectory();
        File file = new File(mnt, "update");
        file.mkdir();
        //  /storage/emulate/0/update/update.img
        return file.getAbsoluteFile() + File.separator + "update.img";
    }

    public void reboot() {
        Intent intent = new Intent();
        intent.setAction("com.update.firmware");
        sendBroadcast(intent);
    }

}
