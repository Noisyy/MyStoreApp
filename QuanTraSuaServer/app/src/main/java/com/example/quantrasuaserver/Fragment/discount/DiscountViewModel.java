package com.example.quantrasuaserver.Fragment.discount;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quantrasuaserver.Callback.IDiscountCallbackListener;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Model.DiscountModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DiscountViewModel extends ViewModel implements IDiscountCallbackListener {
    private final MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<DiscountModel>> discountMutableLiveData;
    private final IDiscountCallbackListener discountCallbackListener;

    public DiscountViewModel() {
        discountCallbackListener = this;
    }

    public MutableLiveData<List<DiscountModel>> getDiscountMutableLiveData() {
        if (discountMutableLiveData == null) discountMutableLiveData = new MutableLiveData<>();
        loadDiscount();
        return discountMutableLiveData;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onListDiscountLoadSuccess(List<DiscountModel> discountModelList) {
        discountMutableLiveData.setValue(discountModelList);
    }

    @Override
    public void onListDiscountLoadFailed(String message) {
        if (message.equals("Empty Data"))
            discountMutableLiveData.setValue(null);
        messageError.setValue(message);
    }


    public void loadDiscount() {
        List<DiscountModel> temp = new ArrayList<>();
        DatabaseReference discountRef = FirebaseDatabase.getInstance()
                .getReference(Common.DISCOUNT_REF);
        discountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildren().iterator().hasNext()) {
                    for (DataSnapshot discountSnapshot : snapshot.getChildren()) {
                        DiscountModel discountModel = discountSnapshot.getValue(DiscountModel.class);
                        if (discountModel != null) {
                            discountModel.setKey(discountSnapshot.getKey());
                        }
                        temp.add(discountModel);
                    }
                    discountCallbackListener.onListDiscountLoadSuccess(temp);
                } else
                    discountCallbackListener.onListDiscountLoadFailed("Empty Data");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                discountCallbackListener.onListDiscountLoadFailed(error.getMessage());
            }
        });
    }

}