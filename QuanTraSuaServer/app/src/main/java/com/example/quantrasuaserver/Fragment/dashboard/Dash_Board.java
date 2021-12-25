package com.example.quantrasuaserver.Fragment.dashboard;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.quantrasuaserver.Model.CartItem;
import com.example.quantrasuaserver.Model.OrderModel;
import com.example.quantrasuaserver.Model.ServerUserModel;
import com.example.quantrasuaserver.Model.UserModel;
import com.example.quantrasuaserver.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class Dash_Board extends Fragment {
    @BindView(R.id.text_order_dashboard)
    TextView text_order_dashboard;
    @BindView(R.id.text_sell_dashboard)
    TextView text_sell_dashboard;
    @BindView(R.id.text_user_dashboard)
    TextView text_user_dashboard;

    Unbinder unbinder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ReadData();
        View root = inflater.inflate(R.layout.fragment_dash__board, container, false);
        unbinder = ButterKnife.bind(this,root);
        return root;
    }
    private void ReadData(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef_Order = database.getReference("Orders");
        DatabaseReference myRef_User = database.getReference("Users");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        myRef_Order.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                List<OrderModel> list_order = new ArrayList<>();
                Integer Total=0;
                //if (dateFormat.format(model.getCreateDate()).equals(""))
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    OrderModel model = snapshot.getValue(OrderModel.class);
                    if(dateFormat.format(model.getCreateDate()).equals(dateFormat.format(date))){
                        list_order.add(model);
                    }
                }
                text_order_dashboard.setText(String.valueOf(list_order.size()));
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    OrderModel model = snapshot.getValue(OrderModel.class);
                    if(dateFormat.format(model.getCreateDate()).equals(dateFormat.format(date))) {
                        Total += model.getCartItemList().size();
                    }
                }
                text_sell_dashboard.setText(String.valueOf(Total));
            }


            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        myRef_User.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UserModel> list_user = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserModel model = snapshot.getValue(UserModel.class);
                    list_user.add(model);
                }
                text_user_dashboard.setText(String.valueOf(list_user.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}