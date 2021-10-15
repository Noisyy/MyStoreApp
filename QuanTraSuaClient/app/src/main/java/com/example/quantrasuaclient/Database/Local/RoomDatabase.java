package com.example.quantrasuaclient.Database.Local;

import android.content.Context;


import androidx.room.Database;
import androidx.room.Room;

import com.example.quantrasuaclient.Database.ModelDB.CartItem;
import com.example.quantrasuaclient.Database.ModelDB.Favorite;

@Database(version = 1, entities = {CartItem.class, Favorite.class}, exportSchema = false)
public abstract class RoomDatabase extends androidx.room.RoomDatabase {
    public abstract CartDAO cartDAO();
    public abstract FavoriteDAO favoriteDAO();
    private static RoomDatabase instance;

    public static RoomDatabase getInstance(Context context) {
        if (instance == null)
            instance = Room.databaseBuilder(context, RoomDatabase.class, "DrinksStoreV01").allowMainThreadQueries().build();
        return instance;
    }
}
