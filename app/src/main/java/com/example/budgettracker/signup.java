package com.example.budgettracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;

    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginText = findViewById(R.id.loginRedirect);

        findViewById(R.id.singupButton).setOnClickListener(v -> signUp());

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(signup.this,login.class);
            startActivity(intent);
        });
    }

    private void signUp() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInput(name, email, password)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserData(user, name);
                            }
                            Intent intent = new Intent(signup.this, login.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(signup.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                            // Navigate to the next screen or login screen
                        } else {
                            // Check if the exception is related to an existing account
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(signup.this, "Account already exists with this email!", Toast.LENGTH_SHORT).show();
                            } else {
                                String errorMessage = "Sign-up failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                                Toast.makeText(signup.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    private void saveUserData(FirebaseUser user, String name) {
        User newUser = new User(name, user.getEmail());
        mDatabase.child("users").child(user.getUid()).setValue(newUser);
    }

    private boolean validateInput(String name, String email, String password) {
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Username is required");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters long");
            return false;
        }

        return true;
    }
}
