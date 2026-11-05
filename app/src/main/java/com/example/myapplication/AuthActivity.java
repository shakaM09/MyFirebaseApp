package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private UserDAO userDAO;
    private EditText etEmail, etPassword;
    private Button btnSignIn, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        auth = FirebaseAuth.getInstance();
        userDAO = new UserDAO();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString();
                if (!inputIsValid(email, password, false)) {
                    return;
                }
                setButtonsEnabled(false);
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        routeSignedInUser();
                    } else {
                        setButtonsEnabled(true);
                        Toast.makeText(AuthActivity.this, describeError(task.getException()), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString();
                if (!inputIsValid(email, password, true)) {
                    return;
                }
                setButtonsEnabled(false);
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(AuthActivity.this, ProfileSetupActivity.class));
                        finish();
                    } else {
                        setButtonsEnabled(true);
                        Toast.makeText(AuthActivity.this, describeError(task.getException()), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            routeSignedInUser();
        }
    }

    private void routeSignedInUser() {
        setButtonsEnabled(false);
        userDAO.readUser(auth.getCurrentUser().getUid(), new IUserCallback() {
            @Override
            public void onUserReceived(Users user) {
                Class<?> target = user == null ? ProfileSetupActivity.class : MainActivity.class;
                startActivity(new Intent(AuthActivity.this, target));
                finish();
            }

            @Override
            public void onOperationComplete(boolean success, String message) {
                setButtonsEnabled(true);
                Toast.makeText(AuthActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean inputIsValid(String email, String password, boolean checkLength) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (checkLength && password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setButtonsEnabled(boolean enabled) {
        btnSignIn.setEnabled(enabled);
        btnSignUp.setEnabled(enabled);
    }

    private String describeError(Exception e) {
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Password must be at least 6 characters";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "This email is already registered";
        }
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "No user found with this email";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Wrong email or password";
        }
        if (e instanceof FirebaseNetworkException) {
            return "No internet connection";
        }
        return "Authentication failed";
    }
}
