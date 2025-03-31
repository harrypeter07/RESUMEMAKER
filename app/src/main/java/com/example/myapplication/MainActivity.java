package com.example.myapplication;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Uri selectedFileUri;
    private final ResumeAnalyzer resumeAnalyzer = new ResumeAnalyzer();

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

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Resume Builder");
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
                    // Show the Copy All button if there’s a result
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
}