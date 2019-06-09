package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminOrderActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String orderId;

    private Toolbar toolbar;
    private TextView titleTextView, addressTextView;
    private ProgressBar progressBar;

    private RecyclerView itemsRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private UserOrderItemsAdapter adapter;

    private Order currentOrder;

    ArrayList<UserOrderItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order);

        orderId = getIntent().getStringExtra("order");

        currentOrder = new Order(orderId);

        toolbar = findViewById(R.id.AAO_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleTextView = findViewById(R.id.AAO_title);
        addressTextView = findViewById(R.id.AAO_address);
        progressBar = findViewById(R.id.AAO_progressBar);

        db.collection("orders").document(orderId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot orderSnapshot = task.getResult();
                    if(orderSnapshot.get("user")!=null){
                        currentOrder.setUser(String.valueOf(orderSnapshot.get("user")));
                        db.collection("users").document(currentOrder.getUser()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot userSnapshot = task.getResult();
                                    if(userSnapshot.get("username")!=null){
                                        titleTextView.setText("Заказ пользователя " + userSnapshot.get("username"));
                                    }
                                    if(userSnapshot.get("address")!=null){
                                        addressTextView.setText("Адрес: "+userSnapshot.get("address"));
                                    }
                                }
                            }
                        });
                    }
                    if(orderSnapshot.get("items")!=null){
                        ArrayList<Item> orderItems = new ArrayList<>();
                        currentOrder.setItems(orderItems);
                        ArrayList<Map<String, Object>> itemsList = (ArrayList<Map<String, Object>>) orderSnapshot.get("items");
                        for (Map<String, Object> item : itemsList) {
                            UserOrderItem userOrderItem = new UserOrderItem(String.valueOf(item.get("id")), Integer.valueOf(String.valueOf(item.get("count"))));
                            items.add(userOrderItem);
                            Item orderItem = new Item(String.valueOf(item.get("id")), Integer.valueOf(String.valueOf(item.get("count"))));
                            currentOrder.addItem(orderItem);
                        }
                    }
                    titleTextView.setVisibility(View.VISIBLE);
                    addressTextView.setVisibility(View.VISIBLE);
                    itemsRecyclerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        itemsRecyclerView = findViewById(R.id.AAO_recyclerView);
        itemsRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        itemsRecyclerView.setLayoutManager(layoutManager);

        adapter = new UserOrderItemsAdapter(items);
        itemsRecyclerView.setAdapter(adapter);
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
