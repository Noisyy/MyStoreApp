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
import com.example.quantrasuaclient.Callback.IRecyclerClickListener;
import com.example.quantrasuaclient.EventBus.BestDealItemClick;
import com.example.quantrasuaclient.Model.BestDealModel;
import com.example.quantrasuaclient.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyBestDealsAdapter extends RecyclerView.Adapter<MyBestDealsAdapter.MyBestDealsViewHolder> {

    Context context;
    List<BestDealModel> bestDealModelList;

    public MyBestDealsAdapter(Context context, List<BestDealModel> bestDealModelList) {
        this.context = context;
        this.bestDealModelList = bestDealModelList;
    }

    @NonNull
    @Override
    public MyBestDealsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_best_deal_item, parent, false);
        return new MyBestDealsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyBestDealsViewHolder holder, int position) {
        Glide.with(context).load(bestDealModelList.get(position).getImage())
                .into(holder.img_best_deal);
        holder.txt_best_deal.setText(bestDealModelList.get(position).getName());
        holder.setListener((view, pos) ->
                EventBus.getDefault().postSticky(new BestDealItemClick(bestDealModelList.get(pos)))
        );
    }

    @Override
    public int getItemCount() {
        if (bestDealModelList != null) {
            return bestDealModelList.size();
        }
        return 0;
    }

    public static class MyBestDealsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Unbinder unbinder;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_best_deal)
        ImageView img_best_deal;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_best_deal)
        TextView txt_best_deal;
        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyBestDealsViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClickListener(view, getAdapterPosition());
        }
    }

}
