package com.example.quantrasuaclient.Database.Local;

import com.example.quantrasuaclient.Database.DataSource.ICartDataSource;
import com.example.quantrasuaclient.Database.ModelDB.CartItem;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class CartDataSource implements ICartDataSource {

    private CartDAO cartDAO;

    public CartDataSource(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public Flowable<List<CartItem>> getAllCart(String uid) {
        return cartDAO.getAllCart(uid);
    }

    @Override
    public Single<Integer> countItemInCart(String uid) {
        return cartDAO.countItemInCart(uid);
    }

    @Override
    public Single<Double> sumPriceInCart(String uid) {
        return cartDAO.sumPriceInCart(uid);
    }

    @Override
    public Single<CartItem> getItemInCart(String drinksId, String uid) {
        return cartDAO.getItemInCart(drinksId, uid);
    }

    @Override
    public Completable insertOrReplaceAll(CartItem... cartItems) {
        return cartDAO.insertOrReplaceAll(cartItems);
    }

    @Override
    public Single<Integer> updateCartItems(CartItem cartItems) {
        return cartDAO.updateCartItems(cartItems);
    }

    @Override
    public Single<Integer> deleteCartItems(CartItem cartItems) {
        return cartDAO.deleteCartItems(cartItems);
    }

    @Override
    public Single<Integer> CleanCart(String uid) {
        return cartDAO.CleanCart(uid);
    }

    @Override
    public Single<CartItem> getItemWithOptionsInCart(String uid, String categoryId, String drinksId, String drinksSize, String drinksAddon) {
        return cartDAO.getItemWithOptionsInCart(uid, categoryId, drinksId, drinksSize, drinksAddon);
    }
}
