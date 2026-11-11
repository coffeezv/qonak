package com.qonak.app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.qonak.app.models.Hotel;
import java.util.List;

@Dao
public interface HotelDao {
    @Insert
    void insert(Hotel hotel);

    @Insert
    void insertAll(List<Hotel> hotels);

    @Query("SELECT * FROM hotels")
    List<Hotel> getAllHotels();

    @Query("SELECT * FROM hotels WHERE city = :city")
    List<Hotel> getHotelsByCity(String city);

    @Query("SELECT * FROM hotels WHERE city LIKE '%' || :query || '%'")
    List<Hotel> searchHotelsByCity(String query);

    @Query("SELECT * FROM hotels WHERE id = :id")
    Hotel getHotelById(long id);

    // Расширенная фильтрация с сортировкой
    @Query("SELECT * FROM hotels WHERE " +
            "(:city IS NULL OR city = :city) AND " +
            "pricePerNight >= :minPrice AND " +
            "(:maxPrice = 0 OR pricePerNight <= :maxPrice) AND " +
            "rating >= :minRating " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'price_asc' THEN pricePerNight END ASC, " +
            "CASE WHEN :sortBy = 'price_desc' THEN pricePerNight END DESC, " +
            "CASE WHEN :sortBy = 'rating_desc' THEN rating END DESC")
    List<Hotel> filterHotels(String city, double minPrice, double maxPrice,
                             double minRating, String sortBy);
}