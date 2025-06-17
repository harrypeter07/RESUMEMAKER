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
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModernTemplate implements ResumeTemplate {
    @Override
    public String generateHtmlPreview(String name, String email, String phone, String address, String links, String objective,
                                      String about, String introduction, String experience, String education, String certifications, String skills, String languages,
                                      Uri imageUri, Context context) {
        String css = "body { font-family: Helvetica, sans-serif; margin: 0; background: #ecf0f1; }" +
                "h1 { color: #fff; background: #2980b9; padding: 20px; text-align: center; font-size: 28px; }" +
                ".contact-info { color: #fff; text-align: center; font-size: 14px; padding-bottom: 10px; background: #2980b9; }" +
                ".container { max-width: 900px; margin: 20px auto; display: flex; flex-wrap: wrap; }" +
                ".left, .right { padding: 15px; min-width: 300px; }" +
                ".left { flex: 1; background: #fff; }" +
                ".right { flex: 2; background: #fff; margin-left: 10px; }" +
                "h2 { color: #2980b9; font-size: 18px; font-weight: bold; margin-top: 20px; }" +
                ".profile-img { max-width: 120px; border-radius: 50%; margin: 0 auto 20px; display: block; }" +
                "ul { padding-left: 20px; } li { margin-bottom: 10px; }" +
                "@media (max-width: 600px) { .left, .right { flex: 100%; margin-left: 0; } }";

        String imageTag = imageUri != null ? "<img src='" + imageUri.toString() + "' class='profile-img' />" : "";
        return "<!DOCTYPE html><html><head><style>" + css + "</style></head><body>" +
                "<h1>" + name + "</h1>" +
                "<div class='contact-info'>" + email + " | " + phone + (address.isEmpty() ? "" : " | " + address) + (links.isEmpty() ? "" : " | <a href='" + links + "'>" + links + "</a>") + "</div>" +
                "<div class='container'>" +
                "<div class='left'>" + imageTag +
                (objective.isEmpty() ? "" : "<h2>Objective</h2><p>" + objective + "</p>") +
                (about.isEmpty() ? "" : "<h2>About Me</h2><p>" + about + "</p>") +
                (introduction.isEmpty() ? "" : "<h2>Introduction</h2><p>" + introduction + "</p>") +
                "<h2>Skills</h2><ul>" + skills + "</ul>" +
                (languages.isEmpty() ? "" : "<h2>Languages</h2><ul>" + languages + "</ul>") +
                "</div>" +
                "<div class='right'>" +
                "<h2>Experience</h2><ul>" + experience + "</ul>" +
                "<h2>Education</h2><ul>" + education + "</ul>" +
                (certifications.isEmpty() ? "" : "<h2>Certifications</h2><ul>" + certifications + "</ul>") +
                "</div></div></body></html>";
    }

    @Override
    public void generatePdfContent(Document document, String name, String email, String phone, String address, String links,
                                   String objective, String about, String introduction, String experience, String education, String certifications, String skills,
                                   String languages, Uri imageUri, Context context) throws DocumentException {
        BaseColor primaryColor = new BaseColor(41, 128, 185);
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 28, Font.BOLD, BaseColor.WHITE);
        Font headingFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, primaryColor);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(primaryColor);
        headerCell.setPadding(20);

        Paragraph namePara = new Paragraph(name, titleFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(namePara);

        Paragraph contactInfo = new Paragraph(email + " | " + phone + (address.isEmpty() ? "" : " | " + address) + (links.isEmpty() ? "" : " | " + links), new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.WHITE));
        contactInfo.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(contactInfo);

        headerTable.addCell(headerCell);
        document.add(headerTable);
        document.add(new Paragraph(" "));

        PdfPTable contentTable = new PdfPTable(2);
        contentTable.setWidthPercentage(100);
        contentTable.setWidths(new float[]{1.2f, 1.8f});

        PdfPCell leftCell = new PdfPCell();
        leftCell.setPadding(15);
        leftCell.setBorder(Rectangle.NO_BORDER);

        if (imageUri != null) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Image img = Image.getInstance(byteArray);
                img.scaleToFit(80, 80);
                img.setAlignment(Element.ALIGN_CENTER);
                leftCell.addElement(img);
                inputStream.close();
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!objective.isEmpty()) {
            leftCell.addElement(new Paragraph("OBJECTIVE", headingFont));
            leftCell.addElement(new Paragraph(objective, normalFont));
        }

        if (!about.isEmpty()) {
            leftCell.addElement(new Paragraph("ABOUT ME", headingFont));
            leftCell.addElement(new Paragraph(about, normalFont));
        }

        if (!introduction.isEmpty()) {
            leftCell.addElement(new Paragraph("INTRODUCTION", headingFont));
            leftCell.addElement(new Paragraph(introduction, normalFont));
        }

        leftCell.addElement(new Paragraph("SKILLS", headingFont));
        for (String skill : parseTextToList(skills)) {
            leftCell.addElement(new Paragraph("• " + skill, normalFont));
        }
        if (!languages.isEmpty()) {
            leftCell.addElement(new Paragraph("LANGUAGES", headingFont));
            for (String lang : parseTextToList(languages)) {
                leftCell.addElement(new Paragraph("• " + lang, normalFont));
            }
        }

        PdfPCell rightCell = new PdfPCell();
        rightCell.setPadding(15);
        rightCell.setBorder(Rectangle.NO_BORDER);

        rightCell.addElement(new Paragraph("EXPERIENCE", headingFont));
        for (String exp : parseTextToList(experience)) {
            rightCell.addElement(new Paragraph("• " + exp, normalFont));
        }
        rightCell.addElement(new Paragraph("EDUCATION", headingFont));
        for (String edu : parseTextToList(education)) {
            rightCell.addElement(new Paragraph("• " + edu, normalFont));
        }
        if (!certifications.isEmpty()) {
            rightCell.addElement(new Paragraph("CERTIFICATIONS", headingFont));
            for (String cert : parseTextToList(certifications)) {
                rightCell.addElement(new Paragraph("• " + cert, normalFont));
            }
        }

        contentTable.addCell(leftCell);
        contentTable.addCell(rightCell);
        document.add(contentTable);
    }

    private List<String> parseTextToList(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(text.split("\n")));
    }
}