package com.example.quantrasuaclient.Database.Local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quantrasuaclient.Database.ModelDB.CartItem;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface CartDAO {
    @Query("SELECT * FROM Cart WHERE uid =:uid")
    Flowable<List<CartItem>> getAllCart(String uid);

    @Query("SELECT SUM(drinksQuantity) FROM Cart WHERE uid=:uid")
    Single<Integer> countItemInCart(String uid);

    @Query("SELECT SUM((drinksPrice+drinksExtraPrice)*drinksQuantity) FROM Cart WHERE uid=:uid")
    Single<Double> sumPriceInCart(String uid);

    @Query("SELECT * FROM Cart WHERE drinksId=:drinksId AND uid=:uid")
    Single<CartItem> getItemInCart(String drinksId, String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrReplaceAll(CartItem... cartItems);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCartItems(CartItem cartItems);

    @Delete
    Single<Integer> deleteCartItems(CartItem cartItems);

    @Query("DELETE FROM Cart WHERE uid=:uid")
    Single<Integer> CleanCart(String uid);

    @Query("SELECT * FROM Cart WHERE drinksId=:drinksId AND categoryId=:categoryId AND uid=:uid AND drinksSize=:drinksSize AND drinksAddon=:drinksAddon")
    Single<CartItem> getItemWithOptionsInCart(String uid, String categoryId, String drinksId, String drinksSize, String drinksAddon);
}
