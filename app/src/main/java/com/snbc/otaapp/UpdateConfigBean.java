package com.snbc.otaapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * author: zhougaoxiong
 * date: 2020/5/11,11:15
 * projectName:OTAApp2
 * packageName:com.snbc.otaapp
 */
public class UpdateConfigBean implements Parcelable {

    private List<AndroidMainBoardConfig> mAndroidMainBoardConfig;

    protected UpdateConfigBean(Parcel in) {
        mAndroidMainBoardConfig = in.createTypedArrayList(AndroidMainBoardConfig.CREATOR);
    }

    public static final Creator<UpdateConfigBean> CREATOR = new Creator<UpdateConfigBean>() {
        @Override
        public UpdateConfigBean createFromParcel(Parcel in) {
            return new UpdateConfigBean(in);
        }

        @Override
        public UpdateConfigBean[] newArray(int size) {
            return new UpdateConfigBean[size];
        }
    };

    public void setAndroidMainBoardConfig(List<AndroidMainBoardConfig> androidMainBoardConfig) {
        this.mAndroidMainBoardConfig = androidMainBoardConfig;
    }

    public List<AndroidMainBoardConfig> getAndroidMainBoardConfig() {
        return mAndroidMainBoardConfig;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mAndroidMainBoardConfig);
    }
}
