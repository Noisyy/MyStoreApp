package com.example.quantrasuaserver.services;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFCMServices extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> dataReCV = remoteMessage.getData();
        if (dataReCV.size() > 0) {
            if (dataReCV.get(Common.NOTIFY_TITLE).equals("Đơn hàng mới")) {
                //Here we need call MainActivity because we must assign value of Common.currentUser
                //So we must call MainActivity to do that, if you direct call HomeActivity you will be crash
                //Because Common.currentUser only be assign in MainActivity AFTER LOGIN
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, true); //Use extra to detect is app open from notification
                Common.showNotification(this, new Random().nextInt(),
                        dataReCV.get(Common.NOTIFY_TITLE),
                        dataReCV.get(Common.NOTIFY_CONTENT), intent);
            } else {
                Common.showNotification(this, new Random().nextInt(),
                        dataReCV.get(Common.NOTIFY_TITLE),
                        dataReCV.get(Common.NOTIFY_CONTENT), null);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this, s, true, false);
        //Because we are in Server app so server = true
    }
}
