package com.example.myapp.entity;

public class ComponentItem extends ClassItem{

    private String fragmentClassName;

    public ComponentItem(String label, Class<?> cls, boolean updated, String fragmentClassName) {
        super(label, cls, updated);
        this.fragmentClassName = fragmentClassName;
    }
    public String getFragmentClassName() {
        return fragmentClassName;
    }

}
