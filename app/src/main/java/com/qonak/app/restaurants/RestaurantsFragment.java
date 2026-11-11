package com.qonak.app.restaurants;

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
import com.qonak.app.models.Restaurant;

import java.util.*;

public class RestaurantsFragment extends Fragment {

    private AppDatabase database;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private AutoCompleteTextView cityAutoComplete;
    private TextView guestsText;
    private Button searchButton;
    private LinearLayout filterChip;
    private LinearLayout activeFiltersContainer;
    private RestaurantAdapter adapter;

    // Параметры поиска
    private int adults = 1;
    private int children = 0;

    // Параметры кухонь
    private AutoCompleteTextView cuisineAutoComplete;
    private String selectedCuisine = null;
    private static final List<String> CUISINES = Arrays.asList(
            "Любая кухня", "Казахская", "Европейская", "Итальянская",
            "Французская", "Японская", "Паназиатская", "Китайская",
            "Корейская", "Узбекская", "Грузинская"
    );

    // Параметры фильтрации
    private String currentFilterCity = null;
    private double currentMinPrice = 0;
    private double currentMaxPrice = 0;
    private double currentMinRating = 0;
    private String currentSortBy = "rating_desc";

    private static final List<String> CITIES = Arrays.asList(
            "Алматы", "Астана", "Шымкент",
            "Almaty", "Astana", "Shymkent"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = AppDatabase.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.restaurantsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        cityAutoComplete = view.findViewById(R.id.cityAutoComplete);
        guestsText = view.findViewById(R.id.guestsText);
        searchButton = view.findViewById(R.id.searchButton);
        filterChip = view.findViewById(R.id.filterChip);
        activeFiltersContainer = view.findViewById(R.id.activeFiltersContainer);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Настройка автодополнения
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, CITIES);
        cityAutoComplete.setAdapter(cityAdapter);
        cityAutoComplete.setThreshold(1);

        // Обработчики
        guestsText.setOnClickListener(v -> showGuestsDialog());
        searchButton.setOnClickListener(v -> searchRestaurants());
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
            searchRestaurants();
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

        // Обработчики для выбора кухни
        TextView cuisineAny = view.findViewById(R.id.cuisineAny);
        TextView cuisineKazakh = view.findViewById(R.id.cuisineKazakh);
        TextView cuisineEuropean = view.findViewById(R.id.cuisineEuropean);
        TextView cuisineItalian = view.findViewById(R.id.cuisineItalian);
        TextView cuisineFrench = view.findViewById(R.id.cuisineFrench);
        TextView cuisineJapanese = view.findViewById(R.id.cuisineJapanese);
        TextView cuisinePanAsian = view.findViewById(R.id.cuisinePanAsian);
        TextView cuisineChinese = view.findViewById(R.id.cuisineChinese);
        TextView cuisineKorean = view.findViewById(R.id.cuisineKorean);
        TextView cuisineUzbek = view.findViewById(R.id.cuisineUzbek);
        TextView cuisineGeorgian = view.findViewById(R.id.cuisineGeorgian);
        TextView cuisineAmerican = view.findViewById(R.id.cuisineAmerican);

        List<TextView> cuisineChips = Arrays.asList(
                cuisineAny, cuisineKazakh, cuisineEuropean, cuisineItalian,
                cuisineFrench, cuisineJapanese, cuisinePanAsian, cuisineChinese,
                cuisineKorean, cuisineUzbek, cuisineGeorgian, cuisineAmerican
        );

        View.OnClickListener cuisineClickListener = v -> {
            TextView clicked = (TextView) v;
            String cuisine = clicked.getText().toString();

            if (cuisine.equals("Любая")) {
                selectedCuisine = null;
                for (TextView chip : cuisineChips) {
                    updateChipStyle(chip, chip == cuisineAny);
                }
            } else {
                selectedCuisine = cuisine;
                for (TextView chip : cuisineChips) {
                    updateChipStyle(chip, chip == clicked);
                }
            }
            searchRestaurants();
        };

        for (TextView chip : cuisineChips) {
            chip.setOnClickListener(cuisineClickListener);
        }

        updateChipStyle(cuisineAny, true);

        loadAllRestaurants();
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

    private void loadAllRestaurants() {
        progressBar.setVisibility(View.VISIBLE);

        AppExecutors.db().execute(() -> {
            List<Restaurant> restaurants = database
                    .restaurantDao()
                    .getAllRestaurants();

            AppExecutors.onUi(this, () -> {
                progressBar.setVisibility(View.GONE);
                updateUI(restaurants);
            });
        });
    }

    private void searchRestaurants() {
        String city = cityAutoComplete.getText().toString().trim();
        currentFilterCity = city.isEmpty() ? null : convertToRussianCity(city);

        progressBar.setVisibility(View.VISIBLE);

        AppExecutors.db().execute(() -> {
            List<Restaurant> filtered = database
                    .restaurantDao()
                    .filterRestaurantsWithCuisine(currentFilterCity, selectedCuisine,
                            currentMinPrice, currentMaxPrice, currentMinRating, currentSortBy);

            AppExecutors.onUi(this, () -> {
                progressBar.setVisibility(View.GONE);
                updateUI(filtered);
                updateActiveFiltersChips();

                String message = "Найдено: " + Formatters.restaurants(filtered.size());
                if (currentFilterCity != null) message += " в " + currentFilterCity;
                if (selectedCuisine != null) message += ", кухня: " + selectedCuisine;
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            });
        });
    }

    private void showBottomSheetFilter() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_restaurant, null);
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

            searchRestaurants();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void updateChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.chip_selected_restaurant);
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
        if (selectedCuisine != null && !selectedCuisine.isEmpty()) {
            activeFilters.add("🍳 " + selectedCuisine);
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

    private void updateUI(List<Restaurant> restaurants) {
        if (restaurants.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new RestaurantAdapter(restaurants);
            recyclerView.setAdapter(adapter);
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