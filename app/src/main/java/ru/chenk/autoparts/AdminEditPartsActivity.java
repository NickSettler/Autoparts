package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminEditPartsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private RecyclerView partsRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private PartsEditAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Part> parts = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_parts);

        toolbar = findViewById(R.id.AEPA_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = findViewById(R.id.AEPA_swipeRefreshLayout);

        partsRecyclerView = findViewById(R.id.AEPA_recyclerView);
        partsRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        partsRecyclerView.setLayoutManager(layoutManager);

        adapter = new PartsEditAdapter(parts, getApplicationContext(), partsRecyclerView);
        partsRecyclerView.setAdapter(adapter);

        loadData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
    }

    public void loadData(){
        swipeRefreshLayout.setRefreshing(true);
        parts.clear();
        db.collection("parts").orderBy("orders", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> partsSnapshots = task.getResult().getDocuments();
                    for (DocumentSnapshot partSnapshot : partsSnapshots) {
                        Part part = new Part();
                        part.setUid(partSnapshot.getId());
                        if (partSnapshot.get("name") == null) {
                            part.setName("Имя не задано");
                        } else {
                            part.setName(String.valueOf(partSnapshot.get("name")));
                        }
                        if (partSnapshot.get("model") == null) {
                            part.setModel("Модель на задана");
                        } else {
                            part.setModel(String.valueOf(partSnapshot.get("model")));
                        }
                        if (partSnapshot.get("count") == null) {
                            part.setCount(0);
                        } else {
                            part.setCount(Integer.valueOf(String.valueOf(partSnapshot.get("count"))));
                        }
                        if (partSnapshot.get("orders") == null) {
                            part.setOrders(0);
                        } else {
                            part.setOrders(Integer.valueOf(String.valueOf(partSnapshot.get("orders"))));
                        }
                        if (partSnapshot.get("price") == null) {
                            part.setPrice(0);
                        } else {
                            part.setPrice(Integer.valueOf(String.valueOf(partSnapshot.get("price"))));
                        }
                        if (partSnapshot.get("image") == null) {
                            part.setImageSrc("");
                        } else {
                            part.setImageSrc(String.valueOf(partSnapshot.get("image")));
                        }
                        parts.add(part);
                    }
                    swipeRefreshLayout.setRefreshing(false);
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
