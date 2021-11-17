package com.example.quantrasuaclient.Fragment.View_Orders;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.quantrasuaclient.Adapter.MyOrdersAdapter;
import com.example.quantrasuaclient.Callback.ILoadOrderCallbackListener;
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Common.MySwiperHelper;
import com.example.quantrasuaclient.Database.DataSource.ICartDataSource;
import com.example.quantrasuaclient.Database.Local.CartDataSource;
import com.example.quantrasuaclient.Database.Local.RoomDatabase;
import com.example.quantrasuaclient.Database.ModelDB.CartItem;
import com.example.quantrasuaclient.EventBus.CounterCartEvent;
import com.example.quantrasuaclient.EventBus.HideFABCart;
import com.example.quantrasuaclient.EventBus.MenuItemBack;
import com.example.quantrasuaclient.Model.OrderModel;
import com.example.quantrasuaclient.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ViewOrdersFragment extends Fragment implements ILoadOrderCallbackListener {

    private ICartDataSource cartDataSource;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;
    //AlertDialog dialog;
    KProgressHUD dialog;

    Unbinder unbinder;

    private ViewOrdersViewModel OrdersViewModel;

    private ILoadOrderCallbackListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        OrdersViewModel = new ViewModelProvider(this).get(ViewOrdersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_orders, container, false);
        unbinder = ButterKnife.bind(this, root);
        initViews();
        loadOrdersFromFirebase();

        OrdersViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner(), orderModels -> {
            MyOrdersAdapter adapter = new MyOrdersAdapter(getContext(), orderModels);
            recycler_orders.setAdapter(adapter);
        });
        return root;
    }

    private void loadOrdersFromFirebase() {
        List<OrderModel> orderList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            OrderModel order = itemSnapshot.getValue(OrderModel.class);
                            if (order != null) {
                                order.setOrderNumber(itemSnapshot.getKey());
                            }
                            orderList.add(order);
                        }
                        listener.onLoadOrderSuccess(orderList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onLoadOrderFailed(error.getMessage());
                    }
                });
    }

    private void initViews() {
        EventBus.getDefault().postSticky(new HideFABCart(true));
        cartDataSource = new CartDataSource(RoomDatabase.getInstance(getContext()).cartDAO());
        listener = this;
        //dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();
        dialog = KProgressHUD.create(requireContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).setWindowColor(Color.TRANSPARENT)
                .show();

        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        //recycler_orders.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_orders, 250) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Hủy đơn", 30, 0, Color.parseColor("#FF3c30"), pos -> {
                    OrderModel orderModel = ((MyOrdersAdapter) Objects.requireNonNull(recycler_orders.getAdapter())).getItemAtPosition(pos);
                    if (orderModel.getOrderStatus() == 0) {
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
                        builder.setTitle("Hủy đơn hàng")
                                .setMessage("Bạn có chắc chắn muốn dừng đơn hàng?")
                                .setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss())
                                .setPositiveButton(Common.OPTIONS_ACCEPT, (dialogInterface, i) -> {
                            Map<String, Object> update_data = new HashMap<>();
                            update_data.put("orderStatus", -1); //Cancel order
                            FirebaseDatabase.getInstance()
                                    .getReference(Common.ORDER_REF)
                                    .child(orderModel.getOrderNumber())
                                    .updateChildren(update_data)
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                                    .addOnSuccessListener(unused -> {
                                orderModel.setOrderStatus(-1); //Local update
                                ((MyOrdersAdapter) recycler_orders.getAdapter()).serItemAtPosition(pos, orderModel);
                                recycler_orders.getAdapter().notifyItemChanged(pos);
                                Toast.makeText(getContext(), "Dừng đơn hàng thành công!", Toast.LENGTH_SHORT).show();

                            });
                        });
                        androidx.appcompat.app.AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        Toast.makeText(getContext(), new StringBuilder("Đơn đặt hàng của bạn ")
                                .append(Common.convertStatusToText(orderModel.getOrderStatus()))
                                .append(", vì vậy bạn không thể hủy!"), Toast.LENGTH_SHORT).show();
                    }
                }));

                buf.add(new MyButton(getContext(), "Cập nhật", 30, 0, Color.parseColor("#5d4037"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter) Objects.requireNonNull(recycler_orders.getAdapter())).getItemAtPosition(pos);

                            dialog.show(); //Show dialog if process is run on long time
                            cartDataSource.CleanCart(Common.currentUser.getUid()) //Clear all item in cart first
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@NonNull Integer integer) {
                                            //After clean cart, just add new
                                            CartItem[] cartItems = orderModel
                                                    .getCartItemList().toArray(new CartItem[0]);
                                            //Insert new
                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItems)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(() -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "Chuyển các mặt hàng trở lại giỏ hàng thành công", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true)); //Count fab
                                                    }, throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    })
                                            );
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "[ERROR]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));
            }
        };
    }

    private void scheduleDismiss() {
        Handler handler = new Handler();
        handler.postDelayed(() -> dialog.dismiss(), 500);
    }

    @Override
    public void onLoadOrderSuccess(List<OrderModel> orderList) {
        scheduleDismiss();
        OrdersViewModel.setMutableLiveDataOrderList(orderList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        scheduleDismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new HideFABCart(true));
    }

    @Override
    public void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().postSticky(new HideFABCart(false));
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}