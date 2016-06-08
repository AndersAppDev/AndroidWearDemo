package com.example.afa.watchdemo;

import android.location.Location;

import java.util.Random;

/**
 * Created by afa on 08/06/16.
 */

public class PlaceModel {
    String name = "N/A";
    int rating;

    Location location = new Location("");

    PlaceModel(String name, double latitude, double longitude) {
        this.name = name;
        this.location.setLatitude(latitude);
        this.location.setLongitude(longitude);

        Random ran = new Random();
        rating = ran.nextInt(10); // Random number between 0 and 10
    }
}
