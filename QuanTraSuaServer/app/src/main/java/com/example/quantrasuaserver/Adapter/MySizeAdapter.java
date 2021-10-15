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

import com.example.quantrasuaserver.Callback.IRecyclerClickListener;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.EventBus.SelectSizeModel;
import com.example.quantrasuaserver.Model.SizeModel;
import com.example.quantrasuaserver.Model.UpdateSizeModel;
import com.example.quantrasuaserver.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MySizeAdapter extends RecyclerView.Adapter<MySizeAdapter.MyViewHolder> {

    Context context;
    List<SizeModel> sizeModelsList;
    UpdateSizeModel updateSizeModel;
    int editPos;

    public MySizeAdapter(Context context, List<SizeModel> sizeModelsList) {
        this.context = context;
        this.sizeModelsList = sizeModelsList;
        editPos = -1;
        updateSizeModel = new UpdateSizeModel();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_size_addon_display, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txt_name.setText(sizeModelsList.get(position).getName());
        holder.txt_price.setText(Common.formatPrice(sizeModelsList.get(position).getPrice()));

        //Events
        holder.img_delete.setOnClickListener(view -> {
            sizeModelsList.remove(position);
            notifyItemRemoved(position);
            updateSizeModel.setSizeModelList(sizeModelsList); //Set for event
            EventBus.getDefault().postSticky(updateSizeModel); //Send event
        });

        holder.setListener((view, pos) -> {
            editPos = position;
            EventBus.getDefault().postSticky(new SelectSizeModel(sizeModelsList.get(pos)));
        });
    }

    @Override
    public int getItemCount() {
        return sizeModelsList.size();
    }

    public void addNewSize(SizeModel sizeModel) {
        sizeModelsList.add(sizeModel);
        notifyItemInserted(sizeModelsList.size() - 1);
        updateSizeModel.setSizeModelList(sizeModelsList);
        EventBus.getDefault().postSticky(updateSizeModel);
    }

    public void editSize(SizeModel sizeModel) {
        if(editPos != -1){
            sizeModelsList.set(editPos,sizeModel);
            notifyItemChanged(editPos);
            editPos = -1; // reset variable after success
            //Send update
            updateSizeModel.setSizeModelList(sizeModelsList);
            EventBus.getDefault().postSticky(updateSizeModel);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_name)
        TextView txt_name;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_price)
        TextView txt_price;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_delete)
        ImageView img_delete;

        Unbinder unbinder;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view -> listener
                    .onItemClickListener(view, getAdapterPosition()));
        }
    }
}
