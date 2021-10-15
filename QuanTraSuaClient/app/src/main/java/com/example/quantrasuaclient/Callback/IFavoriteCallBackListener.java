package com.example.quantrasuaclient.Callback;

import com.example.quantrasuaclient.Database.ModelDB.Favorite;

import java.util.List;

public interface IFavoriteCallBackListener {
    void onFavoriteLoadSuccess(List<Favorite> favoriteList);
    void onFavoriteLoadFailed(String message);
}
