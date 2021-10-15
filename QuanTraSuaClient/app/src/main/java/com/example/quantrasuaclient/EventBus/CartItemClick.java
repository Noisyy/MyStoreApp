package com.example.quantrasuaclient.EventBus;

public class CartItemClick {
    private boolean success;

    public CartItemClick(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
