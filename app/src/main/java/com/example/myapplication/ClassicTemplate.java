package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassicTemplate implements ResumeTemplate {
    @Override
    public String generateHtmlPreview(String name, String email, String phone, String address, String links, String objective,
                                      String experience, String education, String certifications, String skills, String languages,
                                      Uri imageUri, Context context) {
        String css = "body { font-family: 'Arial', sans-serif; margin: 40px; line-height: 1.5; color: #333; }" +
                ".container { max-width: 800px; margin: 0 auto; }" +
                "h1 { color: #1a3c5e; text-align: center; font-size: 28px; margin-bottom: 10px; }" +
                "h2 { color: #2c3e50; font-size: 18px; font-weight: bold; border-bottom: 2px solid #3498db; padding-bottom: 5px; margin-top: 25px; }" +
                ".contact-info { color: #7f8c8d; text-align: center; font-size: 14px; margin-bottom: 20px; }" +
                ".profile-img { display: block; margin: 0 auto 20px; max-width: 150px; border-radius: 50%; }" +
                "ul { padding-left: 25px; list-style-type: none; } li { margin-bottom: 12px; position: relative; }" +
                "li:before { content: '•'; color: #3498db; position: absolute; left: -15px; }";

        String imageTag = imageUri != null ? "<img src='" + imageUri.toString() + "' class='profile-img' />" : "";
        return "<!DOCTYPE html><html><head><style>" + css + "</style></head><body><div class='container'>" +
                imageTag +
                "<h1>" + name + "</h1>" +
                "<div class='contact-info'>" + email + " | " + phone + (address.isEmpty() ? "" : " | " + address) + (links.isEmpty() ? "" : " | <a href='" + links + "'>" + links + "</a>") + "</div>" +
                (objective.isEmpty() ? "" : "<h2>Objective</h2><p>" + objective + "</p>") +
                "<h2>Experience</h2><ul>" + experience + "</ul>" +
                "<h2>Education</h2><ul>" + education + "</ul>" +
                (certifications.isEmpty() ? "" : "<h2>Certifications</h2><ul>" + certifications + "</ul>") +
                "<h2>Skills</h2><ul>" + skills + "</ul>" +
                (languages.isEmpty() ? "" : "<h2>Languages</h2><ul>" + languages + "</ul>") +
                "</div></body></html>";
    }

    @Override
    public void generatePdfContent(Document document, String name, String email, String phone, String address, String links,
                                   String objective, String experience, String education, String certifications, String skills,
                                   String languages, Uri imageUri, Context context) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 26, Font.BOLD, new BaseColor(26, 60, 94));
        Font headingFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(44, 62, 80));
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        if (imageUri != null) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Image img = Image.getInstance(bitmap, null);
                img.scaleToFit(100, 100);
                img.setAlignment(Element.ALIGN_CENTER);
                document.add(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Paragraph namePara = new Paragraph(name, titleFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        namePara.setSpacingAfter(10f);
        document.add(namePara);

        Paragraph contactInfo = new Paragraph(email + " | " + phone + (address.isEmpty() ? "" : " | " + address) + (links.isEmpty() ? "" : " | " + links), normalFont);
        contactInfo.setAlignment(Element.ALIGN_CENTER);
        contactInfo.setSpacingAfter(20f);
        document.add(contactInfo);

        if (!objective.isEmpty()) {
            document.add(new Paragraph("OBJECTIVE", headingFont));
            document.add(new Paragraph(objective, normalFont));
            document.add(new Paragraph(" "));
        }

        addSection(document, "EXPERIENCE", experience, headingFont, normalFont);
        addSection(document, "EDUCATION", education, headingFont, normalFont);
        if (!certifications.isEmpty()) addSection(document, "CERTIFICATIONS", certifications, headingFont, normalFont);
        addSection(document, "SKILLS", skills, headingFont, normalFont);
        if (!languages.isEmpty()) addSection(document, "LANGUAGES", languages, headingFont, normalFont);
    }

    private void addSection(Document document, String title, String content, Font headingFont, Font normalFont) throws DocumentException {
        document.add(new Paragraph(title, headingFont));
        document.add(new Paragraph(" "));
        for (String item : parseTextToList(content)) {
            Paragraph itemPara = new Paragraph("• " + item, normalFont);
            itemPara.setIndentationLeft(10f);
            document.add(itemPara);
        }
        document.add(new Paragraph(" "));
    }

    private List<String> parseTextToList(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(text.split("\n")));
    }
}