package com.example.quantrasuaclient.Callback;


import com.example.quantrasuaclient.Model.OrderModel;

public interface ILoadTimeFromFireBaseListener {
    void onLoadTimeSuccess(OrderModel order, long estimateTimeInMs);
    void onLoadTimeSuccess(long estimateTimeInMs);
    void onLoadFailed(String message);
}
