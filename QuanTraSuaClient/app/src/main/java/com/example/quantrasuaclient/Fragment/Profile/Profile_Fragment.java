package com.example.quantrasuaclient.Fragment.Profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.quantrasuaclient.Activity.HomeActivity;
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.EventBus.HideFABCart;
import com.example.quantrasuaclient.EventBus.MenuItemBack;
import com.example.quantrasuaclient.EventBus.ProfileUser;
import com.example.quantrasuaclient.Model.UserModel;
import com.example.quantrasuaclient.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

public class Profile_Fragment extends Fragment {

    View mView;
    private ImageView imgAvatar;
    private TextInputEditText edt_name,edt_address,edt_phone;
    private Button btnUpdate;
    private static final int PICK_IMAGE_REQUEST = 1234;
    private Uri imageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;

    AlertDialog dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_profile,container,false);
        //Hide cart end chat
        EventBus.getDefault().postSticky(new HideFABCart(true));
        initUI();
        initListener();
        setUserInformation();
        return mView;

    }

    private void initListener() {
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strName = edt_name.getText().toString();
                String strAddress = edt_address.getText().toString();
                String strPhone = edt_phone.getText().toString();
                if(strName.isEmpty()){
                    edt_name.setError("Vui lòng nhập tên!");
                    return;
                }
                if(strAddress.isEmpty()){
                    edt_address.setError("Vui lòng nhập địa chỉ!");
                    return;
                }
                if(strPhone.isEmpty()){
                    edt_phone.setError("Vui lòng nhập số ĐT!");
                    return;
                }
                Map<String,Object> update_profile =new HashMap<>();
                update_profile.put("name",strName);
                update_profile.put("phone",strPhone);
                update_profile.put("address",strAddress);

                UserModel user_data = new UserModel();
                user_data.setUid(Common.currentUser.getUid());
                user_data.setName(strName);
                user_data.setAddress(strAddress);
                user_data.setPhone(strPhone);

                if (imageUri != null) {
                    //In this, use Firebase Storage to upload image
                    dialog.setMessage("Uploading...");
                    dialog.show();
                    String unique_name = UUID.randomUUID().toString();
                    StorageReference imageFolder = storageReference.child("images/" + unique_name);

                    imageFolder.putFile(imageUri)
                            .addOnCompleteListener(task -> {
                                dialog.dismiss();
                                imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                    update_profile.put("image", uri.toString());
                                    user_data.setImage(uri.toString());
                                    EventBus.getDefault().postSticky(new ProfileUser(user_data,true));
                                    updateProfile(update_profile);
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
                    user_data.setImage(null);
                    updateProfile(update_profile);
                    EventBus.getDefault().postSticky(new ProfileUser(user_data,true));
                }
                Common.currentUser = user_data;
            }
        });
    }


    private void updateProfile(Map<String, Object> update_profile) {
        FirebaseDatabase.getInstance()
                .getReference(Common.USER_REF)
                .child(Common.currentUser.getUid())
                .updateChildren(update_profile)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "[UPDATE CATEGORY]" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                });

    }

    private void initUI(){
        imgAvatar = mView.findViewById(R.id.img_avatar);
        edt_name = mView.findViewById(R.id.edt_name);
        edt_address = mView.findViewById(R.id.edt_address);
        edt_phone = mView.findViewById(R.id.edt_phone);
        btnUpdate = mView.findViewById(R.id.btn_update);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

    }

    private void setUserInformation(){
        if(Common.currentUser != null){
            edt_name.setText(Common.currentUser.getName());
            edt_address.setText(Common.currentUser.getAddress());
            edt_phone.setText(Common.currentUser.getPhone());
            Glide.with(this).load(Common.currentUser.getImage()).error(R.drawable.app_icon).into(imgAvatar);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                imgAvatar.setImageURI(imageUri);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        EventBus.getDefault().postSticky(new HideFABCart(true));
        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().postSticky(new HideFABCart(true));
        super.onPause();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }


    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));
        EventBus.getDefault().removeAllStickyEvents();
        super.onStop();
    }
}
