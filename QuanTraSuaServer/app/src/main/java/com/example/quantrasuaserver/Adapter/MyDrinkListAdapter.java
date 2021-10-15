package com.example.quantrasuaserver.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quantrasuaserver.Callback.IRecyclerClickListener;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Model.BestDealsModel;
import com.example.quantrasuaserver.Model.DrinksModel;
import com.example.quantrasuaserver.Model.MostPopularModel;
import com.example.quantrasuaserver.R;
import com.google.firebase.database.FirebaseDatabase;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyDrinkListAdapter extends RecyclerView.Adapter<MyDrinkListAdapter.MyViewHolder> {


    private final Context context;
    private final List<DrinksModel> drinksModels;

    private ExpandableLayout lastExpandable;

    public MyDrinkListAdapter(Context context, List<DrinksModel> drinksModels) {
        this.context = context;
        this.drinksModels = drinksModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_drink_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(drinksModels.get(position).getImage()).into(holder.img_food_image);
        holder.txt_drink_price.setText(new StringBuffer().append(Common.formatPrice(drinksModels.get(position).getPrice())).append(" VNĐ"));
        holder.txt_drink_name.setText(new StringBuffer().append(drinksModels.get(position).getName()));

        //Event
        holder.setListener((view, pos) -> {
            Common.selectDrinks = drinksModels.get(pos);
            Common.selectDrinks.setKey(String.valueOf(pos));
            //EventBus.getDefault().postSticky(new DrinksItemClick(true, drinksModels.get(pos)));

            //Show expandable
            if (lastExpandable != null && lastExpandable.isExpanded()) lastExpandable.collapse();

            if (!holder.expandable_layout.isExpanded()) {
                holder.expandable_layout.setSelected(true);
                holder.expandable_layout.expand();
            } else {
                holder.expandable_layout.collapse();
                holder.expandable_layout.setSelected(false);
            }
            lastExpandable = holder.expandable_layout;
        });

        holder.btn_best_deal.setOnClickListener(view ->
                makeDrinksToBestDeal(drinksModels.get(position)));

        holder.btn_most_popular.setOnClickListener(view ->
                makeDrinksToMostPopular(drinksModels.get(position)));

    }

    private void makeDrinksToMostPopular(DrinksModel drinksModel) {
        MostPopularModel mostPopularModel = new MostPopularModel();
        mostPopularModel.setName(drinksModel.getName());
        mostPopularModel.setMenu_id(Common.categorySelected.getMenu_id());
        mostPopularModel.setDrink_id(drinksModel.getId());
        mostPopularModel.setImage(drinksModel.getImage());

        FirebaseDatabase.getInstance()
                .getReference(Common.MOST_POPULAR_REF)
                .child(mostPopularModel.getMenu_id() + "_" + mostPopularModel.getDrink_id())
                .setValue(mostPopularModel)
                .addOnFailureListener(e ->
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(unused ->
                        Toast.makeText(context, "Thêm sản phẩm phổ biến thành công!", Toast.LENGTH_SHORT).show());
    }

    private void makeDrinksToBestDeal(DrinksModel drinksModel) {
        BestDealsModel bestDealsModel = new BestDealsModel();
        bestDealsModel.setName(drinksModel.getName());
        bestDealsModel.setMenu_id(Common.categorySelected.getMenu_id());
        bestDealsModel.setDrink_id(drinksModel.getId());
        bestDealsModel.setImage(drinksModel.getImage());

        FirebaseDatabase.getInstance()
                .getReference(Common.BEST_DEALS_REF)
                .child(bestDealsModel.getMenu_id() + "_" + bestDealsModel.getDrink_id())
                .setValue(bestDealsModel)
                .addOnFailureListener(e -> Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(unused -> Toast.makeText(context, "Thêm sản phẩm được mua nhiều thành công!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return drinksModels.size();
    }

    public DrinksModel getItemAtPosition(int pos) {
        return drinksModels.get(pos);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.expandable_layout)
        ExpandableLayout expandable_layout;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.btn_best_deal)
        Button btn_best_deal;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.btn_most_popular)
        Button btn_most_popular;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
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

