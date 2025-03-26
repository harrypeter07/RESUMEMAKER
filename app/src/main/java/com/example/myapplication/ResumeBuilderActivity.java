package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import com.example.myapplication.databinding.ActivityResumeBuilderBinding;
import java.util.HashMap; // Added import
import java.util.Map;

public class ResumeBuilderActivity extends AppCompatActivity {

    private ActivityResumeBuilderBinding binding;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int PICK_IMAGE_REQUEST_CODE = 103;
    private Uri imageUri;
    private Map<String, ResumeTemplate> templateMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_resume_builder);

        templateMap = new HashMap<>();
        templateMap.put("Modern Template", new ModernTemplate());
        templateMap.put("Classic Template", new ClassicTemplate());
        templateMap.put("Minimal Template", new MinimalTemplate());
        templateMap.put("Professional Template", new ProfessionalTemplate());

        String[] templates = templateMap.keySet().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, templates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.templateSpinner.setAdapter(adapter);

        binding.generateResumeButton.setOnClickListener(v -> generateResume());
        binding.uploadImageButton.setOnClickListener(v -> pickImage());
    }

    private void pickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }

    private void generateResume() {
        String name = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        String address = binding.addressEditText.getText().toString().trim();
        String links = binding.linksEditText.getText().toString().trim();
        String objective = binding.objectiveEditText.getText().toString().trim();
        String experience = binding.experienceEditText.getText().toString().trim();
        String education = binding.educationEditText.getText().toString().trim();
        String certifications = binding.certificationsEditText.getText().toString().trim();
        String skills = binding.skillsEditText.getText().toString().trim();
        String languages = binding.languagesEditText.getText().toString().trim();
        String selectedTemplateName = binding.templateSpinner.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields (Name, Email, Phone)", Toast.LENGTH_SHORT).show();
            return;
        }

        String experienceItems = formatHtmlList(experience);
        String educationItems = formatHtmlList(education);
        String certificationsItems = formatHtmlList(certifications);
        String skillsItems = formatHtmlList(skills);
        String languagesItems = formatHtmlList(languages);

        ResumeTemplate template = templateMap.get(selectedTemplateName);
        String generatedResumeHtml = template.generateHtmlPreview(name, email, phone, address, links, objective, experienceItems, educationItems, certificationsItems, skillsItems, languagesItems, imageUri, this);

        Intent intent = new Intent(this, ResumePreviewActivity.class);
        intent.putExtra("htmlContent", generatedResumeHtml);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("phone", phone);
        intent.putExtra("address", address);
        intent.putExtra("links", links);
        intent.putExtra("objective", objective);
        intent.putExtra("experience", experience);
        intent.putExtra("education", education);
        intent.putExtra("certifications", certifications);
        intent.putExtra("skills", skills);
        intent.putExtra("languages", languages);
        intent.putExtra("templateName", selectedTemplateName);
        intent.putExtra("imageUri", imageUri);
        startActivity(intent);
    }

    private String formatHtmlList(String input) {
        if (input.isEmpty()) return "";
        StringBuilder items = new StringBuilder();
        for (String item : input.split("\n")) {
            if (!item.trim().isEmpty()) {
                items.append("<li>").append(item.trim()).append("</li>");
            }
        }
        return items.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}