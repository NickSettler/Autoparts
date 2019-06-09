package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class UserActivity extends AppCompatActivity {

    private TextView nameTextView, addressTextView;
    private Button ordersButton;

    private Toolbar toolbar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private ArrayList<UserOrder> ordersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        toolbar = findViewById(R.id.UA_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (currentUser.getDisplayName().equals("") || currentUser.getDisplayName() == null) {
            getSupportActionBar().setTitle("Пользователь");
        } else {
            getSupportActionBar().setTitle(currentUser.getDisplayName());
        }

        nameTextView = findViewById(R.id.UA_nameTextView);
        addressTextView = findViewById(R.id.UA_addressTextView);
        ordersButton = findViewById(R.id.UA_ordersButton);

        if (currentUser.getDisplayName().equals("") || currentUser.getDisplayName() == null) {
            nameTextView.setText("Имя не задано");
            nameTextView.setTextColor(Color.parseColor("#b6b6b6"));
        } else {
            nameTextView.setText(currentUser.getDisplayName());
        }
        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userSnapshot = task.getResult();
                    if (userSnapshot.get("address") != null) {
                        String userAddress = String.valueOf(userSnapshot.get("address"));
                        addressTextView.setText(userAddress);
                    }
                }
            }
        });

        nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        db.collection("orders").whereEqualTo("user", currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> orderSnapshots = task.getResult().getDocuments();
                    for (DocumentSnapshot orderSnapshot : orderSnapshots) {
                        UserOrder userOrder = new UserOrder(orderSnapshot.getId());
                        ArrayList<Map<String, Object>> orderItems = (ArrayList<Map<String, Object>>) orderSnapshot.get("items");
                        for (ListIterator it = orderItems.listIterator(); it.hasNext(); ) {
                            Map<String, Object> itemMap = (Map<String, Object>) it.next();
                            UserOrderItem item = new UserOrderItem(String.valueOf(itemMap.get("id")), Integer.valueOf(String.valueOf(itemMap.get("count"))));
                            userOrder.addItem(item);
                        }
                        ordersList.add(userOrder);
                    }
                }
            }
        });

        ordersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userOrdersActivity = new Intent(UserActivity.this, UserOrdersActivity.class);
                startActivity(userOrdersActivity);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("RQC", String.valueOf(requestCode));
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().equals("")) {
                    nameTextView.setText(currentUser.getDisplayName());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().equals("")) {
            nameTextView.setText(currentUser.getDisplayName());
        }
        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userSnapshot = task.getResult();
                    if (userSnapshot.get("address") != null) {
                        String userAddress = String.valueOf(userSnapshot.get("address"));
                        addressTextView.setText(userAddress);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.UAM_edit) {
            Intent userEditActivity = new Intent(UserActivity.this, UserEditActivity.class);
            startActivityForResult(userEditActivity, 1);
        }

        return super.onOptionsItemSelected(item);
    }
}
