package com.example.culturalcuisineapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.culturalcuisineapp.databinding.ActivityFavoritesBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoritesActivity extends AppCompatActivity {
    private ActivityFavoritesBinding binding;
    private List<RecipeInfo> favoriteRecipes;
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private static final String FAVORITES_PREF = "favorites_preferences";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recyclerView = binding.rvFavorites;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setSelectedItemId(R.id.nav_favorites);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_favorites) {
                return true;
            } else if (item.getItemId() == R.id.nav_account) {
                showAccountPopup();
                return true;
            }
            return false;
        });
        loadFavoriteRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteRecipes();
    }

    private void loadFavoriteRecipes() {
        favoriteRecipes = new ArrayList<>();
        sharedPreferences = getSharedPreferences(FAVORITES_PREF, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        Gson gson = new Gson();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("recipe_")) {
                String recipeJson = (String) entry.getValue();
                RecipeInfo recipeInfo = gson.fromJson(recipeJson, RecipeInfo.class);
                if (recipeInfo != null && recipeInfo.getRecipeId() != null && !recipeInfo.getRecipeId().isEmpty()) {
                    favoriteRecipes.add(recipeInfo);
                }
            }
        }
        if (favoriteRecipes.isEmpty()) {
            binding.tvNoFavorites.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoFavorites.setVisibility(View.GONE);
        }
        FavoritesAdapter adapter = new FavoritesAdapter(this, favoriteRecipes);
        recyclerView.setAdapter(adapter);
    }
    private void showAccountPopup() {

    }
}