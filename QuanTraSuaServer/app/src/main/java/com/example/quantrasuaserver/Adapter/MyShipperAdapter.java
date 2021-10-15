package com.example.quantrasuaserver.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quantrasuaserver.EventBus.UpdateShipperEvent;
import com.example.quantrasuaserver.Model.ShipperModel;
import com.example.quantrasuaserver.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyShipperAdapter extends RecyclerView.Adapter<MyShipperAdapter.MyViewHolder> {

    Context context;
    List<ShipperModel> shipperModelList;

    public MyShipperAdapter(Context context, List<ShipperModel> shipperModelList) {
        this.context = context;
        this.shipperModelList = shipperModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_shipper,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_name_shipper.setText(new StringBuffer(shipperModelList.get(position).getName()));
        holder.txt_phone_shipper.setText(new StringBuffer(shipperModelList.get(position).getPhone()));
        holder.btn_enable.setChecked(shipperModelList.get(position).isActive());

        holder.btn_enable.setOnCheckedChangeListener((compoundButton, b) ->
                EventBus.getDefault().postSticky(new UpdateShipperEvent(shipperModelList.get(position),b)));
    }

    @Override
    public int getItemCount() {
        return shipperModelList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Unbinder unbinder;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_name_shipper)
        TextView txt_name_shipper;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_phone_shipper)
        TextView txt_phone_shipper;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.btn_enable)
        SwitchCompat btn_enable;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
