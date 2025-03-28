package com.example.myapplication;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.utils.PrefsManager;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private Uri selectedFileUri;
    private final ResumeAnalyzer resumeAnalyzer = new ResumeAnalyzer();
    private DrawerLayout drawerLayout;
    private PrefsManager prefsManager;

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            result -> {
                if (result != null) {
                    selectedFileUri = result;
                    // Grant permission to read this file
                    getContentResolver().takePersistableUriPermission(result,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    String fileName = FileUtils.getFileName(getContentResolver(), selectedFileUri);
                    binding.selectedFileTextView.setText("Selected File: " + fileName);
                    binding.analyzeButton.setEnabled(true);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        prefsManager = new PrefsManager(this);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up dark mode switch
        SwitchCompat themeSwitch = toolbar.findViewById(R.id.themeSwitch);
        themeSwitch.setChecked(isNightModeEnabled());
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setNightMode(isChecked);
        });

        // Set up navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up user info in nav header
        View headerView = navigationView.getHeaderView(0);
        TextView nameText = headerView.findViewById(R.id.nav_header_name);
        TextView emailText = headerView.findViewById(R.id.nav_header_email);
        nameText.setText(prefsManager.getUserName());
        emailText.setText(prefsManager.getUserEmail());

        // Set up card click listeners
        CardView analyzeCard = findViewById(R.id.analyzeResumeCard);
        CardView createCard = findViewById(R.id.createResumeCard);

        analyzeCard.setOnClickListener(v -> {
            // Handle analyze resume click
            analyzeResume();
        });

        createCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ResumeBuilderActivity.class);
            startActivity(intent);
        });

        // Initialize views
        binding.selectFileButton.setOnClickListener(v -> openFilePicker());
        binding.analyzeButton.setOnClickListener(v -> analyzeResume());
        binding.copyAllButton.setOnClickListener(v -> copyAllText());
        binding.analyzeButton.setEnabled(false);
    }

    private void openFilePicker() {
        // Accept all document types that could be resumes
        filePickerLauncher.launch(new String[]{
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain"
        });
    }

    private void analyzeResume() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a resume file first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.analyzeButton.setEnabled(false);
        binding.analysisResultTextView.setText("");
        binding.copyAllButton.setVisibility(View.GONE);

        // Extract text from the document
        FileUtils.extractTextFromDocument(getContentResolver(), selectedFileUri, extractedText -> {
            if (extractedText == null || extractedText.isEmpty()) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to extract text from the file", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.analyzeButton.setEnabled(true);
                });
                return;
            }

            // Analyze the resume using Gemini API
            resumeAnalyzer.analyzeResume(extractedText, analysis -> {
                // Update UI with analysis results
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.analyzeButton.setEnabled(true);
                    // Format the analysis result before displaying
                    String formattedResult = formatAnalysisResult(analysis);
                    binding.analysisResultTextView.setText(formattedResult);
                    // Show the Copy All button if there's a result
                    if (!formattedResult.isEmpty()) {
                        binding.copyAllButton.setVisibility(View.VISIBLE);
                    }
                });
            });
        });
    }

    private void copyAllText() {
        String textToCopy = binding.analysisResultTextView.getText().toString();
        if (!textToCopy.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Resume Analysis", textToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Analysis result copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No text to copy", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatAnalysisResult(String analysisResult) {
        // Split the analysis result into sections based on possible heading formats
        String[] sections = analysisResult.split("(?i)(?=\\n\\*\\*.*?\\*\\*|\\n# .*?|\\n.*?\\n[-=]+|\\n.*?:(?=\n))");

        StringBuilder formatted = new StringBuilder();
        for (String section : sections) {
            section = section.trim();
            if (section.isEmpty()) continue;

            // Remove markdown-like formatting and clean up
            String cleanedSection = section.replaceAll("(\\*\\*|#|=|-{2,})", "").trim();

            // Format bullet points (e.g., "- Item", "* Item", or numbered lists)
            String[] lines = cleanedSection.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("- ") || line.startsWith("* ")) {
                    line = "• " + line.substring(2).trim();
                } else if (line.matches("^\\d+\\.\\s.*")) {
                    // Handle numbered lists (e.g., "1. Item")
                    line = "• " + line.replaceFirst("^\\d+\\.\\s", "");
                }
                if (!line.isEmpty()) {
                    formatted.append(line).append("\n\n");
                }
            }
        }
        return formatted.toString().trim();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home screen
        } else if (id == R.id.nav_create) {
            Intent intent = new Intent(this, ResumeBuilderActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            prefsManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean isNightModeEnabled() {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
    }

    private void setNightMode(boolean isNightMode) {
        AppCompatDelegate.setDefaultNightMode(
                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}