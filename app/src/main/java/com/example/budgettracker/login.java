package com.example.budgettracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView signUpTextView;
    private TextView forgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(login.this, MainActivity.class));
            finish(); // Close the login activity
            return; // Stop further execution of the login activity
        }
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        signUpTextView = findViewById(R.id.signupRedirect);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        findViewById(R.id.loginButton).setOnClickListener(v -> login());

        signUpTextView.setOnClickListener(v -> startActivity(new Intent(login.this, signup.class)));

        forgotPasswordTextView.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                sendPasswordResetEmail(email);
            } else {
                emailEditText.setError("Please enter your email");
            }
        });
    }

    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInput(email, password)) {
            // Try to sign in with email and password
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, navigate to MainActivity
                            startActivity(new Intent(login.this, MainActivity.class));
                            finish();
                            Toast.makeText(login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            handleLoginError(task.getException());
                        }
                    });
        }
    }

    private void handleLoginError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            String errorCode = authException.getErrorCode();

            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    emailEditText.setError("Invalid email address");
                    break;
                case "ERROR_WRONG_PASSWORD":
                    passwordEditText.setError("Incorrect password");
                    break;
                case "ERROR_USER_NOT_FOUND":
                    Toast.makeText(login.this, "No account found with this email", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(login.this, authException.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            Toast.makeText(login.this, (exception != null ? exception.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String email, String password) {
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

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(login.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
