package com.example.quantrasuaclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Model.UserModel;
import com.example.quantrasuaclient.Remote.ICloudFunctions;
import com.example.quantrasuaclient.Remote.RetrofitICloudClient;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private ICloudFunctions cloudFunctions;
    KProgressHUD dialog;

    private DatabaseReference userRef;
    private List<AuthUI.IdpConfig> providers;

    CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null && firebaseAuth != null)
            firebaseAuth.removeAuthStateListener(listener);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        //Using service login with phone and login with email
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build()
                , new AuthUI.IdpConfig.EmailBuilder().build());
        //Select info user from firebase
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        //Cloud functions
        cloudFunctions = RetrofitICloudClient.getInstance().create(ICloudFunctions.class);
        //Set dialog
        dialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).setWindowColor(Color.TRANSPARENT);
        dialog.dismiss();
        //Set Permissions default run app
        listener = firebaseAuth -> {
            //Cái này để lấy vị trí local
            Dexter.withActivity(this)
                    .withPermissions(
                            Arrays.asList(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA)
                    )
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    //Already Login
                                    //Toast.makeText(MainActivity.this, "Already login", Toast.LENGTH_SHORT).show();
                                    checkUserFromFirebase(user);

                                } else {
                                    //Not login
                                    phoneLogin();
                                }
                            } else
                                Toast.makeText(MainActivity.this, "Bạn phải cho phép tất cả các quyền này!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                        }
                    }).check();
        };
    }

    // Using check user already exists in firebase
    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();
        userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            compositeDisposable.add(cloudFunctions.getToken()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(braintreeToken -> {
                                        Toast.makeText(MainActivity.this, "Xin chào quý khách!", Toast.LENGTH_SHORT).show();
                                        UserModel userModel = snapshot.getValue(UserModel.class);
                                        gotoHomeActivity(userModel, braintreeToken.getToken());
                                    }, throwable -> {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        } else {
                            showRegisterDialog(user);
                            dialog.dismiss();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        //Testing
        EditText edt_name = itemView.findViewById(R.id.edt_name);
        EditText edt_address = itemView.findViewById(R.id.edt_address);
        EditText edt_phone = itemView.findViewById(R.id.edt_phone);
        Button btn_register = itemView.findViewById(R.id.btn_register);
        Button btn_exit = itemView.findViewById(R.id.btn_exit);

        //Set phone
        if (user.getPhoneNumber() == null || TextUtils.isEmpty(user.getPhoneNumber())) {
            //edt_phone.setText(user.getEmail());
            edt_name.setText(user.getDisplayName());
        } else
            edt_phone.setText(user.getPhoneNumber());
        builder.setView(itemView);
        btn_register.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edt_name.getText().toString())) {
                edt_name.setError("Làm ơn điền tên...");
                return;
            } else if (TextUtils.isEmpty(edt_address.getText().toString())) {
                edt_address.setError("Làm ơn điền địa chỉ...");
                return;
            } else if (TextUtils.isEmpty(edt_phone.getText().toString())) {
                edt_phone.setError("Làm ơn điền số điện thoại...");
                return;
            }

            UserModel userModel = new UserModel();
            userModel.setUid(user.getUid());
            userModel.setName(edt_name.getText().toString());
            userModel.setAddress(edt_address.getText().toString());
            userModel.setPhone(edt_phone.getText().toString());

            userRef.child(user.getUid())
                    .setValue(userModel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            compositeDisposable.add(cloudFunctions.getToken()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(braintreeToken -> {
                                        Toast.makeText(MainActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                        gotoHomeActivity(userModel, braintreeToken.getToken());
                                        gotoHomeActivity(userModel, braintreeToken.getToken());
                                    }, throwable -> {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        }
                    });
        });
        btn_exit.setOnClickListener(v -> {
            phoneLogin();
            firebaseAuth.removeAuthStateListener(listener);
        });
        builder.setView(itemView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();

    }

    private void gotoHomeActivity(UserModel userModel, String token) {
        FirebaseMessaging.getInstance().getToken()
                .addOnFailureListener(e -> {
                    Toast.makeText(getBaseContext(), "[ERROR MESS]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Common.currentUser = userModel;
                    Common.currentToken = token;
                    //Start activity soon
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                }).addOnCompleteListener(task -> {
            Common.currentUser = userModel;
            Common.currentToken = token;
            Common.updateToken(MainActivity.this, task.getResult());
            //Start activity soon
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        });

    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setLogo(R.drawable.app_icon)
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers).build(),
                APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}