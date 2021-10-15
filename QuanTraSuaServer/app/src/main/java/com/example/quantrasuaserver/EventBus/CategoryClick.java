package com.example.quantrasuaserver.EventBus;

import com.example.quantrasuaserver.Model.CategoryModel;

public class CategoryClick {
    private final boolean success;
    CategoryModel categoryModel;

    public CategoryClick(boolean success, CategoryModel categoryModel) {
        this.success = success;
        this.categoryModel = categoryModel;
    }

    public boolean isSuccess() {
        return success;
    }

}
