package com.snbc.otaapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


/**
 * author: zhougaoxiong
 * date: 2020/5/21,16:34
 * projectName:Temp
 * packageName:com.snbc.otaapp
 */
public class ReceiverService extends Service {

    private static final String TAG = ReceiverService.class.getName();
    int i;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if ("com.snbc.receiverservice.install".equals(action)) {
            SPUtils.put(getApplicationContext(), Utils.getDate(), "定时安装被拉起");
            intent.setClass(getApplicationContext(), RestartActivity.class);
            startActivity(intent);
        } else if ("com.snbc.receiverservice.checkupdate".equals(action)) {
            SPUtils.put(getApplicationContext(), Utils.getDate(), "定时检查更新被拉起");
            intent.setClass(getApplicationContext(), MainActivity.class);
            intent.putExtra("is_auto", "true");
            startActivity(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

}
