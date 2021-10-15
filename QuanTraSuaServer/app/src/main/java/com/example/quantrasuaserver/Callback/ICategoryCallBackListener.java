package com.example.quantrasuaserver.Callback;

import com.example.quantrasuaserver.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallBackListener {
    void onCategoryLoadSuccess(List<CategoryModel> CategoryModelsList);
    void onCategoryLoadFailed(String message);
}
