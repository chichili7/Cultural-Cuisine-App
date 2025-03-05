package com.example.culturalcuisineapp;

import androidx.recyclerview.widget.RecyclerView;

import com.example.culturalcuisineapp.databinding.ItemRecipeBinding;

public class FavoritesViewHolder extends RecyclerView.ViewHolder {
    ItemRecipeBinding binding;

    public FavoritesViewHolder(ItemRecipeBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}