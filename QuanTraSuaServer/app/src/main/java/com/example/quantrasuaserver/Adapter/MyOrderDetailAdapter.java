package com.example.quantrasuaserver.Adapter;

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
import com.example.quantrasuaserver.Model.AddonModel;
import com.example.quantrasuaserver.Model.CartItem;
import com.example.quantrasuaserver.Model.SizeModel;
import com.example.quantrasuaserver.R;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderDetailAdapter extends RecyclerView.Adapter<MyOrderDetailAdapter.MyViewHolder> {

    Context context;
    List<CartItem> cartItemList;
    Gson gson;

    public MyOrderDetailAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        gson = new Gson();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_order_detail_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(cartItemList.get(position).getDrinksImage()).into(holder.img_drinks_image);
        holder.txt_drinks_name.setText(new StringBuilder().append(cartItemList.get(position).getDrinksName()));
        holder.txt_drinks_quantity.setText(new StringBuilder("Số lượng: ").append(cartItemList.get(position).getDrinksQuantity()));
        try{
            SizeModel sizeModel = gson.fromJson(cartItemList.get(position).getDrinksSize(), new TypeToken<SizeModel>() {
            }.getType());
            if (sizeModel != null) {
                holder.txt_size.setText(new StringBuilder("Size: ").append(sizeModel.getName()));
            }
        }catch (IllegalStateException | JsonSyntaxException exception){
            holder.txt_size.setText(new StringBuilder("Size: ").append("Small"));
        }
        if (!cartItemList.get(position).getDrinksAddon().equals("Default"))
        {
            List<AddonModel> addonModels = gson.fromJson(cartItemList.get(position).getDrinksAddon(), new TypeToken<List<AddonModel>>() {}.getType());
            StringBuilder addonString = new StringBuilder();
            if (addonModels != null) {
                for (AddonModel addonModel : addonModels) {
                    addonString.append(addonModel.getName()).append(",");
                }
                addonString.delete(addonString.length() - 1, addonString.length()); //Remove last "," character
                holder.txt_drinks_add_on.setText(new StringBuilder("Topping: ").append(addonString));
            }
        } else {
            holder.txt_drinks_add_on.setText(new StringBuilder("Topping: Không có"));
        }
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drinks_name)
        TextView txt_drinks_name;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drinks_add_on)
        TextView txt_drinks_add_on;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_size)
        TextView txt_size;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drinks_quantity)
        TextView txt_drinks_quantity;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_drinks_image)
        ImageView img_drinks_image;

        Unbinder unbinder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
