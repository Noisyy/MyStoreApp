package com.example.quantrasuaclient.Callback;

import com.example.quantrasuaclient.Database.ModelDB.CartItem;
import com.example.quantrasuaclient.Model.CategoryModel;

public interface ISearchCategoryCallbackListener {
    void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem);
    void onSearchCategoryNotFound(String message);
}
