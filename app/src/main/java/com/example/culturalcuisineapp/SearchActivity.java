package com.example.culturalcuisineapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.culturalcuisineapp.databinding.ActivitySearchBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    private String Url = "https://api.spoonacular.com/recipes/complexSearch";
    private RequestQueue queue;
    private ActivitySearchBinding binding;
    private String API_KEY = "0329246c6cc845c8a9a8f9251a86f00a";
    private static final String TAG = "searchActivity";
    private List<RecipeInfo>recipeInfoList;
    private List<RecipeInfo> filteredRecipeList;
    private RecyclerView recyclerView;
    private TextInputEditText searchEditText;
    private RecipeAdapter recipeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_favorites) {
                Intent intent = new Intent(SearchActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            } else if(item.getItemId() == R.id.nav_account){
                showAccountPopup();
                return true;
            }
            return false;
        });

        // Initialize search bar
        TextInputLayout searchInputLayout = findViewById(R.id.searchbar);
        searchEditText = (TextInputEditText) searchInputLayout.getEditText();
        setupSearch();

        // Initialize RecyclerView
        recipeInfoList = new ArrayList<>();
        filteredRecipeList = new ArrayList<>();
        recyclerView = binding.rvCuisines;
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // Initialize adapter
        recipeAdapter = new RecipeAdapter(this, filteredRecipeList);
        recyclerView.setAdapter(recipeAdapter);

        // Load recipes based on cuisine
        String cuisineName = getIntent().getStringExtra("CUISINE_NAME");
        if (cuisineName != null && !cuisineName.isEmpty()) {
            searchRecipesByCuisine(cuisineName);
        }
    }
    private void showAccountPopup() {
        View view = findViewById(R.id.nav_account);
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.account_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.user_id) {
                showUserId();
            } else {
                performLogout();

            }
            return true;
        });

        popupMenu.show();
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter recipes as text changes
                filterRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Set action listener for the keyboard search button
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard when search is pressed
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void filterRecipes(String query) {
        filteredRecipeList.clear();

        if (query.isEmpty()) {
            // If query is empty, show all recipes
            filteredRecipeList.addAll(recipeInfoList);
        } else {
            // Filter recipes based on query
            String lowerCaseQuery = query.toLowerCase();
            for (RecipeInfo recipe : recipeInfoList) {
                if (recipe.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredRecipeList.add(recipe);
                }
            }
        }

        // Update the adapter
        recipeAdapter.notifyDataSetChanged();
    }
    public void searchRecipesByCuisine(String cuisineName) {
        queue = Volley.newRequestQueue(this);
        Uri.Builder builder = Uri.parse(Url).buildUpon();
        builder.appendQueryParameter("apiKey", API_KEY);
        builder.appendQueryParameter("cuisine",cuisineName);
        String urlToUse = builder.build().toString();
        Log.d(TAG, "URL: " + urlToUse);
        downloadDirections(urlToUse);

    }

    public void downloadDirections(String Url) {

        Response.Listener<JSONObject> listener =
                response -> parsing(response.toString());
        Response.ErrorListener error = error1 -> {
            Log.e(TAG, "Error: " + error1.toString());
            if (error1.networkResponse != null) {
                String errorResponse = new String(error1.networkResponse.data);
                Log.e(TAG, "Error Response: " + errorResponse);
                Log.e(TAG, "Status Code: " + error1.networkResponse.statusCode);
            }

            String message = error1.getMessage();
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.d(TAG, "download: " + message);
            }
        };
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, Url,
                        null, listener, error) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Accept", "application/json");
                        return headers;
                    }
                };
        queue.add(jsonObjectRequest);
    }

    public void parsing(String s) {
        recipeInfoList.clear();
        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray resultsArray = jObjMain.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject recipeObj = resultsArray.getJSONObject(i);
                String recipeId = recipeObj.getString("id");
                String recipeName = recipeObj.getString("title");
                String imageUrl = recipeObj.getString("image");
                RecipeInfo recipeInfo = new RecipeInfo(recipeName,imageUrl);
                recipeInfo.setRecipeId(recipeId);
                recipeInfoList.add(recipeInfo);
            }

            filteredRecipeList.addAll(recipeInfoList);
            recipeAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(TAG, "Parse Error: " + e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }
    private void showUserId() {

        String userId = "User12345"; // Replace with actual user ID from authentication
        Toast.makeText(this, "User ID: " + userId, Toast.LENGTH_LONG).show();
    }

    private void performLogout() {
        Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SearchActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}