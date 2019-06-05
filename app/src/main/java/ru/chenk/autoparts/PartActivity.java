package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class PartActivity extends AppCompatActivity {


    private FirebaseFirestore db;

    private boolean exists = true;
    private Part part;

    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView toolbarImage;
    private RecyclerView specsRecyclerView;
    private RecyclerView.LayoutManager specsLayoutManager;
    private SpecsAdapter adapter;

    public TextView nameTextView, priceTextView, ordersTextView, countTextView;

    private FloatingActionButton addToCart;

    private SharedPreferences shPref;
    private SharedPreferences.Editor shPrefEditor;

    private ArrayList<CartItem> cartList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part);
        part = (Part) getIntent().getSerializableExtra("part");

        Log.d("PA_SPEC", part.getSpecs().toString());

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarImage = findViewById(R.id.PA_toolbarImageView);
        setSupportActionBar(toolbar);
        collapsingToolbarLayout = findViewById(R.id.PA_collapsingToolbar);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.colorOnBackground));
        collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.colorOnPrimary));

        getSupportActionBar().setTitle(part.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        shPref = getSharedPreferences("SHPREF", MODE_PRIVATE);
        shPrefEditor = shPref.edit();

        getCart();

        appBarLayout = findViewById(R.id.PA_appBarLayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Drawable arrow = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back_white_24dp, null);
                if(verticalOffset < -200){
                    arrow.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
                    getSupportActionBar().setHomeAsUpIndicator(arrow);
                }else{
                    arrow.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP);
                    getSupportActionBar().setHomeAsUpIndicator(arrow);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        });

        if(part.image == null){
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(part.imageSrc);
            ref.getBytes(Long.MAX_VALUE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    if(task.isSuccessful()){
                        toolbarImage.setImageBitmap(BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length));
                    }
                }
            });
        }else{
            toolbarImage.setImageBitmap(BitmapFactory.decodeByteArray(part.image, 0, part.image.length));
        }
//        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Glide.with(getApplicationContext()).load(uri).into(toolbarImage);
//            }
//        });

        specsRecyclerView = findViewById(R.id.PA_specsRecyclerView);
        specsRecyclerView.setHasFixedSize(true);

        specsLayoutManager = new LinearLayoutManager(this);
        specsRecyclerView.setLayoutManager(specsLayoutManager);

        adapter = new SpecsAdapter(part.getSpecs());
        specsRecyclerView.setAdapter(adapter);

        priceTextView = findViewById(R.id.PA_priceTextView);
        nameTextView = findViewById(R.id.PA_partNameTextView);
        ordersTextView = findViewById(R.id.PA_partOrdersTextView);
        countTextView = findViewById(R.id.PA_partCountTextView);

        priceTextView.setText(String.format(getString(R.string.PA_partPriceTextView), String.valueOf(part.getPrice())));
        nameTextView.setText(part.getName());
        ordersTextView.setText(String.format(getString(R.string.PA_partOrdersTextView), String.valueOf(part.getOrders())));
        countTextView.setText(String.format(getString(R.string.PA_partCountTextView), String.valueOf(part.getCount())));

        addToCart = findViewById(R.id.PA_addToCartButton);
                addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = -1;
                int c = 0;
                for(CartItem ci : cartList){
                    String uid = ci.getUid();
                    if (part.getUid().equals(uid)) {
                        index = c;
                    }
                    c++;
                }
                CartItem ci = index == -1 ? new CartItem(part.getUid()) : cartList.get(index);
                if(index==-1){
                    cartList.add(ci);
                }else{
                    ci.inc();
                }
                saveCart();
                Log.d("CART", String.valueOf(cartList.get(0).getCount()));
                Snackbar.make(v, "Something here", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
            }
        });
    }


    public void saveCart(){
        Gson gson = new Gson();
        String json = gson.toJson(cartList);
        shPrefEditor.putString("CART", json);
        shPrefEditor.commit();
    }

    public void getCart(){
        Gson gson = new Gson();
        String json = shPref.getString("CART", "");
        cartList = json.equals("") ? new ArrayList<CartItem>() : (ArrayList<CartItem>) gson.fromJson(json, new TypeToken<ArrayList<CartItem>>() {
        }.getType());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
