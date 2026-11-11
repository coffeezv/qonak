package com.qonak.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hotels")
public class Hotel {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String city;
    public String description;
    public double pricePerNight;
    public double rating;
    /** Approximate location of the venue, used for the map screen. */
    public double latitude;
    public double longitude;

    public Hotel(long id, String name, String city, String description,
                 double pricePerNight, double rating, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
