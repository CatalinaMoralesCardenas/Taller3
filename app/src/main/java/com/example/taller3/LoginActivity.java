package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailL;
    private EditText passwordL;
    private Button loginButton;

    private FirebaseAuth mAuth;
    public static final String TAG = "FB_APP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailL = findViewById(R.id.emailL);
        passwordL = findViewById(R.id.passwordL);
        loginButton = findViewById(R.id.loginButton);

        passwordL.setTransformationMethod(PasswordTransformationMethod.getInstance());
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailL.getText().toString();
                String password = passwordL.getText().toString();

                if(validateForm(email, password)) {
                    singIn(email, password);
                }
            }
        });
    }

    private boolean validateForm( String email, String password ){
        if(email != null && password != null){
            if(!email.isEmpty() && !password.isEmpty()){
                if (email.contains("@") && email.contains(".com") && password.length() > 5){
                    return true;
                } else{
                    this.emailL.setError("Su correo debe estar en formato: correo@correo.com");
                    this.passwordL.setError( "Su contraseña debe tener 6 caracteres");
                }
            }
        }
        return false;
    }

    private void singIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI (FirebaseUser user){
        if(user != null){
            startActivity(new Intent(this, HomeScreenActivity.class));
            System.out.println("Logeado");

        }else{
            emailL.setText("");
            passwordL.setText("");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
}