package com.example.myapp.entity;



public class Banner {
    private String label;
    private String url;

    public Banner(String url) {
        this("", url);
    }

    public Banner(String label, String url) {
        this.label = label;
        this.url = url;
    }


    public String getUrl() {
        return url;
    }

}
