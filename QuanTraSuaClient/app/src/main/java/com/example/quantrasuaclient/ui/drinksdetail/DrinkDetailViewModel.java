package com.example.quantrasuaclient.ui.drinksdetail;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Model.CommentModel;
import com.example.quantrasuaclient.Model.DrinksModel;

public class DrinkDetailViewModel extends ViewModel {

    private MutableLiveData<DrinksModel> mutableLiveDataDrinks;
    private final MutableLiveData<CommentModel> mutableLiveDataComment;

    public void setCommentModel(CommentModel commentModel){
        mutableLiveDataComment.setValue(commentModel);
    }

    public MutableLiveData<CommentModel> getMutableLiveDataComment() {
        return mutableLiveDataComment;
    }

    public DrinkDetailViewModel(){
        mutableLiveDataComment = new MutableLiveData<>();
    }

    public MutableLiveData<DrinksModel> getMutableLiveDataDrinks() {
        if(mutableLiveDataDrinks == null)
            mutableLiveDataDrinks = new MutableLiveData<>();
        mutableLiveDataDrinks.setValue(Common.selectDrinks);
        return mutableLiveDataDrinks;
    }

    public void setDrinksModel(DrinksModel drinksModel) {
        if(mutableLiveDataDrinks != null)
            mutableLiveDataDrinks.setValue(drinksModel);
    }
}