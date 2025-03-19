package com.example.culturalcuisineapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.culturalcuisineapp.databinding.ActivityRecipeDetailBinding;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RecipeDetailActivity extends AppCompatActivity {
    private ActivityRecipeDetailBinding binding;
    private static final String TAG = "RecipeDetailsActivity";
    private static final String API_KEY = "0329246c6cc845c8a9a8f9251a86f00a";
    private RequestQueue queue;
    private String recipeId;
    private RecipeInfo currentRecipe;
    private SharedPreferences sharedPreferences;
    private static final String FAVORITES_PREF = "favorites_preferences";
    private String sourceUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences(FAVORITES_PREF, Context.MODE_PRIVATE);
        recipeId = getIntent().getStringExtra("RECIPE_ID");
        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "Recipe ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.btnBack.setOnClickListener(v -> finish());
        queue = Volley.newRequestQueue(this);
        fetchRecipeDetails(recipeId);
        binding.btnViewFullRecipe.setOnClickListener(v -> {
            if (sourceUrl != null && !sourceUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl));
                startActivity(browserIntent);
            }
        });
    }

    private void fetchRecipeDetails(String recipeId) {
        String url = "https://api.spoonacular.com/recipes/" + recipeId + "/information";

        Uri.Builder builder = Uri.parse(url).buildUpon();
        builder.appendQueryParameter("apiKey", API_KEY);
        String urlToUse = builder.build().toString();

        Log.d(TAG, "URL: " + urlToUse);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                urlToUse,
                null,
                response -> parseRecipeDetails(response),
                error -> {
                    Log.e(TAG, "Error fetching recipe details: " + error.toString());
                    Toast.makeText(RecipeDetailActivity.this,
                            "Error loading recipe details", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    private void parseRecipeDetails(JSONObject response) {
        try {
            String title = response.getString("title");
            String imageUrl = response.getString("image");
            int readyInMinutes = response.getInt("readyInMinutes");
            int servings = response.getInt("servings");
            sourceUrl = response.getString("sourceUrl");
            String sourceName = response.getString("sourceName");
            boolean vegetarian = response.getBoolean("vegetarian");
            boolean vegan = response.getBoolean("vegan");
            boolean glutenFree = response.getBoolean("glutenFree");
            boolean dairyFree = response.getBoolean("dairyFree");
            double healthScore = response.getDouble("healthScore");
            double pricePerServing = response.getDouble("pricePerServing") / 100; // Convert to dollars

            String summary = response.getString("summary");
            summary = Html.fromHtml(summary, Html.FROM_HTML_MODE_LEGACY).toString();

            JSONArray cuisinesArray = response.getJSONArray("cuisines");
            StringBuilder cuisines = new StringBuilder();
            for (int i = 0; i < cuisinesArray.length(); i++) {
                if (i > 0) cuisines.append(", ");
                cuisines.append(cuisinesArray.getString(i));
            }

            JSONArray dishTypesArray = response.getJSONArray("dishTypes");
            StringBuilder dishTypes = new StringBuilder();
            for (int i = 0; i < Math.min(3, dishTypesArray.length()); i++) {
                if (i > 0) dishTypes.append(", ");
                dishTypes.append(dishTypesArray.getString(i));
            }

            String calories = extractFromSummary(summary, "calories");
            String protein = extractFromSummary(summary, "protein");
            String fat = extractFromSummary(summary, "fat");

            currentRecipe = new RecipeInfo(title, imageUrl);
            currentRecipe.setRecipeId(recipeId);

            updateUI(title, imageUrl, readyInMinutes, servings, vegetarian, vegan,
                    glutenFree, dairyFree, healthScore, calories, protein, fat,
                    summary, pricePerServing, cuisines.toString(),
                    dishTypes.toString(), sourceName);

            setupFavoriteButton();

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing recipe details: " + e.getMessage());
            Toast.makeText(this, "Error parsing recipe details", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractFromSummary(String summary, String nutrient) {

        String lowerSummary = summary.toLowerCase();
        String lowerNutrient = nutrient.toLowerCase();

        int index = lowerSummary.indexOf(lowerNutrient);

        if (index != -1) {
            int start = Math.max(0, index - 30);
            int end = Math.min(lowerSummary.length(), index + 30);
            String segment = lowerSummary.substring(start, end);

            if (segment.matches(".*?(\\d+(\\.\\d+)?)\\s*" + lowerNutrient + ".*")) {
                int valueStart = segment.lastIndexOf(" ", index - start - 1) + 1;
                int valueEnd = segment.indexOf(lowerNutrient, valueStart) - 1;
                if (valueStart >= 0 && valueEnd > valueStart) {
                    String value = segment.substring(valueStart, valueEnd).trim();
                    return value + " " + nutrient;
                }
            }
            String pattern2 = "\\d+(\\.\\d+)?g\\s+of\\s+" + lowerNutrient;
            int patternIndex = segment.indexOf("g of " + lowerNutrient);
            if (patternIndex != -1) {
                int numStart = segment.lastIndexOf(" ", patternIndex - 1) + 1;
                if (numStart >= 0) {
                    String value = segment.substring(numStart, patternIndex + 1).trim();
                    return value + " of " + nutrient;
                }
            }
        }
        return "N/A";
    }

    private void updateUI(String title, String imageUrl, int readyInMinutes, int servings,
                          boolean vegetarian, boolean vegan, boolean glutenFree,
                          boolean dairyFree, double healthScore, String calories,
                          String protein, String fat, String summary, double pricePerServing,
                          String cuisines, String dishTypes, String sourceName) {

        binding.tvRecipeTitle.setText(title);
        Picasso.get()
                .load(imageUrl)
                .into(binding.ivRecipeImage);
        binding.tvCookingTime.setText(readyInMinutes + " minutes");
        binding.tvServings.setText(servings + " servings");
        binding.tvVegetarian.setVisibility(vegetarian ? View.VISIBLE : View.GONE);
        binding.tvVegan.setVisibility(vegan ? View.VISIBLE : View.GONE);
        binding.tvGlutenFree.setVisibility(glutenFree ? View.VISIBLE : View.GONE);
        binding.tvDairyFree.setVisibility(dairyFree ? View.VISIBLE : View.GONE);
        binding.tvHealthScore.setText("Score: " + (int)healthScore);
        binding.tvCalories.setText(calories);
        binding.tvProtein.setText(protein);
        binding.tvFat.setText(fat);
        binding.tvRecipeSummary.setText(summary);
        binding.tvPrice.setText(String.format("$%.2f per serving", pricePerServing));
        binding.tvCuisine.setText(cuisines.isEmpty() ? "Not specified" : cuisines);
        binding.tvDishType.setText(dishTypes.isEmpty() ? "Not specified" : dishTypes);
        binding.tvSource.setText(sourceName);
    }

    private void setupFavoriteButton() {
        boolean isFavorite = sharedPreferences.getBoolean(currentRecipe.getTitle(), false);
        if (isFavorite) {
            binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
        }
        binding.btnFavorite.setOnClickListener(v -> {
            boolean newFavoriteStatus = !isFavorite;
            if (newFavoriteStatus) {
                binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(currentRecipe.getTitle(), newFavoriteStatus);
            if (newFavoriteStatus) {
                Gson gson = new Gson();
                String recipeJson = gson.toJson(currentRecipe);
                editor.putString("recipe_" + currentRecipe.getTitle(), recipeJson);
            } else {
                editor.remove("recipe_" + currentRecipe.getTitle());
            }

            editor.apply();
        });
    }
}