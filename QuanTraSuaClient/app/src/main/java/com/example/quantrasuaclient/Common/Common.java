package com.example.quantrasuaclient.Common;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.quantrasuaclient.Database.DataSource.FavoriteRepository;
import com.example.quantrasuaclient.Database.Local.RoomDatabase;
import com.example.quantrasuaclient.Model.AddonModel;
import com.example.quantrasuaclient.Model.CategoryModel;
import com.example.quantrasuaclient.Model.DiscountModel;
import com.example.quantrasuaclient.Model.DrinksModel;
import com.example.quantrasuaclient.Model.SizeModel;
import com.example.quantrasuaclient.Model.TokenModel;
import com.example.quantrasuaclient.Model.UserModel;
import com.example.quantrasuaclient.R;
import com.google.firebase.database.FirebaseDatabase;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class Common {
    public static final String OPTIONS_ACCEPT = "ĐỒNG Ý";
    public static final String OPTIONS_CANCEL = "TỪ CHỐI";
    public static final String ORDER_REF = "Orders";
    public static final String NOTIFY_TITLE = "title";
    public static final String NOTIFY_CONTENT = "content";
    public static final String USER_REF = "Users";
    public static final String IS_SUBSCRIBE_NEWS = "IS_SUBSCRIBE_NEWS";
    public static final String NEWS_TOPIC = "news";
    public static final String IS_SEND_IMAGE = "IS_SEND_IMAGE";
    public static final String IMAGE_URL = "IMAGE_URL";
    public static final String QR_CODE_TAG = "QRCode";
    public static final String DISCOUNT_REF = "Discount";
    private static final String TOKEN_REF = "Tokens";
    public static UserModel currentUser = new UserModel();
    public static String POPULAR_CATEGORY_REF = "MostPopular";
    public static String BEST_DEALS_REF = "BestDeals";
    public static int DEFAULT_COLUMN_COUNT = 0;
    public static int FULL_WIDTH_COLUMN = 1;
    public static String CATEGORY_REF = "Category";
    public static CategoryModel categorySelected;
    public static DrinksModel selectDrinks;
    public static String COMMENT_REF = "Comments";
    public static RoomDatabase roomDatabase;
    public static FavoriteRepository favoriteRepository;
    public static DiscountModel discountApply;
    public static String currentToken="";

    public static String formatPrice(double price) {
        if (price != 0) {
            DecimalFormat df = new DecimalFormat("#,##0.000");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = df.format(price);
            return finalPrice.replace(".", ",");
        } else
            return "0.000";
    }

    public static Double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {
        double result = 0.0;
        if (userSelectedSize == null && userSelectedAddon == null)
            return 0.0;
        else if (userSelectedSize == null) {
            for (AddonModel addonModel : userSelectedAddon)
                result += addonModel.getPrice();
            return result;
        } else if (userSelectedAddon == null) {
            return userSelectedSize.getPrice() * 1.0;

        } else {
            result = userSelectedSize.getPrice() * 1.0;
            for (AddonModel addonModel : userSelectedAddon)
                result += addonModel.getPrice();
            return result;
        }
    }

    public static void setSpanString(String welcome, String name, TextView textView) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static String createOrderNumber() {
        return String.valueOf(System.currentTimeMillis()) + //Get current time in millisecond
                Math.abs(new Random().nextInt()); //Add random number to block same order at same time
    }

    public static String getDateOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return "Chủ nhật";
            case 2:
                return "Thứ hai";
            case 3:
                return "Thứ ba";
            case 4:
                return "Thứ tư";
            case 5:
                return "Thứ năm";
            case 6:
                return "Thứ sáu";
            case 7:
                return "Thứ bảy";
            default:
                return "Unk";
        }
    }

    public static String convertStatusToText(int orderStatus) {
        switch (orderStatus) {
            case 0:
                return "Đã đặt hàng";
            case 1:
                return "Đang giao hàng";
            case 2:
                return "Đã giao";
            case -1:
                return "Đã hủy";
            default:
                return "Unk";
        }
    }

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
                .setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon));
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

    //Notify
    public static void updateToken(Context context, String newToken) {
        if (Common.currentUser.getUid() != null) {
            FirebaseDatabase.getInstance()
                    .getReference(Common.TOKEN_REF)
                    .child(Common.currentUser.getUid())
                    .setValue(new TokenModel(Common.currentUser.getPhone(), newToken))
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    public static String createTopicOrder() {
        return "/topics/new_order";
    }

    public static String getListAddon(List<AddonModel> addonModels) {
        StringBuilder result = new StringBuilder();
        if (addonModels.size() > 0) {
            for (AddonModel addonModel : addonModels) {
                result.append(addonModel.getName()).append(", ");
            }
            return result.substring(0, result.length() - 2); //remove last ","
        }
        return "Không có";
    }

    public static DrinksModel findDrinksInListById(CategoryModel categoryModel, String drinksId) {
        if (categoryModel.getDrinks() != null && categoryModel.getDrinks().size() > 0) {
            for (DrinksModel drinksModel : categoryModel.getDrinks())
                if (drinksModel.getId().equals(drinksId))
                    return drinksModel;
        }
        return null;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public static void showNotificationBigStyle(Context context, int id, String title, String content, Bitmap bitmap, Intent intent) {
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
                .setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));

        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

}
