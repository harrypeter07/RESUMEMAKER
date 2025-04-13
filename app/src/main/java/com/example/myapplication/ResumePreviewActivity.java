package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import android.media.MediaScannerConnection;
import androidx.annotation.NonNull;
import android.text.TextUtils;

public class ResumePreviewActivity extends AppCompatActivity {

    private WebView resumePreviewWebView;
    private MaterialButton downloadPdfButton;
    private String htmlContent;
    private ResumeTemplate template;
    private String name, email, phone, address, links, objective, experience, education, certifications, skills, languages;
    private Uri imageUri;
    private static final int STORAGE_WRITE_PERMISSION_CODE = 104;
    private static final int MANAGE_STORAGE_PERMISSION_CODE = 105;
    private static final int NOTIFICATION_PERMISSION_CODE = 106;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_preview);

        notificationHelper = new NotificationHelper(this);
        
        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        resumePreviewWebView = findViewById(R.id.resumePreviewWebView);
        downloadPdfButton = findViewById(R.id.downloadPdfButton);

        resumePreviewWebView.getSettings().setJavaScriptEnabled(true);

        // Retrieve data from intent
        Intent intent = getIntent();
        htmlContent = intent.getStringExtra("htmlContent");
        name = intent.getStringExtra("name");
        email = intent.getStringExtra("email");
        phone = intent.getStringExtra("phone");
        address = intent.getStringExtra("address");
        links = intent.getStringExtra("links");
        objective = intent.getStringExtra("objective");
        experience = intent.getStringExtra("experience");
        education = intent.getStringExtra("education");
        certifications = intent.getStringExtra("certifications");
        skills = intent.getStringExtra("skills");
        languages = intent.getStringExtra("languages");
        String templateName = intent.getStringExtra("templateName");
        imageUri = intent.getParcelableExtra("imageUri");

        // Initialize template
        switch (templateName) {
            case "Modern Template": template = new ModernTemplate(); break;
            case "Classic Template": template = new ClassicTemplate(); break;
            case "Minimal Template": template = new MinimalTemplate(); break;
            case "Professional Template": template = new ProfessionalTemplate(); break;
            default: template = new ClassicTemplate();
        }

        resumePreviewWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);

        downloadPdfButton.setOnClickListener(v -> requestPermissionsAndSave());
    }

    private void requestPermissionsAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.parse("package:" + getPackageName());
                    intent.setData(uri);
                    startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
                }
            } else {
                exportToPdf();
            }
        } else {
            String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
            
            if (!hasPermissions(permissions)) {
                ActivityCompat.requestPermissions(this, permissions, STORAGE_WRITE_PERMISSION_CODE);
            } else {
                exportToPdf();
            }
        }
    }

    private void exportToPdf() {
        try {
            String fileName = "Resume_" + name.replace(" ", "_") + ".pdf";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            File file = new File(downloadsDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            Document document = new Document();
            PdfWriter.getInstance(document, fos);
            document.open();
            template.generatePdfContent(document, name, email, phone, address, links, objective, experience, education, certifications, skills, languages, imageUri, this);
            document.close();
            fos.close();

            // Trigger resume analysis after PDF generation
            String resumeContent = String.format(
                "Name: %s\nEmail: %s\nPhone: %s\nAddress: %s\n" +
                "Links: %s\nObjective: %s\nExperience: %s\n" +
                "Education: %s\nCertifications: %s\nSkills: %s\n" +
                "Languages: %s",
                name, email, phone, address, links, objective,
                experience, education, certifications, skills, languages
            );
            
            analyzeResumeAndNotify(resumeContent, notificationHelper);

            // Ensure the file is visible in Downloads
            MediaScannerConnection.scanFile(this,
                    new String[]{file.getAbsolutePath()},
                    null,
                    (path, uri) -> {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Resume saved to Downloads folder", Toast.LENGTH_LONG).show();
                        });
                    });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    private void analyzeResumeAndNotify(String resumeContent, NotificationHelper notificationHelper) {
        new Thread(() -> {
            // Analyze the resume content
            StringBuilder analysis = new StringBuilder();
            analysis.append("Resume Analysis Results:\n\n");

            // Check for essential sections
            final int[] scoreHolder = {0};  // Using array to hold score since it needs to be final

            // Contact Information
            if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(email)) {
                analysis.append("✓ Contact Information complete\n");
                scoreHolder[0] += 20;
            } else {
                analysis.append("⚠ Contact Information incomplete\n");
            }

            // Professional Summary
            if (!TextUtils.isEmpty(objective)) {
                analysis.append("✓ Professional Summary present\n");
                scoreHolder[0] += 20;
            } else {
                analysis.append("⚠ Professional Summary missing\n");
            }

            // Work Experience
            if (!TextUtils.isEmpty(experience)) {
                analysis.append("✓ Work Experience section present\n");
                scoreHolder[0] += 20;
                if (experience.toLowerCase().contains("achieved") || 
                    experience.toLowerCase().contains("improved") ||
                    experience.toLowerCase().contains("developed")) {
                    analysis.append("✓ Experience includes action verbs and achievements\n");
                    scoreHolder[0] += 5;
                }
            } else {
                analysis.append("⚠ Work Experience section missing\n");
            }

            // Education
            if (!TextUtils.isEmpty(education)) {
                analysis.append("✓ Education section present\n");
                scoreHolder[0] += 20;
            } else {
                analysis.append("⚠ Education section missing\n");
            }

            // Skills
            if (!TextUtils.isEmpty(skills)) {
                analysis.append("✓ Skills section present\n");
                scoreHolder[0] += 20;
                if (skills.split(",").length >= 5) {
                    analysis.append("✓ Good range of skills listed\n");
                    scoreHolder[0] += 5;
                }
            } else {
                analysis.append("⚠ Skills section missing\n");
            }

            // Add recommendations
            analysis.append("\nRecommendations:\n");
            if (scoreHolder[0] < 90) {
                analysis.append("• Add more quantifiable achievements\n");
                analysis.append("• Include relevant keywords for your industry\n");
                analysis.append("• Ensure all dates are in consistent format\n");
            } else {
                analysis.append("• Great job! Your resume is well-structured\n");
                analysis.append("• Consider adding a LinkedIn profile if not included\n");
            }

            // Add final score
            analysis.append(String.format("\nOverall Score: %d/100", scoreHolder[0]));

            // Show notification with analysis results
            final int finalScore = scoreHolder[0];  // Create final variable for the lambda
            final String analysisText = analysis.toString();  // Create final variable for the analysis text
            
            // Check notification permission before sending
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            == PackageManager.PERMISSION_GRANTED) {
                        notificationHelper.showResumeAnalysisNotification(
                            "Resume Analysis Complete",
                            String.format("Resume Score: %d/100", finalScore),
                            analysisText
                        );
                    } else {
                        // Show analysis in a dialog if notification permission not granted
                        showAnalysisDialog(finalScore, analysisText);
                    }
                } else {
                    // For Android 12 and below, just show the notification
                    notificationHelper.showResumeAnalysisNotification(
                        "Resume Analysis Complete",
                        String.format("Resume Score: %d/100", finalScore),
                        analysisText
                    );
                }
            });
        }).start();
    }

    private void showAnalysisDialog(int score, String analysisText) {
        new AlertDialog.Builder(this)
            .setTitle("Resume Analysis Complete")
            .setMessage(String.format("Score: %d/100\n\n%s", score, analysisText))
            .setPositiveButton("OK", null)
            .setNeutralButton("Enable Notifications", (dialog, which) -> {
                requestNotificationPermission();
            })
            .show();
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == STORAGE_WRITE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportToPdf();
            } else {
                showPermissionDeniedDialog();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, 
                    "Notification permission denied. You won't receive analysis notifications.", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Storage permission is required to save your resume as PDF. Please grant the permission in Settings.")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    exportToPdf();
                } else {
                    showPermissionDeniedDialog();
                }
            }
        }
    }
}