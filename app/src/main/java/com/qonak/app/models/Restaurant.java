package com.qonak.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "restaurants")
public class Restaurant {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String city;
    public String cuisine;
    public String description;
    public double avgPricePerPerson;
    public double rating;
    /** Approximate location of the venue, used for the map screen. */
    public double latitude;
    public double longitude;

    public Restaurant(long id, String name, String city, String cuisine, String description,
                      double avgPricePerPerson, double rating, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.cuisine = cuisine;
        this.description = description;
        this.avgPricePerPerson = avgPricePerPerson;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
