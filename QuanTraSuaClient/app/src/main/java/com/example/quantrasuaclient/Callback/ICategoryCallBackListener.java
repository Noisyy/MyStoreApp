package com.example.quantrasuaclient.Callback;

import com.example.quantrasuaclient.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallBackListener {
    void onCategoryLoadSuccess(List<CategoryModel> CategoryModelsList);
    void onCategoryLoadFailed(String message);
}
