package com.qonak.app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.qonak.app.models.Restaurant;
import java.util.List;

@Dao
public interface RestaurantDao {
    @Insert
    void insert(Restaurant restaurant);

    @Insert
    void insertAll(List<Restaurant> restaurants);

    @Query("SELECT * FROM restaurants")
    List<Restaurant> getAllRestaurants();

    @Query("SELECT * FROM restaurants WHERE city = :city")
    List<Restaurant> getRestaurantsByCity(String city);

    @Query("SELECT * FROM restaurants WHERE city LIKE '%' || :query || '%'")
    List<Restaurant> searchRestaurantsByCity(String query);

    @Query("SELECT * FROM restaurants WHERE id = :id")
    Restaurant getRestaurantById(long id);

    @Query("SELECT * FROM restaurants WHERE " +
            "(:city IS NULL OR city = :city) AND " +
            "(:cuisine IS NULL OR cuisine = :cuisine) AND " +
            "avgPricePerPerson >= :minPrice AND " +
            "(:maxPrice = 0 OR avgPricePerPerson <= :maxPrice) AND " +
            "rating >= :minRating " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'price_asc' THEN avgPricePerPerson END ASC, " +
            "CASE WHEN :sortBy = 'price_desc' THEN avgPricePerPerson END DESC, " +
            "CASE WHEN :sortBy = 'rating_desc' THEN rating END DESC")
    List<Restaurant> filterRestaurantsWithCuisine(String city, String cuisine,
                                                  double minPrice, double maxPrice,
                                                  double minRating, String sortBy);
}