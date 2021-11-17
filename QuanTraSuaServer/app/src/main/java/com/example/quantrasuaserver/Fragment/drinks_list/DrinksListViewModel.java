package com.example.quantrasuaserver.Fragment.drinks_list;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Model.DrinksModel;

import java.util.List;

public class DrinksListViewModel extends ViewModel {

    private MutableLiveData<List<DrinksModel>> mutableLiveDataDrinkList;

    public DrinksListViewModel() {

    }

    public MutableLiveData<List<DrinksModel>> getMutableLiveDataDrinkList() {
        if(mutableLiveDataDrinkList == null)
            mutableLiveDataDrinkList = new MutableLiveData<>();
        mutableLiveDataDrinkList.setValue(Common.categorySelected.getDrinks());
        return mutableLiveDataDrinkList;
    }
}