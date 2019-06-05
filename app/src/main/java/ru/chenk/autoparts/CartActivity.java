package ru.chenk.autoparts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private SharedPreferences shPref;
    private SharedPreferences.Editor shPrefEditor;

    private ArrayList<CartItem> cartList;

    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        shPref = getSharedPreferences("SHPREF", MODE_PRIVATE);
        shPrefEditor = shPref.edit();

        getCart();

        toolbar = findViewById(R.id.CA_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.CA_recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CartAdapter(cartList);
        recyclerView.setAdapter(adapter);
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
