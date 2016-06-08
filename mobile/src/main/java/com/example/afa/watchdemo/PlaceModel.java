package com.example.afa.watchdemo;

import java.util.Random;

/**
 * Created by afa on 08/06/16.
 */

public class PlaceModel {
    String name = "N/A";
    int rating;

    double locationLongitude;
    double locationLatitude;

    PlaceModel(String name, double longitude, double latitude) {
        this.name = name;
        this.locationLongitude = longitude;
        this.locationLatitude = latitude;

        Random ran = new Random();
        rating = ran.nextInt(10); // Random number between 0 and 10
    }
}
