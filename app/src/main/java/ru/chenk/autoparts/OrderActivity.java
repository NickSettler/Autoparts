package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class OrderActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private Button infoButton;
    private TextInputLayout nameTextLayout, addressTextLayout;

    private CartController cartController;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private OrderItemsAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private String userName, userAddress;

    private boolean needToFillName, needToFillAddress;

    private boolean errors = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        cartController = new CartController(getSharedPreferences("SHPREF", MODE_PRIVATE));

        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.OA_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameTextLayout = findViewById(R.id.OA_name);
        addressTextLayout = findViewById(R.id.OA_address);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean fillErrors = false;
                if (nameTextLayout.getEditText().getText().toString().equals("")) {
                    nameTextLayout.setError("Необходимо заполнить имя");
                    fillErrors = true;
                } else {
                    fillErrors = false;
                }
                if (addressTextLayout.getEditText().getText().toString().equals("")) {
                    addressTextLayout.setError("Необходимо заполнить имя");
                    fillErrors = true;
                } else {
                    fillErrors = false;
                }

                if (!fillErrors) {
                    String name = nameTextLayout.getEditText().getText().toString();
                    final String address = addressTextLayout.getEditText().getText().toString();
                    if (needToFillName) {
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        currentUser.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Some errors with user update", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    if(needToFillAddress){
                        final DocumentReference userRef = db.collection("users").document(currentUser.getUid());
                        db.runTransaction(new Transaction.Function<Object>() {
                            @Nullable
                            @Override
                            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                DocumentSnapshot userSnapshot = transaction.get(userRef);
                                transaction.update(userRef, "address", address);
                                return address;
                            }
                        });
                    }
                    final ArrayList<CartItem> cartList = cartController.getCart();
                    for (final CartItem cartItem : cartList) {
                        final DocumentReference partRef = db.collection("parts").document(cartItem.getUid());
                        db.runTransaction(new Transaction.Function<Object>() {
                            @Nullable
                            @Override
                            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                DocumentSnapshot partSnapshot = transaction.get(partRef);
                                Integer newOrders = Integer.valueOf(String.valueOf(partSnapshot.get("orders"))) + 1;
                                Integer newCount = Integer.valueOf(String.valueOf(partSnapshot.get("count"))) - cartItem.getCount();
                                transaction.update(partRef, "orders", newOrders);
                                transaction.update(partRef, "count", newCount);
                                return null;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                errors = true;
                            }
                        });
                    }

                    Map<String, Object> newUserOrder = new HashMap<>();
                    newUserOrder.put("user", currentUser.getUid());
                    ArrayList<Map<String, Object>> items = new ArrayList<>();
                    for(CartItem cartItem : cartList){
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("id", cartItem.getUid());
                        itemMap.put("count", cartItem.getCount());
                        items.add(itemMap);
                    }
                    newUserOrder.put("items", items);
                    db.collection("orders").add(newUserOrder).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            errors = true;
                        }
                    });

                    if (errors) {
                        Snackbar.make(v, "Произошла ошибка", Snackbar.LENGTH_LONG).show();
                    }else{
                        Log.d("OA", "Good!");
                        cartController.clear();
                        Intent intent = new Intent();
                        intent.putExtra("res", "OK");
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });

        infoButton = findViewById(R.id.OA_infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userSnapshot = task.getResult();
                            ArrayList<Map<String, ArrayList<Map<String, Object>>>> allUserOrders = (ArrayList<Map<String, ArrayList<Map<String, Object>>>>) userSnapshot.getData().get("orders");
                            Log.d("OR", "BEFORE: " + allUserOrders.toString());
                            ArrayList<Map<String, Object>> userOrders = allUserOrders.get(0).get("items");
                            Map<String, Object> newItem = new HashMap<>();
                            newItem.put("uid", "LELELELE");
                            newItem.put("count", 123);
                            userOrders.add(newItem);
                            Map<String, ArrayList<Map<String, Object>>> newItemsList = new HashMap<>();
                            newItemsList.put("items", userOrders);
                            ArrayList<Map<String, ArrayList<Map<String, Object>>>> newOrdersList = new ArrayList<>();
                            newOrdersList.add(newItemsList);
                            for (Object currentUserOrder : allUserOrders.get(0).get("items")) {
                                Map<String, Double> userOrder = (Map<String, Double>) currentUserOrder;
                            }
                            Log.d("OR", "AFTER: " + newOrdersList.toString());
                        }
                    }
                });
            }
        });

        recyclerView = findViewById(R.id.OA_itemsRecyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new OrderItemsAdapter(cartController);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isShown()) {
                    fab.hide();
                }
                if (dy < -10 && !fab.isShown()) {
                    fab.show();
                }
            }
        });

        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().equals("")) {
            userName = currentUser.getDisplayName();
            nameTextLayout.getEditText().setText(userName);
        } else {
            needToFillName = true;
        }

        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot userSnapshot = task.getResult();
                    if(userSnapshot.get("address")!=null){
                        userAddress = String.valueOf(userSnapshot.get("address"));
                        addressTextLayout.getEditText().setText(userAddress);
                    }else{
                        needToFillAddress = true;
                    }
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
