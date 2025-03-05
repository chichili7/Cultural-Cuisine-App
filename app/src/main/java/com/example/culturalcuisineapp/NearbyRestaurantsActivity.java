package com.example.culturalcuisineapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.culturalcuisineapp.databinding.ActivityNearbyRestaurantsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NearbyRestaurantsActivity extends AppCompatActivity {

    private static final String TAG = "NearbyRestaurantActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int RADIUS_METERS = 1000;
    private static final int LOCATION_TIMEOUT_MS = 15000; // 15 seconds timeout

    private ActivityNearbyRestaurantsBinding binding;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<Restaurant> restaurantList;
    private RestaurantAdapter adapter;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNearbyRestaurantsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize UI first
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoRestaurants.setVisibility(View.GONE);

        // Initialize RecyclerView before any heavy operations
        restaurantList = new ArrayList<>();
        adapter = new RestaurantAdapter(this, restaurantList);
        binding.rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRestaurants.setAdapter(adapter);

        // Set up back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Initialize location services
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Places API in background
        executor.execute(() -> {
            try {
                // Use a real API key here
                String apiKey = "AIzaSyDaEi-WmB41Tq5mdENwzbbvXNatWkff8PY";
                if (!Places.isInitialized()) {
                    Places.initialize(getApplicationContext(), apiKey);
                }

                mainHandler.post(() -> {
                    placesClient = Places.createClient(this);
                    // Get location permission after Places API is initialized
                    getLocationPermission();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Places API: " + e.getMessage());
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvNoRestaurants.setVisibility(View.VISIBLE);
                    binding.tvNoRestaurants.setText("Error initializing location services. Please try again.");
                });
            }
        });
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                getDeviceLocation();
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvNoRestaurants.setVisibility(View.VISIBLE);
                binding.tvNoRestaurants.setText("Location permission denied. Cannot show nearby restaurants.");
            }
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                // Set timeout for location request
                Handler timeoutHandler = new Handler(Looper.getMainLooper());
                Runnable timeoutRunnable = () -> {
                    if (lastKnownLocation == null) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvNoRestaurants.setVisibility(View.VISIBLE);
                        binding.tvNoRestaurants.setText("Location request timed out. Please try again.");
                    }
                };
                timeoutHandler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT_MS);

                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    // Cancel timeout
                    timeoutHandler.removeCallbacks(timeoutRunnable);

                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation == null) {
                            // Create a mock location for testing (this is San Francisco)
                            lastKnownLocation = new Location("mock");
                            lastKnownLocation.setLatitude(37.7749);
                            lastKnownLocation.setLongitude(-122.4194);

                            // Add debug log
                            Log.d(TAG, "Using mock location for testing");
                        }

                        // Continue with searching nearby restaurants
                        executor.execute(this::searchNearbyRestaurants);
                    }else {
                        Log.e(TAG, "Exception: %s", task.getException());
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvNoRestaurants.setVisibility(View.VISIBLE);
                        binding.tvNoRestaurants.setText("Error finding your location. Please try again.");
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            binding.progressBar.setVisibility(View.GONE);
            binding.tvNoRestaurants.setVisibility(View.VISIBLE);
            binding.tvNoRestaurants.setText("Security error accessing location. Please try again.");
        }
    }

    private void searchNearbyRestaurants() {
        if (lastKnownLocation == null) {
            mainHandler.post(() -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvNoRestaurants.setVisibility(View.VISIBLE);
                binding.tvNoRestaurants.setText("No location available");
            });
            return;
        }

        try {
            // Use Places API to find nearby restaurants
            List<Place.Field> placeFields = Arrays.asList(
                    Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES,
                    Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS);

            FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvNoRestaurants.setVisibility(View.VISIBLE);
                    binding.tvNoRestaurants.setText("Location permission required");
                });
                return;
            }

            placesClient.findCurrentPlace(request).addOnSuccessListener(response -> {
                // Process results on background thread
                executor.execute(() -> {
                    List<Restaurant> tempList = new ArrayList<>();

                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Place place = placeLikelihood.getPlace();

                        // Check if the place is a restaurant and within 1000 meters
                        if (isRestaurant(place) && isWithinRadius(place.getLatLng(), RADIUS_METERS)) {
                            float[] distance = new float[1];
                            Location.distanceBetween(
                                    lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                                    place.getLatLng().latitude, place.getLatLng().longitude, distance);

                            // Get the first photo if available
                            String photoUrl = "";
                            List<PhotoMetadata> photoMetadata = place.getPhotoMetadatas();
                            if (photoMetadata != null && !photoMetadata.isEmpty()) {
                                PhotoMetadata firstPhoto = photoMetadata.get(0);
                                // Use your real API key here
                                photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                                        "?maxwidth=400" +
                                        "&photo_reference=" + firstPhoto.zza() +
                                        "&key=AIzaSyDaEi-WmB41Tq5mdENwzbbvXNatWkff8PY";
                            }

                            Restaurant restaurant = new Restaurant(
                                    place.getName(),
                                    place.getAddress(),
                                    photoUrl,
                                    distance[0]
                            );

                            tempList.add(restaurant);
                        }
                    }

                    // Sort restaurants by distance
                    Collections.sort(tempList, Comparator.comparingDouble(Restaurant::getDistance));

                    // Update UI on main thread
                    mainHandler.post(() -> {
                        restaurantList.clear();
                        restaurantList.addAll(tempList);
                        binding.progressBar.setVisibility(View.GONE);

                        if (restaurantList.isEmpty()) {
                            binding.tvNoRestaurants.setVisibility(View.VISIBLE);
                            binding.tvNoRestaurants.setText("No restaurants found within 1000 meters");
                        } else {
                            binding.tvNoRestaurants.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }
                    });
                });
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Place search failed: " + exception.getMessage());
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvNoRestaurants.setVisibility(View.VISIBLE);
                    binding.tvNoRestaurants.setText("Error finding nearby restaurants: " + exception.getMessage());
                });
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in searchNearbyRestaurants: " + e.getMessage());
            mainHandler.post(() -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvNoRestaurants.setVisibility(View.VISIBLE);
                binding.tvNoRestaurants.setText("Error searching for restaurants: " + e.getMessage());
            });
        }
    }

    private boolean isRestaurant(Place place) {
        List<Place.Type> types = place.getTypes();
        if (types == null) return false;

        return types.contains(Place.Type.RESTAURANT) ||
                types.contains(Place.Type.CAFE) ||
                types.contains(Place.Type.BAKERY) ||
                types.contains(Place.Type.BAR) ||
                types.contains(Place.Type.MEAL_TAKEAWAY) ||
                types.contains(Place.Type.MEAL_DELIVERY);
    }

    private boolean isWithinRadius(LatLng placeLatLng, int radiusMeters) {
        if (lastKnownLocation == null || placeLatLng == null) return false;

        float[] distance = new float[1];
        Location.distanceBetween(
                lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                placeLatLng.latitude, placeLatLng.longitude, distance);

        return distance[0] <= radiusMeters;
    }
}