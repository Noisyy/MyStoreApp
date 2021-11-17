package com.example.quantrasuaclient.Services;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.quantrasuaclient.Common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MyFCMServices extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String,String> dataReCV = remoteMessage.getData();
        if (dataReCV.get(Common.IS_SEND_IMAGE) != null && Objects.equals(dataReCV.get(Common.IS_SEND_IMAGE), "true"))
        {
            Glide.with(this)
                    .asBitmap()
                    .load(dataReCV.get(Common.IMAGE_URL))
                    .into(new CustomTarget<Bitmap>() {

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Common.showNotificationBigStyle(MyFCMServices.this,new Random().nextInt(),
                                    dataReCV.get(Common.NOTIFY_TITLE),
                                    dataReCV.get(Common.NOTIFY_CONTENT),
                                    resource,
                                    null);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
        else {
            Common.showNotification(this,new Random().nextInt(),
                    dataReCV.get(Common.NOTIFY_TITLE),
                    dataReCV.get(Common.NOTIFY_CONTENT),
                    null);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this,s);
    }
}
