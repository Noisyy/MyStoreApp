package com.example.quantrasuaserver.ui.discount;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quantrasuaserver.Adapter.MyDiscountAdapter;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Common.MySwiperHelper;
import com.example.quantrasuaserver.EventBus.ToastEvent;
import com.example.quantrasuaserver.Model.DiscountModel;
import com.example.quantrasuaserver.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class DiscountFragment extends Fragment {

    private DiscountViewModel mViewModel;

    Unbinder unbinder;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_discount)
    RecyclerView recycler_discount;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyDiscountAdapter adapter;
    List<DiscountModel> discountModelList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(DiscountViewModel.class);
        View root = inflater.inflate(R.layout.discount_fragment, container, false);

        unbinder = ButterKnife.bind(this, root);
        initViews();
        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getDiscountMutableLiveData().observe(getViewLifecycleOwner(), list -> {
            dialog.dismiss();
            if (list == null)
                discountModelList = new ArrayList<>();
            else
                discountModelList = list;
            adapter = new MyDiscountAdapter(getContext(), discountModelList);
            recycler_discount.setAdapter(adapter);
            recycler_discount.setLayoutAnimation(layoutAnimationController);
        });
        return root;
    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        setHasOptionsMenu(true);

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_discount.setLayoutManager(layoutManager);
        //recycler_discount.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));

        MySwiperHelper swiperHelper = new MySwiperHelper(getContext(), recycler_discount, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Xóa nè", 25, 0, Color.parseColor("#F44336"),
                        pos -> {
                            Common.discountSelected = discountModelList.get(pos);
                            showDeleteDialog();
                        }));
                buf.add(new MyButton(getContext(), "Cập nhật", 25, 0, Color.parseColor("#2196F3"),
                        pos -> {
                            Common.discountSelected = discountModelList.get(pos);
                            showUpdateDialog();
                        }));
            }
        };

    }

    private void showAddDialog() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar selectedDate = Calendar.getInstance();
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("THÊM MỚI");
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_discount, null);
        TextInputEditText edt_code = itemView.findViewById(R.id.edt_code);
        TextInputEditText edt_percent = itemView.findViewById(R.id.edt_percent);
        TextInputEditText edt_valid = itemView.findViewById(R.id.edt_valid);
        ImageView img_calendar = itemView.findViewById(R.id.pickDate);

        //Event
        DatePickerDialog.OnDateSetListener listener = (view, year, month, day) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, day);
            edt_valid.setText(simpleDateFormat.format(selectedDate.getTime()));
        };

        img_calendar.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(), listener, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show(); //Don't fogot it
        });

        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) ->
                dialogInterface.dismiss())
                .setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {
                    DiscountModel discountModel = new DiscountModel();
                    discountModel.setKey(Objects.requireNonNull(edt_code.getText()).toString().toLowerCase());
                    discountModel.setPercent(Integer.parseInt(Objects.requireNonNull(edt_percent.getText()).toString()));
                    discountModel.setUntilDate(selectedDate.getTimeInMillis());

                    createDiscount(discountModel);
                });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();


    }

    @SuppressLint("NotifyDataSetChanged")
    private void createDiscount(DiscountModel discountModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.DISCOUNT_REF)
                .child(discountModel.getKey())
                .setValue(discountModel)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mViewModel.loadDiscount();
                adapter.notifyDataSetChanged();
                EventBus.getDefault().post(new ToastEvent(Common.ACTION.CREATE, true));
            }
        });
    }

    private void showUpdateDialog() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar selectedDate = Calendar.getInstance();
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("CẬP NHẬT");
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_discount, null);
        TextInputEditText edt_code = itemView.findViewById(R.id.edt_code);
        TextInputEditText edt_percent = itemView.findViewById(R.id.edt_percent);
        TextInputEditText edt_valid = itemView.findViewById(R.id.edt_valid);
        ImageView img_calendar = itemView.findViewById(R.id.pickDate);

        //set Data
        edt_code.setText(Common.discountSelected.getKey());
        edt_code.setEnabled(false); //lock key

        edt_percent.setText(new StringBuilder().append(Common.discountSelected.getPercent()));
        edt_valid.setText(simpleDateFormat.format(Common.discountSelected.getUntilDate()));

        //Event
        DatePickerDialog.OnDateSetListener listener = (view, year, month, day) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, day);
            edt_valid.setText(simpleDateFormat.format(selectedDate.getTime()));
        };

        img_calendar.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(), listener, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show(); //Don't fogot it
        });

        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) ->
                dialogInterface.dismiss()).setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("percent", Integer.parseInt(Objects.requireNonNull(edt_percent.getText()).toString()));
            updateData.put("untilDate", selectedDate.getTimeInMillis());

            updateDiscount(updateData);
        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void updateDiscount(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.DISCOUNT_REF)
                .child(Common.discountSelected.getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mViewModel.loadDiscount();
                adapter.notifyDataSetChanged();
                EventBus.getDefault().post(new ToastEvent(Common.ACTION.UPDATE, true));
            }
        });
    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Cảnh báo");
        builder.setMessage("Bạn có chắc chắn muốn xóa sản phẩm này không?");
        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) ->
                dialogInterface.dismiss())
                .setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) ->
                        deleteDiscount());
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteDiscount() {
        FirebaseDatabase.getInstance()
                .getReference(Common.DISCOUNT_REF)
                .child(Common.discountSelected.getKey())
                .removeValue()
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mViewModel.loadDiscount();
                adapter.notifyDataSetChanged();
                EventBus.getDefault().post(new ToastEvent(Common.ACTION.DELETE, true));
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.discount_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create)
            showAddDialog();
        return super.onOptionsItemSelected(item);
    }

}