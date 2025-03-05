package com.example.culturalcuisineapp;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.culturalcuisineapp.databinding.ItemCuisineBinding;

import java.util.List;


public class CuisineAdapter extends RecyclerView.Adapter<CuisineViewHolder>{

private MainActivity mainActivity;
private List<CuisineInfo>cuisineInfoList;
private SearchActivity searchActivity;

    public CuisineAdapter(MainActivity mainActivity, List<CuisineInfo> cuisineInfoList){
        this.cuisineInfoList = cuisineInfoList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public CuisineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCuisineBinding binding = ItemCuisineBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent , false
        );
        return new CuisineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CuisineViewHolder holder, int position) {
        CuisineInfo cuisines = cuisineInfoList.get(position);
        holder.binding.tvCuisineName.setText(cuisines.getCuisineName());

        if (cuisines.getImageResourceId() != 0) {
            try {
                holder.binding.ivCuisineImage.setImageResource(cuisines.getImageResourceId());
            } catch (Exception e) {
                Log.e("CuisineAdapter", "Error loading image: " + e.getMessage());
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cuisineName = cuisines.getCuisineName();
                Intent intent = new Intent(mainActivity, SearchActivity.class);
                intent.putExtra("CUISINE_NAME", cuisineName);
                mainActivity.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return cuisineInfoList.size();
    }
}
