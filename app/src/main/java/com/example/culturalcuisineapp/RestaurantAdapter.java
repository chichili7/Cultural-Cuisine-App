package com.example.culturalcuisineapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
    private List<Restaurant> restaurantList;
    private Context context;

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
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
        Restaurant restaurant = restaurantList.get(position);

        holder.tvRestaurantName.setText(restaurant.getName());
        holder.tvRestaurantAddress.setText(restaurant.getAddress());

        // Format distance
        double distanceInKm = restaurant.getDistance() / 1000;
        String formattedDistance;
        if (distanceInKm < 1) {
            formattedDistance = String.format(Locale.getDefault(), "%.0f m", restaurant.getDistance());
        } else {
            formattedDistance = String.format(Locale.getDefault(), "%.1f km", distanceInKm);
        }
        holder.tvRestaurantDistance.setText(formattedDistance + " away");

        // Load image
        if (restaurant.getPhotoUrl() != null && !restaurant.getPhotoUrl().isEmpty()) {
            Picasso.get()
                    .load(restaurant.getPhotoUrl())
                    .resize(300,300)
                    .centerCrop()
                    .into(holder.ivRestaurantImage);
        } else {
//            holder.ivRestaurantImage.setImageResource(R.drawable.placeholder_restaurant);
        }
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRestaurantImage;
        TextView tvRestaurantName;
        TextView tvRestaurantAddress;
        TextView tvRestaurantDistance;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRestaurantImage = itemView.findViewById(R.id.ivRestaurantImage);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvRestaurantAddress = itemView.findViewById(R.id.tvRestaurantAddress);
            tvRestaurantDistance = itemView.findViewById(R.id.tvRestaurantDistance);
        }
    }
}
