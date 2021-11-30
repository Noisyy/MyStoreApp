package com.example.quantrasuaclient.Fragment.Cart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.example.quantrasuaclient.Callback.ISearchCategoryCallbackListener;
import com.example.quantrasuaclient.Common.CustomDialog;
import com.example.quantrasuaclient.Database.DataSource.ICartDataSource;
import com.example.quantrasuaclient.Database.Local.RoomDatabase;
import com.example.quantrasuaclient.Model.AddonModel;
import com.example.quantrasuaclient.Model.CategoryModel;
import com.example.quantrasuaclient.Model.DiscountModel;
import com.example.quantrasuaclient.Model.DrinksModel;
import com.example.quantrasuaclient.Model.FCMSendData;
import com.example.quantrasuaclient.Adapter.MyCartAdapter;
import com.example.quantrasuaclient.Callback.ILoadTimeFromFireBaseListener;
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Common.MySwiperHelper;
import com.example.quantrasuaclient.Database.ModelDB.CartItem;
import com.example.quantrasuaclient.Database.Local.CartDataSource;
import com.example.quantrasuaclient.EventBus.CounterCartEvent;
import com.example.quantrasuaclient.EventBus.HideFABCart;
import com.example.quantrasuaclient.EventBus.MenuItemBack;
import com.example.quantrasuaclient.EventBus.UpdateItemInCart;
import com.example.quantrasuaclient.Model.OrderModel;
import com.example.quantrasuaclient.Model.SizeModel;
import com.example.quantrasuaclient.R;
import com.example.quantrasuaclient.Remote.ICloudFunctions;
import com.example.quantrasuaclient.Remote.IFCMService;
import com.example.quantrasuaclient.Remote.RetrofitFCMClient;
import com.example.quantrasuaclient.Remote.RetrofitICloudClient;
import com.example.quantrasuaclient.Activity.ScanQRActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements ILoadTimeFromFireBaseListener, ISearchCategoryCallbackListener, TextWatcher {

    private static final int SCAN_QR_PERMISSION = 7171;
    private static final int REQUEST_BRAINTREE_CODE = 7777;
    String address, comment;
    private BottomSheetDialog addonBottomSheetDialog;
    private ChipGroup chip_group_addon, chip_group_user_select_addon;
    private EditText edt_search;

    private ISearchCategoryCallbackListener searchDrinksCallbackListener;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Parcelable recyclerViewState;
    private CartViewModel cartViewModel;
    private ICartDataSource cartDataSource;
    private MyCartAdapter adapter;
    KProgressHUD dialog;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    ILoadTimeFromFireBaseListener listener;
    IFCMService ifcmService;
    ICloudFunctions cloudFunctions;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edt_discount_code)
    EditText edt_discount_code;

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.img_scan)
    void onScanQRCode() {
        startActivityForResult(new Intent(requireContext(), ScanQRActivity.class), SCAN_QR_PERMISSION);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.img_check)
    void onApplyDiscount() {
        if (!TextUtils.isEmpty(edt_discount_code.getText().toString().toLowerCase())) {
            CustomDialog.show(getContext());

            final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
            final DatabaseReference discountRef = FirebaseDatabase.getInstance().getReference(Common.DISCOUNT_REF);
            offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long offset = snapshot.getValue(Long.class);
                    long estimatedServerTimeMS = System.currentTimeMillis() + offset;

                    discountRef.child(edt_discount_code.getText().toString().toLowerCase())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        DiscountModel discountModel = snapshot.getValue(DiscountModel.class);
                                        if (discountModel != null) {
                                            discountModel.setKey(snapshot.getKey());
                                        }
                                        if (discountModel != null) {
                                            if (discountModel.getUntilDate() < estimatedServerTimeMS) {
                                                CustomDialog.dismiss();
                                                listener.onLoadFailed("Mã giảm giá đã hết hạn sử dụng");
                                            } else {
                                                CustomDialog.dismiss();
                                                Common.discountApply = discountModel;
                                                sumAllItemInCart();
                                            }
                                        }
                                    } else {
                                        CustomDialog.dismiss();
                                        listener.onLoadFailed("Mã giảm giá không hợp lệ");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    CustomDialog.dismiss();
                                    listener.onLoadFailed(error.getMessage());
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    CustomDialog.dismiss();
                    listener.onLoadFailed(error.getMessage());
                }
            });
        }
        EventBus.getDefault().postSticky(new HideFABCart(true));
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick() {
        //new Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);
        //---------------------Find Item---------------------//
        EditText edt_address = view.findViewById(R.id.edt_addresss);
        EditText edt_comment = view.findViewById(R.id.edt_comments);
        TextView txt_address = view.findViewById(R.id.txt_address_detail);
        RadioButton rdi_home = view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_to_this = view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = view.findViewById(R.id.rdi_cod);
        RadioButton rdi_braintree = view.findViewById(R.id.rdi_braintree);
        Button btn_ok = view.findViewById(R.id.btn_ok);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        //---------------------Find Item---------------------//
        //Set Data Default
        edt_address.setText(Common.currentUser.getAddress()); //Address default
        edt_comment.setText(Common.currentUser.getPhone()); //Phone default
        //Address Default
        rdi_home.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                edt_address.setText(Common.currentUser.getAddress());
                edt_comment.setText(Common.currentUser.getPhone());
                txt_address.setVisibility(View.GONE);
            }
        });
        //Address other
        rdi_other_address.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                edt_address.setText(""); //Clear
                txt_address.setVisibility(View.GONE);
            }
        });
        //Get this address
        rdi_ship_to_this.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient.getLastLocation().addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    txt_address.setVisibility(View.GONE);
                }).addOnCompleteListener(task -> {
                    String coordinates = task.getResult().getLatitude() +
                            "/" +
                            task.getResult().getLongitude();
                    Single<String> singleAddress = Single.just(getAddressFromLatLng(task.getResult().getLatitude(), task.getResult().getLongitude()));
                    Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(@NonNull String s) {
                            edt_address.setText(s);
                            txt_address.setText("Tọa độ: " + coordinates);
                            txt_address.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            edt_address.setText(coordinates);
                            txt_address.setText(e.getMessage());
                            txt_address.setVisibility(View.VISIBLE);
                        }
                    });
                    Log.d("TAG", "onPlaceOrderClick: " + disposable);
                });
            }

        });
        //Set View
        builder.setView(view);
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        //Event button
        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_ok.setOnClickListener(v -> {
            //Check Valid Data
            if (edt_address.getText().toString().isEmpty()) {
                edt_address.setError("Vui lòng cung cấp địa chỉ nhận hàng!");
                edt_address.requestFocus();
            } else if (edt_comment.getText().toString().isEmpty()) {
                edt_comment.setError("Vui lòng cung cấp số điện thoại liên lạc!");
                edt_comment.requestFocus();
            } else {
                //Cod Payment
                if (rdi_cod.isChecked())
                    paymentCOD(edt_address.getText().toString(), edt_comment.getText().toString());
                else if (rdi_braintree.isChecked()) { //BrainTree Payment
                    address = edt_address.getText().toString();
                    comment = edt_comment.getText().toString();
                    if (!TextUtils.isEmpty(Common.currentToken)) {
                        DropInRequest dropInRequest = new DropInRequest().clientToken(Common.currentToken);
                        startActivityForResult(dropInRequest.getIntent(getContext()), REQUEST_BRAINTREE_CODE);
                    }
                }
                dialog.dismiss();
            }
        });
    }

    //Payment COD
    private void paymentCOD(String address, String comment) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    //when we have all cartItems, we will get total price
                    cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Double>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onSuccess(@NonNull Double totalPrice) {
                                    double finalPrice = totalPrice; //we will modify this formula for discount late
                                    OrderModel order = new OrderModel();
                                    order.setUserId(Common.currentUser.getUid());
                                    order.setUserName(Common.currentUser.getName());
                                    order.setUserPhone(Common.currentUser.getPhone());
                                    order.setShippingAddress(address);
                                    order.setComment(comment);

                                    if (currentLocation != null) {
                                        order.setLat(currentLocation.getLatitude());
                                        order.setLng(currentLocation.getLongitude());
                                    } else {
                                        order.setLat(-0.1f);
                                        order.setLng(-0.1f);
                                    }
                                    order.setCartItemList(cartItems);
                                    order.setTotalPayment(totalPrice);
                                    if (Common.discountApply != null)
                                        order.setDiscount(Common.discountApply.getPercent());
                                    else
                                        order.setDiscount(0);
                                    order.setFinalPayment(finalPrice);
                                    order.setCod(true);
                                    order.setTransactionId("Thanh toán khi nhận hàng");

                                    //Submit this order object to Firebase
                                    //writeOrderToFirebase(order);
                                    syncLocalTimeWithGlobalTime(order);
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    if (!Objects.requireNonNull(e.getMessage()).contains("Query returned empty result set"))
                                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }, throwable ->
                        Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
    }

    //Sync time local
    private void syncLocalTimeWithGlobalTime(OrderModel order) {
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset;// offset is missing time between your local time and server time
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date resultDate = new Date(estimatedServerTimeMs);
                //format date
                Log.d("TEST DATE", "" + sdf.format(resultDate));

                listener.onLoadTimeSuccess(order, estimatedServerTimeMs);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Save data with firebase
    private void writeOrderToFirebase(OrderModel order) {
        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(Common.createOrderNumber()) //Create order number with only digit
                .setValue(order)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {
            //Write success
            cartDataSource.CleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull Integer integer) {
                            Map<String, String> notifyData = new HashMap<>();
                            notifyData.put(Common.NOTIFY_TITLE, "Đơn hàng mới");
                            notifyData.put(Common.NOTIFY_CONTENT, "Có một đơn hàng mới từ " + Common.currentUser.getPhone());

                            FCMSendData sendData = new FCMSendData(Common.createTopicOrder(), notifyData);

                            compositeDisposable.add(ifcmService.sendNotification(sendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        Toast.makeText(getContext(), "Đặt hàng thành công rồi!", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "Đơn hàng đã được lưu lại nhưng chưa gửi được thông báo!", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }));

                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    //get Address
    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result;
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                result = address.getAddressLine(0);
            } else {
                result = "Địa chỉ của bạn không tồn tại!!";
            }

        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);
        //Service
        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        cloudFunctions = RetrofitICloudClient.getInstance().create(ICloudFunctions.class);
        listener = this;
        //get cart + get data
        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataListCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            if (cartItems == null || cartItems.isEmpty()) {
                recycler_cart.setVisibility(View.GONE);
                group_place_holder.setVisibility(View.GONE);
                txt_empty_cart.setVisibility(View.VISIBLE);
                //Fix bug navigation not working
                EventBus.getDefault().postSticky(new MenuItemBack());
            } else {
                recycler_cart.setVisibility(View.VISIBLE);
                group_place_holder.setVisibility(View.VISIBLE);
                txt_empty_cart.setVisibility(View.GONE);

                adapter = new MyCartAdapter(getContext(), cartItems);
                recycler_cart.setAdapter(adapter);
            }
            scheduleDismiss();
        });
        unbinder = ButterKnife.bind(this, root);
        initViews();
        initLocation();
        return root;
    }

    //Init location
    private void initLocation() {
        buildLocationOnRequest();
        buildLocationCallBack();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    //Callback Location
    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    //Location on request
    private void buildLocationOnRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }

    //Init view
    private void initViews() {
        searchDrinksCallbackListener = this;
        setHasOptionsMenu(true);
        cartDataSource = new CartDataSource(RoomDatabase.getInstance(getContext()).cartDAO());
        //Event bus hide cart + chat
        EventBus.getDefault().postSticky(new HideFABCart(true));
        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        //Show dialog
        dialog = KProgressHUD.create(requireContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).setWindowColor(Color.TRANSPARENT)
                .show();
        scheduleDismiss();
        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_cart, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Xóa nè", 30, 0, Color.parseColor("#FF3c30"),
                        pos -> {
                            CartItem cartItem = adapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItems(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@NonNull Integer integer) {
                                            adapter.notifyItemRemoved(pos);
                                            sumAllItemInCart(); //Update total price
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true)); //Update FAB
                                            Toast.makeText(getContext(), "Xóa sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {
                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));

                buf.add(new MyButton(getContext(), "Cập nhật", 30, 0, Color.parseColor("#5D4037"),
                        pos -> {
                            CartItem cartItem = adapter.getItemAtPosition(pos);
                            FirebaseDatabase.getInstance()
                                    .getReference(Common.CATEGORY_REF)
                                    .child(cartItem.getCategoryId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                CategoryModel categoryModel = snapshot.getValue(CategoryModel.class);
                                                searchDrinksCallbackListener.onSearchCategoryFound(categoryModel, cartItem);
                                            } else {
                                                searchDrinksCallbackListener.onSearchCategoryNotFound("NOT FOUND 404");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            searchDrinksCallbackListener.onSearchCategoryNotFound(error.getMessage());
                                        }
                                    });
                        }));
            }
        };
        Log.d("TAG", "initViews: " + mySwiperHelper);
        sumAllItemInCart();

        //Addon
        addonBottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.DialogStyle);
        @SuppressLint("InflateParams")
        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);
        chip_group_addon = layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = layout_addon_display.findViewById(R.id.edt_search);
        addonBottomSheetDialog.setContentView(layout_addon_display);

        addonBottomSheetDialog.setOnDismissListener(dialogInterface -> {
            displayUserSelectedAddon(chip_group_user_select_addon);
            calculateTotalPrice();
        });
    }

    //Dialog dismiss
    private void scheduleDismiss() {
        Handler handler = new Handler();
        handler.postDelayed(() -> dialog.dismiss(), 500);
    }

    //Select addon
    private void displayUserSelectedAddon(ChipGroup chip_group_user_select_addon) {
        if (Common.selectDrinks.getUserSelectedAddon() != null && Common.selectDrinks.getUserSelectedAddon().size() > 0) {
            chip_group_user_select_addon.removeAllViews();
            for (AddonModel addonModel : Common.selectDrinks.getUserSelectedAddon()) {
                @SuppressLint("InflateParams")
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("( +")
                        .append(Common.formatPrice(addonModel.getPrice())).append(")"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        if (Common.selectDrinks.getUserSelectedAddon() == null)
                            Common.selectDrinks.setUserSelectedAddon(new ArrayList<>());
                        Common.selectDrinks.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_user_select_addon.addView(chip);
            }
        } else
            chip_group_user_select_addon.removeAllViews();
    }

    //Sum item
    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Double aDouble) {
                        if (Common.discountApply != null) {
                            aDouble = aDouble - (aDouble * Common.discountApply.getPercent() / 100);
                            txt_total_price.setText(new StringBuffer("Tổng tiền: ").append(Common.formatPrice(aDouble))
                                    .append(" (-")
                                    .append(Common.discountApply.getPercent())
                                    .append("%)")
                                    .append(" VND"));
                        } else {
                            txt_total_price.setText(new StringBuilder("Tổng tiền: ").append(Common.formatPrice(aDouble)).append(" VND"));
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (!Objects.requireNonNull(e.getMessage()).contains("Query returned empty"))
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.layout_remove_cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            cartDataSource.CleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull Integer integer) {
                            Toast.makeText(getContext(), "Xóa giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        EventBus.getDefault().postSticky(new MenuItemBack());
    }

    @Override
    public void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().postSticky(new HideFABCart(false));
        cartViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
        EventBus.getDefault().postSticky(new HideFABCart(true));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event) {
        if (event.getCartItem() != null) {
            //First , save stage of Recycler view
            recyclerViewState = Objects.requireNonNull(recycler_cart.getLayoutManager()).onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState); //Fix error refresh recycler view after update

                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    //calculate
    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Double price) {
                        txt_total_price.setText(new StringBuffer("Tổng Tiền: ").append(Common.formatPrice(price)).append(" VND"));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (!Objects.requireNonNull(e.getMessage()).contains("Query returned empty result set"))
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onLoadTimeSuccess(OrderModel order, long estimateTimeInMs) {
        order.setCreateDate(estimateTimeInMs);
        order.setOrderStatus(0);
        writeOrderToFirebase(order);
    }

    @Override
    public void onLoadTimeSuccess(long estimateTimeInMs) {

    }

    @Override
    public void onLoadFailed(String message) {
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

    @Override
    public void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem) {
        DrinksModel drinksModel = Common.findDrinksInListById(categoryModel, cartItem.getDrinksId());
        if (drinksModel != null) {
            showUpdateDialog(cartItem, drinksModel);
        } else {
            Toast.makeText(getContext(), "NOT FOUND 404", Toast.LENGTH_SHORT).show();
        }
    }

    //Update order
    private void showUpdateDialog(CartItem cartItem, DrinksModel drinksModel) {
        Common.selectDrinks = drinksModel;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_update_cart, null);
        builder.setView(itemView);
        //View
        Button btn_ok = itemView.findViewById(R.id.btn_ok);
        Button btn_cancel = itemView.findViewById(R.id.btn_cancel);
        RadioGroup rdi_group_size = itemView.findViewById(R.id.rdi_group_size);
        chip_group_user_select_addon = itemView.findViewById(R.id.chip_group_user_selected_addon);
        ImageView img_addon = itemView.findViewById(R.id.img_add_addon);
        //event
        img_addon.setOnClickListener(view -> {
            if (drinksModel.getAddon() != null) {
                displayAddonList();
                addonBottomSheetDialog.show();
            }
        });
        //Size
        if (drinksModel.getSize() != null) {
            for (SizeModel sizeModel : drinksModel.getSize()) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setTextSize(18);
                Typeface face = getResources().getFont(R.font.roboto_slab);
                radioButton.setTypeface(face);
                radioButton.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b)
                        Common.selectDrinks.setUserSelectedSize(sizeModel);
                    calculateTotalPrice();
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                radioButton.setLayoutParams(params);
                radioButton.setText(sizeModel.getName());
                radioButton.setTextSize(16);
                radioButton.setTag(sizeModel.getPrice());

                rdi_group_size.addView(radioButton);
            }

            if (rdi_group_size.getChildCount() > 0) {
                RadioButton radioButton = (RadioButton) rdi_group_size.getChildAt(0); //get first radio button
                radioButton.setChecked(true); //set default at first radio button

            }
        }

        //Addon
        displayAlreadySelectedAddon(chip_group_user_select_addon, cartItem);

        //Show Dialog
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        //Custom dialog
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().setGravity(Gravity.CENTER);
        //Event
        btn_ok.setOnClickListener(view -> {
            //First, delete item in cart
            cartDataSource.deleteCartItems(cartItem)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull Integer integer) {
                            //After that, update information and add new
                            //Update price and info
                            if (Common.selectDrinks.getUserSelectedAddon() != null) {
                                cartItem.setDrinksAddon(new Gson().toJson(Common.selectDrinks.getUserSelectedAddon()));
                            } else
                                cartItem.setDrinksAddon("Default");
                            if (Common.selectDrinks.getUserSelectedSize() != null) {
                                cartItem.setDrinksSize(new Gson().toJson(Common.selectDrinks.getUserSelectedSize()));
                            } else {
                                cartItem.setDrinksSize("Default");
                            }

                            cartItem.setDrinksExtraPrice(Common.calculateExtraPrice(Common.selectDrinks.getUserSelectedSize(),
                                    Common.selectDrinks.getUserSelectedAddon()));

                            //Insert new
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true)); //Count cart again
                                        calculateTotalPrice();
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "Cập nhật giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                                    }, throwable ->
                                            Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show())
                            );
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        btn_cancel.setOnClickListener(view -> dialog.dismiss());
    }

    //Already addon
    private void displayAlreadySelectedAddon(ChipGroup chip_group_user_select_addon, CartItem cartItem) {
        //this function will display all addon we already select before add to cart end display on layout
        if (cartItem.getDrinksAddon() != null && !cartItem.getDrinksAddon().equals("Default")) {
            List<AddonModel> addonModels = new Gson().
                    fromJson(cartItem.getDrinksAddon(), new TypeToken<List<AddonModel>>() {
                    }.getType());
            Common.selectDrinks.setUserSelectedAddon(addonModels);
            chip_group_user_select_addon.removeAllViews();
            //Add all view
            for (AddonModel addonModel : addonModels) {
                @SuppressLint("InflateParams")
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("( +")
                        .append(Common.formatPrice(addonModel.getPrice())).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view -> {
                    chip_group_user_select_addon.removeView(view);
                    Common.selectDrinks.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });
                chip_group_user_select_addon.addView(chip);
            }
        }
    }

    //Addon List
    private void displayAddonList() {
        if (Common.selectDrinks.getAddon() != null && Common.selectDrinks.getAddon().size() > 0) {
            chip_group_addon.clearCheck();
            chip_group_addon.removeAllViews();
            edt_search.addTextChangedListener(this);
            //Add all view
            for (AddonModel addonModel : Common.selectDrinks.getAddon()) {
                @SuppressLint("InflateParams")
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("( +")
                        .append(Common.formatPrice(addonModel.getPrice())).append(")"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        if (Common.selectDrinks.getUserSelectedAddon() == null)
                            Common.selectDrinks.setUserSelectedAddon(new ArrayList<>());
                        Common.selectDrinks.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_addon.addView(chip);
            }
        }
    }

    @Override
    public void onSearchCategoryNotFound(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    //Search Addon
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();
        for (AddonModel addonModel : Common.selectDrinks.getAddon()) {
            if (addonModel.getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                @SuppressLint("InflateParams")
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("( +")
                        .append(Common.formatPrice(addonModel.getPrice())).append(")"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        if (Common.selectDrinks.getUserSelectedAddon() == null)
                            Common.selectDrinks.setUserSelectedAddon(new ArrayList<>());
                        Common.selectDrinks.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_addon.addView(chip);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BRAINTREE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                assert data != null;
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                //Calculate sum cart
                cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Double>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@NonNull Double totalPrice) {
                                //Get all item in cart to create order
                                compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(cartItems -> {
                                            //submit payment
                                            assert nonce != null;
                                            compositeDisposable.add(cloudFunctions.submitPayment(totalPrice,
                                                    nonce.getNonce())
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(braintreeTransaction -> {
                                                        if (braintreeTransaction.isSuccess()) {
                                                            double finalPrice = totalPrice; //we will modify this formula for discount late
                                                            OrderModel order = new OrderModel();
                                                            order.setUserId(Common.currentUser.getUid());
                                                            order.setUserName(Common.currentUser.getName());
                                                            order.setUserPhone(Common.currentUser.getPhone());
                                                            order.setShippingAddress(address);
                                                            order.setComment(comment);

                                                            if (currentLocation != null) {
                                                                order.setLat(currentLocation.getLatitude());
                                                                order.setLng(currentLocation.getLongitude());
                                                            } else {
                                                                order.setLat(-0.1f);
                                                                order.setLng(-0.1f);
                                                            }
                                                            order.setCartItemList(cartItems);
                                                            order.setTotalPayment(totalPrice);
                                                            if (Common.discountApply != null)
                                                                order.setDiscount(Common.discountApply.getPercent());
                                                            else
                                                                order.setDiscount(0);
                                                            order.setFinalPayment(finalPrice);
                                                            order.setCod(false);
                                                            order.setTransactionId(braintreeTransaction.getTransaction().getId());

                                                            //Submit this order object to Firebase
                                                            //writeOrderToFirebase(order);
                                                            syncLocalTimeWithGlobalTime(order);
                                                        }
                                                    }, throwable -> {

                                                    }));
                                        }, throwable -> Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        if (requestCode == SCAN_QR_PERMISSION) {
            if (requestCode == Activity.RESULT_OK) {
                edt_discount_code.setText(data.getStringExtra(Common.QR_CODE_TAG).toLowerCase());
            }
        }
    }
}