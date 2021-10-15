package com.example.quantrasuaclient.ui.comments;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quantrasuaclient.Model.CommentModel;

import java.util.List;

public class CommentViewModel extends ViewModel {
    private final MutableLiveData<List<CommentModel>> mutableLiveDataDrinksList;

    public CommentViewModel() {
        mutableLiveDataDrinksList = new MutableLiveData<>();
    }

    public MutableLiveData<List<CommentModel>> getMutableLiveDataDrinksList() {
        return mutableLiveDataDrinksList;
    }


    public void setCommentList(List<CommentModel> commentList) {
        mutableLiveDataDrinksList.setValue(commentList);
    }
}
