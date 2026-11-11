package com.qonak.app.restaurants;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.qonak.app.R;
import com.qonak.app.bookings.BookingsFragment;
import com.qonak.app.database.AppDatabase;
import com.qonak.app.models.Booking;
import com.qonak.app.models.Restaurant;
import com.qonak.app.utils.AppExecutors;
import com.qonak.app.utils.BookingForm;
import com.qonak.app.utils.Formatters;
import com.qonak.app.utils.PlaceImages;

public class RestaurantDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    private static final int MAX_PERSONS = 20;

    private TextView restaurantName, restaurantCity, restaurantCuisine,
            restaurantPrice, restaurantRating, restaurantDescription;
    private ImageView restaurantImage;
    private Restaurant currentRestaurant;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Детали ресторана");
        }

        long restaurantId = getIntent().getLongExtra(EXTRA_RESTAURANT_ID, -1);
        if (restaurantId == -1) {
            Toast.makeText(this, "Ошибка загрузки ресторана", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        database = AppDatabase.getInstance(this);
        restaurantName = findViewById(R.id.detailRestaurantName);
        restaurantCity = findViewById(R.id.detailRestaurantCity);
        restaurantCuisine = findViewById(R.id.detailRestaurantCuisine);
        restaurantPrice = findViewById(R.id.detailRestaurantPrice);
        restaurantRating = findViewById(R.id.detailRestaurantRating);
        restaurantDescription = findViewById(R.id.detailRestaurantDescription);
        restaurantImage = findViewById(R.id.detailRestaurantImage);
        Button bookButton = findViewById(R.id.bookButton);

        loadRestaurant(restaurantId);
        bookButton.setOnClickListener(v -> showBookingDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadRestaurant(long restaurantId) {
        AppExecutors.db().execute(() -> {
            Restaurant restaurant = database.restaurantDao().getRestaurantById(restaurantId);
            AppExecutors.main(() -> {
                if (isFinishing()) return;
                if (restaurant == null) {
                    Toast.makeText(this, "Ресторан не найден", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentRestaurant = restaurant;
                restaurantName.setText(restaurant.name);
                restaurantCity.setText(restaurant.city);
                restaurantCuisine.setText("🍳 " + restaurant.cuisine);
                restaurantPrice.setText(Formatters.price(restaurant.avgPricePerPerson) + " / чел");
                restaurantRating.setText("★ " + restaurant.rating);
                restaurantDescription.setText(restaurant.description);
                restaurantImage.setImageResource(PlaceImages.restaurant(restaurant.id));
            });
        });
    }

    private void showBookingDialog() {
        if (currentRestaurant == null) return;

        LinearLayout layout = BookingForm.container(this);
        EditText personsInput = BookingForm.numberField(this, "Количество гостей", 2);
        EditText dateInput = BookingForm.dateField(this, "Дата");
        EditText timeInput = BookingForm.timeField(this, "Время");
        TextView totalLabel = BookingForm.totalLabel(this);
        layout.addView(personsInput);
        layout.addView(dateInput);
        layout.addView(timeInput);
        layout.addView(totalLabel);

        Runnable updateTotal = () -> {
            int persons = BookingForm.readInt(personsInput, 1, MAX_PERSONS);
            totalLabel.setText(persons > 0
                    ? "Средний чек: " + Formatters.price(currentRestaurant.avgPricePerPerson * persons)
                    : "Укажите от 1 до " + MAX_PERSONS + " гостей");
        };
        personsInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { updateTotal.run(); }
        });
        updateTotal.run();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Столик в " + currentRestaurant.name)
                .setView(layout)
                .setPositiveButton("Забронировать", null)
                .setNegativeButton("Отмена", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int persons = BookingForm.readInt(personsInput, 1, MAX_PERSONS);
            if (persons < 0) {
                personsInput.setError("От 1 до " + MAX_PERSONS);
                return;
            }
            saveBooking(dateInput.getText().toString(), timeInput.getText().toString(), persons);
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void saveBooking(String date, String time, int persons) {
        Restaurant restaurant = currentRestaurant;
        double total = restaurant.avgPricePerPerson * persons;
        AppExecutors.db().execute(() -> {
            Booking booking = new Booking(BookingsFragment.GUEST_USER_ID, Booking.TYPE_RESTAURANT,
                    restaurant.id, restaurant.name, restaurant.city, date, time, persons, total);
            long id = database.bookingDao().insert(booking);
            AppExecutors.main(() -> {
                if (id > 0) {
                    Toast.makeText(this, "Столик забронирован: " + date + ", " + time,
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Ошибка бронирования", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
