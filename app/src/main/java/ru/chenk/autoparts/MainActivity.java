package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        swipeRefreshLayout = findViewById(R.id.MA_refreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                data.clear();
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
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener(){
            @Override
            public void onItemClick(View view, int position) {
                Intent partActivity = new Intent(MainActivity.this, PartActivity.class);
                partActivity.putExtra("part", data.get(position));
                startActivity(partActivity);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
    }

    public static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener{
        private OnItemClickListener mListener;

        public interface OnItemClickListener{
            public void onItemClick(View view, int position);
            public void onLongItemClick(View view, int position);
        }

        GestureDetector gestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener){
            mListener = listener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if(child != null && mListener != null){
                        mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && gestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public void loadData() {

        db.collection("parts").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                queryDocumentSnapshots.getDocuments().size();
                for (Iterator<QueryDocumentSnapshot> it = queryDocumentSnapshots.iterator(); it.hasNext(); ) {
                    DocumentSnapshot doc = it.next();
                    Part p = new Part();
                    if(doc.get("name")!=null){
                        p.setName(doc.get("name").toString());
                    }
                    if(doc.get("image")!=null){
                        p.setImage(doc.get("image").toString());
                    }
                    p.setUid(doc.getId());

                    data.add(p);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        db.collection("users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if(doc.get("role") != null && doc.get("role").toString().equals("admin")){
                    menu.getItem(1).setVisible(true);
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
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(loginIntent, 1);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
