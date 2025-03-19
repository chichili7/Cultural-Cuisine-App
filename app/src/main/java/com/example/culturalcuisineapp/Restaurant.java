package com.example.culturalcuisineapp;

public class Restaurant {


    private String id;
    private String name;
    private String amenity;
    private String cuisine;
    private String address;
    private double latitude;
    private double longitude;
    private float distance;

    public Restaurant(String id, String name, String amenity, String cuisine,
                      String address, double latitude, double longitude, float distance) {
        this.id = id;
        this.name = name;
        this.amenity = amenity;
        this.cuisine = cuisine;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAmenity() { return amenity; }
    public String getCuisine() { return cuisine; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getDistance() { return distance; }
}