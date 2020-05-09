package com.snbc.otaapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

/**
 * author: zhougaoxiong
 * date: 2020/5/13,14:27
 * projectName:Temp
 * packageName:com.snbc.temp
 */
public class InstallBroadCastReceiver extends BroadcastReceiver {


    private static final String TAG = InstallBroadCastReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_BOOT_COMPLETED.equals(action)) {
            String fileInstallVersion = (String) SPUtils.get(context, "prepare.version", "1.0.1");
            String versionName = Utils.getProperty("ro.release.version.name", "1.0.0");
            if (fileInstallVersion.equals(versionName)) {
                if (!TextUtils.isEmpty(getPath())) {
                    Utils.deleteFile(getPath());
                    Utils.deleteFileWhenUpdate(context);
                    ToastUtils.showStringToast(context, R.string.update_success);
                }
            }
        }
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
}
