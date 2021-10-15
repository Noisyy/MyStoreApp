package com.example.quantrasuaserver.EventBus;

import com.example.quantrasuaserver.Common.Common;

public class ToastEvent {
    private Common.ACTION action;
    boolean isFromDrinksList;

    public ToastEvent(Common.ACTION action, boolean isFromDrinksList) {
        this.action = action;
        this.isFromDrinksList = isFromDrinksList;
    }

    public Common.ACTION getAction() {
        return action;
    }

    public void setAction(Common.ACTION action) {
        this.action = action;
    }

}
