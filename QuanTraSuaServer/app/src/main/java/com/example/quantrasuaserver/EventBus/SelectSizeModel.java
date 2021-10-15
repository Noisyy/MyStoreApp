package com.example.quantrasuaserver.EventBus;

import com.example.quantrasuaserver.Model.SizeModel;

public class SelectSizeModel {
    private final SizeModel sizeModel;

    public SelectSizeModel(SizeModel sizeModel) {
        this.sizeModel = sizeModel;
    }

    public SizeModel getSizeModel() {
        return sizeModel;
    }

}
