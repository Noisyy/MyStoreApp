package com.example.quantrasuaclient.Fragment.Cart;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Database.DataSource.ICartDataSource;
import com.example.quantrasuaclient.Database.Local.RoomDatabase;
import com.example.quantrasuaclient.Database.ModelDB.CartItem;
import com.example.quantrasuaclient.Database.Local.CartDataSource;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private final CompositeDisposable compositeDisposable;
    private ICartDataSource cartDataSource;
    private MutableLiveData<List<CartItem>> mutableLiveDataListCartItems;

    public CartViewModel() {
        compositeDisposable = new CompositeDisposable();
    }

    public void initCartDataSource(Context context) {
        cartDataSource = new CartDataSource(RoomDatabase.getInstance(context).cartDAO());
    }

    public void onStop() {
        compositeDisposable.clear();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataListCartItems() {
        if (mutableLiveDataListCartItems == null)
            mutableLiveDataListCartItems = new MutableLiveData<>();
        getAllCartItems();
        return mutableLiveDataListCartItems;
    }

    private void getAllCartItems() {

        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> mutableLiveDataListCartItems.setValue(cartItems)
                        , throwable -> mutableLiveDataListCartItems.setValue(null))
        );
    }
}