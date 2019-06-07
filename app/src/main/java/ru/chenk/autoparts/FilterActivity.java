package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class FilterActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private ChipGroup categoryChipGroup;

    private String modelFilter;

    private FirebaseFirestore db;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        context = this;

        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.FA_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        categoryChipGroup = findViewById(R.id.FA_categoryChipGroup);

        Intent intent = getIntent();
        if(intent.getStringExtra("model") != null){
            modelFilter = intent.getStringExtra("model");
        }

        db.collection("models").orderBy("order", Query.Direction.ASCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<DocumentSnapshot> modelDocuments = task.getResult().getDocuments();
                    for(ListIterator<DocumentSnapshot> it = modelDocuments.listIterator(); it.hasNext() ;){
                        DocumentSnapshot modelSnapshot = it.next();
                        final String name = modelSnapshot.get("name") != null ? String.valueOf(modelSnapshot.get("name")): "";
                        Chip modelChip = new Chip(context, null, R.style.Widget_MaterialComponents_Chip_Choice);
                        modelChip.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        modelChip.setClickable(true);
                        modelChip.setCheckable(true);
                        if(modelFilter!=null){
                            modelChip.setChecked(modelFilter.equals(name));
                        }
                        modelChip.setText(name);
                        modelChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked){
                                    categoryChipGroup.clearCheck();
                                    categoryChipGroup.check(buttonView.getId());
                                    modelFilter = name;
                                }else{
                                    modelFilter = "";
                                }
                            }
                        });
                        categoryChipGroup.addView(modelChip);
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_activity_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            Intent intent = new Intent();
            intent.putExtra("model", modelFilter);
            setResult(RESULT_OK, intent);
            finish();
        }else if(id == R.id.FAM_apply){
            Intent intent = new Intent();
            intent.putExtra("model", modelFilter);
            setResult(RESULT_OK, intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
