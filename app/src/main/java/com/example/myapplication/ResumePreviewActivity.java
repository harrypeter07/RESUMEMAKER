package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;

public class ResumePreviewActivity extends AppCompatActivity {

    private WebView resumePreviewWebView;
    private MaterialButton downloadPdfButton;
    private String htmlContent;
    private ResumeTemplate template;
    private String name, email, phone, address, links, objective, experience, education, certifications, skills, languages;
    private Uri imageUri;
    private static final int STORAGE_WRITE_PERMISSION_CODE = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_preview);

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

        downloadPdfButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_WRITE_PERMISSION_CODE);
            } else {
                exportToPdf();
            }
        });
    }

    private void exportToPdf() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "Resume_" + name.replace(" ", "_") + ".pdf";
        File file = new File(downloadsDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            Document document = new Document();
            PdfWriter.getInstance(document, fos);
            document.open();
            template.generatePdfContent(document, name, email, phone, address, links, objective, experience, education, certifications, skills, languages, imageUri, this);
            document.close();
            fos.close();
            Toast.makeText(this, "Resume downloaded to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error downloading PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_WRITE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportToPdf();
        } else {
            Toast.makeText(this, "Write permission denied, cannot save PDF", Toast.LENGTH_SHORT).show();
        }
    }
}