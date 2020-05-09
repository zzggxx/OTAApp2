package com.snbc.otaapp;

/**
 * @author zhougaoxiong
 */
public enum AppConstant {

    NETWORN_NONE("code", "0", "没有网络"),
    NETWORN_WIFI("code", "1", "WIFI网络"),
    NETWORN_2G("code", "2", "2G网络"),
    NETWORN_3G("code", "3", "3G网络"),
    NETWORN_4G("code", "4", "4G网络"),
    NETWORN_MOBILE("code", "5", "移动数据");

    public final String name;
    public final String value;
    public final String desc;

    AppConstant(String name, String code, String desc) {
        this.name = name;
        this.value = code;
        this.desc = desc;
    }

    public static final String a = new String("2222");
}
