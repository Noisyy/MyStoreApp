package com.example.quantrasuaserver.Fragment.order;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quantrasuaserver.Adapter.MyShipperSelectionAdapter;
import com.example.quantrasuaserver.Callback.IShipperLoadCallbackListener;
import com.example.quantrasuaserver.EventBus.LoadOrderEvent;
import com.example.quantrasuaserver.Model.FCMSendData;
import com.example.quantrasuaserver.Model.OrderModel;
import com.example.quantrasuaserver.Model.ShipperModel;
import com.example.quantrasuaserver.Model.ShippingOrderModel;
import com.example.quantrasuaserver.Model.TokenModel;
import com.example.quantrasuaserver.Adapter.MyOrderAdapter;
import com.example.quantrasuaserver.Common.BottomSheetOrderFragment;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Common.MySwiperHelper;
import com.example.quantrasuaserver.R;
import com.example.quantrasuaserver.Remote.IFCMService;
import com.example.quantrasuaserver.Remote.RetrofitFCMClient;
import com.example.quantrasuaserver.databinding.FragmentOrderBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderFragment extends Fragment implements IShipperLoadCallbackListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_order_filter)
    TextView txt_order_filter;

    private RecyclerView recycler_shipper;

    Unbinder unbinder;
    private LayoutAnimationController layoutAnimationController;
    private MyOrderAdapter adapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;

    private IShipperLoadCallbackListener shipperLoadCallbackListener;

    private OrderViewModel orderViewModel;
    private FragmentOrderBinding binding;
    private MyShipperSelectionAdapter myShipperSelectedAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        orderViewModel =
                new ViewModelProvider(this).get(OrderViewModel.class);

        binding = FragmentOrderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        unbinder = ButterKnife.bind(this, root);
        initViews();
        orderViewModel.getMessageError().observe(getViewLifecycleOwner(), s ->
                Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());
        orderViewModel.getOrderModelMutableLiveData().observe(getViewLifecycleOwner(), orderModelList -> {
            if (orderModelList != null) {
                adapter = new MyOrderAdapter(getContext(), orderModelList);
                recycler_order.setAdapter(adapter);
                recycler_order.setLayoutAnimation(layoutAnimationController);

                updateTextCounter();
            }
        });
        return root;
    }

    private void initViews() {

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        shipperLoadCallbackListener = this;

        setHasOptionsMenu(true);

        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;


        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_order, width / 6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Gọi điện", 30, 0, Color.parseColor("#560027"), pos -> {
                    Dexter.withActivity(getActivity())
                            .withPermission(Manifest.permission.CALL_PHONE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                    OrderModel orderModel = adapter.getItemAtPosition(pos);
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel: " +
                                            orderModel.getUserPhone()));
                                    startActivity(intent);
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(getContext(), "You must accept" + response.getPermissionName(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                                }
                            }).check(); //Don't forget call check()
                }));
                buf.add(new MyButton(getContext(), "Xóa nè", 25, 0, Color.parseColor("#F44336"), pos -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                            .setTitle("Hủy đơn hàng")
                            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này?")
                            .setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {
                                OrderModel orderModel = adapter.getItemAtPosition(pos);
                                FirebaseDatabase.getInstance()
                                        .getReference(Common.ORDER_REF)
                                        .child(orderModel.getKey())
                                        .removeValue()
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                                        .addOnCompleteListener(task -> {
                                            adapter.removeItem(pos);
                                            adapter.notifyItemRemoved(pos);
                                            updateTextCounter();
                                            dialogInterface.dismiss();
                                            Toast.makeText(getContext(), "Đơn hàng đã hủy thành công!", Toast.LENGTH_SHORT).show();
                                        });
                            });
                    //Create dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    Button navigateButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    navigateButton.setTextColor(Color.GRAY);
                    Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    positiveButton.setTextColor(Color.RED);
                }));
                buf.add(new MyButton(getContext(), "Cập nhật", 25, 0, Color.parseColor("#2196F3"), pos ->
                        showEditDialog(adapter.getItemAtPosition(pos), pos)));
            }
        };
    }

    @SuppressLint("InflateParams")
    private void showEditDialog(OrderModel orderModel, int pos) {
        View layout_dialog;
        AlertDialog.Builder builder;
        if (orderModel.getOrderStatus() == 0) {
            layout_dialog = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_shipping, null);
            recycler_shipper = layout_dialog.findViewById(R.id.recycler_shippers);
            builder = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen).setView(layout_dialog);
        } else if (orderModel.getOrderStatus() == -1) { //Cancelled
            layout_dialog = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_cancelled, null);
            builder = new AlertDialog.Builder(requireContext()).setView(layout_dialog);
        } else { //Shipped
            layout_dialog = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_shipped, null);
            builder = new AlertDialog.Builder(requireContext()).setView(layout_dialog);
        }

        //View
        Button btn_ok = layout_dialog.findViewById(R.id.btn_ok);
        Button btn_cancel = layout_dialog.findViewById(R.id.btn_cancel);

        RadioButton rdi_shipping = layout_dialog.findViewById(R.id.rdi_shipping);
        RadioButton rdi_shipped = layout_dialog.findViewById(R.id.rdi_shipped);
        RadioButton rdi_cancelled = layout_dialog.findViewById(R.id.rdi_cancelled);
        RadioButton rdi_delete = layout_dialog.findViewById(R.id.rdi_delete);
        RadioButton rdi_restore_placed = layout_dialog.findViewById(R.id.rdi_restore_placed);

        TextView txt_status = layout_dialog.findViewById(R.id.txt_status);

        //set Data
        txt_status.setText(new StringBuffer("Trạng thái: ").append(Common.convertStatusToString(orderModel.getOrderStatus())));

        //Create Dialog
        AlertDialog dialog = builder.create();

        if (orderModel.getOrderStatus() == 0) //Shipping
            loadShipperList(pos, orderModel, dialog, btn_ok, btn_cancel,
                    rdi_shipping, rdi_shipped, rdi_cancelled, rdi_delete, rdi_restore_placed);
        else
            showDialog(pos, orderModel, dialog, btn_ok, btn_cancel,
                    rdi_shipping, rdi_shipped, rdi_cancelled, rdi_delete, rdi_restore_placed);

    }

    private void loadShipperList(int pos, OrderModel orderModel, AlertDialog dialog, Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed) {
        List<ShipperModel> tempList = new ArrayList<>();
        DatabaseReference shipperRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF);
        Query shipperActive = shipperRef.orderByChild("active").equalTo(true); //Load only shipper active by server app
        shipperActive.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot shipperSnapshot : snapshot.getChildren()) {
                    ShipperModel shipperModel = shipperSnapshot.getValue(ShipperModel.class);
                    if (shipperModel != null) {
                        shipperModel.setKey(shipperSnapshot.getKey());
                    }
                    tempList.add(shipperModel);
                }
                shipperLoadCallbackListener.onShipperLoadSuccess(pos, orderModel, tempList,
                        dialog, btn_ok, btn_cancel, rdi_shipping, rdi_shipped, rdi_cancelled, rdi_delete, rdi_restore_placed
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                shipperLoadCallbackListener.onShipperLoadFailed(error.getMessage());
            }
        });
    }

    private void showDialog(int pos, OrderModel orderModel, AlertDialog dialog, Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed) {
        dialog.show();
        //Custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_cancel.setOnClickListener(view -> dialog.dismiss());
        btn_ok.setOnClickListener(view -> {

            if (rdi_cancelled != null && rdi_cancelled.isChecked()) {
                updateOrder(pos, orderModel, -1);
                dialog.dismiss();
            } else if (rdi_shipping != null && rdi_shipping.isChecked()) //Shipping
            {
                ShipperModel shipperModel;
                if (myShipperSelectedAdapter != null) {
                    shipperModel = myShipperSelectedAdapter.getSelectShipper();
                    if (shipperModel != null) {
                        createShippingOrdeṛ(pos, shipperModel, orderModel, dialog);
                    } else {
                        Toast.makeText(getContext(), "Làm ơn chọn shipper", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (rdi_shipped != null && rdi_shipped.isChecked()) {
                updateOrder(pos, orderModel, 2);
                dialog.dismiss();
            } else if (rdi_restore_placed != null && rdi_restore_placed.isChecked()) {
                updateOrder(pos, orderModel, 0);
                dialog.dismiss();
            } else if (rdi_delete != null && rdi_delete.isChecked()) {
                deleteOrder(pos, orderModel);
                dialog.dismiss();
            }
        });
    }

    private void createShippingOrdeṛ(int pos, ShipperModel shipperModel, OrderModel orderModel, AlertDialog dialog) {
        ShippingOrderModel shippingOrder = new ShippingOrderModel();
        shippingOrder.setShipperPhone(shipperModel.getPhone());
        shippingOrder.setShipperName(shipperModel.getName());
        shippingOrder.setOrderModel(orderModel);
        shippingOrder.setStartTrip(false);
        shippingOrder.setCurrentLat(-1.0);
        shippingOrder.setCurrentLng(-1.0);

        FirebaseDatabase.getInstance()
                .getReference(Common.SHIPPING_ORDER_REF)
                .push()
                .setValue(shippingOrder)
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                dialog.dismiss();
                //First, get token of user
                FirebaseDatabase.getInstance()
                        .getReference(Common.TOKEN_REF)
                        .child(shipperModel.getKey())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                                    Map<String, String> notifyData = new HashMap<>();
                                    notifyData.put(Common.NOTIFY_TITLE, "Đơn hàng mới");
                                    notifyData.put(Common.NOTIFY_CONTENT, "Bạn có một đơn hàng mới cần giao đến " +
                                            orderModel.getUserPhone());

                                    FCMSendData sendData = null;
                                    if (tokenModel != null) {
                                        sendData = new FCMSendData(tokenModel.getToken(), notifyData);
                                    }

                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread()).subscribe(fcmResponse -> {
                                                dialog.dismiss();
                                                if (fcmResponse.getSuccess() == 1) {
                                                    updateOrder(pos, orderModel, 1);
                                                } else {
                                                    Toast.makeText(getContext(), "Cập nhật đơn hàng không thành công!", Toast.LENGTH_SHORT).show();
                                                }
                                            }, throwable ->
                                                    Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(), "Token not found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void deleteOrder(int pos, OrderModel orderModel) {
        if (!TextUtils.isEmpty(orderModel.getKey())) {

            FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .removeValue()
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(unused -> {
                        adapter.removeItem(pos);
                        adapter.notifyItemRemoved(pos);
                        updateTextCounter();
                        Toast.makeText(getContext(), "Hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Không có đơn hàng!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOrder(int pos, OrderModel orderModel, int status) {
        if (!TextUtils.isEmpty(orderModel.getKey())) {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("orderStatus", status);

            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(unused -> {

                        //Show dialog
                        android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
                        dialog.show();

                        //First, get token of user
                        FirebaseDatabase.getInstance()
                                .getReference(Common.TOKEN_REF)
                                .child(orderModel.getUserId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                                            Map<String, String> notifyData = new HashMap<>();
                                            notifyData.put(Common.NOTIFY_TITLE, "Đơn hàng: " + orderModel.getKey());
                                            notifyData.put(Common.NOTIFY_CONTENT, "Của bạn " +
                                                    Common.convertStatusToString(status));

                                            FCMSendData sendData = null;
                                            if (tokenModel != null) {
                                                sendData = new FCMSendData(tokenModel.getToken(), notifyData);
                                            }

                                            compositeDisposable.add(ifcmService.sendNotification(sendData)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread()).subscribe(fcmResponse -> {
                                                        dialog.dismiss();
                                                        if (fcmResponse.getSuccess() == 1) {
                                                            Toast.makeText(getContext(), "Cập nhật đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(getContext(), "Cập nhật đơn hàng thành công nhưng không gửi được thông báo!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, throwable ->
                                                            Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
                                        } else {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "Token not found", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        adapter.removeItem(pos);
                        adapter.notifyItemRemoved(pos);
                        updateTextCounter();

                    });
        } else {
            Toast.makeText(getContext(), "Không có đơn hàng!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTextCounter() {
        txt_order_filter.setText(new StringBuffer("Đơn hàng (").append(adapter.getItemCount()).append(")"));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
            bottomSheetOrderFragment.show(requireActivity().getSupportFragmentManager(), "OrderFilter");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        if (EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadOrderEvent(LoadOrderEvent event) {
        orderViewModel.loadOrderByStatus(event.getStatus());
    }

    @Override
    public void onShipperLoadSuccess(List<ShipperModel> shipperModelList) {
        //Do nothing
    }

    @Override
    public void onShipperLoadSuccess(int pos, OrderModel orderModel, List<ShipperModel> shipperModels, AlertDialog dialog, Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed) {
        if (recycler_shipper != null) {
            recycler_shipper.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            recycler_shipper.setLayoutManager(layoutManager);
            //recycler_shipper.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));

            myShipperSelectedAdapter = new MyShipperSelectionAdapter(getContext(), shipperModels);
            recycler_shipper.setAdapter(myShipperSelectedAdapter);
        }
        showDialog(pos, orderModel, dialog, btn_ok, btn_cancel, rdi_shipping, rdi_shipped, rdi_cancelled, rdi_delete, rdi_restore_placed);
    }

    @Override
    public void onShipperLoadFailed(String message) {
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();
    }


}