package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class UserOrdersActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private RecyclerView ordersRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private UserOrdersAdapter adapter;

    private ProgressBar progressBar;

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<UserOrder> ordersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_orders);

        toolbar = findViewById(R.id.UAO_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(currentUser.getDisplayName().equals("") || currentUser.getDisplayName() == null) {
            getSupportActionBar().setTitle("Заказы");
        }else{
            getSupportActionBar().setTitle("Заказы пользователя "+currentUser.getDisplayName());
        }

        progressBar = findViewById(R.id.UAO_progressBar);

        db.collection("orders").whereEqualTo("user", currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<DocumentSnapshot> orderSnapshots = task.getResult().getDocuments();
                    for(DocumentSnapshot orderSnapshot : orderSnapshots){
                        UserOrder userOrder = new UserOrder(orderSnapshot.getId());
                        ArrayList<Map<String, Object>> orderItems = (ArrayList<Map<String, Object>>) orderSnapshot.get("items");
                        for(ListIterator it = orderItems.listIterator(); it.hasNext(); ){
                            Map<String, Object> itemMap = (Map<String, Object>) it.next();
                            UserOrderItem item = new UserOrderItem(String.valueOf(itemMap.get("id")), Integer.valueOf(String.valueOf(itemMap.get("count"))));
                            userOrder.addItem(item);
                        }
                        ordersList.add(userOrder);
                    }
                    progressBar.setVisibility(View.GONE);
                    ordersRecyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        ordersRecyclerView = findViewById(R.id.UAO_recyclerView);
        ordersRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        ordersRecyclerView.setLayoutManager(layoutManager);

        adapter = new UserOrdersAdapter(ordersList);
        ordersRecyclerView.setAdapter(adapter);
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
