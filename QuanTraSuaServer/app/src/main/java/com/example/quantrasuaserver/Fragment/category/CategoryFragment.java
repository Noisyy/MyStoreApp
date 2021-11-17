package com.example.quantrasuaserver.Fragment.category;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Common.MySwiperHelper;
import com.example.quantrasuaserver.EventBus.ToastEvent;
import com.example.quantrasuaserver.Model.CategoryModel;
import com.example.quantrasuaserver.Adapter.MyCategoriesAdapter;
import com.example.quantrasuaserver.R;
import com.example.quantrasuaserver.databinding.FragmentCategoryBinding;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class CategoryFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private CategoryViewModel categoryViewModel;
    private FragmentCategoryBinding binding;

    Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;

    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;

    List<CategoryModel> categoryModels;

    ImageView img_category;
    private Uri imageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        categoryViewModel =
                new ViewModelProvider(this).get(CategoryViewModel.class);

        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        unbinder = ButterKnife.bind(this, root);
        initViews();


        categoryViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show());
        categoryViewModel.getCategoryListMutable().observe(getViewLifecycleOwner(), categoryModelsList -> {
            dialog.dismiss();
            categoryModels = categoryModelsList;
            adapter = new MyCategoriesAdapter(getContext(), categoryModels);
            recycler_menu.setAdapter(adapter);
            recycler_menu.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void initViews() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        //dialog.show(); Remove it to fix loading show when resume fragment
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_menu.setLayoutManager(layoutManager);
        //recycler_menu.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_menu, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Cập nhật", 25, 0, Color.parseColor("#2196F3"),
                        pos -> {
                            Common.categorySelected = categoryModels.get(pos);
                            showUpdateDialog();
                        }));

                buf.add(new MyButton(getContext(), "Xóa nè", 25, 0, Color.parseColor("#F44336"),
                        pos -> {
                            Common.categorySelected = categoryModels.get(pos);
                            showDeleteDialog();
                        }));
            }
        };
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create) {
            showAddDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Xóa nè");
        builder.setMessage("Bạn có chắc chắn muốn xóa cái này hong?");
        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> deleteCategory());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void deleteCategory() {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .removeValue()
                .addOnFailureListener(e -> Toast.makeText(getContext(), "[DELETE CATEGORY]" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.DELETE, false));
                });
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Cập nhật");
        builder.setMessage("Vui lòng nhập đầy đủ thông tin");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = itemView.findViewById(R.id.edt_category_name);
        img_category = itemView.findViewById(R.id.image_category);

        //set Data
        edt_category_name.setText(new StringBuffer().append(Common.categorySelected.getName()));
        Glide.with(requireContext()).load(Common.categorySelected.getImage()).into(img_category);

        //set Event
        img_category.setOnClickListener(view -> {
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
                                updateCategory(updateData);
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
                updateCategory(updateData);
            }
        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateCategory(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "[UPDATE CATEGORY]" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE, false));
                });
    }

    private void showAddDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm mới");
        builder.setMessage("Vui lòng nhập đầy đủ thông tin");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = itemView.findViewById(R.id.edt_category_name);
        img_category = itemView.findViewById(R.id.image_category);

        //set Data
        Glide.with(requireContext()).load(R.drawable.image).into(img_category);

        //set Event
        img_category.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {

            CategoryModel categoryModel = new CategoryModel();
            categoryModel.setName(edt_category_name.getText().toString());
            categoryModel.setDrinks(new ArrayList<>()); //create Empty list for drinks list

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
                                categoryModel.setImage(uri.toString());
                                addCategory(categoryModel);
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
                addCategory(categoryModel);
            }
        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addCategory(CategoryModel categoryModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .push()
                .setValue(categoryModel)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "[CREATE CATEGORY]" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.CREATE, false));
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                img_category.setImageURI(imageUri);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}