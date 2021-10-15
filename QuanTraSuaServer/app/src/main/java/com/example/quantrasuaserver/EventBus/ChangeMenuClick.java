package com.example.quantrasuaserver.EventBus;

public class ChangeMenuClick {
    private final boolean isFromDrinksList;

    public ChangeMenuClick(boolean isFromDrinksList) {
        this.isFromDrinksList = isFromDrinksList;
    }

    public boolean isFromDrinksList() {
        return isFromDrinksList;
    }

}
