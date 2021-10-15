package com.example.quantrasuaserver.Callback;


import com.example.quantrasuaserver.Model.OrderModel;

import java.util.List;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);

    void onOrderLoadFailed(String message);
}
