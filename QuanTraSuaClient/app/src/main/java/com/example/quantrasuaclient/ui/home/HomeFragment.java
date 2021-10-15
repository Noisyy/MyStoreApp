package com.example.quantrasuaclient.ui.home;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.quantrasuaclient.Adapter.MyBestDealsAdapter;
import com.example.quantrasuaclient.Adapter.MyPopularCategoriesAdapter;
import com.example.quantrasuaclient.Common.DepthPageTransformer;
import com.example.quantrasuaclient.Model.BestDealModel;
import com.example.quantrasuaclient.R;
import com.example.quantrasuaclient.databinding.FragmentHomeBinding;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    Unbinder unbinder;
    List<BestDealModel> listBestDeal;
    KProgressHUD dialog;
    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(viewpager2.getCurrentItem() == listBestDeal.size() -1){
                viewpager2.setCurrentItem(0);
            }else {
                viewpager2.setCurrentItem(viewpager2.getCurrentItem() + 1);
            }
        }
    };

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_popular)
    RecyclerView recycler_popular;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_paper2)
    ViewPager2 viewpager2;

    LayoutAnimationController layoutAnimationController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        unbinder = ButterKnife.bind(this, root);
        init();
        dialog.show();
        homeViewModel.getPopularList().observe(getViewLifecycleOwner(), popularCategoryModels -> {
            dialog.dismiss();
            MyPopularCategoriesAdapter adapter = new MyPopularCategoriesAdapter(getContext(),popularCategoryModels);
           recycler_popular.setAdapter(adapter);
           recycler_popular.setLayoutAnimation(layoutAnimationController);
        });
        homeViewModel.getBestDealList().observe(getViewLifecycleOwner(), bestDealModels -> {
            listBestDeal = bestDealModels;
            MyBestDealsAdapter adapter = new MyBestDealsAdapter(getContext(),bestDealModels);
            viewpager2.setAdapter(adapter);

            viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    mHandler.removeCallbacks(mRunnable);
                    mHandler.postDelayed(mRunnable,4000);
                }
            });
            viewpager2.setPageTransformer(new DepthPageTransformer());
        });
        return root;
    }

    private void init() {
        //Animation
        dialog = KProgressHUD.create(requireContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).setWindowColor(Color.TRANSPARENT);
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_form_left);
        recycler_popular.setHasFixedSize(true);
        recycler_popular.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable,4000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
    }
}