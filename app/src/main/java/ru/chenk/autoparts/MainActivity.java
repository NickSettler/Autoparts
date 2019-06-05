package ru.chenk.autoparts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseFirestoreSettings settings;


    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;
    private PartsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<Part> data = new ArrayList<>();

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
        settings = new FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent cartActivity = new Intent(MainActivity.this, CartActivity.class);
                startActivity(cartActivity);
            }
        });
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//        navigationView.setNavigationItemSelectedListener(this);
//
//        View headerView = navigationView.getHeaderView(0);

//        headerNameTextView = headerView.findViewById(R.id.NH_name);
//        headerEmailTextView = headerView.findViewById(R.id.NH_email);
//
//        if(currentUser.isAnonymous()){
//            headerNameTextView.setText(getString(R.string.NH_nameTextViewGuest));
//        }else{
//            String displayName = currentUser.getDisplayName();
//            if(displayName!=null){
//                if(displayName.equals("")){
//                    headerNameTextView.setText(getString(R.string.NH_nameTextViewDefault));
//                }else{
//                    headerNameTextView.setText(currentUser.getDisplayName());
//                }
//            }
//            headerEmailTextView.setText(currentUser.getEmail());
//        }

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
    }

    public void loadData() {
        db.collection("parts").whereEqualTo("shown", true).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                data.clear();
                for (Iterator<QueryDocumentSnapshot> it = queryDocumentSnapshots.iterator(); it.hasNext(); ) {
                    DocumentSnapshot doc = it.next();
                    Map<String, Object> partData = doc.getData();
                    Part p = new Part();
                    p.setUid(doc.getId());
                    if(doc.get("name")!=null){
                        p.setName(doc.get("name").toString());
                    }
                    if(doc.get("image")!=null){
                        p.setImageSrc(doc.get("image").toString());
                    }
                    if(doc.get("count")!=null){
                        p.setCount(Integer.valueOf(doc.get("count").toString()));
                    }
                    if(doc.get("orders")!=null){
                        p.setOrders(Integer.valueOf(doc.get("orders").toString()));
                    }
                    if(doc.get("price")!=null){
                        p.setPrice(Double.valueOf(doc.get("price").toString()));
                    }
                    Map<String, Map<String, String>> specs = (Map<String, Map<String, String>>) doc.get("specs");
                    Set<String> specKeys = specs.keySet();
                    for(String key : specKeys){
                        Map<String, String> specByKey = specs.get(key);
                        Spec s = new Spec(specByKey.get("name"), specByKey.get("value"));
                        p.addSpec(s);
                    }

                    data.add(p);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        db.collection("users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if(doc.get("role") != null && doc.get("role").toString().equals("admin")){
                    menu.getItem(3).setVisible(true);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

        }else if(id == R.id.action_admin){
            Intent adminActivity = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(adminActivity);
        }else if (id == R.id.action_signout) {
            FirebaseAuth.getInstance().signOut();
//            Intent reloadIntent = getIntent();
//            finish();
//            startActivity(reloadIntent);
        }else if(id == R.id.action_filter){

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_home) {

        } else if (id == R.id.nav_orders) {

        } else if (id == R.id.nav_cart) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
