package com.example.quantrasuaserver.Fragment.most_popular;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.quantrasuaserver.Adapter.MyMostPopularAdapter;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Common.MySwiperHelper;
import com.example.quantrasuaserver.EventBus.ToastEvent;
import com.example.quantrasuaserver.Model.MostPopularModel;
import com.example.quantrasuaserver.R;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class MostPopularFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private MostPopularViewModel mViewModel;

    Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_most_popular)
    RecyclerView recycler_most_popular;

    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyMostPopularAdapter adapter;

    List<MostPopularModel> mostPopularModels;

    ImageView img_most_popular;
    private Uri imageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.best_deals_fragment, container, false);
        mViewModel =
                new ViewModelProvider(this).get(MostPopularViewModel.class);
        View root = inflater.inflate(R.layout.fragment_most_popular, container, false);

        unbinder = ButterKnife.bind(this, root);
        initViews();

        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show());
        mViewModel.getMostPopularListMutable().observe(getViewLifecycleOwner(), list -> {
            dialog.dismiss();
            mostPopularModels = list;
            adapter = new MyMostPopularAdapter(getContext(), mostPopularModels);
            recycler_most_popular.setAdapter(adapter);
            recycler_most_popular.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void initViews() {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_most_popular.setLayoutManager(layoutManager);
        //recycler_most_popular.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_most_popular, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Cập nhật", 25, 0, Color.parseColor("#2196F3"), pos -> {
                    Common.mostPopularSelected = mostPopularModels.get(pos);
                    showUpdateDialog();
                }));
                buf.add(new MyButton(getContext(), "Xóa nè", 25, 0, Color.parseColor("#F44336"),
                        pos -> {
                            Common.mostPopularSelected = mostPopularModels.get(pos);
                            showDeleteDialog();
                        }));
            }
        };
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Cập nhật");
        builder.setMessage("Vui lòng nhập đầy đủ thông tin");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = itemView.findViewById(R.id.edt_category_name);
        img_most_popular = itemView.findViewById(R.id.image_category);

        //set Data
        edt_category_name.setText(new StringBuffer().append(Common.mostPopularSelected.getName()));
        Glide.with(requireContext()).load(Common.mostPopularSelected.getImage()).into(img_most_popular);

        //set Event
        img_most_popular.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", edt_category_name.getText().toString());

            if (imageUri != null) {
                //In this, we will use Firebase Storage to upload image
                dialog.setMessage("Uploading...");
                dialog.show();
                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);

                imageFolder.putFile(imageUri)
                        .addOnCompleteListener(task -> {
                            dialog.dismiss();
                            imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                updateData.put("image", uri.toString());
                                updateMostPopular(updateData);
                            })
                                    .addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "ERROR " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuffer("Uploading: ").append(progress).append("%"));
                });
            } else {
                updateMostPopular(updateData);
            }
        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateMostPopular(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.MOST_POPULAR_REF)
                .child(Common.mostPopularSelected.getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "[UPDATE CATEGORY]" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    mViewModel.loadMostPopular();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE, true));
                });
    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Xóa nè");
        builder.setMessage("Bạn có chắc chắn muốn xóa cái này hong?");
        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> deleteMostPopular());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteMostPopular() {
        FirebaseDatabase.getInstance()
                .getReference(Common.MOST_POPULAR_REF)
                .child(Common.mostPopularSelected.getKey())
                .removeValue()
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    mViewModel.loadMostPopular();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.DELETE, true));
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                img_most_popular.setImageURI(imageUri);
            }
        }
    }



}