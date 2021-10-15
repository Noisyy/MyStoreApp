package com.example.quantrasuaclient.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Database.ModelDB.CartItem;
import com.example.quantrasuaclient.EventBus.UpdateItemInCart;
import com.example.quantrasuaclient.Model.AddonModel;
import com.example.quantrasuaclient.Model.SizeModel;
import com.example.quantrasuaclient.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder> {

    Context context;
    List<CartItem> cartItemList;
    Gson gson;

    public MyCartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.gson = new Gson();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(cartItemList.get(position).getDrinksImage()).into(holder.img_cart);
        holder.txt_drink_name.setText(new StringBuffer(cartItemList.get(position).getDrinksName()));
        String price = Common.formatPrice(cartItemList.get(position).getDrinksPrice() + cartItemList.get(position).getDrinksExtraPrice());
        holder.txt_drink_price.setText(new StringBuffer("Giá tiền: ").append(price).append(" VND"));

        if (cartItemList.get(position).getDrinksSize() != null) {
            if (cartItemList.get(position).getDrinksSize().equals("Default")) {
                holder.txt_drinks_size.setText(new StringBuilder("Size: ").append("Small"));
            } else {
                SizeModel sizeModel = gson.fromJson(cartItemList.get(position).getDrinksSize(), new TypeToken<SizeModel>() {
                }.getType());
                holder.txt_drinks_size.setText(new StringBuilder("Size: ").append(sizeModel.getName()));
            }
        }
        if (cartItemList.get(position).getDrinksAddon() != null) {
            if (cartItemList.get(position).getDrinksAddon().equals("Default")) {
                holder.txt_drinks_add_on.setText(new StringBuilder("Topping: ").append("Không có"));
            } else {
                List<AddonModel> addonModels = gson.fromJson(cartItemList.get(position).getDrinksAddon()
                        , new TypeToken<List<AddonModel>>() {}.getType());
                holder.txt_drinks_add_on.setText(new StringBuilder("Topping: ").append(Common.getListAddon(addonModels)));
            }
        }

        holder.numberButton.setNumber(String.valueOf(cartItemList.get(position).getDrinksQuantity()));

        //Event
        holder.numberButton.setOnValueChangeListener((view, oldValue, newValue) -> {
            //When user click button, we will update database
            cartItemList.get(position).setDrinksQuantity(newValue);
            EventBus.getDefault().postSticky(new UpdateItemInCart(cartItemList.get(position)));
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public CartItem getItemAtPosition(int pos) {
        return cartItemList.get(pos);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        Unbinder unbinder;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_cart)
        ImageView img_cart;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drinks_price)
        TextView txt_drink_price;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drinks_name)
        TextView txt_drink_name;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drinks_add_on)
        TextView txt_drinks_add_on;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drinks_size)
        TextView txt_drinks_size;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.number_buttons)
        ElegantNumberButton numberButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
