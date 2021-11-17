package com.example.quantrasuaclient.Fragment.Drink_List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quantrasuaclient.Adapter.MyDrinkListAdapter;
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.EventBus.CartItemClick;
import com.example.quantrasuaclient.EventBus.CounterCartEvent;
import com.example.quantrasuaclient.EventBus.HideFABCart;
import com.example.quantrasuaclient.EventBus.MenuItemBack;
import com.example.quantrasuaclient.Model.DrinksModel;
import com.example.quantrasuaclient.R;
import com.example.quantrasuaclient.databinding.FragmentDrinkListBinding;
import com.nex3z.notificationbadge.NotificationBadge;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class DrinkListFragment extends Fragment {

    private FragmentDrinkListBinding binding;
    private List<DrinksModel> drinksModelList;
    NotificationBadge badge;
    Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_drink_list)
    RecyclerView recycler_drink_list;

    LayoutAnimationController layoutAnimationController;
    MyDrinkListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DrinkListViewModel drinkListViewModel = new ViewModelProvider(this).get(DrinkListViewModel.class);

        binding = FragmentDrinkListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        unbinder = ButterKnife.bind(this, root);
        initViews();

        drinkListViewModel.getMutableLiveDataDrinkList().observe(getViewLifecycleOwner(), drinksModels -> {
            if (drinksModels != null) {
                drinksModelList = drinksModels;
                adapter = new MyDrinkListAdapter(getContext(), drinksModelList);
                recycler_drink_list.setAdapter(adapter);
                recycler_drink_list.setLayoutAnimation(layoutAnimationController);
            }
        });
        return root;
    }

    private void initViews() {
        EventBus.getDefault().postSticky(new HideFABCart(true));
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(Common.categorySelected.getName());
        setHasOptionsMenu(true);
        recycler_drink_list.setHasFixedSize(true);
        recycler_drink_list.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().postSticky(new CounterCartEvent(true));
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new HideFABCart(true));
        EventBus.getDefault().postSticky(new CounterCartEvent(true));
    }

    @Override
    public void onStop() {
        //EventBus.getDefault().postSticky(new HideFABCart(false));
        EventBus.getDefault().removeAllStickyEvents();
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.layout_menu_cart_bar,menu);
        View view = menu.findItem(R.id.cart_menu).getActionView();
        view.setOnClickListener(view1 -> EventBus.getDefault().postSticky(new CartItemClick(true)));
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}