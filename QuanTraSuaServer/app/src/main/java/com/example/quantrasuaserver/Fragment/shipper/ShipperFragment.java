package com.example.quantrasuaserver.Fragment.shipper;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.example.quantrasuaserver.Adapter.MyShipperAdapter;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.EventBus.UpdateShipperEvent;
import com.example.quantrasuaserver.Model.ShipperModel;
import com.example.quantrasuaserver.R;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class ShipperFragment extends Fragment {

    Unbinder unbinder;
    private AlertDialog dialog;
    private LayoutAnimationController layoutAnimationController;
    private MyShipperAdapter adapter;
    private List<ShipperModel> shipperModelList, saveShipperBeforeSearchList;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_shipper)
    RecyclerView recycler_shipper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_shipper, container, false);
        ShipperViewModel mViewModel = new ViewModelProvider(this).get(ShipperViewModel.class);
        unbinder = ButterKnife.bind(this, itemView);
        initViews();
        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getShipperMutableList().observe(getViewLifecycleOwner(), shippers -> {
            dialog.dismiss();
            shipperModelList = shippers;
            if (saveShipperBeforeSearchList == null)
                saveShipperBeforeSearchList = shippers;
            adapter = new MyShipperAdapter(getContext(), shipperModelList);
            recycler_shipper.setAdapter(adapter);
            recycler_shipper.setLayoutAnimation(layoutAnimationController);
        });
        return itemView;
    }

    private void initViews() {
        setHasOptionsMenu(true);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_shipper.setLayoutManager(layoutManager);
        //recycler_shipper.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateShipperEvent.class))
            EventBus.getDefault().removeStickyEvent(UpdateShipperEvent.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateShipperActive(UpdateShipperEvent event) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("active", event.isActive()); //get state of button, not of shipper
        FirebaseDatabase.getInstance()
                .getReference(Common.SHIPPER_REF)
                .child(event.getShipperModel().getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Cập nhật trạng thái hoạt động thành " + event.isActive(), Toast.LENGTH_SHORT).show());

    }

}