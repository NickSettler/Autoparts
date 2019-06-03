package ru.chenk.autoparts;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn, guestLoginBtn, registerButton;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        loginBtn = findViewById(R.id.LA_loginButton);
        guestLoginBtn = findViewById(R.id.LA_guestLoginButton);
        registerButton = findViewById(R.id.LA_registerButton);

        emailInputLayout = findViewById(R.id.LA_emailInputLayout);
        passwordInputLayout = findViewById(R.id.LA_passwordInputLayout);

        loginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String email = emailInputLayout.getEditText().getText().toString();
                String password = passwordInputLayout.getEditText().getText().toString();
                boolean errors = false;

                if(email.equals("")){
                    emailInputLayout.setError("Введите почту");
                    errors = true;
                }else{
                    emailInputLayout.setError(null);
                }

                if(password.equals("")){
                    passwordInputLayout.setError("Введите пароль");
                    errors = true;
                }else{
                    passwordInputLayout.setError(null);
                }

                if(!errors){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        finish();
                                    }else{
                                        try{
                                            throw task.getException();
                                        }catch(FirebaseAuthInvalidUserException invUserEx){
                                            emailInputLayout.setError("Проверьте правильность почты");
                                        }catch(FirebaseAuthInvalidCredentialsException invCredEx){
                                            passwordInputLayout.setError("Введена неправильная почта или пароль");
                                        }catch(Exception ex){
                                            Toast.makeText(getApplicationContext(), "Войти не удалось. Используйте гостевой аккаунт", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                }
            }
        });

        guestLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signInAnonymously()
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    finish();
                                }
                            }
                        });
//                updateIntent();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(registerActivity, 1);
            }
        });
    }

    public void updateIntent(){
        Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }

    @Override
    public void onBackPressed() {
//        Nothing
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(data == null){ return ; }
        boolean regged = data.getBooleanExtra("reg", false);
        if(regged){
            finish();
        }
    }
}
