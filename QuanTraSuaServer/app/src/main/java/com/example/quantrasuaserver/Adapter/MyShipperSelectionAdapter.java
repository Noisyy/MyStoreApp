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
import com.example.quantrasuaserver.Model.ShipperModel;
import com.example.quantrasuaserver.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyShipperSelectionAdapter extends RecyclerView.Adapter<MyShipperSelectionAdapter.MyViewHolder> {

    private final Context context;
    private final List<ShipperModel> shipperModelList;
    private ImageView lastCheckedImageView = null;
    private ShipperModel selectShipper = null;

    public MyShipperSelectionAdapter(Context context, List<ShipperModel> shipperModelList) {
        this.context = context;
        this.shipperModelList = shipperModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_shipper_selected,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_name_shipper.setText(new StringBuffer(shipperModelList.get(position).getName()));
        holder.txt_phone_shipper.setText(new StringBuffer(shipperModelList.get(position).getPhone()));
        holder.setIRecyclerClickListener((view, pos) -> {
            if(lastCheckedImageView != null)
                lastCheckedImageView.setImageResource(0);
            holder.img_checked.setImageResource(R.drawable.ic_done);
            lastCheckedImageView = holder.img_checked;
            selectShipper = shipperModelList.get(pos);
        });
    }

    public ShipperModel getSelectShipper() {
        return selectShipper;
    }

    @Override
    public int getItemCount() {
        return shipperModelList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;

        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_name_shipper)
        TextView txt_name_shipper;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_phone_shipper)
        TextView txt_phone_shipper;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_checked)
        ImageView img_checked;

        IRecyclerClickListener iRecyclerClickListener;

        public void setIRecyclerClickListener(IRecyclerClickListener iRecyclerClickListener) {
            this.iRecyclerClickListener = iRecyclerClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerClickListener.onItemClickListener(view,getAdapterPosition());
        }
    }
}
