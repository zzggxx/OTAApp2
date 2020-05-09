package com.snbc.otaapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author: zhougaoxiong
 * date: 2020/5/11,11:40
 * projectName:OTAApp2
 * packageName:com.snbc.otaapp
 */
public class AndroidMainBoardConfig implements Parcelable {

    private String update_enable;
    private String client_version;
    private String release_version;
    private String description;
    private String md5value;
    private String total_ftp_url;

    protected AndroidMainBoardConfig(Parcel in) {
        update_enable = in.readString();
        client_version = in.readString();
        release_version = in.readString();
        description = in.readString();
        md5value = in.readString();
        total_ftp_url = in.readString();
    }

    public static final Creator<AndroidMainBoardConfig> CREATOR = new Creator<AndroidMainBoardConfig>() {
        @Override
        public AndroidMainBoardConfig createFromParcel(Parcel in) {
            return new AndroidMainBoardConfig(in);
        }

        @Override
        public AndroidMainBoardConfig[] newArray(int size) {
            return new AndroidMainBoardConfig[size];
        }
    };

    public void setUpdate_enable(String update_enable) {
        this.update_enable = update_enable;
    }

    public String getUpdate_enable() {
        return update_enable;
    }

    public void setClient_version(String client_version) {
        this.client_version = client_version;
    }

    public String getClient_version() {
        return client_version;
    }

    public void setRelease_version(String release_version) {
        this.release_version = release_version;
    }

    public String getRelease_version() {
        return release_version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setMd5value(String md5value) {
        this.md5value = md5value;
    }

    public String getMd5value() {
        return md5value;
    }

    public void setTotal_ftp_url(String total_ftp_url) {
        this.total_ftp_url = total_ftp_url;
    }

    public String getTotal_ftp_url() {
        return total_ftp_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(update_enable);
        dest.writeString(client_version);
        dest.writeString(release_version);
        dest.writeString(description);
        dest.writeString(md5value);
        dest.writeString(total_ftp_url);
    }
}
