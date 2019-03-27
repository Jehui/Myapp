package com.example.myapp.entity;

import org.litepal.crud.DataSupport;

public class PictuerInfor extends DataSupport {
    private int id;
    private String  name;
    private String flag;//缺少的元素 K  N  P
    private String time;//拍摄的时间
    private String imageUri;//图片的Uri地址
    private byte[]images;//将图片以二进制的形式存到数据库中

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

    public void setImages(byte[] images) {
        this.images = images;
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

    public byte[] getImages() {
        return images;
    }
}
