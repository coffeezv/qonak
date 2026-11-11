package com.qonak.app.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.qonak.app.R;
import com.qonak.app.database.AppDatabase;
import com.qonak.app.hotels.HotelDetailActivity;
import com.qonak.app.models.Hotel;
import com.qonak.app.models.Restaurant;
import com.qonak.app.restaurants.RestaurantDetailActivity;
import com.qonak.app.utils.AppExecutors;
import com.qonak.app.utils.Formatters;

import java.util.List;

/**
 * Shows every hotel and restaurant at its own location.
 * Tap a marker to see name and price, tap the info window to open the venue.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static final LatLng ALMATY = new LatLng(43.2380, 76.9450);
    private static final int MAP_PADDING_PX = 120;

    private GoogleMap map;
    private AppDatabase database;

    private final ActivityResultLauncher<String> locationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    enableMyLocation();
                } else {
                    Toast.makeText(getContext(),
                            "Без доступа к геолокации карта не покажет, где вы находитесь",
                            Toast.LENGTH_LONG).show();
                }
            });

    /** What a marker points to; stored in the marker's tag. */
    private static final class Place {
        final boolean hotel;
        final long id;

        Place(boolean hotel, long id) {
            this.hotel = hotel;
            this.id = id;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = AppDatabase.getInstance(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ALMATY, 11));
        map.setOnInfoWindowClickListener(this::openPlace);

        loadMarkers();

        if (hasLocationPermission()) {
            enableMyLocation();
        } else {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void loadMarkers() {
        AppExecutors.db().execute(() -> {
            List<Hotel> hotels = database.hotelDao().getAllHotels();
            List<Restaurant> restaurants = database.restaurantDao().getAllRestaurants();

            AppExecutors.onUi(this, () -> {
                if (map == null) return;
                if (hotels.isEmpty() && restaurants.isEmpty()) {
                    Toast.makeText(getContext(), "Нет данных для отображения на карте",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                LatLngBounds.Builder bounds = LatLngBounds.builder();
                for (Hotel hotel : hotels) {
                    LatLng position = new LatLng(hotel.latitude, hotel.longitude);
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(position)
                            .title("🏨 " + hotel.name)
                            .snippet(Formatters.price(hotel.pricePerNight) + " / ночь • ★ " + hotel.rating)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    if (marker != null) marker.setTag(new Place(true, hotel.id));
                    bounds.include(position);
                }
                for (Restaurant restaurant : restaurants) {
                    LatLng position = new LatLng(restaurant.latitude, restaurant.longitude);
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(position)
                            .title("🍽 " + restaurant.name)
                            .snippet(restaurant.cuisine + " • " + Formatters.price(restaurant.avgPricePerPerson) + " / чел")
                            .icon(BitmapDescriptorFactory.defaultMarker(markerHue(restaurant.cuisine))));
                    if (marker != null) marker.setTag(new Place(false, restaurant.id));
                    bounds.include(position);
                }

                fitToBounds(bounds.build());
            });
        });
    }

    /** Zooms the camera so that every venue is visible. */
    private void fitToBounds(LatLngBounds area) {
        View view = getView();
        if (view != null && view.getWidth() > 0 && view.getHeight() > 0) {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    area, view.getWidth(), view.getHeight(), MAP_PADDING_PX));
        } else {
            map.setOnMapLoadedCallback(() ->
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(area, MAP_PADDING_PX)));
        }
    }

    private void openPlace(Marker marker) {
        Object tag = marker.getTag();
        if (!(tag instanceof Place)) return;
        Place place = (Place) tag;

        Intent intent;
        if (place.hotel) {
            intent = new Intent(requireContext(), HotelDetailActivity.class);
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_ID, place.id);
        } else {
            intent = new Intent(requireContext(), RestaurantDetailActivity.class);
            intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, place.id);
        }
        startActivity(intent);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission") // checked in hasLocationPermission()
    private void enableMyLocation() {
        if (map != null && hasLocationPermission()) {
            map.setMyLocationEnabled(true);
        }
    }

    private static float markerHue(String cuisine) {
        if (cuisine == null) return BitmapDescriptorFactory.HUE_CYAN;
        switch (cuisine) {
            case "Казахская": return BitmapDescriptorFactory.HUE_GREEN;
            case "Европейская": return BitmapDescriptorFactory.HUE_BLUE;
            case "Итальянская": return BitmapDescriptorFactory.HUE_RED;
            case "Японская": return BitmapDescriptorFactory.HUE_ORANGE;
            case "Французская": return BitmapDescriptorFactory.HUE_VIOLET;
            case "Паназиатская": return BitmapDescriptorFactory.HUE_YELLOW;
            case "Китайская": return BitmapDescriptorFactory.HUE_ROSE;
            default: return BitmapDescriptorFactory.HUE_CYAN;
        }
    }
}
