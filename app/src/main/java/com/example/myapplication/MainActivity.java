package com.example.myapplication;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int MANAGE_STORAGE_PERMISSION_CODE = 101;
    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    private ActivityMainBinding binding;
    private Uri selectedFileUri;
    private final ResumeAnalyzer resumeAnalyzer = new ResumeAnalyzer();
    private NotificationHelper notificationHelper;

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

        // Setup toolbar with proper styling
        binding.toolbar.setBackgroundColor(getResources().getColor(R.color.purple_500));
        binding.toolbar.setElevation(4f); // Set fixed elevation value
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("REMAA");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        binding.selectFileButton.setOnClickListener(v -> openFilePicker());
        binding.analyzeButton.setOnClickListener(v -> analyzeResume());
        binding.copyAllButton.setOnClickListener(v -> copyAllText());
        binding.makeResumeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ResumeBuilderActivity.class);
            startActivity(intent);
        });
        binding.analyzeButton.setEnabled(false);

        // Check storage permissions when activity starts
        checkAndRequestPermissions();

        notificationHelper = new NotificationHelper(this);
        
        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_chat) {
            startActivity(new Intent(this, ChatActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) and above
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
                }
            }
        } else {
            // Below Android 11
            String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
            
            if (!hasPermissions(permissions)) {
                ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_CODE);
            }
        }
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == STORAGE_PERMISSION_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                showPermissionExplanationDialog();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPermissionExplanationDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Storage Permission Required")
            .setMessage("REMAA needs storage permission to save your resume files. Without this permission, you won't be able to save or access your resumes.")
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                checkAndRequestPermissions();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                Toast.makeText(this, "Storage permission is required for saving resumes", Toast.LENGTH_LONG).show();
            })
            .setCancelable(false)
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Storage permission is required for saving resumes", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // Method to check if we have storage permission before saving
    private boolean checkStoragePermissionBeforeSaving() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showPermissionExplanationDialog();
                return false;
            }
        } else {
            String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
            
            if (!hasPermissions(permissions)) {
                checkAndRequestPermissions();
                return false;
            }
        }
        return true;
    }

    // Call this method before any file operation
    private void saveResume() {
        if (!checkStoragePermissionBeforeSaving()) {
            return;
        }
        // Proceed with saving the resume
        // Your existing save logic here
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    // Add this method to handle resume analysis and notification
    private void analyzeResumeAndNotify(String resumeContent) {
        // Create a background thread for analysis
        new Thread(() -> {
            // Simulate resume analysis (replace with your actual analysis logic)
            StringBuilder analysis = new StringBuilder();
            analysis.append("Resume Analysis Results:\n\n");
            
            // Add basic analysis points
            if (resumeContent.toLowerCase().contains("experience")) {
                analysis.append("✓ Work experience section present\n");
            } else {
                analysis.append("⚠ Work experience section missing\n");
            }
            
            if (resumeContent.toLowerCase().contains("education")) {
                analysis.append("✓ Education section present\n");
            } else {
                analysis.append("⚠ Education section missing\n");
            }
            
            if (resumeContent.toLowerCase().contains("skills")) {
                analysis.append("✓ Skills section present\n");
            } else {
                analysis.append("⚠ Skills section missing\n");
            }
            
            // Add overall score
            analysis.append("\nOverall Score: 85/100\n");
            analysis.append("\nSuggestions:\n");
            analysis.append("• Consider adding more quantifiable achievements\n");
            analysis.append("• Ensure all dates are in consistent format\n");
            analysis.append("• Add a brief professional summary");

            // Show notification with analysis results
            runOnUiThread(() -> {
                notificationHelper.showResumeAnalysisNotification(
                    "Resume Analysis Complete",
                    "Tap to view detailed analysis",
                    analysis.toString()
                );
            });
        }).start();
    }

    // Add this to your existing code where you process the resume
    private void processResume() {
        // After generating the resume content
        String resumeContent = ""; // Get your resume content here
        analyzeResumeAndNotify(resumeContent);
    }
}