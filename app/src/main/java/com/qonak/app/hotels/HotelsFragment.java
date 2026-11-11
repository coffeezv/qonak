package com.qonak.app.hotels;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.core.content.ContextCompat;
import com.qonak.app.R;
import com.qonak.app.utils.AppExecutors;
import com.qonak.app.utils.Formatters;
import com.qonak.app.database.AppDatabase;
import com.qonak.app.models.Hotel;
import java.text.SimpleDateFormat;
import java.util.*;

public class HotelsFragment extends Fragment {

    private AppDatabase database;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private AutoCompleteTextView cityAutoComplete;
    private TextView checkInDate, checkOutDate, guestsText;
    private Button searchButton;
    private LinearLayout filterChip;
    private LinearLayout activeFiltersContainer;
    private HotelAdapter hotelAdapter;

    private String selectedCity = null;
    private String checkIn = null;
    private String checkOut = null;
    private int adults = 1;
    private int children = 0;

    private String currentFilterCity = null;
    private double currentMinPrice = 0;
    private double currentMaxPrice = 0;
    private double currentMinRating = 0;
    private String currentSortBy = "rating_desc";

    private static final List<String> CITIES = Arrays.asList(
            "Алматы", "Астана", "Шымкент",
            "Almaty", "Astana", "Shymkent"
    );

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hotels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = AppDatabase.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        cityAutoComplete = view.findViewById(R.id.cityAutoComplete);
        checkInDate = view.findViewById(R.id.checkInDate);
        checkOutDate = view.findViewById(R.id.checkOutDate);
        guestsText = view.findViewById(R.id.guestsText);
        searchButton = view.findViewById(R.id.searchButton);
        filterChip = view.findViewById(R.id.filterChip);
        activeFiltersContainer = view.findViewById(R.id.activeFiltersContainer);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, CITIES);
        cityAutoComplete.setAdapter(cityAdapter);
        cityAutoComplete.setThreshold(1);

        checkIn = dateFormat.format(calendar.getTime());
        checkInDate.setText(checkIn);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        checkOut = dateFormat.format(calendar.getTime());
        checkOutDate.setText(checkOut);

        checkInDate.setOnClickListener(v -> showDatePicker(true));
        checkOutDate.setOnClickListener(v -> showDatePicker(false));
        guestsText.setOnClickListener(v -> showGuestsDialog());
        searchButton.setOnClickListener(v -> searchHotels());
        filterChip.setOnClickListener(v -> showBottomSheetFilter());

        TextView cityAlmaty = view.findViewById(R.id.cityAlmaty);
        TextView cityAstana = view.findViewById(R.id.cityAstana);
        TextView cityShymkent = view.findViewById(R.id.cityShymkent);
        TextView cityAktau = view.findViewById(R.id.cityAktau);
        TextView cityAktobe = view.findViewById(R.id.cityAktobe);
        TextView cityKaraganda = view.findViewById(R.id.cityKaraganda);
        TextView cityTaraz = view.findViewById(R.id.cityTaraz);
        TextView cityPavlodar = view.findViewById(R.id.cityPavlodar);
        TextView cityTurkistan = view.findViewById(R.id.cityTurkistan);

        View.OnClickListener cityClickListener = v -> {
            TextView tv = (TextView) v;
            String city = tv.getText().toString();
            cityAutoComplete.setText(city);
            searchHotels();
        };

        cityAlmaty.setOnClickListener(cityClickListener);
        cityAstana.setOnClickListener(cityClickListener);
        cityShymkent.setOnClickListener(cityClickListener);
        cityAktau.setOnClickListener(cityClickListener);
        cityAktobe.setOnClickListener(cityClickListener);
        cityKaraganda.setOnClickListener(cityClickListener);
        cityTaraz.setOnClickListener(cityClickListener);
        cityPavlodar.setOnClickListener(cityClickListener);
        cityTurkistan.setOnClickListener(cityClickListener);

        loadAllHotels();
    }

    private void showDatePicker(boolean isCheckIn) {
        Calendar initial = Calendar.getInstance();
        java.util.Date current = Formatters.parseDate(isCheckIn ? checkIn : checkOut);
        if (current != null) initial.setTime(current);
        android.app.DatePickerDialog datePicker = new android.app.DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    String dateStr = dateFormat.format(selectedDate.getTime());

                    if (isCheckIn) {
                        checkIn = dateStr;
                        checkInDate.setText(checkIn);
                        // Dates are compared as dates, not as "dd.MM.yyyy" strings
                        // (string comparison put 01.12 before 15.11).
                        if (!Formatters.isAfter(checkOut, checkIn)) {
                            selectedDate.add(Calendar.DAY_OF_MONTH, 1);
                            checkOut = dateFormat.format(selectedDate.getTime());
                            checkOutDate.setText(checkOut);
                        }
                    } else {
                        if (!Formatters.isAfter(dateStr, checkIn)) {
                            Toast.makeText(getContext(), "Дата выезда должна быть позже даты заезда", Toast.LENGTH_SHORT).show();
                        } else {
                            checkOut = dateStr;
                            checkOutDate.setText(checkOut);
                        }
                    }
                },
                initial.get(Calendar.YEAR),
                initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private void showGuestsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_guests, null);
        builder.setView(dialogView);

        TextView adultsCount = dialogView.findViewById(R.id.adultsCount);
        TextView childrenCount = dialogView.findViewById(R.id.childrenCount);
        Button decreaseAdults = dialogView.findViewById(R.id.decreaseAdults);
        Button increaseAdults = dialogView.findViewById(R.id.increaseAdults);
        Button decreaseChildren = dialogView.findViewById(R.id.decreaseChildren);
        Button increaseChildren = dialogView.findViewById(R.id.increaseChildren);
        Button confirmButton = dialogView.findViewById(R.id.confirmGuests);

        adultsCount.setText(String.valueOf(adults));
        childrenCount.setText(String.valueOf(children));

        decreaseAdults.setOnClickListener(v -> {
            if (adults > 1) {
                adults--;
                adultsCount.setText(String.valueOf(adults));
            }
        });

        increaseAdults.setOnClickListener(v -> {
            adults++;
            adultsCount.setText(String.valueOf(adults));
        });

        decreaseChildren.setOnClickListener(v -> {
            if (children > 0) {
                children--;
                childrenCount.setText(String.valueOf(children));
            }
        });

        increaseChildren.setOnClickListener(v -> {
            children++;
            childrenCount.setText(String.valueOf(children));
        });

        AlertDialog dialog = builder.create();

        confirmButton.setOnClickListener(v -> {
            String guests = adults + " " + getAdultWord(adults);
            if (children > 0) {
                guests += ", " + children + " " + getChildWord(children);
            }
            guestsText.setText(guests);
            dialog.dismiss();
        });

        dialog.show();
    }

    private String getAdultWord(int count) {
        return Formatters.plural(count, "взрослый", "взрослых", "взрослых");
    }

    private String getChildWord(int count) {
        return Formatters.plural(count, "ребёнок", "ребёнка", "детей");
    }

    private void loadAllHotels() {
        progressBar.setVisibility(View.VISIBLE);

        AppExecutors.db().execute(() -> {
            List<Hotel> hotels = database
                    .hotelDao()
                    .getAllHotels();

            AppExecutors.onUi(this, () -> {
                progressBar.setVisibility(View.GONE);
                updateUI(hotels);
            });
        });
    }

    private void searchHotels() {
        String city = cityAutoComplete.getText().toString().trim();
        currentFilterCity = city.isEmpty() ? null : convertToRussianCity(city);

        progressBar.setVisibility(View.VISIBLE);

        AppExecutors.db().execute(() -> {
            List<Hotel> filtered = database
                    .hotelDao()
                    .filterHotels(currentFilterCity, currentMinPrice, currentMaxPrice,
                            currentMinRating, currentSortBy);

            AppExecutors.onUi(this, () -> {
                progressBar.setVisibility(View.GONE);
                updateUI(filtered);
                updateActiveFiltersChips();

                int nights = calculateNights();
                String message = "Найдено: " + Formatters.hotels(filtered.size());
                if (currentFilterCity != null) message += " в " + currentFilterCity;
                if (nights > 0) message += " на " + Formatters.nights(nights);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            });
        });
    }

    private int calculateNights() {
        return Formatters.nightsBetween(checkIn, checkOut);
    }

    private void showBottomSheetFilter() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_hotel, null);
        bottomSheetDialog.setContentView(sheetView);

        AutoCompleteTextView filterCity = sheetView.findViewById(R.id.filterCity);
        Spinner sortSpinner = sheetView.findViewById(R.id.sortSpinner);
        EditText minPriceInput = sheetView.findViewById(R.id.minPrice);
        EditText maxPriceInput = sheetView.findViewById(R.id.maxPrice);
        TextView resetAll = sheetView.findViewById(R.id.resetAllText);
        Button applyButton = sheetView.findViewById(R.id.applyFilterButton);

        TextView ratingAny = sheetView.findViewById(R.id.ratingAny);
        TextView rating3 = sheetView.findViewById(R.id.rating3);
        TextView rating4 = sheetView.findViewById(R.id.rating4);
        TextView rating45 = sheetView.findViewById(R.id.rating45);
        List<TextView> ratingChips = Arrays.asList(ratingAny, rating3, rating4, rating45);

        String[] sortOptions = {"По рейтингу ↓", "По цене ↑", "По цене ↓"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, sortOptions);
        sortSpinner.setAdapter(sortAdapter);

        int sortPosition = currentSortBy.equals("price_asc") ? 1 : (currentSortBy.equals("price_desc") ? 2 : 0);
        sortSpinner.setSelection(sortPosition);

        if (currentFilterCity != null) filterCity.setText(currentFilterCity);
        if (currentMinPrice > 0) minPriceInput.setText(String.valueOf((int) currentMinPrice));
        if (currentMaxPrice > 0) maxPriceInput.setText(String.valueOf((int) currentMaxPrice));

        int ratingPosition = 0;
        if (currentMinRating >= 4.5) ratingPosition = 3;
        else if (currentMinRating >= 4) ratingPosition = 2;
        else if (currentMinRating >= 3) ratingPosition = 1;

        for (int i = 0; i < ratingChips.size(); i++) {
            updateChipStyle(ratingChips.get(i), i == ratingPosition);
        }

        ratingAny.setOnClickListener(v -> {
            currentMinRating = 0;
            for (int i = 0; i < ratingChips.size(); i++) updateChipStyle(ratingChips.get(i), i == 0);
        });
        rating3.setOnClickListener(v -> {
            currentMinRating = 3;
            updateChipStyle(rating3, true);
            updateChipStyle(ratingAny, false);
            updateChipStyle(rating4, false);
            updateChipStyle(rating45, false);
        });
        rating4.setOnClickListener(v -> {
            currentMinRating = 4;
            updateChipStyle(rating4, true);
            updateChipStyle(ratingAny, false);
            updateChipStyle(rating3, false);
            updateChipStyle(rating45, false);
        });
        rating45.setOnClickListener(v -> {
            currentMinRating = 4.5;
            updateChipStyle(rating45, true);
            updateChipStyle(ratingAny, false);
            updateChipStyle(rating3, false);
            updateChipStyle(rating4, false);
        });

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, CITIES);
        filterCity.setAdapter(cityAdapter);
        filterCity.setThreshold(1);

        resetAll.setOnClickListener(v -> {
            filterCity.setText("");
            minPriceInput.setText("");
            maxPriceInput.setText("");
            sortSpinner.setSelection(0);
            ratingAny.performClick();
        });

        applyButton.setOnClickListener(v -> {
            String city = filterCity.getText().toString().trim();
            currentFilterCity = city.isEmpty() ? null : convertToRussianCity(city);

            String minStr = minPriceInput.getText().toString();
            currentMinPrice = minStr.isEmpty() ? 0 : Double.parseDouble(minStr);

            String maxStr = maxPriceInput.getText().toString();
            currentMaxPrice = maxStr.isEmpty() ? 0 : Double.parseDouble(maxStr);

            int sortPos = sortSpinner.getSelectedItemPosition();
            currentSortBy = sortPos == 1 ? "price_asc" : (sortPos == 2 ? "price_desc" : "rating_desc");

            searchHotels();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void updateChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.chip_selected);
            chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.chip_unselected);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_gray));
        }
    }

    private void updateActiveFiltersChips() {
        activeFiltersContainer.removeAllViews();
        List<String> activeFilters = new ArrayList<>();

        if (currentFilterCity != null && !currentFilterCity.isEmpty()) {
            activeFilters.add("🏙️ " + currentFilterCity);
        }
        if (currentMinPrice > 0) {
            activeFilters.add("💰 от " + (int) currentMinPrice + "₸");
        }
        if (currentMaxPrice > 0) {
            activeFilters.add("💰 до " + (int) currentMaxPrice + "₸");
        }
        if (currentMinRating > 0) {
            activeFilters.add("⭐ " + currentMinRating + "+");
        }

        if (activeFilters.isEmpty()) {
            activeFiltersContainer.setVisibility(View.GONE);
            return;
        }

        activeFiltersContainer.setVisibility(View.VISIBLE);
        for (String filter : activeFilters) {
            TextView chip = new TextView(requireContext());
            chip.setText(filter);
            chip.setPadding(32, 8, 32, 8);
            chip.setBackgroundResource(R.drawable.chip_selected);
            chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            chip.setTextSize(12);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(8);
            chip.setLayoutParams(params);
            activeFiltersContainer.addView(chip);
        }
    }

    private void updateUI(List<Hotel> hotels) {
        if (hotels.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            hotelAdapter = new HotelAdapter(hotels);
            recyclerView.setAdapter(hotelAdapter);
        }
    }

    private String convertToRussianCity(String city) {
        String lowerCity = city.toLowerCase().trim();
        switch (lowerCity) {
            case "almaty": return "Алматы";
            case "astana": return "Астана";
            case "shymkent": case "chimkent": return "Шымкент";
            case "nur-sultan": case "nursultan": return "Астана";
            default: return city;
        }
    }
}