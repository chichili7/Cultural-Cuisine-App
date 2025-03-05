package com.example.culturalcuisineapp;

public class Restaurant {
    private String name;
    private String address;
    private String photoUrl;
    private double distance; // in meters

    public Restaurant(String name, String address, String photoUrl, double distance) {
        this.name = name;
        this.address = address;
        this.photoUrl = photoUrl;
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public double getDistance() {
        return distance;
    }
}