package com.example.quantrasuaclient.Fragment.Drink_List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Model.DrinksModel;

import java.util.List;

public class DrinkListViewModel extends ViewModel {

    private MutableLiveData<List<DrinksModel>> mutableLiveDataDrinkList;

    public DrinkListViewModel() {

    }

    public MutableLiveData<List<DrinksModel>> getMutableLiveDataDrinkList() {
        if(mutableLiveDataDrinkList == null)
            mutableLiveDataDrinkList = new MutableLiveData<>();
        mutableLiveDataDrinkList.setValue(Common.categorySelected.getDrinks());
        return mutableLiveDataDrinkList;
    }
}