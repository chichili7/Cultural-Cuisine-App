package com.example.culturalcuisineapp;

import androidx.recyclerview.widget.RecyclerView;

import com.example.culturalcuisineapp.databinding.ItemRecipeBinding;

public class RecipeViewHolder extends RecyclerView.ViewHolder{

    ItemRecipeBinding binding;

    public RecipeViewHolder(ItemRecipeBinding binding){
        super(binding.getRoot());
        this.binding=binding;
    }
}
