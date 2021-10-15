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
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Database.ModelDB.Favorite;
import com.example.quantrasuaclient.R;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyFavoriteAdapter extends RecyclerView.Adapter<MyFavoriteAdapter.MyViewHolder> {

    Context context;
    List<Favorite> favoriteList;

    public MyFavoriteAdapter(Context context, List<Favorite> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_favorite_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(favoriteList.get(position).image).into(holder.img_food_image);
        holder.txt_drink_name.setText(favoriteList.get(position).name);
        holder.txt_drink_price.setText("Giá: " + Common.formatPrice(favoriteList.get(position).price) + " VNĐ");
        holder.txt_drink_description.setText("Mô tả: " + favoriteList.get(position).description);
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public Favorite getItemAtPosition(int pos) {
        return favoriteList.get(pos);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Unbinder unbinder;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drink_name)
        TextView txt_drink_name;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drink_price)
        TextView txt_drink_price;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_drink_image)
        ImageView img_food_image;
        @BindView(R.id.txt_drink_description)
        TextView txt_drink_description;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
