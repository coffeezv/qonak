package com.qonak.app.hotels;

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
import com.qonak.app.models.Hotel;
import com.qonak.app.utils.AppExecutors;
import com.qonak.app.utils.BookingForm;
import com.qonak.app.utils.Formatters;
import com.qonak.app.utils.PlaceImages;

public class HotelDetailActivity extends AppCompatActivity {

    public static final String EXTRA_HOTEL_ID = "hotel_id";
    private static final int MAX_NIGHTS = 30;

    private TextView hotelName, hotelCity, hotelPrice, hotelRating, hotelDescription;
    private ImageView hotelImage;
    private Hotel currentHotel;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);

        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Детали отеля");
        }

        long hotelId = getIntent().getLongExtra(EXTRA_HOTEL_ID, -1);
        if (hotelId == -1) {
            Toast.makeText(this, "Ошибка загрузки отеля", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        database = AppDatabase.getInstance(this);
        hotelName = findViewById(R.id.detailHotelName);
        hotelCity = findViewById(R.id.detailHotelCity);
        hotelPrice = findViewById(R.id.detailHotelPrice);
        hotelRating = findViewById(R.id.detailHotelRating);
        hotelDescription = findViewById(R.id.detailHotelDescription);
        hotelImage = findViewById(R.id.detailHotelImage);
        Button bookButton = findViewById(R.id.bookButton);

        loadHotel(hotelId);
        bookButton.setOnClickListener(v -> showBookingDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadHotel(long hotelId) {
        AppExecutors.db().execute(() -> {
            Hotel hotel = database.hotelDao().getHotelById(hotelId);
            // All view updates happen on the main thread (the old version touched the
            // ImageView from the background thread, which can crash the app).
            AppExecutors.main(() -> {
                if (isFinishing()) return;
                if (hotel == null) {
                    Toast.makeText(this, "Отель не найден", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentHotel = hotel;
                hotelName.setText(hotel.name);
                hotelCity.setText(hotel.city);
                hotelPrice.setText(Formatters.price(hotel.pricePerNight) + " / ночь");
                hotelRating.setText(String.valueOf(hotel.rating));
                hotelDescription.setText(hotel.description);
                hotelImage.setImageResource(PlaceImages.hotel(hotel.id));
            });
        });
    }

    private void showBookingDialog() {
        if (currentHotel == null) return;

        LinearLayout layout = BookingForm.container(this);
        EditText dateInput = BookingForm.dateField(this, "Дата заезда");
        EditText nightsInput = BookingForm.numberField(this, "Количество ночей", 1);
        TextView totalLabel = BookingForm.totalLabel(this);
        layout.addView(dateInput);
        layout.addView(nightsInput);
        layout.addView(totalLabel);

        Runnable updateTotal = () -> {
            int nights = BookingForm.readInt(nightsInput, 1, MAX_NIGHTS);
            totalLabel.setText(nights > 0
                    ? "Итого: " + Formatters.price(currentHotel.pricePerNight * nights)
                    : "Укажите от 1 до " + MAX_NIGHTS + " ночей");
        };
        nightsInput.addTextChangedListener(new SimpleWatcher(updateTotal));
        updateTotal.run();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Бронирование: " + currentHotel.name)
                .setView(layout)
                .setPositiveButton("Забронировать", null)
                .setNegativeButton("Отмена", null)
                .create();

        // Custom click handler so the dialog stays open when the input is invalid.
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int nights = BookingForm.readInt(nightsInput, 1, MAX_NIGHTS);
            if (nights < 0) {
                nightsInput.setError("От 1 до " + MAX_NIGHTS);
                return;
            }
            saveBooking(dateInput.getText().toString(), nights);
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void saveBooking(String date, int nights) {
        Hotel hotel = currentHotel;
        double total = hotel.pricePerNight * nights;
        AppExecutors.db().execute(() -> {
            Booking booking = new Booking(BookingsFragment.GUEST_USER_ID, Booking.TYPE_HOTEL,
                    hotel.id, hotel.name, hotel.city, date, nights, total);
            long id = database.bookingDao().insert(booking);
            AppExecutors.main(() -> {
                if (id > 0) {
                    Toast.makeText(this, "Отель забронирован: " + Formatters.nights(nights)
                            + ", " + Formatters.price(total), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Ошибка бронирования", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /** TextWatcher that only needs afterTextChanged. */
    static class SimpleWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
        @Override public void afterTextChanged(Editable s) { onChange.run(); }
    }
}
