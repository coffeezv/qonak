package com.qonak.app.bookings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qonak.app.R;
import com.qonak.app.database.AppDatabase;
import com.qonak.app.models.Booking;
import com.qonak.app.utils.AppExecutors;

import java.util.List;

public class BookingsFragment extends Fragment {

    /** The app has no accounts yet, so every booking belongs to the guest user. */
    public static final long GUEST_USER_ID = 1;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private AppDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = AppDatabase.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.bookingsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload every time the tab is shown: a booking may have been made on another screen.
        loadBookings();
    }

    private void loadBookings() {
        progressBar.setVisibility(View.VISIBLE);
        AppExecutors.db().execute(() -> {
            List<Booking> bookings = database.bookingDao().getBookingsByUser(GUEST_USER_ID);
            AppExecutors.onUi(this, () -> {
                progressBar.setVisibility(View.GONE);
                recyclerView.setAdapter(new BookingsAdapter(bookings, database,
                        () -> showEmptyState(bookings.isEmpty())));
                showEmptyState(bookings.isEmpty());
            });
        });
    }

    private void showEmptyState(boolean empty) {
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
