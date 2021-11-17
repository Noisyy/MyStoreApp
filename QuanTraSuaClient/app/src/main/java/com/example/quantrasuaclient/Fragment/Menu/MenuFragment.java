package com.example.quantrasuaclient.Fragment.Menu;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quantrasuaclient.Adapter.MyCategoriesAdapter;
import com.example.quantrasuaclient.Common.SpacesItemDecoration;
import com.example.quantrasuaclient.EventBus.HideFABCart;
import com.example.quantrasuaclient.EventBus.MenuItemBack;
import com.example.quantrasuaclient.Model.CategoryModel;
import com.example.quantrasuaclient.R;
import com.example.quantrasuaclient.databinding.FragmentMenuBinding;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MenuFragment extends Fragment {

    private MenuViewModel menuViewModel;
    private FragmentMenuBinding binding;
    Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;

    KProgressHUD dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        menuViewModel =
                new ViewModelProvider(this).get(MenuViewModel.class);

        binding = FragmentMenuBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        unbinder = ButterKnife.bind(this, root);
        initViews();


        menuViewModel.getMessageError().observe(getViewLifecycleOwner(), s ->
                Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());
        menuViewModel.getCategoryListMutable().observe(getViewLifecycleOwner(), categoryModels -> {
            dialog.dismiss();
            adapter = new MyCategoriesAdapter(getContext(), categoryModels);
            recycler_menu.setAdapter(adapter);
            recycler_menu.setLayoutAnimation(layoutAnimationController);
        });
        return root;
    }

    private void initViews() {
        setHasOptionsMenu(true);
        EventBus.getDefault().postSticky(new HideFABCart(true));

        //Show dialog
        dialog = KProgressHUD.create(requireContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).setWindowColor(Color.TRANSPARENT)
                .show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter != null) {
                    switch (adapter.getItemViewType(position)) {
                        case 0:
                            return 1;
                        case 1:
                            return 2;
                        default:
                            return -1;
                    }

                }
                return -1;
            }
        });
        recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.layout_search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));

        //Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //Clear text when click to clear button on search view
        ImageView closeButton = searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(view -> {
            EditText ed = searchView.findViewById(R.id.search_src_text);
            //Clear text
            ed.setText("");
            //Clear query
            searchView.setQuery("", false);
            //Collapse the action view
            searchView.onActionViewCollapsed();
            //Collapse the search widget
            menuItem.collapseActionView();
            //Restore result to original
            menuViewModel.loadCategories();
        });
    }

    private void startSearch(String search) {
        List<CategoryModel> resultList = new ArrayList<>();
        for (int i = 0; i < adapter.getListCategory().size(); i++) {
            CategoryModel categoryModel = adapter.getListCategory().get(i);
            if (categoryModel.getName().toLowerCase().contains(search.toLowerCase()))
                resultList.add(categoryModel);
        }
        menuViewModel.getCategoryListMutable().setValue(resultList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new HideFABCart(true));
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().postSticky(new HideFABCart(false));
        super.onStop();
    }
}