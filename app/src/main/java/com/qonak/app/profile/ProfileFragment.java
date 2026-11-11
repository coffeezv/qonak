package com.qonak.app.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.qonak.app.R;
import com.qonak.app.bookings.BookingsFragment;
import com.qonak.app.database.AppDatabase;
import com.qonak.app.utils.AppExecutors;
import com.qonak.app.utils.Formatters;

import java.util.Date;

public class ProfileFragment extends Fragment {

    private TextView userName, userEmail, totalBookingsText, memberSinceText;
    private AppDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = AppDatabase.getInstance(requireContext());

        userName = view.findViewById(R.id.userName);
        userEmail = view.findViewById(R.id.userEmail);
        totalBookingsText = view.findViewById(R.id.totalBookings);
        memberSinceText = view.findViewById(R.id.memberSince);
        TextView clearDataButton = view.findViewById(R.id.clearDataButton);

        userName.setText("Гость");
        userEmail.setText("guest@qonak.kz");
        memberSinceText.setText(Formatters.dateFormat().format(new Date()));

        clearDataButton.setOnClickListener(v -> showClearDataDialog());
        loadUserStats();
    }

    private void loadUserStats() {
        AppExecutors.db().execute(() -> {
            int total = database.bookingDao().countByUser(BookingsFragment.GUEST_USER_ID);
            AppExecutors.onUi(this, () -> totalBookingsText.setText(String.valueOf(total)));
        });
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Очистка данных")
                .setMessage("Удалить все бронирования? Это действие нельзя отменить.")
                .setPositiveButton("Да, удалить", (dialog, which) -> clearAllBookings())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void clearAllBookings() {
        AppExecutors.db().execute(() -> {
            // One SQL statement instead of loading and deleting bookings one by one.
            database.bookingDao().deleteAllByUser(BookingsFragment.GUEST_USER_ID);
            AppExecutors.onUi(this, () -> {
                loadUserStats();
                Toast.makeText(getContext(), "Все бронирования удалены", Toast.LENGTH_LONG).show();
            });
        });
    }
}
