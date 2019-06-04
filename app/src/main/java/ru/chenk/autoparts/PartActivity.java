package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class PartActivity extends AppCompatActivity {


    private FirebaseFirestore db;

    private boolean exists = true;
    private Part part;

    private ImageView toolbarImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part);
        part = (Part) getIntent().getSerializableExtra("part");

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarImage = findViewById(R.id.PA_toolbarImageView);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(part.name);

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(part.image);
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(toolbarImage);
            }
        });
    }
}
