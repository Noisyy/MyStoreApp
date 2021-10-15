package com.example.quantrasuaclient.Database.DataSource;

import com.example.quantrasuaclient.Database.ModelDB.Favorite;

import java.util.List;

import io.reactivex.Flowable;

public interface IFavoriteDataSource {
    Flowable<List<Favorite>> getFavItem();

    int isFavorite(int itemId);

    void insert(Favorite...favorites);

    void delete(Favorite favorite);
}
