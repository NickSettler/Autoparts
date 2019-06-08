package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.ListIterator;

public class CartActivity extends AppCompatActivity {

    private SharedPreferences shPref;
    private SharedPreferences.Editor shPrefEditor;

    private ArrayList<CartItem> cartList;

    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CartAdapter adapter;

    private CartController cartController;

    private ExtendedFloatingActionButton fab;

    private TextView totalPriceTextView;

    private int totalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        shPref = getSharedPreferences("SHPREF", MODE_PRIVATE);
        shPrefEditor = shPref.edit();

        cartController = new CartController(shPref);
        totalPrice = cartController.getTotalPrice();

        toolbar = findViewById(R.id.CA_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = findViewById(R.id.fab);

        if (cartController.getSize() == 0) {
            fab.setEnabled(false);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartController.getSize() > 0) {
                    Intent orderActivity = new Intent(CartActivity.this, OrderActivity.class);
                    startActivityForResult(orderActivity, 1);
                } else {
                    Snackbar.make(v, "Корзина пуста", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        totalPriceTextView = findViewById(R.id.CA_totalPrice);
        totalPriceTextView.setText("Итого: " + totalPrice + " руб.");

        recyclerView = findViewById(R.id.CA_recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CartAdapter(getApplicationContext(), cartController);
        recyclerView.setAdapter(adapter);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (cartController.getCart().size() == 0) {
                    fab.setEnabled(false);
                }
                totalPrice = cartController.getTotalPrice();
                totalPriceTextView.setText("Итого: " + totalPrice + " руб.");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (data.getStringExtra("res").equals("OK")) {
                Log.d("CA", "Good!");
                Intent intent = new Intent();
                intent.putExtra("res", "OK");
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.CAM_clear) {
            adapter.clear();
        }

        return super.onOptionsItemSelected(item);
    }
}
