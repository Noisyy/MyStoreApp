package com.example.quantrasuashipper.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.quantrasuashipper.Common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFCMServices extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> dataReCV = remoteMessage.getData();
        if (dataReCV.size() > 0) {
            Log.d("PhiDep", "Message data payload: " + remoteMessage.getData());
            Common.showNotification(this, new Random().nextInt(),
                    dataReCV.get(Common.NOTI_TITLE),
                    dataReCV.get(Common.NOTI_CONTENT), null);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String TAG = MyFCMServices.class.getName();
        Log.e(TAG, s);
        Common.updateToken(this, s,false,true);
        //Because we are in Shipper app so shipper = true
    }
}
