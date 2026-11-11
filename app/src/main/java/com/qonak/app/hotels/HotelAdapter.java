package com.qonak.app.hotels;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.qonak.app.R;
import com.qonak.app.models.Hotel;
import com.qonak.app.utils.Formatters;
import com.qonak.app.utils.PlaceImages;
import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hotels;

    public HotelAdapter(List<Hotel> hotels) {
        this.hotels = hotels;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);
        holder.nameText.setText(hotel.name);
        holder.cityText.setText(hotel.city);
        holder.priceText.setText(Formatters.price(hotel.pricePerNight));
        holder.ratingText.setText("★ " + hotel.rating);
        holder.descriptionText.setText(hotel.description);

        holder.hotelImage.setImageResource(PlaceImages.hotel(hotel.id));

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), HotelDetailActivity.class);
            intent.putExtra(HotelDetailActivity.EXTRA_HOTEL_ID, hotel.id);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, cityText, priceText, ratingText, descriptionText;
        CardView cardView;
        ImageView hotelImage;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.hotelName);
            cityText = itemView.findViewById(R.id.hotelCity);
            priceText = itemView.findViewById(R.id.hotelPrice);
            ratingText = itemView.findViewById(R.id.hotelRating);
            descriptionText = itemView.findViewById(R.id.hotelDescription);
            cardView = itemView.findViewById(R.id.hotelCard);
            hotelImage = itemView.findViewById(R.id.hotelImage);
        }
    }
}