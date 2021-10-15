package com.example.quantrasuaserver.EventBus;

public class AddonSizeEditEvent {
    private final boolean addon;
    private int pos;

    public AddonSizeEditEvent(boolean addon, int pos) {
        this.addon = addon;
        this.pos = pos;
    }

    public boolean isAddon() {
        return addon;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
