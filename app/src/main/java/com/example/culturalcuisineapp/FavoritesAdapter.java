package com.example.culturalcuisineapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.culturalcuisineapp.databinding.ItemRecipeBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesViewHolder> {
    private FavoritesActivity favoritesActivity;
    private List<RecipeInfo> favoriteRecipes;
    private SharedPreferences sharedPreferences;
    private static final String FAVORITES_PREF = "favorites_preferences";

    public FavoritesAdapter(FavoritesActivity activity, List<RecipeInfo> recipes) {
        this.favoritesActivity = activity;
        this.favoriteRecipes = recipes;
        this.sharedPreferences = activity.getSharedPreferences(FAVORITES_PREF, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipeBinding binding = ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new FavoritesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        RecipeInfo recipeInfo = favoriteRecipes.get(position);
        holder.binding.tvRecipeName.setText(recipeInfo.getTitle());

        // Load image
        Picasso.get()
                .load(recipeInfo.getImageUrl())
                .fit()
                .centerCrop()
                .into(holder.binding.ivRecipeImage);

        // Always show filled heart in favorites
        holder.binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_24);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(favoritesActivity, RecipeDetailActivity.class);
            intent.putExtra("RECIPE_ID", recipeInfo.getRecipeId());
            favoritesActivity.startActivity(intent);
        });

        // Set click listener to remove from favorites
        holder.binding.btnFavorite.setOnClickListener(v -> {
            // Remove from SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("recipe_" + recipeInfo.getTitle());
            editor.putBoolean(recipeInfo.getTitle(), false);
            editor.apply();

            // Remove from list and update adapter
            favoriteRecipes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, favoriteRecipes.size());

            // Show empty state if needed
            if (favoriteRecipes.isEmpty()) {
                favoritesActivity.findViewById(R.id.tv_no_favorites).setVisibility(View.VISIBLE);
            }

            Toast.makeText(favoritesActivity, "Removed from favorites", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return favoriteRecipes.size();
    }
}