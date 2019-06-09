package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

public class UserEditActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextInputLayout nameInputLayout, addressInputLayout;
    private Button saveButton;

    private ProgressBar progressBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        toolbar = findViewById(R.id.UEA_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (currentUser.getDisplayName().equals("") || currentUser.getDisplayName() == null) {
            getSupportActionBar().setTitle("Пользователь");
        } else {
            getSupportActionBar().setTitle(currentUser.getDisplayName());
        }

        nameInputLayout = findViewById(R.id.UEA_nameInputLayout);
        addressInputLayout = findViewById(R.id.UEA_addressInputLayout);
        saveButton = findViewById(R.id.UEA_saveButton);
        progressBar = findViewById(R.id.UEA_progressBar);

        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().equals("")) {
            String userName = currentUser.getDisplayName();
            nameInputLayout.getEditText().setText(userName);
        }

        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userSnapshot = task.getResult();
                    if (userSnapshot.get("address") != null) {
                        String userAddress = String.valueOf(userSnapshot.get("address"));
                        addressInputLayout.getEditText().setText(userAddress);
                    }
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean fillErrors = false;
                final String name = nameInputLayout.getEditText().getText().toString();
                final String address = addressInputLayout.getEditText().getText().toString();
                if(name.equals("")){
                    fillErrors = true;
                }
                if(address.equals("")){
                    fillErrors = true;
                }
                if(!fillErrors){
                    nameInputLayout.setEnabled(false);
                    addressInputLayout.setEnabled(false);
                    saveButton.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    final boolean[] errors = {false};
                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    currentUser.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                errors[0] = true;
                                nameInputLayout.setEnabled(true);
                                addressInputLayout.setEnabled(true);
                                saveButton.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Some errors with user update", Toast.LENGTH_SHORT).show();
                            } else {
                                final DocumentReference userRef = db.collection("users").document(currentUser.getUid());
                                db.runTransaction(new Transaction.Function<Object>() {
                                    @Nullable
                                    @Override
                                    public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                        DocumentSnapshot userSnapshot = transaction.get(userRef);
                                        transaction.update(userRef, "address", address);
                                        transaction.update(userRef, "username", name);
                                        return address;
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Object>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Object> task) {
                                        if (!task.isSuccessful()) {
                                            errors[0] = true;
                                            nameInputLayout.setEnabled(true);
                                            addressInputLayout.setEnabled(true);
                                            saveButton.setEnabled(true);
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(), "Some errors with user address update", Toast.LENGTH_SHORT).show();
                                        }else{
                                            if (!errors[0]) {
                                                Intent data = new Intent();
                                                data.putExtra("res", "OK");
                                                setResult(RESULT_OK, data);
                                                finish();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
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
