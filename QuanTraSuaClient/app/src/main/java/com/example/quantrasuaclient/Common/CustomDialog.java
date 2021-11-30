package com.example.quantrasuaclient.Common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;

import com.kaopiz.kprogresshud.KProgressHUD;

public class CustomDialog {
    @SuppressLint("StaticFieldLeak")
    public static KProgressHUD dialog;

    public static void show(Context context) {
        dialog = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).setWindowColor(Color.TRANSPARENT)
                .show();
    }

    public static void dismiss() {
        dialog.dismiss();
    }
}
