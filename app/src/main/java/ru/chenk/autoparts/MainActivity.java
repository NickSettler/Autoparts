package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseFirestoreSettings settings;

    private boolean isAdmin = false;

    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView notFoundTextView;
    private FloatingActionButton fab;

    private RecyclerView recyclerView;
    private PartsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<Part> data = new ArrayList<>();

    private CartController cartController;

    private String modelFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(loginIntent, 1);
        } else {
            currentUser = firebaseAuth.getCurrentUser();
        }
        setContentView(R.layout.activity_main);

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        if(currentUser!=null){
            db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot userSnapshot = task.getResult();
                        if(userSnapshot.get("role") != null){
                            isAdmin = String.valueOf(userSnapshot.get("role")).equals("admin");
                        }
                    }
                }
            });
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartActivity = new Intent(MainActivity.this, CartActivity.class);
                startActivityForResult(cartActivity, 2);
            }
        });

        swipeRefreshLayout = findViewById(R.id.MA_refreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.notifyDataSetChanged();
                loadData();
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                loadData();
            }
        });

        recyclerView = findViewById(R.id.MA_recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PartsAdapter(data, this);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(currentUser!=null && !currentUser.isAnonymous()){
                    if (dy > 0 && fab.isShown()) {
                        fab.hide();
                    }
                    if (dy < -10 && !fab.isShown()) {
                        fab.show();
                    }
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        notFoundTextView = findViewById(R.id.MA_notFoundTextView);

        if(currentUser != null && currentUser.isAnonymous()){
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        invalidateOptionsMenu();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null){
            if(currentUser.isAnonymous()){
                fab.setVisibility(View.GONE);
            }
            db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot userSnapshot = task.getResult();
                        if(userSnapshot.get("role") != null){
                            isAdmin = String.valueOf(userSnapshot.get("role")).equals("admin");
                        }
                    }
                }
            });
        }
    }

    public void loadData() {
        Log.d("LOAD_DATA", "Loading....");
        CollectionReference partsCollection = db.collection("parts");
        Query partsQuery = partsCollection.whereEqualTo("shown", true);
        if (modelFilter != null && !modelFilter.equals("")) {
            partsQuery = partsQuery.whereEqualTo("model", modelFilter);
        }
        partsQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.size() > 0) {
                    notFoundTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    data.clear();
                    for (Iterator<QueryDocumentSnapshot> it = queryDocumentSnapshots.iterator(); it.hasNext(); ) {
                        DocumentSnapshot doc = it.next();
                        Map<String, Object> partData = doc.getData();
                        Part p = new Part();
                        p.setUid(doc.getId());
                        if (doc.get("name") != null) {
                            Log.d("MA_PART", doc.get("name").toString());
                            p.setName(doc.get("name").toString());
                        }
                        if (doc.get("image") != null) {
                            p.setImageSrc(doc.get("image").toString());
                        }
                        if (doc.get("count") != null) {
                            p.setCount(Integer.valueOf(doc.get("count").toString()));
                        }
                        if (doc.get("orders") != null) {
                            p.setOrders(Integer.valueOf(doc.get("orders").toString()));
                        }
                        if (doc.get("price") != null) {
                            p.setPrice(Double.valueOf(doc.get("price").toString()));
                        }
                        if(doc.get("specs")!=null){
                            Map<String, Map<String, String>> specs = (Map<String, Map<String, String>>) doc.get("specs");
                            Set<String> specKeys = specs.keySet();
                            for (String key : specKeys) {
                                Map<String, String> specByKey = specs.get(key);
                                Spec s = new Spec(specByKey.get("name"), specByKey.get("value"));
                                p.addSpec(s);
                            }
                        }
                        data.add(p);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    notFoundTextView.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d("MA", "Good!");
            if(requestCode == 1){
                adapter.notifyDataSetChanged();
                String modFilter = data.getStringExtra("model");
                modelFilter = modFilter;
            }
            this.loadData();
            adapter.notifyDataSetChanged();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (currentUser != null) {
            if(isAdmin){
                menu.getItem(2).setVisible(true);
            }else{
                menu.getItem(2).setVisible(false);
            }
            if(currentUser.isAnonymous()){
                menu.getItem(1).setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_admin) {
            Intent adminActivity = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(adminActivity);
        } else if (id == R.id.action_signout) {
            FirebaseAuth.getInstance().signOut();
            Intent reloadIntent = getIntent();
            finish();
            startActivity(reloadIntent);
        } else if (id == R.id.action_filter) {
            Intent filterActivity = new Intent(MainActivity.this, FilterActivity.class);
            filterActivity.putExtra("model", modelFilter);
            startActivityForResult(filterActivity, 1);
        }else if(id == R.id.action_profile){
            Intent userActivity = new Intent(MainActivity.this, UserActivity.class);
            startActivity(userActivity);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }
}
