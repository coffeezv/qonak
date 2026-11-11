package com.qonak.app.utils;

import com.qonak.app.database.AppDatabase;
import com.qonak.app.models.Hotel;
import com.qonak.app.models.Restaurant;

import java.util.Arrays;
import java.util.List;

/**
 * Fills the database with demo venues on first launch.
 * Coordinates are approximate and only used to place markers on the map.
 */
public final class DataSeeder {

    private DataSeeder() {
    }

    /** Must be called on a background thread. */
    public static void seedIfEmpty(AppDatabase db) {
        if (db.hotelDao().getAllHotels().isEmpty()) {
            db.hotelDao().insertAll(hotels());
        }
        if (db.restaurantDao().getAllRestaurants().isEmpty()) {
            db.restaurantDao().insertAll(restaurants());
        }
    }

    private static List<Hotel> hotels() {
        return Arrays.asList(
                new Hotel(1, "The Ritz-Carlton", "Алматы", "Роскошный отель в центре Алматы", 85000, 4.9, 43.2185, 76.9275),
                new Hotel(2, "Rixos President", "Астана", "5-звёздочный отель с видом на Байтерек", 72000, 4.8, 51.1310, 71.4180),
                new Hotel(3, "Shymbulak Resort", "Алматы", "Горнолыжный курорт на Шымбулаке", 62000, 4.7, 43.1280, 77.0810),
                new Hotel(4, "Hilton Astana", "Астана", "Современный бизнес-отель", 45000, 4.6, 51.0890, 71.4160),
                new Hotel(5, "Kazzhol Park", "Алматы", "Уютный отель 4 звезды", 38000, 4.5, 43.2390, 76.9070),
                new Hotel(6, "Wyndham Garden", "Астана", "Комфортабельный отель", 42000, 4.4, 51.1690, 71.4310),
                new Hotel(7, "Grand Park Esil", "Астана", "Отель в тихом районе", 35000, 4.3, 51.1670, 71.4200),
                new Hotel(8, "Hotel Kazakhstan", "Алматы", "Знаменитая гостиница", 30000, 4.2, 43.2445, 76.9575),
                new Hotel(9, "Rixos Almaty", "Алматы", "Элегантный отель Rixos в центре Алматы", 78000, 4.85, 43.2530, 76.9440),
                new Hotel(10, "The Ritz-Carlton", "Астана", "Роскошный Ritz-Carlton в Астане", 95000, 4.95, 51.1290, 71.4300),
                new Hotel(11, "Kazzhol Hotel", "Астана", "Уютный отель 4 звезды", 38000, 4.5, 51.1750, 71.4380)
        );
    }

    private static List<Restaurant> restaurants() {
        return Arrays.asList(
                new Restaurant(1, "Auyl", "Алматы", "Казахская", "Традиционная казахская кухня", 12000, 4.8, 43.2160, 76.9590),
                new Restaurant(2, "Line Brew", "Алматы", "Европейская", "Популярный крафтовый ресторан", 15000, 4.7, 43.2560, 76.9290),
                new Restaurant(3, "Saksaul", "Астана", "Казахская", "Современная казахская кухня", 14000, 4.8, 51.1260, 71.4260),
                new Restaurant(4, "La Rivière", "Астана", "Французская", "Изысканная французская кухня", 20000, 4.6, 51.1230, 71.4400),
                new Restaurant(5, "MÖTE", "Алматы", "Европейская", "Премиальный ресторан с авторской кухней", 10000, 4.7, 43.2380, 76.9450),
                new Restaurant(6, "Manga", "Алматы", "Японская", "Лучшие роллы в городе", 10000, 4.5, 43.2400, 76.9300),
                new Restaurant(7, "BAO Noodles & Sushi Bar", "Астана", "Паназиатская", "Азиатская кухня", 11000, 4.6, 51.1330, 71.4040),
                new Restaurant(8, "Cinotti", "Алматы", "Итальянская", "Интерьер в стиле богемной Италии", 8000, 5.0, 43.2330, 76.9550),
                new Restaurant(9, "Sandyq", "Алматы", "Казахская", "Авторская казахская кухня", 13000, 4.4, 43.2370, 76.9620),
                new Restaurant(10, "Selfie", "Астана", "Европейская", "Современная европейская кухня", 20000, 4.3, 51.1400, 71.4200)
        );
    }
}
