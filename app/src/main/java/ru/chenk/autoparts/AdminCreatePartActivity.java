package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminCreatePartActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private Button chooseImageButton;

    private TextInputLayout nameInputLayout, priceInputLayout, countInputLayout;
    private FloatingActionButton fab;

    private ImageView preview;
    private ProgressBar progressBar;
    private Spinner spinner;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    private String partId;
    private Uri fileUri;
    private String httpImageUrl;

    private List<String> modelsArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_part);

        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.ACPA_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preview = findViewById(R.id.ACPA_preview);

        nameInputLayout = findViewById(R.id.ACPA_nameInputLayout);
        priceInputLayout = findViewById(R.id.ACPA_priceInputLayout);
        countInputLayout = findViewById(R.id.ACPA_countInputLayout);

        progressBar = findViewById(R.id.ACPA_progressBar);

        spinner = findViewById(R.id.ACPA_modelsSpinner);
        db.collection("models").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot models = task.getResult();
                    List<DocumentSnapshot> modelSnapshots = models.getDocuments();
                    for(DocumentSnapshot modelSnapshot : modelSnapshots){
                        if(modelSnapshot.get("name")!=null){
                            modelsArray.add(String.valueOf(modelSnapshot.get("name")));
                            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(), android.R.layout.simple_spinner_item, modelsArray
                            );
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(spinnerAdapter);
                        }
                    }
                }
            }
        });



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String name = nameInputLayout.getEditText().getText().toString();
                String price = priceInputLayout.getEditText().getText().toString();
                String count = countInputLayout.getEditText().getText().toString();

                boolean errors = false;

                if (name.equals("")) {
                    nameInputLayout.setError("Введите название");
                    errors = true;
                }
                if (price.equals("")) {
                    priceInputLayout.setError("Введите название");
                    errors = true;
                }
                if (count.equals("")) {
                    countInputLayout.setError("Введите название");
                    errors = true;
                }
                if (!errors) {
                    progressBar.setVisibility(View.VISIBLE);
                    nameInputLayout.setEnabled(false);
                    priceInputLayout.setEnabled(false);
                    countInputLayout.setEnabled(false);
                    chooseImageButton.setEnabled(false);
                    final Map<String, Object> partMap = new HashMap<>();
                    partMap.put("name", name);
                    partMap.put("shown", true);
                    partMap.put("orders", 0);
                    partMap.put("count", Integer.valueOf(count));
                    partMap.put("price", Integer.valueOf(price));
                    db.collection("parts").add(partMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                DocumentReference partSnapshot = task.getResult();
                                partId = partSnapshot.getId();
                                Log.d("PARTID", partId);
                                String imageName = "parts/" + partSnapshot.getId() + "/" + System.currentTimeMillis();
                                final StorageReference imageRef = storageRef.child(imageName);
                                imageRef.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> fileTask) {
                                        if (fileTask.isSuccessful()) {
                                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    httpImageUrl = String.valueOf(uri);
                                                    final DocumentReference partRef = db.collection("parts").document(partId);
                                                    db.runTransaction(new Transaction.Function<Object>() {
                                                        @Nullable
                                                        @Override
                                                        public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                                            DocumentSnapshot partSnapshot = transaction.get(partRef);
                                                            transaction.update(partRef, "image", httpImageUrl);
                                                            return null;
                                                        }
                                                    });
                                                    setResult(RESULT_OK);
                                                    finish();
                                                }
                                            });
                                        }else{
                                            progressBar.setVisibility(View.GONE);
                                            nameInputLayout.setEnabled(true);
                                            priceInputLayout.setEnabled(true);
                                            countInputLayout.setEnabled(true);
                                            chooseImageButton.setEnabled(true);
                                            Snackbar.make(v, "Не удалось загрузить изображение на сервер", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }else{
                                progressBar.setVisibility(View.GONE);
                                nameInputLayout.setEnabled(true);
                                priceInputLayout.setEnabled(true);
                                countInputLayout.setEnabled(true);
                                chooseImageButton.setEnabled(true);
                                Snackbar.make(v, "Не удалось создать автозапчасть", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        chooseImageButton = findViewById(R.id.ACPA_chooseImageButton);
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("*/*");
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(Intent.createChooser(fileIntent, "Select an Image to Upload"), 0);

                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "No File Manager found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Удалить черновик")
                .setMessage("Запчасть еще не сохранена в базе. Данные не будут сохранены.")
                .setPositiveButton("Отменить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                preview.setImageURI(uri);
                fileUri = uri;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Удалить черновик")
                    .setMessage("Запчасть еще не сохранена в базе. Данные не будут сохранены.")
                    .setPositiveButton("Отменить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    })
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }
}
