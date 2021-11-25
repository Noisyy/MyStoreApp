package com.example.quantrasuaserver.Fragment.most_popular;

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
import com.example.quantrasuaserver.Adapter.MyMostPopularAdapter;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Common.CustomDialog;
import com.example.quantrasuaserver.Common.MySwiperHelper;
import com.example.quantrasuaserver.EventBus.ToastEvent;
import com.example.quantrasuaserver.Model.MostPopularModel;
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

public class MostPopularFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private MostPopularViewModel mViewModel;

    Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_most_popular)
    RecyclerView recycler_most_popular;

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
        mViewModel = new ViewModelProvider(this).get(MostPopularViewModel.class);
        View root = inflater.inflate(R.layout.fragment_most_popular, container, false);

        unbinder = ButterKnife.bind(this, root);
        initViews();

        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show());
        mViewModel.getMostPopularListMutable().observe(getViewLifecycleOwner(), list -> {
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

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_most_popular.setLayoutManager(layoutManager);
        //recycler_most_popular.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_most_popular, Common.BUTTON_SIZE) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), Common.OPTIONS_UPDATE, Common.TEXT_SIZE, 0, Color.parseColor(Common.COLOR_UPDATE), pos -> {
                    Common.mostPopularSelected = mostPopularModels.get(pos);
                    showUpdateDialog();
                }));
                buf.add(new MyButton(getContext(), Common.OPTIONS_DELETE, Common.TEXT_SIZE, 0, Color.parseColor(Common.COLOR_DELETE),
                        pos -> {
                            Common.mostPopularSelected = mostPopularModels.get(pos);
                            showDeleteDialog();
                        }));
            }
        };
        Log.d("TAG", "initViews: " + mySwiperHelper);
    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        /*------------------------------------Init UI-----------------------------------------*/
        TextInputEditText edt_category_name = itemView.findViewById(R.id.edt_category_name);
        img_most_popular = itemView.findViewById(R.id.image_category);
        TextView txt_view = itemView.findViewById(R.id.txt_view);
        AppCompatButton btn_accept = itemView.findViewById(R.id.btn_accept);
        AppCompatButton btn_cancel = itemView.findViewById(R.id.btn_cancel);
        /*------------------------------------------------------------------------------------*/
        txt_view.setText(Common.TV_UPDATE);
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
        //show dialog
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
        //button Event
        btn_cancel.setOnClickListener(view -> dialog.dismiss());
        btn_accept.setOnClickListener(view -> {
            CustomDialog.show(getContext());
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", Objects.requireNonNull(edt_category_name.getText()).toString());
            if (imageUri != null) {
                //In this, we will use Firebase Storage to upload image
                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);
                imageFolder.putFile(imageUri)
                        .addOnCompleteListener(task -> imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                            updateData.put("image", uri.toString());
                            updateMostPopular(updateData);
                            dialog.dismiss();
                            CustomDialog.dismiss();
                        }).addOnFailureListener(e -> {
                            dialog.dismiss();
                            CustomDialog.dismiss();
                            Toast.makeText(getContext(), "ERROR " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }));
            } else {
                updateMostPopular(updateData);
                dialog.dismiss();
                CustomDialog.dismiss();
            }
        });
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cảnh báo");
        builder.setMessage("Bạn có chắc chắn muốn xóa cái này không?");
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