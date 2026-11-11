package com.qonak.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.qonak.app.models.Booking;
import com.qonak.app.models.Hotel;
import com.qonak.app.models.Restaurant;
import com.qonak.app.utils.AppExecutors;
import com.qonak.app.utils.DataSeeder;

@Database(entities = {Hotel.class, Restaurant.class, Booking.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HotelDao hotelDao();
    public abstract RestaurantDao restaurantDao();
    public abstract BookingDao bookingDao();

    private static volatile AppDatabase INSTANCE;

    /**
     * Returns the database singleton. Call it on the main thread BEFORE submitting a query
     * to {@link AppExecutors#db()}: the demo data is seeded on the same single thread,
     * so it is always in place before the first query runs.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    AppDatabase db = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "qonak_database")
                            // v1 → v2 adds coordinates and persons; demo data is re-seeded.
                            .fallbackToDestructiveMigration()
                            .build();
                    AppExecutors.db().execute(() -> DataSeeder.seedIfEmpty(db));
                    INSTANCE = db;
                }
            }
        }
        return INSTANCE;
    }
}
