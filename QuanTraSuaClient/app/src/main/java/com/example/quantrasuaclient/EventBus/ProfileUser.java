package com.example.quantrasuaclient.EventBus;

import com.example.quantrasuaclient.Model.UserModel;

public class ProfileUser {
    private UserModel userModel;
    private boolean success;

    public ProfileUser(UserModel userModel, boolean success) {
        this.userModel = userModel;
        this.success = success;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
