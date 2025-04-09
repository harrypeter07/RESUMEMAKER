package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 3000; // 3 seconds
    private TextView appNameText, taglineText;
    private LottieAnimationView animationView;
    private CircularProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        appNameText = findViewById(R.id.appNameText);
        taglineText = findViewById(R.id.taglineText);
        animationView = findViewById(R.id.animationView);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Initially hide text views
        appNameText.setVisibility(View.INVISIBLE);
        taglineText.setVisibility(View.INVISIBLE);

        // Start animations after a short delay
        new Handler().postDelayed(this::startAnimations, 500);

        // Navigate to main activity after splash delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }

    private void startAnimations() {
        // Fade in animation for text views
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                appNameText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Start tagline animation after app name animation
                AlphaAnimation taglineFadeIn = new AlphaAnimation(0.0f, 1.0f);
                taglineFadeIn.setDuration(1000);
                taglineFadeIn.setFillAfter(true);
                taglineText.setVisibility(View.VISIBLE);
                taglineText.startAnimation(taglineFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // Start app name animation
        appNameText.startAnimation(fadeIn);
    }
} 