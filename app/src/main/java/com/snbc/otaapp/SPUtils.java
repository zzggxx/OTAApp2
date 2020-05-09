/**
 * File Name:SPUtils.java
 * Copyright (c) 2015, doubibi All Rights Reserved.
 */
package com.snbc.otaapp;

import android.content.Context;
import android.content.SharedPreferences;


public class SPUtils {
    /**
     * 保存在手机里面的文件名
     */
    public static final String FILE_NAME = "com.snbc.otaapp";


    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void put(Context context, String key, Object object) {
        if (null == object) {
            remove(context, key);
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.commit();
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object get(Context context, String key, Object defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        Object result = null;
        if (defaultObject instanceof String) {
            result = sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            result = sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            result = sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            result = sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            result = sp.getLong(key, (Long) defaultObject);
        }

        return result;
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }

}
