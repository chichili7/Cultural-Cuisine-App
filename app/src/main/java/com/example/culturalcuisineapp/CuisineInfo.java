package com.example.culturalcuisineapp;

import java.io.Serializable;

public class CuisineInfo implements Serializable {

    private final String cuisineName;
    private int imageResourceId;

    public CuisineInfo(String cuisineName){
        this.cuisineName=cuisineName;
        this.imageResourceId = imageResourceId;
    }

    public String getCuisineName() {
        return cuisineName;
    }
    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }
}
