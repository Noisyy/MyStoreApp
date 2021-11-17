package com.example.quantrasuaserver.Fragment.drinks_list;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quantrasuaserver.Common.Common;
import com.example.quantrasuaserver.Common.MySwiperHelper;
import com.example.quantrasuaserver.EventBus.AddonSizeEditEvent;
import com.example.quantrasuaserver.EventBus.ChangeMenuClick;
import com.example.quantrasuaserver.EventBus.ToastEvent;
import com.example.quantrasuaserver.Model.DrinksModel;
import com.example.quantrasuaserver.Adapter.MyDrinkListAdapter;
import com.example.quantrasuaserver.R;
import com.example.quantrasuaserver.Activity.SizeAddonEditActivity;
import com.example.quantrasuaserver.databinding.FragmentDrinkListBinding;
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
import dmax.dialog.SpotsDialog;

public class DrinksListFragment extends Fragment {

    //Image upload
    private static final int PICK_IMAGE_REQUEST = 1234;
    private ImageView img_drinks;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    private DrinksListViewModel drinksListViewModel;
    private List<DrinksModel> drinksModelList;
    private FragmentDrinkListBinding binding;
    Unbinder unbinder;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_drink_list)
    RecyclerView recycler_drink_list;

    LayoutAnimationController layoutAnimationController;
    MyDrinkListAdapter adapter;
    private Uri imageUri = null;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.drinks_list_menu, menu);

        //Search
        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));

        //Events
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearchDrinks(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        //Clear text when click to clear button Search view
        ImageView closeButton = searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(view -> {
            EditText edt = searchView.findViewById(R.id.search_src_text);
            edt.setText("");
            searchView.setQuery("", false);
            //Collapse the action view
            searchView.onActionViewCollapsed();
            //Collapse the search widget
            menuItem.collapseActionView();
            //Restore result to original
            drinksListViewModel.getMutableLiveDataDrinkList().setValue(Common.categorySelected.getDrinks());
        });
    }

    private void startSearchDrinks(String s) {
        List<DrinksModel> resultDrinks = new ArrayList<>();
        for (int i = 0; i < Common.categorySelected.getDrinks().size(); i++) {
            DrinksModel drinksModel = Common.categorySelected.getDrinks().get(i);
            if (drinksModel.getName().toLowerCase().contains(s.toLowerCase())) {
                drinksModel.setPositionInList(i); //Save index
                resultDrinks.add(drinksModel);
            }
        }
        drinksListViewModel.getMutableLiveDataDrinkList().setValue(resultDrinks); //Set search result
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        drinksListViewModel =
                new ViewModelProvider(this).get(DrinksListViewModel.class);

        binding = FragmentDrinkListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        unbinder = ButterKnife.bind(this, root);
        initViews();

        drinksListViewModel.getMutableLiveDataDrinkList().observe(getViewLifecycleOwner(), drinksModels -> {
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
        setHasOptionsMenu(true); //Enable menu in fragment

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(Common.categorySelected.getName());
        recycler_drink_list.setHasFixedSize(true);
        recycler_drink_list.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_form_left);

        //Get Size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;


        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_drink_list, width / 6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Xóa nè", 25, 0, Color.parseColor("#F44336"), pos -> {
                    if (drinksModelList != null) {
                        Common.selectDrinks = drinksModelList.get(pos);
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setTitle("Xóa sản phẩm")
                                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này hong?")
                                .setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) ->
                                        dialogInterface.dismiss()).setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {
                            DrinksModel drinksModel = adapter.getItemAtPosition(pos); //Get item in adapter
                            if (drinksModel.getPositionInList() == -1) //If == -1 default, do nothing
                                Common.categorySelected.getDrinks().remove(pos);
                            else
                                Common.categorySelected.getDrinks().remove(drinksModel.getPositionInList()); //Remove by index we was save
                            updateDrinks(Common.categorySelected.getDrinks(), Common.ACTION.DELETE);
                        });
                        AlertDialog deleteDialog = builder.create();
                        deleteDialog.show();
                    }
                }));
                buf.add(new MyButton(getContext(), "Cập nhật", 25, 0, Color.parseColor("#2196F3"), pos -> {
                    //Similar
                    DrinksModel drinksModel = adapter.getItemAtPosition(pos);
                    if (drinksModel.getPositionInList() == -1)
                        showUpdateDialog(pos, drinksModel);
                    else
                        showUpdateDialog(drinksModel.getPositionInList(), drinksModel);
                }));
                buf.add(new MyButton(getContext(), "Size", 25, 0, Color.parseColor("#B39DDB"), pos -> {
                    DrinksModel drinksModel = adapter.getItemAtPosition(pos);
                    if (drinksModel.getPositionInList() == -1)
                        Common.selectDrinks = drinksModelList.get(pos);
                    else
                        Common.selectDrinks = drinksModel;
                    startActivity(new Intent(getContext(), SizeAddonEditActivity.class));
                    //Change pos
                    if (drinksModel.getPositionInList() == -1)
                        EventBus.getDefault().postSticky(new AddonSizeEditEvent(false, pos));
                    else
                        EventBus.getDefault().postSticky(new AddonSizeEditEvent(false, drinksModel.getPositionInList()));
                }));
                buf.add(new MyButton(getContext(), "Topping", 25, 0, Color.parseColor("#4CAF50"), pos -> {
                    DrinksModel drinksModel = adapter.getItemAtPosition(pos);
                    if (drinksModel.getPositionInList() == -1)
                        Common.selectDrinks = drinksModelList.get(pos);
                    else
                        Common.selectDrinks = drinksModel;
                    startActivity(new Intent(getContext(), SizeAddonEditActivity.class));
                    //Change pos
                    if (drinksModel.getPositionInList() == -1)
                        EventBus.getDefault().postSticky(new AddonSizeEditEvent(true, pos));
                    else
                        EventBus.getDefault().postSticky(new AddonSizeEditEvent(true, drinksModel.getPositionInList()));
                }));
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create)
            showAddDialog();
        return super.onOptionsItemSelected(item);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm mới");
        builder.setMessage("Vui lòng nhập đầy đủ thông tin");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_drinks, null);
        TextInputEditText edt_drink_name = itemView.findViewById(R.id.edt_drinks_name);
        TextInputEditText edt_drink_price = itemView.findViewById(R.id.edt_drinks_price);
        TextInputEditText edt_drink_description = itemView.findViewById(R.id.edt_drinks_description);
        img_drinks = itemView.findViewById(R.id.img_drink);

        //Set data
        Glide.with(requireContext()).load(R.drawable.image).into(img_drinks);

        //Set Event
        img_drinks.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) ->
                dialogInterface.dismiss()).setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {
            DrinksModel updateDrinks = new DrinksModel();
            updateDrinks.setName(Objects.requireNonNull(edt_drink_name.getText()).toString());
            updateDrinks.setId(UUID.randomUUID().toString());
            updateDrinks.setDescription(Objects.requireNonNull(edt_drink_description.getText()).toString());
            updateDrinks.setPrice(TextUtils.isEmpty(edt_drink_price.getText()) ? 0 : Long.parseLong(Objects.requireNonNull(edt_drink_price.getText()).toString()));
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
                                updateDrinks.setImage(uri.toString());
                                if (Common.categorySelected.getDrinks() == null)
                                    Common.categorySelected.setDrinks(new ArrayList<>());
                                Common.categorySelected.getDrinks().add(updateDrinks);
                                updateDrinks(Common.categorySelected.getDrinks(), Common.ACTION.CREATE);
                            }).addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "ERROR " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuffer("Uploading: ").append(progress).append("%"));
                });

            } else {
                if (Common.categorySelected.getDrinks() == null)
                    Common.categorySelected.setDrinks(new ArrayList<>());
                Common.categorySelected.getDrinks().add(updateDrinks);
                updateDrinks(Common.categorySelected.getDrinks(), Common.ACTION.CREATE);
            }
        });
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showUpdateDialog(int pos, DrinksModel drinksModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cập nhật");
        builder.setMessage("Vui lòng nhập đầy đủ thông tin");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_drinks, null);
        TextInputEditText edt_drink_name = itemView.findViewById(R.id.edt_drinks_name);
        TextInputEditText edt_drink_price = itemView.findViewById(R.id.edt_drinks_price);
        TextInputEditText edt_drink_description = itemView.findViewById(R.id.edt_drinks_description);
        img_drinks = itemView.findViewById(R.id.img_drink);

        //Set data
        edt_drink_name.setText(new StringBuffer().append(drinksModel.getName()));
        edt_drink_price.setText(new StringBuffer().append(drinksModel.getPrice()));
        edt_drink_description.setText(new StringBuffer().append(drinksModel.getDescription()));
        Glide.with(requireContext()).load(drinksModel.getImage()).into(img_drinks);

        //Set Event
        img_drinks.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton(Common.OPTIONS_CANCEL, (dialogInterface, i) ->
                dialogInterface.dismiss()).setPositiveButton(Common.OPTIONS_OK, (dialogInterface, i) -> {

            drinksModel.setName(Objects.requireNonNull(edt_drink_name.getText()).toString());
            drinksModel.setDescription(Objects.requireNonNull(edt_drink_description.getText()).toString());
            drinksModel.setPrice(TextUtils.isEmpty(edt_drink_price.getText()) ? 0 : Long.parseLong(Objects.requireNonNull(edt_drink_price.getText()).toString()));
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
                                drinksModel.setImage(uri.toString());
                                Common.categorySelected.getDrinks().set(pos, drinksModel);
                                updateDrinks(Common.categorySelected.getDrinks(), Common.ACTION.UPDATE);
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
                Common.categorySelected.getDrinks().set(pos, drinksModel);
                updateDrinks(Common.categorySelected.getDrinks(), Common.ACTION.UPDATE);
            }
        });
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateDrinks(List<DrinksModel> drinks, Common.ACTION action) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("drinks", drinks);

        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        drinksListViewModel.getMutableLiveDataDrinkList();
                        EventBus.getDefault().postSticky(new ToastEvent(action, true));
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                img_drinks.setImageURI(imageUri);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //Fix bug change menu failed
    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

}