package com.snbc.otaapp;

import android.app.Application;

import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;

/**
 * author: zhougaoxiong
 * date: 2019/10/29,17:19
 * projectName:OTAApp2
 * packageName:com.snbc.otaapp
 */
public class SNBCOTAApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .connectTimeout(30_000) // set connection timeout.
                        .readTimeout(30_000) // set read timeout.
                ))
                .commit();
    }
}
