package com.example.culturalcuisineapp;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.culturalcuisineapp.databinding.ActivityNearbyRestaurantsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NearbyRestaurantsActivity extends AppCompatActivity {
    private static final String TAG = "NearbyRestaurants";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int RADIUS_METERS = 1000;
    private static final String OVERPASS_API_URL = "https://overpass-api.de/api/interpreter";

    private ActivityNearbyRestaurantsBinding binding;
    private boolean locationPermissionGranted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<Restaurant> restaurantList = new ArrayList<>();
    private RestaurantAdapter adapter;
    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNearbyRestaurantsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Nearby Restaurants (1000m)");
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RestaurantAdapter(this, restaurantList);
        binding.rvRestaurants.setAdapter(adapter);
        binding.rvRestaurants.setVisibility(View.GONE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            getUserLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                getUserLocation();
            } else {
                showError("Location permission is required to find nearby restaurants");
            }
        }
    }

    private void getUserLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Location lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            searchRestaurantsNearby(
                                    lastKnownLocation.getLatitude(),
                                    lastKnownLocation.getLongitude()
                            );
                        } else {
                            requestLocationUpdates();
                        }
                    } else {
                        Log.e(TAG, "Current location is null. Using defaults.");
                        showError("Unable to determine your location. Please try again later.");
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            showError("Error accessing location");
        }
    }

    private void requestLocationUpdates() {
        try {
            LocationRequest locationRequest = new LocationRequest.Builder(10000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setWaitForAccurateLocation(true)
                    .setMinUpdateIntervalMillis(5000)
                    .setMaxUpdateDelayMillis(15000)
                    .build();

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        searchRestaurantsNearby(location.getLatitude(), location.getLongitude());
                        fusedLocationProviderClient.removeLocationUpdates(this);
                    }
                }
            };

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());

        } catch (SecurityException e) {
            Log.e(TAG, "Exception requesting location updates: " + e.getMessage());
            showError("Error requesting location updates");
        }
    }

    private void searchRestaurantsNearby(double latitude, double longitude) {
        Log.d(TAG, "Searching for restaurants near: " + latitude + ", " + longitude);

        // Create Overpass QL query
        String query = "[out:json];" +
                "(" +
                "  node[\"amenity\"=\"restaurant\"](around:" + RADIUS_METERS + "," +
                latitude + "," + longitude + ");" +
                "  node[\"amenity\"=\"cafe\"](around:" + RADIUS_METERS + "," +
                latitude + "," + longitude + ");" +
                "  node[\"amenity\"=\"fast_food\"](around:" + RADIUS_METERS + "," +
                latitude + "," + longitude + ");" +
                "  node[\"amenity\"=\"bar\"](around:" + RADIUS_METERS + "," +
                latitude + "," + longitude + ");" +
                ");" +
                "out body;";
        RequestBody requestBody = RequestBody.create(
                query, MediaType.parse("text/plain"));

        Request request = new Request.Builder()
                .url(OVERPASS_API_URL)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Overpass API request failed: " + e.getMessage());
                runOnUiThread(() -> showError("Failed to fetch restaurants: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    parseOverpassResponse(responseData, latitude, longitude);
                } else {
                    Log.e(TAG, "Overpass API error: " + response.code() + " - " + response.message());
                    runOnUiThread(() -> showError("Error from Overpass API: " + response.message()));
                }
            }
        });
    }

    private void parseOverpassResponse(String responseJson, double userLat, double userLon) {
        try {
            JSONObject jsonObject = new JSONObject(responseJson);
            JSONArray elementsArray = jsonObject.getJSONArray("elements");

            List<Restaurant> restaurants = new ArrayList<>();

            for (int i = 0; i < elementsArray.length(); i++) {
                JSONObject element = elementsArray.getJSONObject(i);

                if (element.getString("type").equals("node")) {
                    String id = String.valueOf(element.getLong("id"));
                    double lat = element.getDouble("lat");
                    double lon = element.getDouble("lon");
                    JSONObject tags = element.optJSONObject("tags");
                    if (tags != null) {
                        String name = tags.optString("name", "Unnamed Restaurant");
                        String amenity = tags.optString("amenity", "");
                        String cuisine = tags.optString("cuisine", "");
                        StringBuilder addressBuilder = new StringBuilder();
                        if (tags.has("addr:housenumber")) {
                            addressBuilder.append(tags.getString("addr:housenumber")).append(" ");
                        }
                        if (tags.has("addr:street")) {
                            addressBuilder.append(tags.getString("addr:street")).append(", ");
                        }
                        if (tags.has("addr:city")) {
                            addressBuilder.append(tags.getString("addr:city")).append(", ");
                        }
                        if (tags.has("addr:postcode")) {
                            addressBuilder.append(tags.getString("addr:postcode"));
                        }

                        String address = addressBuilder.toString();
                        if (address.endsWith(", ")) {
                            address = address.substring(0, address.length() - 2);
                        }
                        float[] distance = new float[1];
                        Location.distanceBetween(userLat, userLon, lat, lon, distance);
                        Restaurant restaurant = new Restaurant(
                                id, name, amenity, cuisine, address,
                                lat, lon, distance[0]);

                        restaurants.add(restaurant);
                    }
                }
            }
            Collections.sort(restaurants, Comparator.comparingDouble(Restaurant::getDistance));
            runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);
                if (restaurants.isEmpty()) {
                    binding.rvRestaurants.setVisibility(View.GONE);
                } else {
                    binding.rvRestaurants.setVisibility(View.VISIBLE);

                    restaurantList.clear();
                    restaurantList.addAll(restaurants);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(NearbyRestaurantsActivity.this,
                            "Found " + restaurants.size() + " restaurants within 1000 meters",
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing Overpass response: " + e.getMessage());
            runOnUiThread(() -> showError("Error processing restaurant data"));
        }
    }

    private void showError(String message) {
        binding.rvRestaurants.setVisibility(View.GONE);

        Log.e(TAG, "Error: " + message);
    }
}