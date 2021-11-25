package com.example.quantrasuaserver.Fragment.best_deals;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quantrasuaserver.Adapter.MyBestDealsAdapter;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Common.CustomDialog;
import com.example.quantrasuaserver.Common.MySwiperHelper;
import com.example.quantrasuaserver.EventBus.ToastEvent;
import com.example.quantrasuaserver.Model.BestDealsModel;
import com.example.quantrasuaserver.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BestDealsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private BestDealsViewModel mViewModel;

    Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_best_deals)
    RecyclerView recycler_best_deals;

    LayoutAnimationController layoutAnimationController;
    MyBestDealsAdapter adapter;

    List<BestDealsModel> bestDealsModels;

    ImageView img_best_deals;
    private Uri imageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.best_deals_fragment, container, false);
        mViewModel =
                new ViewModelProvider(this).get(BestDealsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_best_deals, container, false);

        unbinder = ButterKnife.bind(this, root);
        initViews();

        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show());
        mViewModel.getBestDealsListMutable().observe(getViewLifecycleOwner(), list -> {
            bestDealsModels = list;
            adapter = new MyBestDealsAdapter(getContext(), bestDealsModels);
            recycler_best_deals.setAdapter(adapter);
            recycler_best_deals.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void initViews() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_best_deals.setLayoutManager(layoutManager);

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_best_deals, Common.BUTTON_SIZE) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), Common.OPTIONS_UPDATE, Common.TEXT_SIZE, 0, Color.parseColor(Common.COLOR_UPDATE), pos -> {
                    Common.bestDealsSelected = bestDealsModels.get(pos);
                    showUpdateDialog();
                }));
                buf.add(new MyButton(getContext(), Common.OPTIONS_DELETE, Common.TEXT_SIZE, 0, Color.parseColor(Common.COLOR_DELETE),
                        pos -> {
                            Common.bestDealsSelected = bestDealsModels.get(pos);
                            showDeleteDialog();
                        }));
            }
        };
        Log.d("TAG", "initViews: " + mySwiperHelper);
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cảnh báo");
        builder.setMessage("Bạn có chắc chắn muốn xóa cái này không?");
        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> deleteBestDeals());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteBestDeals() {
        FirebaseDatabase.getInstance()
                .getReference(Common.BEST_DEALS_REF)
                .child(Common.bestDealsSelected.getKey())
                .removeValue()
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    mViewModel.loadBestDeals();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.DELETE, true));
                });
    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        /*------------------------------------Init UI-----------------------------------------*/
        TextInputEditText edt_category_name = itemView.findViewById(R.id.edt_category_name);
        img_best_deals = itemView.findViewById(R.id.image_category);
        TextView txt_view = itemView.findViewById(R.id.txt_view);
        AppCompatButton btn_accept = itemView.findViewById(R.id.btn_accept);
        AppCompatButton btn_cancel = itemView.findViewById(R.id.btn_cancel);
        /*------------------------------------------------------------------------------------*/
        txt_view.setText(Common.TV_UPDATE);
        //set Data
        edt_category_name.setText(new StringBuffer().append(Common.bestDealsSelected.getName()));
        Glide.with(requireContext()).load(Common.bestDealsSelected.getImage()).into(img_best_deals);
        //set Event
        img_best_deals.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });
        //show Dialog
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
        //event Button
        btn_cancel.setOnClickListener(view -> dialog.dismiss());
        btn_accept.setOnClickListener(view -> {
            CustomDialog.show(getContext());
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", Objects.requireNonNull(edt_category_name.getText()).toString());
            if (imageUri != null) {
                //In this, use Firebase Storage to upload image
                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);
                imageFolder.putFile(imageUri)
                        .addOnCompleteListener(task -> {
                            dialog.dismiss();
                            imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                updateData.put("image", uri.toString());
                                updateBestDeals(updateData);
                                dialog.dismiss();
                                CustomDialog.dismiss();
                            }).addOnFailureListener(e -> {
                                dialog.dismiss();
                                CustomDialog.dismiss();
                                Toast.makeText(getContext(), "ERROR " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        });
            } else {
                dialog.dismiss();
                CustomDialog.dismiss();
                updateBestDeals(updateData);
            }
        });
    }

    private void updateBestDeals(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.BEST_DEALS_REF)
                .child(Common.bestDealsSelected.getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "[UPDATE CATEGORY]" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    mViewModel.loadBestDeals();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE, true));
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                img_best_deals.setImageURI(imageUri);
            }
        }
    }
}