package com.example.quantrasuaserver.Services;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Activity.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MyFCMServices extends FirebaseMessagingService {
    //Message received form client app
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> dataReCV = remoteMessage.getData();
        if (dataReCV.size() > 0) {
            if (Objects.equals(dataReCV.get(Common.NOTIFY_TITLE), "Đơn hàng mới")) {
                //Here we need call MainActivity because we must assign value of Common.currentUser
                //So we must call MainActivity to do that, if direct call HomeActivity you will be crash
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
    //if first setup app then create new token
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this, s, true, false);
        //Because in Server app so server = true
    }
}
