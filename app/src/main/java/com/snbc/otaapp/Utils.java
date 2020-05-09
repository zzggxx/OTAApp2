package com.snbc.otaapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * author: zhougaoxiong
 * date: 2019/11/29,16:37
 * projectName:OTAApp2
 * packageName:com.snbc.otaapp
 */
public class Utils {

    public static final String TAG = Utils.class.getName();

    public static String getProperty(String key, String defaultValue) {

        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, value));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public static void setProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentMinutes() {
        Date d = new Date();
        System.out.println(d);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(d);
    }

    /**
     * 删除目录以及目录下的文件
     *
     * @param sPath 被删除目录的路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) {
            return false;
        }
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 升级的时候需要删除的文件:sp文件和下载的升级使用的json文件
     */
    public static void deleteFileWhenUpdate(Context context) {
//        Utils.deleteFile(getPath());
        Utils.deleteDirectory(context.getFilesDir().getPath());
        String spPath = "/data/data/" + SPUtils.FILE_NAME + "/shared_prefs";
        Utils.deleteDirectory(spPath);
    }

    public static void reboot(Context context) {
//        重启前保存本次升级的时间:

//        SystemProperties.set("the_last_update_system_time", Utils.getCurrentMinutes());
        Utils.setProperty("persist.last.update.time", Utils.getCurrentMinutes());
        Intent intent = new Intent();
        intent.setAction("com.update.firmware");
        context.sendBroadcast(intent);

        Log.i(TAG, "reboot: ");
    }

    /**
     * 返回当前网络连接类型
     *
     * @param context 上下文
     * @return
     */
    public static AppConstant getNetworkState(Context context) {

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connManager)
            return AppConstant.NETWORN_NONE;
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return AppConstant.NETWORN_NONE;
        }

        // Wifi
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return AppConstant.NETWORN_WIFI;
                }
        }

        // 网络
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null != networkInfo) {
            NetworkInfo.State state = networkInfo.getState();
            String strSubTypeName = networkInfo.getSubtypeName();
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    switch (activeNetInfo.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return AppConstant.NETWORN_2G;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return AppConstant.NETWORN_3G;

                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return AppConstant.NETWORN_4G;

                        default://有机型返回16,17
                            //中国移动 联通 电信 三种3G制式
                            if ("TD-SCDMA".equalsIgnoreCase(strSubTypeName) ||
                                    "WCDMA".equalsIgnoreCase(strSubTypeName) ||
                                    "CDMA2000".equalsIgnoreCase(strSubTypeName)) {
                                return AppConstant.NETWORN_3G;
                            } else {
                                return AppConstant.NETWORN_MOBILE;
                            }
                    }
                }
        }
        return AppConstant.NETWORN_NONE;
    }

    public static String getDate(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());
    }
}

