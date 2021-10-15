package com.example.quantrasuaclient.ui.drinksdetail;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.quantrasuaclient.Common.Common;
import com.example.quantrasuaclient.Database.DataSource.ICartDataSource;
import com.example.quantrasuaclient.Database.Local.RoomDatabase;
import com.example.quantrasuaclient.Database.ModelDB.CartItem;
import com.example.quantrasuaclient.Database.Local.CartDataSource;
import com.example.quantrasuaclient.EventBus.CartItemClick;
import com.example.quantrasuaclient.EventBus.CounterCartEvent;
import com.example.quantrasuaclient.EventBus.HideFABCart;
import com.example.quantrasuaclient.EventBus.MenuItemBack;
import com.example.quantrasuaclient.Model.AddonModel;
import com.example.quantrasuaclient.Model.CommentModel;
import com.example.quantrasuaclient.Model.DrinksModel;
import com.example.quantrasuaclient.Model.SizeModel;
import com.example.quantrasuaclient.R;
import com.example.quantrasuaclient.ui.comments.CommentFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nex3z.notificationbadge.NotificationBadge;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DrinkDetailFragment extends Fragment implements TextWatcher {

    //Khai báo mấy cái này để ở dưới sài
    private DrinkDetailViewModel drinkDetailViewModel;
    private ICartDataSource cartDataSource;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    Unbinder unbinder;
    //private android.app.AlertDialog waitingDialog;
    private BottomSheetDialog addonBottomSheetDialog;

    //View need inflate
    ChipGroup chip_group_addon;
    EditText edt_search;
    //Notification cart
    NotificationBadge badge;
    //Dialog
    KProgressHUD dialog;

    //Mấy cái này là khai báo để biding dữ liệu tương tự như c# thôi
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.img_drinks)
    ImageView img_drinks;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btnCart)
    CounterFab btnCart;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_rating)
    FloatingActionButton btn_rating;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.drinks_name)
    TextView drinks_name;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.drinks_description)
    TextView drinks_description;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.drinks_price)
    TextView drinks_price;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.number_button)
    ElegantNumberButton numberButton;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btnShowComment)
    Button btnShowComment;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rdi_group_size)
    RadioGroup rdi_group_size;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.img_add_addon)
    ImageView img_add_on;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.chip_group_user_selected_addon)
    ChipGroup chip_group_user_selected_addon;

    @OnClick({R.id.btn_rating})
    void onRatingButtonClick() {
        showDialogRating();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btnShowComment)
    void onShowCommentButtonClick() {
        CommentFragment commentFragment = CommentFragment.getInstance();
        commentFragment.show(requireActivity().getSupportFragmentManager(), "CommentFragment");
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.img_add_addon)
    void onAddonClick() {
        displayAddonList();
        addonBottomSheetDialog.show();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btnCart)
    void onCartItemAdd() {
        CartItem cartItem = new CartItem();
        cartItem.setUid(Common.currentUser.getUid());
        cartItem.setUserPhone(Common.currentUser.getPhone());

        cartItem.setCategoryId(Common.categorySelected.getMenu_id());
        cartItem.setDrinksId(Common.selectDrinks.getId());
        cartItem.setDrinksName(Common.selectDrinks.getName());
        cartItem.setDrinksImage(Common.selectDrinks.getImage());
        cartItem.setDrinksPrice(Double.valueOf(String.valueOf(Common.selectDrinks.getPrice())));
        cartItem.setDrinksQuantity(Integer.parseInt(numberButton.getNumber()));
        cartItem.setDrinksExtraPrice(Common.calculateExtraPrice(Common.selectDrinks.getUserSelectedSize(), Common.selectDrinks.getUserSelectedAddon())); //Because default we not choose size + addon so extra price is 0
        if (Common.selectDrinks.getUserSelectedAddon() != null)
            cartItem.setDrinksAddon(new Gson().toJson(Common.selectDrinks.getUserSelectedAddon()));
        else
            cartItem.setDrinksAddon("Default");
        if (Common.selectDrinks.getUserSelectedSize() != null)
            cartItem.setDrinksSize(new Gson().toJson(Common.selectDrinks.getUserSelectedSize()));
        else
            cartItem.setDrinksSize("Default");

        cartDataSource.getItemWithOptionsInCart(Common.currentUser.getUid(),
                Common.categorySelected.getMenu_id(),
                cartItem.getDrinksId(),
                cartItem.getDrinksSize(),
                cartItem.getDrinksAddon())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }
                    @Override
                    public void onSuccess(@NonNull CartItem cartItemFromDB) {
                        if (cartItemFromDB.equals(cartItem)) {
                            //Already in database, just update
                            cartItemFromDB.setDrinksExtraPrice(cartItem.getDrinksExtraPrice());
                            cartItemFromDB.setDrinksAddon(cartItem.getDrinksAddon());
                            cartItemFromDB.setDrinksSize(cartItem.getDrinksSize());
                            cartItemFromDB.setDrinksQuantity(cartItemFromDB.getDrinksQuantity() + cartItem.getDrinksQuantity());

                            cartDataSource.updateCartItems(cartItemFromDB)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@NonNull Integer integer) {
                                            Toast.makeText(getContext(), "Cập nhật giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {
                                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            //Item not available in cart before, insert new
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Thêm giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable ->
                                            Toast.makeText(getContext(), "[CART ERROR]", Toast.LENGTH_SHORT).show())
                            );
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (Objects.requireNonNull(e.getMessage()).contains("empty")) {
                            //Default, if Cart is empty, this code will be fired
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Thêm giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable ->
                                            Toast.makeText(getContext(), "[CART ERROR]", Toast.LENGTH_SHORT).show())
                            );
                        } else
                            Toast.makeText(getContext(), "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayAddonList() {
        if (Common.selectDrinks.getAddon().size() > 0) {
            chip_group_addon.clearCheck(); //clear check all views
            chip_group_addon.removeAllViews();

            edt_search.addTextChangedListener(this);
            //add all view
            for (AddonModel addonModel : Common.selectDrinks.getAddon()) {

                @SuppressLint("InflateParams")
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuffer(addonModel.getName()).append(": ").append(Common.formatPrice(addonModel.getPrice())).append(" VNĐ"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        if (Common.selectDrinks.getUserSelectedAddon() == null)
                            Common.selectDrinks.setUserSelectedAddon(new ArrayList<>());
                        Common.selectDrinks.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_addon.addView(chip);

            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.drink_detail_fragment, container, false);
        unbinder = ButterKnife.bind(this, root);

        drinkDetailViewModel = new ViewModelProvider(this).get(DrinkDetailViewModel.class);
        initViews();
        drinkDetailViewModel.getMutableLiveDataDrinks().observe(getViewLifecycleOwner(), this::displayInfo);
        drinkDetailViewModel.getMutableLiveDataComment().observe(getViewLifecycleOwner(), this::submitRatingToFirebase);
        return root;
    }

    private void initViews() {
        //Show dialog
        dialog = KProgressHUD.create(requireContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).setWindowColor(Color.TRANSPARENT);
        setHasOptionsMenu(true);
        EventBus.getDefault().postSticky(new HideFABCart(true));
        cartDataSource = new CartDataSource(RoomDatabase.getInstance(getContext()).cartDAO());

        //waitingDialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        addonBottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.DialogStyle);
        @SuppressLint("InflateParams")
        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);
        chip_group_addon = layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = layout_addon_display.findViewById(R.id.edt_search);
        addonBottomSheetDialog.setContentView(layout_addon_display);

        addonBottomSheetDialog.setOnDismissListener(dialogInterface -> {
            displayUserSelectedAddon();
            calculateTotalPrice();
        });

    }

    private void displayUserSelectedAddon() {
        if (Common.selectDrinks.getUserSelectedAddon() != null &&
                Common.selectDrinks.getUserSelectedAddon().size() > 0) {
            chip_group_user_selected_addon.removeAllViews();
            for (AddonModel addonModel : Common.selectDrinks.getUserSelectedAddon()) {
                @SuppressLint("InflateParams")
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
                chip.setText(new StringBuffer(addonModel.getName()).append("( +").append(Common.formatPrice(addonModel.getPrice())).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view -> {
                    //Remove when user select delete
                    chip_group_user_selected_addon.removeView(view);
                    Common.selectDrinks.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });
                chip_group_user_selected_addon.addView(chip);
            }
        } else
            chip_group_user_selected_addon.removeAllViews();
    }

    private void submitRatingToFirebase(CommentModel commentModel) {
        dialog.show();
        FirebaseDatabase.getInstance()
                .getReference(Common.COMMENT_REF)
                .child(Common.selectDrinks.getId())
                .push()
                .setValue(commentModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //After submit to CommentRef, we will update value avenger in Drinks
                        addRatingToDrinks(commentModel.getRatingValue());
                    }
                    dialog.dismiss();
                });
    }

    private void addRatingToDrinks(float ratingValue) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .child("drinks")
                .child(Common.selectDrinks.getKey()) //Because drinks item is array list so key is index of array list
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            DrinksModel drinksModel = snapshot.getValue(DrinksModel.class);
                            if (drinksModel != null) {
                                drinksModel.setKey(Common.selectDrinks.getKey());
                            }

                            //Apply rating
                            if (drinksModel != null && drinksModel.getRatingValue() == null)
                                drinksModel.setRatingValue(0d);
                            if (drinksModel != null && drinksModel.getRatingCount() == 0)
                                drinksModel.setRatingCount(0L);

                            double sumRating = 0;
                            if (drinksModel != null) {
                                sumRating = drinksModel.getRatingValue() + ratingValue;
                            }
                            long ratingCount = 0;
                            if (drinksModel != null) {
                                ratingCount = drinksModel.getRatingCount() + 1;
                            }

                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("ratingValue", sumRating);
                            updateData.put("ratingCount", ratingCount);

                            //Update rating
                            if (drinksModel != null) {
                                drinksModel.setRatingValue(sumRating);
                            }
                            if (drinksModel != null) {
                                drinksModel.setRatingCount(ratingCount);
                            }

                            snapshot.getRef()
                                    .updateChildren(updateData)
                                    .addOnCompleteListener(task -> {
                                        dialog.dismiss();
                                        if (task.isSuccessful()) {
                                            //Chổ này hiện thông báo khi đánh giá xong
                                            Toast.makeText(getContext(), "Cảm ơn nhận xét của quý khách!", Toast.LENGTH_SHORT).show();
                                            Common.selectDrinks = drinksModel;
                                            drinkDetailViewModel.setDrinksModel(drinksModel);
                                        }
                                    });
                        } else
                            dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scheduleDismiss() {
        Handler handler = new Handler();
        handler.postDelayed(() -> dialog.dismiss(), 800);
    }

    // Chổ này để hiển thị thông tin lên
    private void displayInfo(DrinksModel drinksModel) {
        scheduleDismiss();
        Glide.with(requireContext()).load(drinksModel.getImage()).into(img_drinks);
        drinks_name.setText(new StringBuffer(drinksModel.getName()));
        drinks_description.setText(new StringBuffer(drinksModel.getDescription()));
        drinks_price.setText(new StringBuffer(Common.formatPrice(drinksModel.getPrice())).append(" VND"));

        if (drinksModel.getRatingValue() != null)
            ratingBar.setRating(drinksModel.getRatingValue().floatValue() / drinksModel.getRatingCount());

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(Common.selectDrinks.getName());

        //Size
        for (SizeModel sizeModel : Common.selectDrinks.getSize()) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setTextSize(18);
            Typeface face = getResources().getFont(R.font.roboto_slab);
            radioButton.setTypeface(face);
            radioButton.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b)
                    Common.selectDrinks.setUserSelectedSize(sizeModel);
                calculateTotalPrice(); //Update price
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            radioButton.setLayoutParams(params);
            radioButton.setText(sizeModel.getName());
            radioButton.setTag(sizeModel.getPrice());

            rdi_group_size.addView(radioButton);
        }

        if (rdi_group_size.getChildCount() > 0) {
            RadioButton radioButton = (RadioButton) rdi_group_size.getChildAt(0);
            radioButton.setChecked(true); //Default first select
        }

        calculateTotalPrice();
    }

    @SuppressLint("SetTextI18n")
    private void calculateTotalPrice() {
        double totalPrice = Double.parseDouble(String.valueOf(Common.selectDrinks.getPrice())), displayPrice;
        //Addon
        if (Common.selectDrinks.getUserSelectedAddon() != null && Common.selectDrinks.getUserSelectedAddon().size() > 0)
            for (AddonModel addonModel : Common.selectDrinks.getUserSelectedAddon())
                totalPrice += Double.parseDouble(String.valueOf(addonModel.getPrice()));

        //Size
        if (Common.selectDrinks.getUserSelectedSize() != null)
            totalPrice += Double.parseDouble(String.valueOf(Common.selectDrinks.getUserSelectedSize().getPrice()));

        displayPrice = totalPrice * (Integer.parseInt(numberButton.getNumber()));
        displayPrice = Math.round(displayPrice * 100.0 / 100.0);

        drinks_price.setText(Common.formatPrice(displayPrice) + " VND");
    }

    //Cái này là dialog nhập rating với comment
    private void showDialogRating() {
        scheduleDismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rating, null);
        RatingBar ratingBar = itemView.findViewById(R.id.rating_bar);
        EditText edt_comment = itemView.findViewById(R.id.edt_comment);
        Button btnCancel = itemView.findViewById(R.id.btn_cancel);
        Button btnOk = itemView.findViewById(R.id.btn_ok);


        builder.setView(itemView);

        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if(window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnOk.setOnClickListener(v -> {
            CommentModel commentModel = new CommentModel();
            commentModel.setName(Common.currentUser.getName());
            commentModel.setUid(Common.currentUser.getUid());
            commentModel.setComment(edt_comment.getText().toString());
            commentModel.setRatingValue(ratingBar.getRating());
            Map<String, Object> serverTimeStamp = new HashMap<>();
            serverTimeStamp.put("timeStamp", ServerValue.TIMESTAMP);
            commentModel.setCommentTimeStamp(serverTimeStamp);

            drinkDetailViewModel.setCommentModel(commentModel);
            dialog.dismiss();
        });

        dialog.show();

    }

    //Su kien cua cai add on
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //Nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();

        for (AddonModel addonModel : Common.selectDrinks.getAddon()) {
            if (addonModel.getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                @SuppressLint("InflateParams")
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
                chip.setText(new StringBuffer(addonModel.getName()).append(": ").append(Common.formatPrice(addonModel.getPrice())).append(" VNĐ"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        if (Common.selectDrinks.getUserSelectedAddon() == null)
                            Common.selectDrinks.setUserSelectedAddon(new ArrayList<>());
                        Common.selectDrinks.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_addon.addView(chip);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cart_bar,menu);
        View view = menu.findItem(R.id.cart_menu).getActionView();
        view.setOnClickListener(view1 -> EventBus.getDefault().postSticky(new CartItemClick(true)));
        badge = view.findViewById(R.id.badge);
        EventBus.getDefault().postSticky(new CounterCartEvent(true));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.cart_menu){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        EventBus.getDefault().postSticky(new CounterCartEvent(true));
        super.onStart();
    }

    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new HideFABCart(true));
        EventBus.getDefault().postSticky(new CounterCartEvent(true));
    }

    @Override
    public void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().postSticky(new HideFABCart(false));
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

}