package com.example.quantrasuaclient.ui.favorite;



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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.example.quantrasuaclient.Adapter.MyFavoriteAdapter;
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Common.MySwiperHelper;
import com.example.quantrasuaclient.Database.ModelDB.Favorite;
import com.example.quantrasuaclient.EventBus.CartItemClick;
import com.example.quantrasuaclient.EventBus.CounterCartEvent;
import com.example.quantrasuaclient.EventBus.HideFABCart;
import com.example.quantrasuaclient.EventBus.MenuItemBack;
import com.example.quantrasuaclient.R;
import com.example.quantrasuaclient.databinding.FavoriteFragmentBinding;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nex3z.notificationbadge.NotificationBadge;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FavoriteFragment extends Fragment {

    FavoriteFragmentBinding binding;
    Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_fav)
    RecyclerView recycler_fav;
    MyFavoriteAdapter adapter;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    LayoutAnimationController layoutAnimationController;
    KProgressHUD dialog;
    //Notification cart
    NotificationBadge badge;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FavoriteFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        unbinder = ButterKnife.bind(this, root);
        setHasOptionsMenu(true);
        //Show dialog
        dialog = KProgressHUD.create(requireContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).setWindowColor(Color.TRANSPARENT)
                .show();
        scheduleDismiss();
        //Hide cart
        EventBus.getDefault().postSticky(new HideFABCart(true));
        //Setup recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_fav.setLayoutManager(layoutManager);
        recycler_fav.setHasFixedSize(true);
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);
        recycler_fav.setLayoutAnimation(layoutAnimationController);
        //Swiper unfollow
        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_fav, 250){
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Bỏ thích", 40, 0, Color.parseColor("#5d4037"),
                        pos -> {
                            Favorite favorite = adapter.getItemAtPosition(pos);
                            Common.favoriteRepository.delete(favorite);
                            Toast.makeText(getContext(), "Hủy bỏ yêu thích thành công !!", Toast.LENGTH_SHORT).show();
                        }));
            }
        };
        return root;
    }

    private void scheduleDismiss() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                loadFavoriteItem();
            }
        }, 800);
    }

    private void loadFavoriteItem() {
        compositeDisposable.add(Common.favoriteRepository.getFavItem()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Favorite>>() {
                    @Override
                    public void accept(List<Favorite> favoriteList) throws Exception {
                        //dialog.dismiss();
                        displayFavoriteItem(favoriteList);
                    }
                }));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cart_bar,menu);
        View view = menu.findItem(R.id.cart_menu).getActionView();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().postSticky(new CartItemClick(true));
                //EventBus.getDefault().postSticky(new MenuItemBack());
            }
        });
        badge = view.findViewById(R.id.badge);
        EventBus.getDefault().postSticky(new CounterCartEvent(true));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.cart_menu){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayFavoriteItem(List<Favorite> favorites) {
        adapter = new MyFavoriteAdapter(getContext(), favorites);
        recycler_fav.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().postSticky(new HideFABCart(false));
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new HideFABCart(true));
        EventBus.getDefault().postSticky(new CounterCartEvent(true));
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}