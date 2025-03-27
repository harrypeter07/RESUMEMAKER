package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinimalTemplate implements ResumeTemplate {
    @Override
    public String generateHtmlPreview(String name, String email, String phone, String address, String links, String objective,
                                      String experience, String education, String certifications, String skills, String languages,
                                      Uri imageUri, Context context) {
        String css = "body { font-family: Helvetica, sans-serif; margin: 50px; color: #333; }" +
                ".container { max-width: 700px; margin: 0 auto; }" +
                "h1 { color: #000; text-transform: uppercase; font-size: 26px; border-bottom: 1px solid #3498db; padding-bottom: 5px; }" +
                "h2 { color: #000; font-size: 14px; margin-top: 20px; }" +
                ".contact-info { color: #555; font-size: 12px; margin-bottom: 20px; }" +
                ".profile-img { max-width: 100px; border-radius: 50%; float: right; margin-left: 20px; }" +
                "ul { padding-left: 20px; } li { margin-bottom: 8px; font-size: 11px; }" +
                ".skills li, .languages li { display: inline; margin-right: 15px; }";

        String imageTag = imageUri != null ? "<img src='" + imageUri.toString() + "' class='profile-img' />" : "";
        return "<!DOCTYPE html><html><head><style>" + css + "</style></head><body><div class='container'>" +
                imageTag +
                "<h1>" + name + "</h1>" +
                "<div class='contact-info'>" + email + " | " + phone + (address.isEmpty() ? "" : " | " + address) + (links.isEmpty() ? "" : " | <a href='" + links + "'>" + links + "</a>") + "</div>" +
                (objective.isEmpty() ? "" : "<h2>Objective</h2><p>" + objective + "</p>") +
                "<h2>Experience</h2><ul>" + experience + "</ul>" +
                "<h2>Education</h2><ul>" + education + "</ul>" +
                (certifications.isEmpty() ? "" : "<h2>Certifications</h2><ul>" + certifications + "</ul>") +
                "<h2>Skills</h2><ul class='skills'>" + skills + "</ul>" +
                (languages.isEmpty() ? "" : "<h2>Languages</h2><ul class='languages'>" + languages + "</ul>") +
                "</div></body></html>";
    }

    @Override
    public void generatePdfContent(Document document, String name, String email, String phone, String address, String links,
                                   String objective, String experience, String education, String certifications, String skills,
                                   String languages, Uri imageUri, Context context) throws DocumentException {
        Font nameFont = new Font(Font.FontFamily.HELVETICA, 24, Font.NORMAL);
        Font headingFont = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);

        document.setMargins(50, 50, 50, 50);

        if (imageUri != null) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Image img = Image.getInstance(byteArray);
                img.scaleToFit(80, 80);
                img.setAbsolutePosition(450, 750); // Top-right corner
                document.add(img);
                inputStream.close();
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Paragraph namePara = new Paragraph(name.toUpperCase(), nameFont);
        namePara.setSpacingAfter(5f);
        document.add(namePara);
        document.add(new LineSeparator(1f, 100f, new com.itextpdf.text.BaseColor(52, 152, 219), Element.ALIGN_LEFT, 0));

        Paragraph contactInfo = new Paragraph(email + " | " + phone + (address.isEmpty() ? "" : " | " + address) + (links.isEmpty() ? "" : " | " + links), normalFont);
        document.add(contactInfo);
        document.add(new Paragraph(" "));

        if (!objective.isEmpty()) {
            document.add(new Paragraph("OBJECTIVE", headingFont));
            document.add(new Paragraph(objective, normalFont));
            document.add(new Paragraph(" "));
        }

        addSection(document, "EXPERIENCE", experience, headingFont, normalFont);
        addSection(document, "EDUCATION", education, headingFont, normalFont);
        if (!certifications.isEmpty()) addSection(document, "CERTIFICATIONS", certifications, headingFont, normalFont);
        document.add(new Paragraph("SKILLS", headingFont));
        Paragraph skillsPara = new Paragraph();
        boolean first = true;
        for (String skill : parseTextToList(skills)) {
            if (!first) skillsPara.add(" • ");
            skillsPara.add(skill);
            first = false;
        }
        document.add(skillsPara);
        if (!languages.isEmpty()) {
            document.add(new Paragraph(" "));
            document.add(new Paragraph("LANGUAGES", headingFont));
            Paragraph langPara = new Paragraph();
            first = true;
            for (String lang : parseTextToList(languages)) {
                if (!first) langPara.add(" • ");
                langPara.add(lang);
                first = false;
            }
            document.add(langPara);
        }
    }

    private void addSection(Document document, String title, String content, Font headingFont, Font normalFont) throws DocumentException {
        document.add(new Paragraph(title, headingFont));
        document.add(new Paragraph(" "));
        for (String item : parseTextToList(content)) {
            Paragraph itemPara = new Paragraph("• " + item, normalFont);
            itemPara.setIndentationLeft(10);
            document.add(itemPara);
        }
        document.add(new Paragraph(" "));
    }

    private List<String> parseTextToList(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(text.split("\n")));
    }
}