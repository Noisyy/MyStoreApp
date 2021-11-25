package com.example.quantrasuaserver.Common;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.quantrasuaserver.Model.BestDealsModel;
import com.example.quantrasuaserver.Model.CategoryModel;
import com.example.quantrasuaserver.Model.DiscountModel;
import com.example.quantrasuaserver.Model.DrinksModel;
import com.example.quantrasuaserver.Model.MostPopularModel;
import com.example.quantrasuaserver.Model.ServerUserModel;
import com.example.quantrasuaserver.Model.TokenModel;
import com.example.quantrasuaserver.R;
import com.google.firebase.database.FirebaseDatabase;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Common {
    //Global
    public static final String OPTIONS_OK = "ĐỒNG Ý";
    public static final String OPTIONS_CANCEL = "TỪ CHỐI";
    public static final int TEXT_SIZE = 50;
    public static final int BUTTON_SIZE = 210;
    public static final String OPTIONS_DELETE = "Xóa";
    public static final String COLOR_DELETE = "#ff3d00";
    public static final String OPTIONS_UPDATE = "Sửa";
    public static final String COLOR_UPDATE = "#2962ff";
    public static final String SEVER_REF = "Server";
    public static final String OPTIONS_CALL = "Gọi điện";
    public static final String COLOR_CALL = "#1de9b6";
    public static final String CATEGORY_REF = "Category";
    public static final String NOTIFY_TITLE = "title";
    public static final String NOTIFY_CONTENT = "content";
    public static final String TOKEN_REF = "Tokens";
    public static final String ORDER_REF = "Orders";
    public static final String IS_OPEN_ACTIVITY_NEW_ORDER = "IsOpenActivityNewOrder";
    public static final String BEST_DEALS_REF = "BestDeals";
    public static final String MOST_POPULAR_REF = "MostPopular";
    public static final String IS_SEND_IMAGE = "IS_SEND_IMAGE";
    public static final String IMAGE_URL = "IMAGE_URL";
    public static final String DISCOUNT_REF = "Discount";
    public static ServerUserModel currentServerUser = new ServerUserModel();
    public static CategoryModel categorySelected;
    public static int DEFAULT_COLUMN_COUNT = 0;
    public static int FULL_WIDTH_COLUMN = 1;
    public static DrinksModel selectDrinks = new DrinksModel();
    public static String SHIPPER_REF = "Shippers";
    public static String SHIPPING_ORDER_REF = "ShippingOrder";
    public static BestDealsModel bestDealsSelected;
    public static MostPopularModel mostPopularSelected;
    public static DiscountModel discountSelected;
    public static String TV_UPDATE = "CẬP NHẬT SẢN PHẨM";
    public static String TV_INSERT = "THÊM MỚI SẢN PHẨM";
    //Because function usually 3 action create, update, delete
    public enum ACTION {
        CREATE,
        UPDATE,
        DELETE
    }
    //Format Price, example: 5 -> 5,000
    public static String formatPrice(double price) {
        if (price != 0) {
            DecimalFormat df = new DecimalFormat("#,##0.000");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = df.format(price);
            return finalPrice.replace(".", ",");
        } else
            return "0.000";
    }
    //Setting font
    public static void setSpanString(String welcome, String name, TextView textView) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }
    public static void setSpanStringColor(String welcome, String name, TextView textView, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }
    // Format status order
    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus) {
            case 0:
                return "Đã đặt hàng";
            case 1:
                return "Đang giao hàng";
            case 2:
                return "Đã giao hàng";
            case -1:
                return "Đã hủy đơn";
            default:
                return "Lỗi rồi ông ơi";
        }
    }

    //Show message
    @SuppressLint("UnspecifiedImmutableFlag")
    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String NOTIFICATION_CHANNEL_ID = "dev_drinks_it_v1";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Drinks Milk Tea V1", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Drinks Milk Tea V1");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.icon_sms)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon));
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);

    }

    //First login update token
    public static void updateToken(Context context, String newToken, boolean isServer, boolean isShipper) {
        if (Common.currentServerUser.getUid() != null) {
            FirebaseDatabase.getInstance()
                    .getReference(Common.TOKEN_REF)
                    .child(Common.currentServerUser.getUid())
                    .setValue(new TokenModel(Common.currentServerUser.getPhone(), newToken, isServer, isShipper))
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    //
    public static String createTopicOrder() {
        return "/topics/new_order";
    }

    //
    public static String getNewsTopic() {
        return "/topics/news";
    }
}
