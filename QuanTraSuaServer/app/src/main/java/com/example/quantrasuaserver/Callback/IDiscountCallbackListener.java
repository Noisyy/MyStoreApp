package com.example.quantrasuaserver.Callback;

import com.example.quantrasuaserver.Model.DiscountModel;

import java.util.List;

public interface IDiscountCallbackListener {
    void onListDiscountLoadSuccess(List<DiscountModel> discountModelList);
    void onListDiscountLoadFailed(String message);
}
