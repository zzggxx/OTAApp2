package com.snbc.otaapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    private Button mBtn;
    private ViewHolder mHolder;
    private static LinearLayout mLlRestartShow;
    private static TextView mTvSure;
    private FtpHelper mFtpHelper;
    private static final int[] n = {10};
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
                    mHolder.updateProgress((int) soFarM, percent/*, speed*/);
                    break;
                case 2:
                    long soFarMTotal = (long) msg.obj;
                    mHolder.updateProgress((int) soFarMTotal, 100/*, 0*/);
                    mLlRestartShow.setVisibility(View.VISIBLE);
                    mFtpHelper.closeChannel();
                    mBtn.setText(getResources().getString(R.string.download_file_complete));
                    if (mIsAutoDownload) {
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTvSure.setText(getString(R.string.restart_now) + "(" + n[0]-- + ")");
                                        if ("0".equals(String.valueOf(n[0]))) {
                                            Utils.reboot(MainActivity.this);
                                        }
                                    }
                                });
                            }
                        }, 1000, 1000);
                    }
                    break;
                case 3:
                    break;
                case 4:
                    mShowEnableUpdateZip.setVisibility(View.VISIBLE);

                    Bundle data1 = msg.getData();
                    String description = data1.getString("description");
                    String md5value = data1.getString("md5value");
                    mTarget_version = data1.getString("target_version");
                    String total_ftp_url = data1.getString("total_ftp_url");

                    mTargetVersion.setText(getResources().getString(R.string.current_system_have_enable_update) + mTarget_version);
                    mTargetVersionDesc.setText(getResources().getString(R.string.update_description) + description);

                    mFtpHelper.setSftpProgressMonitor(mSftpProgressMonitor);
                    mFtpHelper.setSrc(total_ftp_url);
                    mFtpHelper.setDest(getPath());
                    if (mIsAutoDownload) {
                        startDownLoad();
                    }
                    break;
                case 5:
                    mCurrentSystemIsLastest.setVisibility(View.VISIBLE);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 3000);
                    break;
                case 6:
                    mCheckUpdateConfig.setVisibility(View.GONE);
                    break;
                case 7:
                    deleteConfigAndDownloadFile();
                    break;
                default:
                    break;
            }
        }
    };
    private boolean mIsAutoDownload;
    private Timer mTimer;

    private void deleteConfigAndDownloadFile() {
//        提示,删除下载的配置文件,删除下载的文件,删除sp文件,删除Systemprop文件(写入的)
        ToastUtils.showStringToast(MainActivity.this, R.string.file_compare_error);
        Utils.deleteFile(getPath());
        Utils.deleteDirectory(getFilesDir().getPath());
        String spPath = "/data/data/" + SPUtils.FILE_NAME + "/shared_prefs";
        Utils.deleteDirectory(spPath);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }

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
//            mHolder.updateProgress((int) (this.count / 1024 / 1024), percent, 0);

            if (System.currentTimeMillis() - mTime >= 1500) {
                Log.i(TAG, "percent: " + percent);
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
            //文件校验
            String fileMD5 = EncryptUtil.md5ForFile(new File(getPath()));
            Log.i(TAG, "fileMD5: " + fileMD5);
            Log.i(TAG, "mMd5value: " + mMd5value);
            if (mMd5value.equals(fileMD5)) {
                Message message = mHandler.obtainMessage();
                message.what = 2;
                message.obj = this.max / 1024 / 1024;
                mHandler.sendMessage(message);
                SPUtils.put(MainActivity.this, "prepare.version", mTarget_version);
                SPUtils.put(MainActivity.this, "update.file.zip.status", "finish");
            } else {
//                文件匹配出错删除相关文件关闭,并提示请稍后再试
                Message message = mHandler.obtainMessage();
                message.what = 7;
                mHandler.sendMessage(message);
            }

        }
    };
    private SftpProgressMonitor mSftpProgressMonitorConfig = new SftpProgressMonitor() {

        @Override
        public void init(int i, String s, String s1, final long l) {
        }

        @Override
        public boolean count(long l) {
            return false;
        }

        @Override
        public void end() {
            Message message = mHandler.obtainMessage();
            message.what = 6;
            mHandler.sendMessage(message);

            String s = getUpdateConfigFile();
            checkUpdateConfigFile(s);
        }
    };
    private String mAndroidSystemType;
    private boolean mIsHaveEnableUpdate;
    private TextView mTargetVersion;
    private TextView mTargetVersionDesc;
    private String mMd5value;
    private String mTarget_version;

    private void checkUpdateConfigFile(String s) {

        String versionName = Utils.getProperty("ro.release.version.name", "1.0.0");
//        todo:test only!
//        String versionName = Utils.getProperty("ro.persist.name", "1.0.0");

        JsonObject mSummaryjson = new JsonParser().parse(s).getAsJsonObject();
        JsonArray asJsonArray = mSummaryjson.getAsJsonArray(mAndroidSystemType);

        for (int i = 0; i < asJsonArray.size(); i++) {

            JsonObject asJsonObject = asJsonArray.get(i).getAsJsonObject();
            String client_version = asJsonObject.get("client_version").getAsString();

            if (versionName.equals(client_version)) {

                mIsHaveEnableUpdate = true;

                String update_enable = asJsonObject.get("update_enable").getAsString();
                String release_version = asJsonObject.get("release_version").getAsString();
                String description = asJsonObject.get("description").getAsString();
                SPUtils.put(MainActivity.this, "description", description);
                mMd5value = asJsonObject.get("md5value").getAsString();
                String total_ftp_url = asJsonObject.get("total_ftp_url").getAsString();

                if (!Boolean.parseBoolean(update_enable)) {
//            升级不可用
                    Message message = mHandler.obtainMessage();
                    message.what = 3;
                    mHandler.sendMessage(message);
                } else {
                    String[] vs = release_version.split("v");
                    String release = Build.VERSION.RELEASE;
//            同一个平台:7.1.2
                    if (release != null && release.equals(vs[0])) {
                        Message message = mHandler.obtainMessage();
                        message.what = 4;
                        Bundle bundle = new Bundle();
                        bundle.putString("description", description);
                        bundle.putString("md5value", mMd5value);
                        bundle.putString("target_version", vs[1]);
                        bundle.putString("total_ftp_url", total_ftp_url);
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }
                break;
            }
        }

        if (!mIsHaveEnableUpdate) {
            Message message = mHandler.obtainMessage();
            message.what = 5;
            mHandler.sendMessage(message);
        }

    }

    private LinearLayout mCheckUpdateConfig;
    private TextView mCurrentSystemIsLastest;
    private LinearLayout mShowEnableUpdateZip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppConstant networkState = Utils.getNetworkState(this);
        if (AppConstant.NETWORN_NONE.value.equals(networkState.value)) {
//            提示没有网络,关闭升级界面
            ToastUtils.showStringToast(this, R.string.no_network);
            finish();
        } else {
            if (!AppConstant.NETWORN_WIFI.value.equals(networkState.value)) {
//            提示非wifi网络注意流量
                ToastUtils.showStringToast(this, R.string.no_network);
            }
            initView();
            downloadUpdateConfigFile();
//            Log.i(TAG, "onCreate:__ " + networkState.desc);
        }

        Intent intent = getIntent();
        if (intent != null) {
            String is_auto_string = intent.getStringExtra("is_auto");
            if (!TextUtils.isEmpty(is_auto_string)) {
                mIsAutoDownload = Boolean.parseBoolean(is_auto_string);
            }
        }
    }

    private String getUpdateConfigFile() {
        String configPath = getConfigPath();
        InputStreamReader summaryIsr;
        try {
            summaryIsr = new InputStreamReader(new FileInputStream(new File(configPath)), "UTF-8");
            BufferedReader summaryBr = new BufferedReader(summaryIsr);
            String summayLine;
            StringBuilder summaryBuilder = new StringBuilder();
            while ((summayLine = summaryBr.readLine()) != null) {
                summaryBuilder.append(summayLine);
            }
            summaryBr.close();
            summaryIsr.close();
            return summaryBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void downloadUpdateConfigFile() {
        deleteUpdateConfigFile();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mFtpHelper.download();
            }
        }).start();
    }

    private void deleteUpdateConfigFile() {
        File file = new File(getConfigPath());
        if (file.exists()) {
            file.delete();
        }
    }

    private void initView() {
        /*------------------download ui-------------------------*/
        mCheckUpdateConfig = (LinearLayout) findViewById(R.id.check_update_config);
        mCurrentSystemIsLastest = (TextView) findViewById(R.id.current_system_is_lastest);
        mShowEnableUpdateZip = (LinearLayout) findViewById(R.id.show_enable_update_zip);

        /*------------------download ui-------------------------*/
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.progressbar);
//        TextView download_speed = (TextView) findViewById(R.id.download_speed);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(this);
        TextView total_length = (TextView) findViewById(R.id.total_length);
        TextView current_offset = (TextView) findViewById(R.id.current_offset);
        mTargetVersion = (TextView) findViewById(R.id.target_version);
        mTargetVersionDesc = (TextView) findViewById(R.id.target_version_desc);

        mHolder = new ViewHolder(new WeakReference<>(this), mBtn, total_length,
                current_offset, progressbar, /*download_speed,*/ this);

        /*------------------restart now?-------------------------*/
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        mTvSure = (TextView) findViewById(R.id.tv_sure);
        mTvSure.setOnClickListener(this);
        mLlRestartShow = (LinearLayout) findViewById(R.id.ll_restart_show);


//        mAndroidSystemType = SystemProperties.get("ro.product.name", "rk3399_snbc_lvds_box");
        mAndroidSystemType = Utils.getProperty("ro.product.name", "rk3399_snbc_lvds_box");

        String src = "/file/" + mAndroidSystemType + File.separator + "update.json";
        mFtpHelper = new FtpHelper("192.168.188.4", "22", "xin", "xin",
                src, getConfigPath(), 160000, 5000,
                mSftpProgressMonitorConfig);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                startDownLoad();
                break;
            case R.id.tv_cancel:
                if (mIsAutoDownload) {
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                }
                finish();
                break;
            case R.id.tv_sure:
                Utils.reboot(MainActivity.this);
                break;
            default:
                break;
        }
    }

    private void startDownLoad() {
        //                已经下载完毕(下载工具有待优化,先如此处理)
        String version = (String) SPUtils.get(MainActivity.this, "prepare.version", "1.0.0");
        String fileStatus = (String) SPUtils.get(MainActivity.this, "update.file.zip.status", "start");
        if (mTarget_version.equals(version) && "finish".equals(fileStatus)) {
            File file = new File(getPath());
            if (file.exists()) {
                mHolder.setProgress(100);
                mBtn.setClickable(false);
                mBtn.setBackgroundResource(R.drawable.btn_bg_gray);
                mBtn.setText(getResources().getString(R.string.download_file_complete));
                mLlRestartShow.setVisibility(View.VISIBLE);
                return;
            }
        }
//                开始下载
        SPUtils.put(MainActivity.this, "update.file.zip.status", "start");
        new Thread(new Runnable() {
            @Override
            public void run() {
                mFtpHelper.download();
            }
        }).start();
        mBtn.setText(getResources().getString(R.string.downloading));
        mBtn.setClickable(false);
        mBtn.setBackgroundResource(R.drawable.btn_bg_gray);
    }

    private String getPath() {
        File mnt = Environment.getExternalStorageDirectory();
        File file = new File(mnt, "update");
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
        //  /storage/emulate/0/update/update.img
        return file.getAbsoluteFile() + File.separator + "update.zip";
    }

    private String getConfigPath() {
        return getFilesDir() + File.separator + "update.json";
    }

}
