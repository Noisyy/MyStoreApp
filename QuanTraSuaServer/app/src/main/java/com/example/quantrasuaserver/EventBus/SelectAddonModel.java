package com.example.quantrasuaserver.EventBus;

import com.example.quantrasuaserver.Model.AddonModel;

public class SelectAddonModel {
    private final AddonModel addonModel;

    public SelectAddonModel(AddonModel addonModel) {
        this.addonModel = addonModel;
    }

    public AddonModel getAddonModel() {
        return addonModel;
    }

}
