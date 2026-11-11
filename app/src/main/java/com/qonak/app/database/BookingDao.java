package com.qonak.app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.qonak.app.models.Booking;
import java.util.List;

@Dao
public interface BookingDao {
    @Insert
    long insert(Booking booking);

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY bookingTimestamp DESC")
    List<Booking> getBookingsByUser(long userId);

    @Query("SELECT COUNT(*) FROM bookings WHERE userId = :userId")
    int countByUser(long userId);

    @Query("DELETE FROM bookings WHERE id = :bookingId")
    void deleteBooking(long bookingId);

    @Query("DELETE FROM bookings WHERE userId = :userId")
    void deleteAllByUser(long userId);
}
