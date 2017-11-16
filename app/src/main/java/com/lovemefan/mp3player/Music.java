package com.lovemefan.mp3player;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by Lovemefan on 2017/11/7.
 */

public class Music implements Parcelable{
    private String name;//歌曲名
    private int coverId;//封面的ID
    private int resourceId;//资源路径ID
    public Music(String name, int coverId, int resourceId) {
        this.name = name;
        this.coverId = coverId;
        this.resourceId = resourceId;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCoverId() {
        return coverId;
    }

    public void setCoverId(int coverId) {
        this.coverId = coverId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

//    实现接口Parcelable需要重写下面两个方法
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getName());
        Bundle bundle = new Bundle();
        bundle.putInt("coverId",getCoverId());
        bundle.putInt("resourceId",getResourceId());
        parcel.writeBundle(bundle);
    }//将成员变量一一写入Parcel中
    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel parcel) {
            Bundle bundle = parcel.readBundle();
            return new Music(parcel.readString(), bundle.getInt("coverId"), bundle.getInt("resourceId"));
        }

        @Override
        public Music[] newArray(int i) {
            return new Music[i];
        }
    };
}
