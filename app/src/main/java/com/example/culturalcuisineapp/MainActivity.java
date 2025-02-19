package com.example.culturalcuisineapp;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.culturalcuisineapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> drawerListAdapter;
    private List<String> sourceNamesList;

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
        binding.toolbar.setTitle("Cuisine Explorer");
        binding.toolbar.setTitleTextColor(Color.WHITE);
        Objects.requireNonNull(binding.toolbar.getOverflowIcon()).setTint(Color.WHITE);
        setSupportActionBar(binding.toolbar);

        sourceNamesList = new ArrayList<>();

        drawerListAdapter = new ArrayAdapter<>(this,R.layout.drawer_list,sourceNamesList);
        binding.leftDrawer.setAdapter(drawerListAdapter);
        binding.leftDrawer.setOnItemClickListener((parent, view, position, id) -> handleDrawerClick(position));
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                binding.main,
                binding.toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        binding.main.addDrawerListener(mDrawerToggle);
        mDrawerToggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        mDrawerToggle.syncState();


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


    }

    private void handleDrawerClick(int position) {

    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem1 = menu.findItem(R.id.recipes_btn);
        MenuItem menuItem2 = menu.findItem(R.id.languages_btn);
        MenuItem menuItem3 = menu.findItem(R.id.restaurants_btn);

        return true;
    }

}