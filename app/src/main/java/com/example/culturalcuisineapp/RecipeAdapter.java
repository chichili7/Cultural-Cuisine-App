package com.example.culturalcuisineapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.culturalcuisineapp.databinding.ItemRecipeBinding;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeViewHolder>{

    private SearchActivity searchActivity;
    private List<RecipeInfo> recipeInfoList;
    private SharedPreferences sharedPreferences;
    private static final String FAVORITES_PREF = "favorites_preferences";

    public RecipeAdapter(SearchActivity searchActivity, List<RecipeInfo> recipeInfoList){
        this.recipeInfoList = recipeInfoList;
        this.searchActivity = searchActivity;
        this.sharedPreferences = searchActivity.getSharedPreferences(FAVORITES_PREF, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipeBinding binding = ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent , false
        );
        return new RecipeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeInfo recipeInfo = recipeInfoList.get(position);
        holder.binding.tvRecipeName.setText(recipeInfo.getTitle());
        Picasso.get()
                .load(recipeInfo.getImageUrl())
                .fit()
                .centerCrop()
                .into(holder.binding.ivRecipeImage);
        boolean isFavorite = sharedPreferences.getBoolean(recipeInfo.getTitle(), false);
        if (isFavorite) {
            holder.binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            holder.binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
        }
        holder.binding.btnFavorite.setOnClickListener(v -> {
            boolean newFavoriteStatus = !isFavorite;
            if (newFavoriteStatus) {
                holder.binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
                Toast.makeText(searchActivity, "Added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                holder.binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                Toast.makeText(searchActivity, "Removed from favorites", Toast.LENGTH_SHORT).show();
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(recipeInfo.getTitle(), newFavoriteStatus);
            if (newFavoriteStatus) {
                Gson gson = new Gson();
                String recipeJson = gson.toJson(recipeInfo);
                editor.putString("recipe_" + recipeInfo.getTitle(), recipeJson);
            } else {
                editor.remove("recipe_" + recipeInfo.getTitle());
            }

            editor.apply();
        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(searchActivity, RecipeDetailActivity.class);
            intent.putExtra("RECIPE_ID", recipeInfo.getRecipeId());
            searchActivity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeInfoList.size();
    }
}
