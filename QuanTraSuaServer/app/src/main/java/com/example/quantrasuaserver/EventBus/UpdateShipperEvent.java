package com.example.quantrasuaserver.EventBus;

import com.example.quantrasuaserver.Model.ShipperModel;

public class UpdateShipperEvent {
    private final ShipperModel shipperModel;
    private final boolean active;

    public UpdateShipperEvent(ShipperModel shipperModel, boolean active) {
        this.shipperModel = shipperModel;
        this.active = active;
    }

    public ShipperModel getShipperModel() {
        return shipperModel;
    }

    public boolean isActive() {
        return active;
    }

}
