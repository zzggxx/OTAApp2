package com.snbc.otaapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class RestartActivity extends Activity implements View.OnClickListener {

    private static final String TAG = RestartActivity.class.getName();
    private static final int[] n = {10};
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restart);

        findViewById(R.id.tv_cancel).setOnClickListener(this);
        final TextView tv_sure = (TextView) findViewById(R.id.tv_sure);
        tv_sure.setOnClickListener(this);

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_sure.setText(getString(R.string.restart_now) + "(" + n[0]-- + ")");
                        if ("0".equals(String.valueOf(n[0]))) {
                            Utils.reboot(RestartActivity.this);
                        }
                    }
                });
            }
        }, 1000, 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                mTimer.cancel();
                finish();
                break;
            case R.id.btn:
                Utils.reboot(RestartActivity.this);
                break;
            default:
                break;
        }
    }
}
