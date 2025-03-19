package com.example.culturalcuisineapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
    private Context context;
    private List<Restaurant> restaurants;

    public RestaurantAdapter(Context context, List<Restaurant> restaurants) {
        this.context = context;
        this.restaurants = restaurants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurants.get(position);

        holder.tvName.setText(restaurant.getName());
        if (restaurant.getCuisine() != null && !restaurant.getCuisine().isEmpty()) {
            holder.tvCuisine.setVisibility(View.VISIBLE);
            holder.tvCuisine.setText("Cuisine: " + restaurant.getCuisine());
        } else {
            holder.tvCuisine.setVisibility(View.GONE);
        }
        if (restaurant.getAddress() != null && !restaurant.getAddress().isEmpty()) {
            holder.tvAddress.setText(restaurant.getAddress());
        } else {
            holder.tvAddress.setText("Address not available");
        }
        holder.tvDistance.setText(String.format("%.1f meters away", restaurant.getDistance()));

        holder.itemView.setOnClickListener(v -> {
            openLocationInMap(restaurant);
        });
    }

    private void openLocationInMap(Restaurant restaurant) {

        Uri gmmIntentUri = Uri.parse("geo:" + restaurant.getLatitude() + "," +
                restaurant.getLongitude() + "?q=" + Uri.encode(restaurant.getName()));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.openstreetmap.org/?mlat=" +
                            restaurant.getLatitude() + "&mlon=" + restaurant.getLongitude() +
                            "&zoom=18"));
            context.startActivity(browserIntent);
        }
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCuisine, tvAddress, tvDistance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_restaurant_name);
            tvCuisine = itemView.findViewById(R.id.tv_restaurant_cuisine);
            tvAddress = itemView.findViewById(R.id.tv_restaurant_address);
            tvDistance = itemView.findViewById(R.id.tv_restaurant_distance);
        }
    }
}