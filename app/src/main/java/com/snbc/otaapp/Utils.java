package com.snbc.otaapp;

import java.lang.reflect.Method;

/**
 * author: zhougaoxiong
 * date: 2019/11/29,16:37
 * projectName:OTAApp2
 * packageName:com.snbc.otaapp
 */
public class Utils {

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

}
