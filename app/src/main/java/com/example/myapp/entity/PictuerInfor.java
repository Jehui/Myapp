package com.example.myapp.entity;

import org.litepal.crud.DataSupport;

import java.util.Date;

public class PictuerInfor extends DataSupport {
    private int id;
    private String  name;
    private String flag;//缺少的元素 K  N  P
    private String time;//拍摄的时间
    private String imageUri;//图片的Uri地址
    private int tab;//多张还是单张
    private long multiple_time_flag;

    public void setMultiple_time_flag(long multiple_time_flag) {
        this.multiple_time_flag = multiple_time_flag;
    }

    public long getMultiple_time_flag() {
        return multiple_time_flag;
    }

    public void setTab(int tab) { this.tab = tab; }

    public int getTab() { return tab; }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFlag() {
        return flag;
    }

    public String getTime() {
        return time;
    }

    public String getImageUri() {
        return imageUri;
    }

}
