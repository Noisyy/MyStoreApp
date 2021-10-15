package com.example.quantrasuaclient.Database.DataSource;


import com.example.quantrasuaclient.Database.ModelDB.CartItem;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface ICartDataSource {

    Flowable<List<CartItem>> getAllCart(String uid);

    Single<Integer> countItemInCart(String uid);

    Single<Double> sumPriceInCart(String uid);

    Single<CartItem> getItemInCart(String drinksId, String uid);

    Completable insertOrReplaceAll(CartItem... cartItems);

    Single<Integer> updateCartItems(CartItem cartItems);

    Single<Integer> deleteCartItems(CartItem cartItems);

    Single<Integer> CleanCart(String uid);

    Single<CartItem> getItemWithOptionsInCart(String uid,String categoryId, String drinksId,String drinksSize,String drinksAddon);
}
