package com.example.firebaselogin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView messageText;
    ImageView verifiedTick;
    ProgressBar progressBar;
    Handler handler;
    Runnable checkVerificationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        // Firebase auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        messageText = findViewById(R.id.verification_message);
        verifiedTick = findViewById(R.id.verified_tick);
        progressBar = findViewById(R.id.progress_bar);

        verifiedTick.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // If no user found, redirect to login
        if (user == null) {
            Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 🔁 Auto-check every 3 seconds
        handler = new Handler();
        checkVerificationRunnable = new Runnable() {
            @Override
            public void run() {
                user.reload().addOnSuccessListener(unused -> {
                    if (user.isEmailVerified()) {
                        progressBar.setVisibility(View.GONE);
                        verifiedTick.setVisibility(View.VISIBLE);
                        messageText.setText("Email verified successfully!");

                        // Stop further checks
                        handler.removeCallbacks(checkVerificationRunnable);

                        // Redirect after short delay
                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(EmailVerificationActivity.this, LoginActivity.class));
                            finish();
                        }, 1500);
                    } else {
                        handler.postDelayed(this, 3000); // Retry after 3 seconds
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(EmailVerificationActivity.this, "Error checking verification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    handler.postDelayed(this, 5000); // Retry after 5 seconds if error
                });
            }
        };

        handler.post(checkVerificationRunnable); // Start checking
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop handler to prevent memory leaks
        if (handler != null && checkVerificationRunnable != null) {
            handler.removeCallbacks(checkVerificationRunnable);
        }
    }
}
