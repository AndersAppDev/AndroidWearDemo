package com.example.afa.watchdemo;

import android.location.Location;

import java.util.Random;

class PlaceModel {

    private String name = "N/A";
    private int rating = 0;

    private Location location = new Location("");

    PlaceModel(String name, double latitude, double longitude) {
        this.name = name;
        this.location.setLatitude(latitude);
        this.location.setLongitude(longitude);

        Random ran = new Random();
        rating = ran.nextInt(10); // Random number between 0 and 10
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    public Location getLocation() {
        return location;
    }
}