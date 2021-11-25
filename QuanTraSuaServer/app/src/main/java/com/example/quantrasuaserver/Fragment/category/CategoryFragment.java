package com.example.quantrasuaserver.Fragment.category;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Common.CustomDialog;
import com.example.quantrasuaserver.Common.MySwiperHelper;
import com.example.quantrasuaserver.EventBus.ToastEvent;
import com.example.quantrasuaserver.Model.CategoryModel;
import com.example.quantrasuaserver.Adapter.MyCategoriesAdapter;
import com.example.quantrasuaserver.R;
import com.example.quantrasuaserver.databinding.FragmentCategoryBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CategoryFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1234;
    //ViewModel + Binding data
    private CategoryViewModel categoryViewModel;
    private FragmentCategoryBinding binding;
    //UI Views
    Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;
    AppCompatButton btn_accept;
    AppCompatButton btn_cancel;
    TextView txt_view;
    ImageView img_category;
    TextInputEditText edt_category_name;
    //Animation
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;
    //List Category
    List<CategoryModel> categoryModels;
    //Uri image
    private Uri imageUri = null;
    //FireStorage
    FirebaseStorage storage;
    StorageReference storageReference;
    //onCreateView
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        categoryViewModel =
                new ViewModelProvider(this).get(CategoryViewModel.class);
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        unbinder = ButterKnife.bind(this, root);
        initViews();
        //Get data
        categoryViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show());
        categoryViewModel.getCategoryListMutable().observe(getViewLifecycleOwner(), categoryModelsList -> {
            categoryModels = categoryModelsList;
            adapter = new MyCategoriesAdapter(getContext(), categoryModels);
            recycler_menu.setAdapter(adapter);
            recycler_menu.setLayoutAnimation(layoutAnimationController);
        });
        return root;
    }
    //Init
    private void initViews() {
        //Init FireStorage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);
        //Set LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_menu.setLayoutManager(layoutManager);
        //Swiper button
        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_menu, Common.BUTTON_SIZE) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), Common.OPTIONS_UPDATE, Common.TEXT_SIZE, 0, Color.parseColor(Common.COLOR_UPDATE),
                        pos -> {
                            Common.categorySelected = categoryModels.get(pos);
                            showUpdateDialog();
                        }));

                buf.add(new MyButton(getContext(), Common.OPTIONS_DELETE, Common.TEXT_SIZE, 0, Color.parseColor(Common.COLOR_DELETE),
                        pos -> {
                            Common.categorySelected = categoryModels.get(pos);
                            showDeleteDialog();
                        }));
            }
        };
        Log.d("TAG", "initViews: " + mySwiperHelper);
        //Set options menu
        setHasOptionsMenu(true);
    }
    //Add Menu Options
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_menu, menu);
    }
    //Select Add Menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create) {
            showAddDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    //Show Add Dialog
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        /*------------------------------------Init UI-----------------------------------------*/
        edt_category_name = itemView.findViewById(R.id.edt_category_name);
        img_category = itemView.findViewById(R.id.image_category);
        txt_view = itemView.findViewById(R.id.txt_view);
        btn_accept = itemView.findViewById(R.id.btn_accept);
        btn_cancel = itemView.findViewById(R.id.btn_cancel);
        /*------------------------------------------------------------------------------------*/
        txt_view.setText(Common.TV_UPDATE);
        //set Data
        Glide.with(requireContext()).load(R.drawable.image).into(img_category);
        //set Event
        img_category.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });
        //Set view
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
        // Event Listener
        btn_cancel.setOnClickListener(view -> dialog.dismiss());
        btn_accept.setOnClickListener(view -> {
            CustomDialog.show(getContext());
            CategoryModel categoryModel = new CategoryModel();
            categoryModel.setName(Objects.requireNonNull(edt_category_name.getText()).toString());
            categoryModel.setDrinks(new ArrayList<>()); //create Empty list for drinks list
            if (imageUri != null) {
                //In this, we will use Firebase Storage to upload image
                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);
                //Image
                imageFolder.putFile(imageUri)
                        .addOnCompleteListener(task -> {
                            dialog.dismiss();
                            imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                categoryModel.setImage(uri.toString());
                                addCategory(categoryModel);
                                dialog.dismiss();
                                CustomDialog.dismiss();
                            }).addOnFailureListener(e -> {
                                dialog.dismiss();
                                CustomDialog.dismiss();
                                Toast.makeText(getContext(), "ERROR " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        });
            } else {
                addCategory(categoryModel);
                dialog.dismiss();
                CustomDialog.dismiss();
            }
        });
    }
    //Add Category (Save data in Firebase)
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
    //Show Dialog Delete Category
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cảnh báo");
        builder.setMessage("Bạn có chắc chắn muốn xóa cái này không?");
        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> deleteCategory());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //Delete Category (Save data in Firebase)
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
    //Show Update Dialog Category
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        /*------------------------------------Init UI-----------------------------------------*/
        edt_category_name = itemView.findViewById(R.id.edt_category_name);
        img_category = itemView.findViewById(R.id.image_category);
        txt_view = itemView.findViewById(R.id.txt_view);
        btn_accept = itemView.findViewById(R.id.btn_accept);
        btn_cancel = itemView.findViewById(R.id.btn_cancel);
        /*------------------------------------------------------------------------------------*/
        txt_view.setText(Common.TV_UPDATE);
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
        //set Item
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
        //event Listener
        btn_cancel.setOnClickListener(view -> dialog.dismiss());
        btn_accept.setOnClickListener(view -> {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", Objects.requireNonNull(edt_category_name.getText()).toString());
            if (imageUri != null) {
                //In this, we will use Firebase Storage to upload image
                CustomDialog.show(getContext());
                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);

                imageFolder.putFile(imageUri)
                        .addOnCompleteListener(task -> imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                            updateData.put("image", uri.toString());
                            updateCategory(updateData);
                            dialog.dismiss();
                            CustomDialog.dismiss();
                        }).addOnFailureListener(e -> {
                            dialog.dismiss();
                            CustomDialog.dismiss();
                            Toast.makeText(getContext(), "ERROR " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }));
            } else {
                updateCategory(updateData);
                dialog.dismiss();
                CustomDialog.dismiss();
            }
        });
    }
    //Update Category (Save data in Firebase)
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
    //Get Image
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