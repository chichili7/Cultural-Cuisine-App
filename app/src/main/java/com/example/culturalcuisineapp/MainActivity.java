package com.example.culturalcuisineapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.culturalcuisineapp.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private RequestQueue queue;
    private static final String TAG = "main";
    private List<CuisineInfo> cuisineInfoList;
    private RecyclerView recyclerView;
    private CuisineAdapter cuisineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        // In your Application class or MainActivity.onCreate
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2; // Load images at half the resolution to save memory

        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                if (!(this instanceof MainActivity)) {
                    Intent homeIntent = new Intent(this, MainActivity.class);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                    finish();
                }
            } else if (item.getItemId() == R.id.nav_favorites) {
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
            } else {
                showAccountPopup();
            }
            return true;
        });
        binding.btnNearby.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NearbyRestaurantsActivity.class);
            startActivity(intent);
        });
        cuisineInfoList = new ArrayList<>();
        recyclerView = binding.rvCuisines;
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));

        cuisineAdapter = new CuisineAdapter(this,cuisineInfoList);
        recyclerView.setAdapter(cuisineAdapter);
        loadHardcodedCuisines();
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
    private void loadHardcodedCuisines() {
        cuisineInfoList.clear();

        // Create a map of cuisine names to drawable resource IDs
        Map<String, Integer> cuisineImages = new HashMap<>();
        cuisineImages.put("African", R.drawable.african);
        cuisineImages.put("American", R.drawable.american);
        cuisineImages.put("British", R.drawable.british);
        cuisineImages.put("Cajun", R.drawable.cajun);
        cuisineImages.put("Caribbean", R.drawable.caribbean);
        cuisineImages.put("Chinese", R.drawable.chinease);
        cuisineImages.put("Eastern European", R.drawable.easterneuropean);
        cuisineImages.put("European", R.drawable.european);
        cuisineImages.put("French", R.drawable.french);
        cuisineImages.put("German", R.drawable.german);
        cuisineImages.put("Greek", R.drawable.greek);
        cuisineImages.put("Indian", R.drawable.indian);
        cuisineImages.put("Irish", R.drawable.irish);
        cuisineImages.put("Italian", R.drawable.italian);
        cuisineImages.put("Japanese", R.drawable.japanese);
        cuisineImages.put("Jewish", R.drawable.jewish);
        cuisineImages.put("Korean", R.drawable.korean);
        cuisineImages.put("Latin American", R.drawable.latinamerica);
        cuisineImages.put("Mediterranean", R.drawable.mediterrian);
        cuisineImages.put("Mexican", R.drawable.mexican);
        cuisineImages.put("Middle Eastern", R.drawable.middleeastern);
        cuisineImages.put("Nordic", R.drawable.nordic);
        cuisineImages.put("Southern", R.drawable.southern);
        cuisineImages.put("Spanish", R.drawable.spanish);
        cuisineImages.put("Thai", R.drawable.thai);
        cuisineImages.put("Vietnamese", R.drawable.vietnamese);

        String[] cuisines = {
                "African", "American", "British", "Cajun", "Caribbean", "Chinese",
                "Eastern European", "European", "French", "German", "Greek", "Indian",
                "Irish", "Italian", "Japanese", "Jewish", "Korean", "Latin American",
                "Mediterranean", "Mexican", "Middle Eastern", "Nordic", "Southern",
                "Spanish", "Thai", "Vietnamese"
        };

        for (String cuisine : cuisines) {
            CuisineInfo cuisineInfo = new CuisineInfo(cuisine);

            // Get image resource ID from map, or use default if not found
            Integer resId = cuisineImages.get(cuisine);
            if (resId != null) {
                cuisineInfo.setImageResourceId(resId);
            } else {
                cuisineInfo.setImageResourceId(R.drawable.american);
            }

            cuisineInfoList.add(cuisineInfo);
        }

        cuisineAdapter.notifyDataSetChanged();
    }


    private void showUserId() {

        String userId = "User12345"; // Replace with actual user ID from authentication
        Toast.makeText(this, "User ID: " + userId, Toast.LENGTH_LONG).show();
    }

    private void performLogout() {
        Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}