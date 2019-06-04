package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.util.FirestoreChannel;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private Button registerButton;
    private TextInputLayout emailLayout, passwordLayout, confirmLayout;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.RA_toolbar);
        setSupportActionBar(toolbar);

        registerButton = findViewById(R.id.RA_registerButton);

        emailLayout = findViewById(R.id.RA_emailInputLayout);
        passwordLayout = findViewById(R.id.RA_passwordInputLayout);
        confirmLayout = findViewById(R.id.RA_confirmPasswordInputLayout);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailLayout.getEditText().getText().toString();
                String password = passwordLayout.getEditText().getText().toString();
                String confPassword = confirmLayout.getEditText().getText().toString();

                boolean errors = false;

                if(email.equals("")){
                    emailLayout.setError("Введите почту");
                    errors = true;
                }else{
                    emailLayout.setError(null);
                }
                if(password.equals("")){
                    passwordLayout.setError("Введите пароль");
                    errors = true;
                }else{
                    passwordLayout.setError(null);
                }
                if(!(password.equals("")) && (confPassword.equals(""))){
                    confirmLayout.setError("Введите подтверждение пароля");
                    errors = true;
                }else if(!(password.equals("")) && !(confPassword.equals(""))){
                    if(!(password.equals(confPassword))){
                        confirmLayout.setError("Подтверждение не совпадает");
                        errors = true;
                    }else{
                        confirmLayout.setError(null);
                    }
                }

                if(!errors){
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("email", email);
                                        user.put("role", "user");
                                        db.collection("users").document(uid)
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("SIGNUP", "DB record created successfully");
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getApplicationContext(), "Some minor errors occured", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                        Intent intent = new Intent();
                                        intent.putExtra("reg", true);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }else{
                                        try{
                                            throw task.getException();
                                        }catch(FirebaseAuthWeakPasswordException weakPassEx){
                                            passwordLayout.setError("Введите более надеждный пароль");
                                        }catch(FirebaseAuthInvalidCredentialsException invCredEx){
                                            emailLayout.setError("Проверьте правильность почты");
                                        }catch(FirebaseAuthUserCollisionException userCollEx){
                                            emailLayout.setError("Пользователь уже зарегистрирован");
                                        }catch (Exception ex){
                                            Toast.makeText(getApplicationContext(), "Регистрация не удалась. Используйте гостевой аккаунт", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent();
                                            intent.putExtra("reg", false);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }
                                        Log.w("AUTH", "createUserWithEmail:failure", task.getException());
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
