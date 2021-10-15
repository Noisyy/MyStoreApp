package com.example.quantrasuaclient.Database.ModelDB;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "Cart", primaryKeys = {"uid", "categoryId", "drinksId", "drinksAddon", "drinksSize"})
public class CartItem {
    @NonNull
    @ColumnInfo(name = "categoryId")
    private String categoryId;

    @NonNull
    @ColumnInfo(name = "drinksId")
    private String drinksId;

    @ColumnInfo(name = "drinksName")
    private String drinksName;

    @ColumnInfo(name = "drinksImage")
    private String drinksImage;

    @ColumnInfo(name = "drinksPrice")
    private Double drinksPrice;

    @ColumnInfo(name = "drinksQuantity")
    private int drinksQuantity;

    @ColumnInfo(name = "userPhone")
    private String userPhone;

    @ColumnInfo(name = "drinksExtraPrice")
    private Double drinksExtraPrice;

    @NonNull
    @ColumnInfo(name = "drinksAddon")
    private String drinksAddon;
    @NonNull
    @ColumnInfo(name = "drinksSize")
    private String drinksSize;

    @NonNull
    @ColumnInfo(name = "uid")
    private String uid;

    @NonNull
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }

    @NonNull
    public String getDrinksId() {
        return drinksId;
    }

    public void setDrinksId(@NonNull String drinksId) {
        this.drinksId = drinksId;
    }

    public String getDrinksName() {
        return drinksName;
    }

    public void setDrinksName(String drinksName) {
        this.drinksName = drinksName;
    }

    public String getDrinksImage() {
        return drinksImage;
    }

    public void setDrinksImage(String drinksImage) {
        this.drinksImage = drinksImage;
    }

    public Double getDrinksPrice() {
        return drinksPrice;
    }

    public void setDrinksPrice(Double drinksPrice) {
        this.drinksPrice = drinksPrice;
    }

    public int getDrinksQuantity() {
        return drinksQuantity;
    }

    public void setDrinksQuantity(int drinksQuantity) {
        this.drinksQuantity = drinksQuantity;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public Double getDrinksExtraPrice() {
        return drinksExtraPrice;
    }

    public void setDrinksExtraPrice(Double foodExtraPrice) {
        this.drinksExtraPrice = foodExtraPrice;
    }

    public String getDrinksAddon() {
        return drinksAddon;
    }

    public void setDrinksAddon(@NonNull String drinksAddon) {
        this.drinksAddon = drinksAddon;
    }

    public String getDrinksSize() {
        return drinksSize;
    }

    public void setDrinksSize(@NonNull String drinksSize) {
        this.drinksSize = drinksSize;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof CartItem))
            return false;
        CartItem cartItem = (CartItem) obj;
        return cartItem.getDrinksId().equals(this.drinksId) &&
                cartItem.getDrinksAddon().equals(this.drinksAddon) &&
                cartItem.getDrinksSize().equals(this.drinksSize);
    }
}
