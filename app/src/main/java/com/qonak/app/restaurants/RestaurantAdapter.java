package com.qonak.app.restaurants;

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
import com.qonak.app.models.Restaurant;
import com.qonak.app.utils.Formatters;
import com.qonak.app.utils.PlaceImages;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private List<Restaurant> restaurants;

    public RestaurantAdapter(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurants.get(position);
        holder.nameText.setText(restaurant.name);
        holder.cityText.setText(restaurant.city);
        holder.cuisineText.setText("🍳 " + restaurant.cuisine);
        holder.priceText.setText(Formatters.price(restaurant.avgPricePerPerson) + "/чел");
        holder.ratingText.setText("★ " + restaurant.rating);
        holder.descriptionText.setText(restaurant.description);

        holder.restaurantImage.setImageResource(PlaceImages.restaurant(restaurant.id));

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), RestaurantDetailActivity.class);
            intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurant.id);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, cityText, cuisineText, priceText, ratingText, descriptionText;
        CardView cardView;
        ImageView restaurantImage;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.restaurantName);
            cityText = itemView.findViewById(R.id.restaurantCity);
            cuisineText = itemView.findViewById(R.id.restaurantCuisine);
            priceText = itemView.findViewById(R.id.restaurantPrice);
            ratingText = itemView.findViewById(R.id.restaurantRating);
            descriptionText = itemView.findViewById(R.id.restaurantDescription);
            cardView = itemView.findViewById(R.id.restaurantCard);
            restaurantImage = itemView.findViewById(R.id.restaurantImage);
        }
    }
}