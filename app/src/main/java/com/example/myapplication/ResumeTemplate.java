package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

public interface ResumeTemplate {
    String generateHtmlPreview(String name, String email, String phone, String address, String links, String objective,
                               String experience, String education, String certifications, String skills, String languages,
                               Uri imageUri, Context context);
    void generatePdfContent(Document document, String name, String email, String phone, String address, String links,
                            String objective, String experience, String education, String certifications, String skills,
                            String languages, Uri imageUri, Context context) throws DocumentException;
}