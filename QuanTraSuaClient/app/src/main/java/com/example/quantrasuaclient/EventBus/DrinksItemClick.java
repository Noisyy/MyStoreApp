package com.example.quantrasuaclient.EventBus;


import com.example.quantrasuaclient.Model.DrinksModel;

public class DrinksItemClick {
    private boolean success;
    private DrinksModel drinksModel;

    public DrinksItemClick() {
    }

    public DrinksItemClick(boolean success, DrinksModel drinksModel) {
        this.success = success;
        this.drinksModel = drinksModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DrinksModel getDrinksModel() {
        return drinksModel;
    }

    public void setDrinksModel(DrinksModel drinksModel) {
        this.drinksModel = drinksModel;
    }
}
