package com.example.quantrasuaclient.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Database.DataSource.FavoriteRepository;
import com.example.quantrasuaclient.Database.DataSource.ICartDataSource;
import com.example.quantrasuaclient.Database.Local.FavoriteDataSource;
import com.example.quantrasuaclient.Database.Local.RoomDatabase;
import com.example.quantrasuaclient.Database.Local.CartDataSource;
import com.example.quantrasuaclient.EventBus.BestDealItemClick;
import com.example.quantrasuaclient.EventBus.CartItemClick;
import com.example.quantrasuaclient.EventBus.CategoryClick;
import com.example.quantrasuaclient.EventBus.CounterCartEvent;
import com.example.quantrasuaclient.EventBus.DrinksItemClick;
import com.example.quantrasuaclient.EventBus.HideFABCart;
import com.example.quantrasuaclient.EventBus.MenuItemBack;
import com.example.quantrasuaclient.EventBus.PopularCategoryClick;
import com.example.quantrasuaclient.EventBus.ProfileUser;
import com.example.quantrasuaclient.Model.CategoryModel;
import com.example.quantrasuaclient.Model.DrinksModel;
import com.example.quantrasuaclient.R;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nex3z.notificationbadge.NotificationBadge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;

    private ICartDataSource cartDataSource;

    KProgressHUD dialog;

    private int menuClickId = -1;

    NotificationBadge badge;

    @SuppressLint("StaticFieldLeak")
    public static TextView txt_user, txt_phone;
    @SuppressLint("StaticFieldLeak")
    public static ImageView imageView;
    @SuppressLint("StaticFieldLeak")
    public static Context context;


    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.fab)
    CounterFab fab;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.fab_chat)
    CounterFab fab_chat;

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.fab_chat)
    void onFabChatClick() {
        startActivity(new Intent(this, ChatActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        countCartItem();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.quantrasuaclient.databinding.ActivityHomeBinding binding = com.example.quantrasuaclient.databinding.ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Show dialog
        dialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).setWindowColor(Color.TRANSPARENT);

        ButterKnife.bind(this);
        cartDataSource = new CartDataSource(RoomDatabase.getInstance(this).cartDAO());

        setSupportActionBar(binding.appBarHome.toolbar);
        binding.appBarHome.fab.setOnClickListener(view -> navController.navigate(R.id.nav_cart));
        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_cart, R.id.nav_profile, R.id.nav_view_orders, R.id.nav_favorite)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront(); //Fixed

        //Init database
        initDB();

        View headerView = navigationView.getHeaderView(0);
        txt_user = headerView.findViewById(R.id.txt_user);
        txt_phone = headerView.findViewById(R.id.txt_phone);
        imageView = headerView.findViewById(R.id.imageView);
        Common.setSpanString("", Common.currentUser.getName(), txt_user);
        txt_phone.setText(Common.currentUser.getPhone());
        Glide.with(this).load(Common.currentUser.getImage()).error(R.drawable.app_icon).into(imageView);
        countCartItem();
    }

    private void initDB() {
        Common.roomDatabase = RoomDatabase.getInstance(this);
        Common.favoriteRepository = FavoriteRepository.getInstance(FavoriteDataSource.getInstance(Common.roomDatabase.favoriteDAO()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_home:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_home);
                break;
            case R.id.nav_menu:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_menu);
                break;
            case R.id.nav_favorite:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_favorite);
                break;
            case R.id.nav_profile:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_profile);
                break;
            case R.id.nav_cart:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_cart);
                break;
            case R.id.nav_news:
                if (item.getItemId() != menuClickId) {
                    EventBus.getDefault().postSticky(new MenuItemBack());
                    showSubscribeNews();
                }
                break;
            case R.id.nav_view_orders:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_view_orders);
                break;
            case R.id.nav_sign_out:
                SignOut();
                break;
        }
        menuClickId = item.getItemId();
        return false;
    }

    private void showSubscribeNews() {
        Paper.init(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_subscribe_news, null);
        CheckBox ckb_news = itemView.findViewById(R.id.ckb_subscribe_news);
        Button btn_cancel = itemView.findViewById(R.id.btn_cancel);
        Button btn_ok = itemView.findViewById(R.id.btn_ok);

        boolean isSubscribeNews = Paper.book().read(Common.IS_SUBSCRIBE_NEWS, false);
        if (isSubscribeNews)
            ckb_news.setChecked(true);

        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_ok.setOnClickListener(v -> {
            if (ckb_news.isChecked()) {
                //Sử dụng paper để lưu lại thông tin đăng ký hay là không đăng ký
                Paper.book().write(Common.IS_SUBSCRIBE_NEWS, true);
                //Đăng ký dịch vụ với firebaseMessaging thôi
                FirebaseMessaging.getInstance()
                        .subscribeToTopic(Common.NEWS_TOPIC)
                        .addOnFailureListener(e ->
                                Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                        .addOnSuccessListener(aVoid -> {
                            EventBus.getDefault().removeAllStickyEvents();
                            Toast.makeText(HomeActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
            } else {
                Paper.book().delete(Common.IS_SUBSCRIBE_NEWS);
                FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic(Common.NEWS_TOPIC)
                        .addOnFailureListener(e ->
                                Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                        .addOnSuccessListener(aVoid -> {
                            EventBus.getDefault().removeAllStickyEvents();
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, "Hủy đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        });
            }
        });

    }

    private void SignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) ->
                        dialogInterface.dismiss())
                .setPositiveButton(Common.OPTIONS_ACCEPT, (dialogInterface, i) -> {
                    Common.selectDrinks = null;
                    Common.categorySelected = null;
                    Common.currentUser = null;
                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void scheduleDismiss() {
        Handler handler = new Handler();
        handler.postDelayed(() -> dialog.dismiss(), 500);
    }

    //Event Bus
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChangeProfile(ProfileUser event){
        if(event.isSuccess()){
            if(event.getUserModel().getImage() != null){
                txt_user.setText(event.getUserModel().getName());
                txt_phone.setText(event.getUserModel().getPhone());
                Glide.with(this).load(event.getUserModel().getImage()).error(R.drawable.app_icon).into(imageView);
            }else {
                txt_user.setText(event.getUserModel().getName());
                txt_phone.setText(event.getUserModel().getPhone());
            }

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_drink_list);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDrinksItemClick(DrinksItemClick event) {
        if (event.isSuccess()) {
            dialog.show();
            navController.navigate(R.id.nav_drink_detail);
            scheduleDismiss();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuCartItemClick(CartItemClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_cart);
        }
    }

    //Add to cart
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event) {
        if (event.isHidden()) {
            fab.hide();
            fab_chat.hide();
        } else {
            fab.show();
            fab_chat.show();
        }
    }

    //Add to cart
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void countCartAgain(CounterCartEvent event) {
        if (event.isSuccess()) {
            countCartItem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void countCartAgain(MenuItemBack event) {
        menuClickId = -1;
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularItemClick(PopularCategoryClick event) {
        if (event.getPopularCategoryModel() != null) {
            dialog.show();
            FirebaseDatabase.getInstance().getReference("Category")
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                if (Common.categorySelected != null) {
                                    Common.categorySelected.setMenu_id(snapshot.getKey());
                                }
                                //Load drinks
                                FirebaseDatabase.getInstance().getReference("Category")
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("drinks")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getDrink_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot itemSnapShot : snapshot.getChildren()) {
                                                        Common.selectDrinks = itemSnapShot.getValue(DrinksModel.class);
                                                        if (Common.selectDrinks != null) {
                                                            Common.selectDrinks.setKey(itemSnapShot.getKey());
                                                        }
                                                    }

                                                    navController.navigate(R.id.nav_drink_detail);

                                                } else {
                                                    Toast.makeText(HomeActivity.this, "Sản phẩm không tồn tại!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Sản phẩm không tồn tại!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //dialog.dismiss();
                            Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealItemClick event) {
        if (event.getBestDealModel() != null) {
            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                if (Common.categorySelected != null) {
                                    Common.categorySelected.setMenu_id(snapshot.getKey());
                                }

                                //Load drinks
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getBestDealModel().getMenu_id())
                                        .child("drinks")
                                        .orderByChild("id") //Fix bug
                                        .equalTo(event.getBestDealModel().getDrink_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                                        Common.selectDrinks = itemSnapshot.getValue(DrinksModel.class);
                                                        if (Common.selectDrinks != null) {
                                                            Common.selectDrinks.setKey(itemSnapshot.getKey());
                                                        }
                                                    }

                                                } else {
                                                    Toast.makeText(HomeActivity.this, "Sản phẩm không tồn tại!", Toast.LENGTH_SHORT).show();
                                                }
                                                navController.navigate(R.id.nav_drink_detail);
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Sản phẩm không tồn tại!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Integer integer) {
                        fab.setCount(integer);
                        badge = findViewById(R.id.badge);
                        if (badge == null) {
                            return;
                        } else if (integer == 1) {
                            badge.setText(String.valueOf(1));
                        } else {
                            badge.setText(String.valueOf(integer));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (!Objects.requireNonNull(e.getMessage()).contains("Query returned empty")) {
                            Toast.makeText(HomeActivity.this, "[COUNT CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            fab.setCount(0);
                            //badge.setText(String.valueOf(0));
                        }
                    }
                });

    }

}