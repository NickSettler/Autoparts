package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class PartActivity extends AppCompatActivity {


    private FirebaseFirestore db;

    private boolean exists = true;
    private Part part;
    private String id;

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

    private CartController cartController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part);
        part = (Part) getIntent().getSerializableExtra("part");
        id = getIntent().getStringExtra("id");

        Log.d("PA_SPEC", part.getSpecs().toString());

        db = FirebaseFirestore.getInstance();

        db.collection("parts").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot partSnapshot = task.getResult();
                    part.setCount(Integer.valueOf(String.valueOf(partSnapshot.get("count"))));
                    part.setOrders(Integer.valueOf(String.valueOf(partSnapshot.get("orders"))));

                    updateUI();
                }
            }
        });

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

        cartController = new CartController(shPref);
        cartList = cartController.getCart();

        appBarLayout = findViewById(R.id.PA_appBarLayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Drawable arrow = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back_white_24dp, null);
                if (verticalOffset < -200) {
                    arrow.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
                    getSupportActionBar().setHomeAsUpIndicator(arrow);
                } else {
                    arrow.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP);
                    getSupportActionBar().setHomeAsUpIndicator(arrow);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        });

        Glide.with(getApplicationContext())
                .load(part.imageSrc)
                .into(toolbarImage);

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
                if (part.getCount() > 0) {
                    if (cartController.exists(part.getUid())) {
                        cartController.increaseItem(part.getUid());
                    } else {
                        cartController.addItem(part.getUid(), 1, part.getCount());
                    }
                    Snackbar.make(v, "Добавлено", Snackbar.LENGTH_LONG)
                            .setAction("В корзину", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent cartActivity = new Intent(PartActivity.this, CartActivity.class);
                                    startActivityForResult(cartActivity, 2);
                                }
                            }).show();
                } else {
                    Snackbar.make(v, "Отсутствует в наличии", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateUI(){
        ordersTextView.setText(String.format(getString(R.string.PA_partOrdersTextView), String.valueOf(part.getOrders())));
        countTextView.setText(String.format(getString(R.string.PA_partCountTextView), String.valueOf(part.getCount())));

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d("PA", "Good!");
            if (data.getStringExtra("res").equals("OK")) {
                Intent intent = new Intent();
                intent.putExtra("res", "OK");
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
