package com.qonak.app.utils;

import androidx.annotation.DrawableRes;

import com.qonak.app.R;

import java.util.HashMap;
import java.util.Map;

/** Single source of truth for venue photos (previously duplicated in 4 classes). */
public final class PlaceImages {

    private static final Map<Long, Integer> HOTELS = new HashMap<>();
    private static final Map<Long, Integer> RESTAURANTS = new HashMap<>();

    static {
        HOTELS.put(1L, R.drawable.ritz_almaty);
        HOTELS.put(2L, R.drawable.rixos_astana);
        HOTELS.put(3L, R.drawable.shymbulak_resort_almaty);
        HOTELS.put(4L, R.drawable.hilton_astana);
        HOTELS.put(5L, R.drawable.kazzhol_park_almaty);
        HOTELS.put(6L, R.drawable.wyndham_garden_astana);
        HOTELS.put(7L, R.drawable.grand_park_esil_astana);
        HOTELS.put(8L, R.drawable.hotel_kazakhstan_almaty);
        HOTELS.put(9L, R.drawable.rixos_almaty);
        HOTELS.put(10L, R.drawable.ritz_astana);
        HOTELS.put(11L, R.drawable.kazzhol_astana);

        RESTAURANTS.put(1L, R.drawable.auyl_almaty);
        RESTAURANTS.put(2L, R.drawable.line_brew_almaty);
        RESTAURANTS.put(3L, R.drawable.saksaul_astana);
        RESTAURANTS.put(4L, R.drawable.la_riviere_astana);
        RESTAURANTS.put(5L, R.drawable.mote_almaty);
        RESTAURANTS.put(6L, R.drawable.manga_almaty);
        RESTAURANTS.put(7L, R.drawable.bao_astana);
        RESTAURANTS.put(8L, R.drawable.cinotti_almaty);
        RESTAURANTS.put(9L, R.drawable.sandyq_almaty);
        RESTAURANTS.put(10L, R.drawable.selfie_astana);
    }

    private PlaceImages() {
    }

    @DrawableRes
    public static int hotel(long id) {
        Integer res = HOTELS.get(id);
        return res != null ? res : R.drawable.ic_hotel_placeholder;
    }

    @DrawableRes
    public static int restaurant(long id) {
        Integer res = RESTAURANTS.get(id);
        return res != null ? res : R.drawable.ic_restaurant_placeholder;
    }
}
