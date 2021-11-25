package com.example.quantrasuashipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.quantrasuashipper.Common.Common;
import com.example.quantrasuashipper.Model.ShipperUserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference serverRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        providers = Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build());

        serverRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        listener = firebaseAuthLocal -> {
            FirebaseUser user = firebaseAuthLocal.getCurrentUser();
            if (user != null) {
                //Check user from firebase
                checkServerUserFromFirebase(user);
            } else {
                phoneLogin();
            }
        };
    }

    private void checkServerUserFromFirebase(FirebaseUser user) {
        dialog.show();
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ShipperUserModel userModel = snapshot.getValue(ShipperUserModel.class);
                            assert userModel != null;
                            if(userModel.isActive()){
                                goToHomeActivity(userModel);
                            }else {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Admin chưa cấp quyền truy cập cho bạn", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //User not exists in database
                            dialog.dismiss();
                            showRegisterDialog(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "[USER LOGIN]" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Đăng ký");
        builder.setMessage("Vui lòng nhập đầy đủ thông tin \nQuản trị viên sẽ chấp nhận tài khoản bạn sau");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        //Set data
        edt_phone.setText(user.getPhoneNumber());
        builder.setNegativeButton("Hủy bỏ", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("Đăng ký", (dialogInterface, i) -> {
                    if (TextUtils.isEmpty(edt_name.getText().toString())) {
                        Toast.makeText(MainActivity.this, "Làm ơn nhập tên", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ShipperUserModel shipperUserModel = new ShipperUserModel();
                    shipperUserModel.setUid(user.getUid());
                    shipperUserModel.setName(edt_name.getText().toString());
                    shipperUserModel.setPhone(edt_phone.getText().toString());
                    shipperUserModel.setActive(false); //Default failed, we must active user by manual in Firebase

                    dialog.show();
                    serverRef.child(shipperUserModel.getUid())
                            .setValue(shipperUserModel)
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }).addOnCompleteListener(task -> {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Đăng ký thành công! Admin sẽ cấp quyền đăng nhập cho bạn sớm nhất", Toast.LENGTH_SHORT).show();
                            });
                });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog registerDialog = builder.create();
        registerDialog.show();
    }

    private void goToHomeActivity(ShipperUserModel shipperUserModel) {
        dialog.dismiss();
        Common.currentShipperUser = shipperUserModel;
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(providers).build(), APP_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }
}