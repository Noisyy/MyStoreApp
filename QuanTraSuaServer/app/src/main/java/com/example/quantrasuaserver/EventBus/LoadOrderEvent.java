package com.example.quantrasuaserver.EventBus;

public class LoadOrderEvent {
    public int status;

    public LoadOrderEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

}
