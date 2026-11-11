package com.qonak.app.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookings")
public class Booking {
    public static final String TYPE_HOTEL = "hotel";
    public static final String TYPE_RESTAURANT = "restaurant";

    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String type;       // TYPE_HOTEL or TYPE_RESTAURANT
    public long itemId;       // hotel or restaurant id
    public String itemName;
    public String city;
    public String date;       // dd.MM.yyyy
    public String time;       // restaurants only, HH:mm
    public int nights;        // hotels only
    public int persons;       // restaurants only
    public double totalPrice;
    public long bookingTimestamp;

    /** Hotel booking (also the constructor Room uses). */
    public Booking(long userId, String type, long itemId, String itemName, String city,
                   String date, int nights, double totalPrice) {
        this.userId = userId;
        this.type = type;
        this.itemId = itemId;
        this.itemName = itemName;
        this.city = city;
        this.date = date;
        this.nights = nights;
        this.totalPrice = totalPrice;
        this.bookingTimestamp = System.currentTimeMillis();
    }

    /** Restaurant booking. */
    @Ignore
    public Booking(long userId, String type, long itemId, String itemName, String city,
                   String date, String time, int persons, double totalPrice) {
        this(userId, type, itemId, itemName, city, date, 0, totalPrice);
        this.time = time;
        this.persons = persons;
    }

    public boolean isHotel() {
        return TYPE_HOTEL.equals(type);
    }
}
