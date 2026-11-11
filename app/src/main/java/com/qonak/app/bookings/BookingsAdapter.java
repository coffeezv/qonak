package com.qonak.app.bookings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.qonak.app.R;
import com.qonak.app.database.AppDatabase;
import com.qonak.app.models.Booking;
import com.qonak.app.utils.AppExecutors;
import com.qonak.app.utils.Formatters;

import java.util.Date;
import java.util.List;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {

    private final List<Booking> bookings;
    private final AppDatabase database;
    private final Runnable onDataChanged;

    public BookingsAdapter(List<Booking> bookings, AppDatabase database, Runnable onDataChanged) {
        this.bookings = bookings;
        this.database = database;
        this.onDataChanged = onDataChanged;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        holder.typeText.setText(booking.isHotel() ? "🏨 Отель" : "🍽 Ресторан");
        holder.nameText.setText(booking.itemName);
        holder.cityText.setText(booking.city);

        if (booking.isHotel()) {
            holder.detailText.setText("📅 " + booking.date + " | " + Formatters.nights(booking.nights));
        } else {
            holder.detailText.setText("📅 " + booking.date + " | 🕐 " + booking.time
                    + " | 👥 " + Formatters.guests(booking.persons));
        }

        holder.priceText.setText(Formatters.price(booking.totalPrice));
        holder.dateText.setText("Забронировано: "
                + Formatters.dateFormat().format(new Date(booking.bookingTimestamp)));

        holder.cardView.setOnLongClickListener(v -> {
            showDeleteDialog(booking, v);
            return true;
        });
    }

    private void showDeleteDialog(Booking booking, View anchor) {
        new AlertDialog.Builder(anchor.getContext())
                .setTitle("Отмена бронирования")
                .setMessage("Отменить бронирование «" + booking.itemName + "»?")
                .setPositiveButton("Да", (dialog, which) -> deleteBooking(booking, anchor))
                .setNegativeButton("Нет", null)
                .show();
    }

    private void deleteBooking(Booking booking, View anchor) {
        AppExecutors.db().execute(() -> {
            database.bookingDao().deleteBooking(booking.id);
            AppExecutors.main(() -> {
                // Look the item up again: its position may have changed since the dialog opened.
                int index = bookings.indexOf(booking);
                if (index >= 0) {
                    bookings.remove(index);
                    notifyItemRemoved(index);
                }
                Toast.makeText(anchor.getContext(), "Бронирование отменено", Toast.LENGTH_SHORT).show();
                if (onDataChanged != null) onDataChanged.run();
            });
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        final TextView typeText, nameText, cityText, detailText, priceText, dateText;
        final CardView cardView;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.bookingType);
            nameText = itemView.findViewById(R.id.bookingName);
            cityText = itemView.findViewById(R.id.bookingCity);
            detailText = itemView.findViewById(R.id.bookingDetail);
            priceText = itemView.findViewById(R.id.bookingPrice);
            dateText = itemView.findViewById(R.id.bookingDate);
            cardView = itemView.findViewById(R.id.bookingCard);
        }
    }
}
