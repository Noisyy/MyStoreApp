package com.example.quantrasuaclient.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quantrasuaclient.Callback.IRecyclerClickListener;
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Database.DataSource.ICartDataSource;
import com.example.quantrasuaclient.Database.Local.RoomDatabase;
import com.example.quantrasuaclient.Database.ModelDB.CartItem;
import com.example.quantrasuaclient.Database.Local.CartDataSource;
import com.example.quantrasuaclient.Database.ModelDB.Favorite;
import com.example.quantrasuaclient.EventBus.CounterCartEvent;
import com.example.quantrasuaclient.EventBus.DrinksItemClick;
import com.example.quantrasuaclient.Model.DrinksModel;
import com.example.quantrasuaclient.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyDrinkListAdapter extends RecyclerView.Adapter<MyDrinkListAdapter.MyViewHolder> {

    private final Context context;
    private final List<DrinksModel> drinksModels;
    private final CompositeDisposable compositeDisposable;
    private final ICartDataSource cartDataSource;

    public MyDrinkListAdapter(Context context, List<DrinksModel> drinksModels) {
        this.context = context;
        this.drinksModels = drinksModels;
        this.compositeDisposable = new CompositeDisposable();
        this.cartDataSource = new CartDataSource(RoomDatabase.getInstance(context).cartDAO());
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
        holder.txt_drink_description.setText(new StringBuffer().append(drinksModels.get(position).getDescription()));

        //Event
        holder.setListener((view, pos) -> {
            Common.selectDrinks = drinksModels.get(pos);
            Common.selectDrinks.setKey(String.valueOf(pos));
            EventBus.getDefault().postSticky(new DrinksItemClick(true, drinksModels.get(pos)));
        });

        //xử lý cart
        holder.img_cart.setOnClickListener(view -> {
            CartItem cartItem = new CartItem();
            cartItem.setUid(Common.currentUser.getUid());
            cartItem.setUserPhone(Common.currentUser.getPhone());

            cartItem.setCategoryId(Common.categorySelected.getMenu_id());
            cartItem.setDrinksId(drinksModels.get(position).getId());
            cartItem.setDrinksName(drinksModels.get(position).getName());
            cartItem.setDrinksImage(drinksModels.get(position).getImage());
            cartItem.setDrinksPrice(Double.valueOf(String.valueOf(drinksModels.get(position).getPrice())));
            cartItem.setDrinksQuantity(1);
            cartItem.setDrinksExtraPrice(0.0); //Because default we not choose size + addon so extra price is 0
            cartItem.setDrinksAddon("Default");
            cartItem.setDrinksSize("Default");

            cartDataSource.getItemWithOptionsInCart(Common.currentUser.getUid(),
                    Common.categorySelected.getMenu_id(),
                    cartItem.getDrinksId(),
                    cartItem.getDrinksSize(),
                    cartItem.getDrinksAddon())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<CartItem>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull CartItem cartItemFromDB) {
                            if (cartItemFromDB.equals(cartItem)) {
                                //Already in database, just update
                                cartItemFromDB.setDrinksExtraPrice(cartItem.getDrinksExtraPrice());
                                cartItemFromDB.setDrinksAddon(cartItem.getDrinksAddon());
                                cartItemFromDB.setDrinksSize(cartItem.getDrinksSize());
                                cartItemFromDB.setDrinksQuantity(cartItemFromDB.getDrinksQuantity() + cartItem.getDrinksQuantity());

                                cartDataSource.updateCartItems(cartItemFromDB)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<Integer>() {
                                            @Override
                                            public void onSubscribe(@NonNull Disposable d) {

                                            }

                                            @Override
                                            public void onSuccess(@NonNull Integer integer) {
                                                Toast.makeText(context, "Cập nhật giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable e) {
                                                Toast.makeText(context, "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                //Item not available in cart before, insert new
                                compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() -> {
                                            Toast.makeText(context, "Thêm giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }, throwable -> Toast.makeText(context, "[CART ERROR]", Toast.LENGTH_SHORT).show())
                                );
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            if (e.getMessage().contains("empty")) {
                                //Default, if Cart is empty, this code will be fired
                                compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() -> {
                                            Toast.makeText(context, "Thêm giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }, throwable -> Toast.makeText(context, "[CART ERROR]", Toast.LENGTH_SHORT).show())
                                );
                            } else
                                Toast.makeText(context, "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        //Xu ly favorite
        if(Common.favoriteRepository.isFavorite(Integer.parseInt(drinksModels.get(position).getId()))==1){
            holder.img_fav.setImageResource(R.drawable.ic_menu_favorite);
        }else {
            holder.img_fav.setImageResource(R.drawable.ic_favorite_border);
        }
        holder.img_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Common.favoriteRepository.isFavorite(Integer.parseInt(drinksModels.get(position).getId()))!=1){
                    addOrRemoveFavorite(drinksModels.get(position),true);
                    holder.img_fav.setImageResource(R.drawable.ic_menu_favorite);
                }
                else {
                    addOrRemoveFavorite(drinksModels.get(position),false);
                    holder.img_fav.setImageResource(R.drawable.ic_favorite_border);
                }
            }
        });
    }

    private void addOrRemoveFavorite(DrinksModel drinksModel,boolean isAdd) {
        Favorite favorite = new Favorite();
        favorite.id = drinksModel.getId();
        favorite.description = drinksModel.getDescription();
        favorite.name = drinksModel.getName();
        favorite.image = drinksModel.getImage();
        favorite.price = drinksModel.getPrice();
        if(isAdd){
            Common.favoriteRepository.insert(favorite);
            Toast.makeText(context, "Thêm vào danh sách yêu thích thành công !!", Toast.LENGTH_SHORT).show();
        }else {
            Common.favoriteRepository.delete(favorite);
            Toast.makeText(context, "Hủy bỏ yêu thích thành công !!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return drinksModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final Unbinder unbinder;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drink_name)
        TextView txt_drink_name;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drink_price)
        TextView txt_drink_price;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.txt_drink_description)
        TextView txt_drink_description;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_drink_image)
        ImageView img_food_image;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_favorite)
        ImageView img_fav;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_quick_cart)
        ImageView img_cart;
        @SuppressLint("NonConstantResourceId")
        @BindView(R.id.img_detail)
        ImageView img_detail;

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
