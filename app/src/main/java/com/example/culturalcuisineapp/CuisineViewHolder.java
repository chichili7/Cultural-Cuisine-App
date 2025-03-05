package com.example.culturalcuisineapp;

import androidx.recyclerview.widget.RecyclerView;

import com.example.culturalcuisineapp.databinding.ItemCuisineBinding;

public class CuisineViewHolder extends RecyclerView.ViewHolder {

    ItemCuisineBinding binding;
    public CuisineViewHolder(ItemCuisineBinding binding){
        super(binding.getRoot());
        this.binding=binding;
    }
}
