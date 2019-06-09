package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class AdminAllOrdersActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Toolbar toolbar;

    private RecyclerView ordersRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private AllOrdersAdapter adapter;

    private List<Order> orders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_orders);

        toolbar = findViewById(R.id.AAOA_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ordersRecyclerView = findViewById(R.id.AAOA_recyclerView);
        ordersRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        ordersRecyclerView.setLayoutManager(layoutManager);

        adapter = new AllOrdersAdapter(orders, this);
        ordersRecyclerView.setAdapter(adapter);

        db.collection("orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<DocumentSnapshot> ordersSnapshots = task.getResult().getDocuments();
                    for(DocumentSnapshot orderSnapshot : ordersSnapshots){
                        Order order = new Order(orderSnapshot.getId());
                        if(orderSnapshot.get("user")!=null){
                            order.setUser(String.valueOf(orderSnapshot.get("user")));
                        }
                        if(orderSnapshot.get("items")!=null){
                            ArrayList<Item> items = new ArrayList<>();
                            order.setItems(items);
                            ArrayList<Map<String, Object>> itemsList = (ArrayList<Map<String, Object>>) orderSnapshot.get("items");
                            for (Map<String, Object> item : itemsList) {
                                Item orderItem = new Item(String.valueOf(item.get("id")), Integer.valueOf(String.valueOf(item.get("count"))));
                                order.addItem(orderItem);
                            }
                        }
                        orders.add(order);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
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
