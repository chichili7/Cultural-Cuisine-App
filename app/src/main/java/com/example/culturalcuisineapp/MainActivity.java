package com.example.culturalcuisineapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.RequestQueue;
import com.example.culturalcuisineapp.databinding.ActivityMainBinding;
import com.example.culturalcuisineapp.databinding.LoginDialogBinding;
import com.example.culturalcuisineapp.databinding.RegristrationDialogBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private RequestQueue queue;
    private static final String TAG = "main";
    private List<CuisineInfo> cuisineInfoList;
    private RecyclerView recyclerView;
    private CuisineAdapter cuisineAdapter;
    private SharedPreferences pref;
    private static final String FILE_NAME = "details";
    private static final String USER_NAME = "user";
    private static final String PASSWORD = "pass";
    private static final String REMEMBER_CREDENTIALS = "remember_credentials";
    private String firstName;
    private String lastName;
    private String email;
    private String userName;
    private String password;
    private static final String IS_LOGGED_IN = "is_logged_in";

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
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
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
        initializeSecureSharedPreferences();
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
        boolean isLoggedIn = pref.getBoolean(IS_LOGGED_IN, false);
        if (isLoggedIn) {
            binding.main.setVisibility(View.VISIBLE);
            loadHardcodedCuisines();
        } else {
            showLoginDialog();
            loadHardcodedCuisines();
        }
    }

    private void initializeSecureSharedPreferences() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pref = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            } else {
                pref = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing secure SharedPreferences: " + e.getMessage());
            pref = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        }
    }

    private void showLoginDialog() {
        binding.main.setVisibility(View.GONE);
        Dialog loginDialog = new Dialog(this);
        loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LoginDialogBinding dialogBinding = LoginDialogBinding.inflate(getLayoutInflater());
        loginDialog.setContentView(dialogBinding.getRoot());

        WindowManager.LayoutParams params = loginDialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        loginDialog.getWindow().setAttributes(params);
        loginDialog.setCancelable(false);

        loadSavedCredentials(dialogBinding);

        dialogBinding.login.setOnClickListener(view -> {
            userName = dialogBinding.usernameText.getText().toString().trim();
            String password = dialogBinding.passwordText.getText().toString().trim();
            boolean saveCredentials = dialogBinding.saveCredentials.isChecked();

            if (userName.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                if (saveCredentials) {
                    saveCredentials(userName, password);
                } else {
                    clearSavedCredentials();
                }
                runOnUiThread(() -> {
                    VerifyCredentials credentialsCheckerAPIVolley = new VerifyCredentials(MainActivity.this);
                    credentialsCheckerAPIVolley.checkCredentials(userName, password);
                    loginDialog.dismiss();
                });
            }).start();
        });
        dialogBinding.register.setOnClickListener(view -> {
            loginDialog.dismiss();
            showRegisterDialog();
        });
        dialogBinding.cancel.setOnClickListener(view -> {
            loginDialog.dismiss();
        });
        loginDialog.show();
    }

    private void saveCredentials(String username, String password) {
        try {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(REMEMBER_CREDENTIALS, true);
            editor.putString(USER_NAME, username);
            editor.putString(PASSWORD, password);
            editor.apply();
            Log.d(TAG, "Credentials saved for user: " + username);
        } catch (Exception e) {
            Log.e(TAG, "Error saving credentials: " + e.getMessage());
        }
    }

    private void loadSavedCredentials(LoginDialogBinding dialogBinding) {
        if (pref.contains(USER_NAME) && pref.contains(PASSWORD) && pref.getBoolean(REMEMBER_CREDENTIALS, false)) {
            String savedUsername = pref.getString(USER_NAME, "");
            String savedPassword = pref.getString(PASSWORD, "");
            if (!savedUsername.isEmpty() && !savedPassword.isEmpty()) {
                dialogBinding.usernameText.setText(savedUsername);
                dialogBinding.passwordText.setText(savedPassword);
                dialogBinding.saveCredentials.setChecked(true);
            }
        }
    }

    private void clearSavedCredentials() {
        try {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(REMEMBER_CREDENTIALS, false);
            editor.remove(USER_NAME);
            editor.remove(PASSWORD);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing credentials: " + e.getMessage());
        }
    }

    public void showRegisterDialog() {
        Dialog registerDialog = new Dialog(this);
        registerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        RegristrationDialogBinding dialogBinding = RegristrationDialogBinding.inflate(getLayoutInflater());
        registerDialog.setContentView(dialogBinding.getRoot());
        WindowManager.LayoutParams params = registerDialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        registerDialog.getWindow().setAttributes(params);
        registerDialog.setCancelable(false);
        dialogBinding.register.setOnClickListener(view -> {

            firstName = dialogBinding.firstnameText.getText().toString();
            lastName = dialogBinding.lastnameText.getText().toString();
            email = dialogBinding.emailText.getText().toString();
            userName = dialogBinding.usernameText.getText().toString();
            password = dialogBinding.passwordText.getText().toString();
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                    userName.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                Log.d(TAG, "Registration: " + firstName + " " + lastName + " " + email + " " + userName);
                runOnUiThread(() -> {
                    CreateUserAccountAPIVolley createUserAccountAPIVolley = new CreateUserAccountAPIVolley(MainActivity.this);
                    createUserAccountAPIVolley.createUser(firstName, lastName, email, userName, password);

                    Toast.makeText(MainActivity.this, "Registration in progress...", Toast.LENGTH_SHORT).show();
                    registerDialog.dismiss();
                });
            }).start();
        });

        dialogBinding.cancel.setOnClickListener(view -> {
            registerDialog.dismiss();
            showLoginDialog();
        });
        registerDialog.show();

    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void handleCreateUserAccountSuccess(String firstName, String lastName,
                                               String email, String userName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Account Created");
        builder.setMessage("First Name: " + firstName + "\n" +
                "Last Name: " + lastName + "\n" +
                "Email: " + email + "\n" +
                "User Name: " + userName + "\n" +
                "Password: " + this.password);
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    public void handleCreateUserAccountFail(Object o) {
        String errorMessage = "";
        if (o != null) {
            errorMessage = o.toString();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Account Creation Failed");
        builder.setIcon(R.drawable.american);
        builder.setMessage(errorMessage);
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    public void handleVerifyUserCredentialsSuccess(String userName, String firstName, String lastName) {
        binding.main.setVisibility(View.VISIBLE);
        this.userName = userName;
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.apply();
        Log.d(TAG, "Login successful, userName saved: " + this.userName);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Verification Success");
        builder.setIcon(R.drawable.american);
        builder.setMessage("You have Successfully Logged In.");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    public void handleVerifyUserCredentialsFail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Credentials Verification Failed");
        builder.setMessage("Invalid User Name or Password");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    private void showAccountPopup() {
        View view = findViewById(R.id.nav_account);
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.account_menu, popupMenu.getMenu());

        MenuItem userMenuItem = popupMenu.getMenu().findItem(R.id.user_id);
        if (userName != null && !userName.isEmpty()) {
            userMenuItem.setTitle("Username: " + userName);
        } else {
            userMenuItem.setTitle("Guest Account");
        }

        popupMenu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.user_id) {
                if (userName != null && !userName.isEmpty()) {
                    Toast.makeText(this, "Username: " + userName, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Username not available", Toast.LENGTH_LONG).show();
                }
            } else {
                performLogout();

            }
            return true;
        });

        popupMenu.show();
    }
    private void loadHardcodedCuisines() {

        cuisineInfoList.clear();
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

    private void performLogout() {
        Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show();
        binding.main.setVisibility(View.GONE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_LOGGED_IN, false);
        editor.apply();
        showLoginDialog();
        Toast.makeText(this, "Logged Out Successfully...", Toast.LENGTH_SHORT).show();
    }

}