package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide the status bar for a full-screen splash experience
        getWindow().setStatusBarColor(0xFF2563EB);

        // Delay for 2 seconds then start MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            // Smooth transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 2000);
    }
}