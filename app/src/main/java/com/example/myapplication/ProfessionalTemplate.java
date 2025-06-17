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

public class ProfessionalTemplate implements ResumeTemplate {
    @Override
    public String generateHtmlPreview(String name, String email, String phone, String address, String links, String objective,
                                      String about, String introduction, String experience, String education, String certifications, String skills, String languages,
                                      Uri imageUri, Context context) {
        String css = "body { font-family: 'Times New Roman', serif; margin: 40px; color: #333; }" +
                ".container { max-width: 800px; margin: 0 auto; }" +
                "h1 { color: #2c3e50; text-align: center; font-size: 28px; }" +
                "h2 { color: #464646; font-size: 18px; font-weight: bold; border-bottom: 2px solid #464646; padding-bottom: 5px; margin-top: 20px; }" +
                ".contact-info { color: #7f8c8d; text-align: center; font-size: 14px; margin-bottom: 20px; }" +
                ".profile-img { display: block; margin: 0 auto 20px; max-width: 150px; border-radius: 50%; }" +
                "ul { padding-left: 25px; } li { margin-bottom: 10px; font-size: 12px; }";

        String imageTag = imageUri != null ? "<img src='" + imageUri.toString() + "' class='profile-img' />" : "";
        return "<!DOCTYPE html><html><head><style>" + css + "</style></head><body><div class='container'>" +
                imageTag +
                "<h1>" + name + "</h1>" +
                "<div class='contact-info'>" + email + " | " + phone + (address.isEmpty() ? "" : " | " + address) + (links.isEmpty() ? "" : " | <a href='" + links + "'>" + links + "</a>") + "</div>" +
                (objective.isEmpty() ? "" : "<h2>Objective</h2><p>" + objective + "</p>") +
                (about.isEmpty() ? "" : "<h2>About Me</h2><p>" + about + "</p>") +
                (introduction.isEmpty() ? "" : "<h2>Introduction</h2><p>" + introduction + "</p>") +
                "<h2>Professional Experience</h2><ul>" + experience + "</ul>" +
                "<h2>Education</h2><ul>" + education + "</ul>" +
                (certifications.isEmpty() ? "" : "<h2>Certifications</h2><ul>" + certifications + "</ul>") +
                "<h2>Skills</h2><ul>" + skills + "</ul>" +
                (languages.isEmpty() ? "" : "<h2>Languages</h2><ul>" + languages + "</ul>") +
                "</div></body></html>";
    }

    @Override
    public void generatePdfContent(Document document, String name, String email, String phone, String address, String links,
                                   String objective, String about, String introduction, String experience, String education, String certifications, String skills,
                                   String languages, Uri imageUri, Context context) throws DocumentException {
        BaseColor accentColor = new BaseColor(70, 70, 70);
        Font nameFont = new Font(Font.FontFamily.TIMES_ROMAN, 24, Font.BOLD);
        Font headingFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD, accentColor);
        Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 11);

        if (imageUri != null) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Image img = Image.getInstance(byteArray);
                img.scaleToFit(100, 100);
                img.setAlignment(Element.ALIGN_CENTER);
                document.add(img);
                inputStream.close();
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Paragraph namePara = new Paragraph(name, nameFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        document.add(namePara);

        Paragraph contactInfo = new Paragraph(email + " | " + phone + (address.isEmpty() ? "" : " | " + address) + (links.isEmpty() ? "" : " | " + links), normalFont);
        contactInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(contactInfo);
        document.add(new Paragraph(" "));

        if (!objective.isEmpty()) {
            PdfPTable objTable = new PdfPTable(1);
            objTable.setWidthPercentage(100);
            PdfPCell objHeader = new PdfPCell(new Phrase("OBJECTIVE", headingFont));
            objHeader.setBorderColor(accentColor);
            objHeader.setPadding(8);
            objHeader.setBorderWidthBottom(2f);
            objHeader.setBorderWidthTop(0);
            objHeader.setBorderWidthLeft(0);
            objHeader.setBorderWidthRight(0);
            objTable.addCell(objHeader);
            PdfPCell objContent = new PdfPCell(new Paragraph(objective, normalFont));
            objContent.setBorder(Rectangle.NO_BORDER);
            objContent.setPadding(5);
            objTable.addCell(objContent);
            document.add(objTable);
            document.add(new Paragraph(" "));
        }

        if (!about.isEmpty()) {
            PdfPTable aboutTable = new PdfPTable(1);
            aboutTable.setWidthPercentage(100);
            PdfPCell aboutHeader = new PdfPCell(new Phrase("ABOUT ME", headingFont));
            aboutHeader.setBorderColor(accentColor);
            aboutHeader.setPadding(8);
            aboutHeader.setBorderWidthBottom(2f);
            aboutHeader.setBorderWidthTop(0);
            aboutHeader.setBorderWidthLeft(0);
            aboutHeader.setBorderWidthRight(0);
            aboutTable.addCell(aboutHeader);
            PdfPCell aboutContent = new PdfPCell(new Paragraph(about, normalFont));
            aboutContent.setBorder(Rectangle.NO_BORDER);
            aboutContent.setPadding(5);
            aboutTable.addCell(aboutContent);
            document.add(aboutTable);
            document.add(new Paragraph(" "));
        }

        if (!introduction.isEmpty()) {
            PdfPTable introTable = new PdfPTable(1);
            introTable.setWidthPercentage(100);
            PdfPCell introHeader = new PdfPCell(new Phrase("INTRODUCTION", headingFont));
            introHeader.setBorderColor(accentColor);
            introHeader.setPadding(8);
            introHeader.setBorderWidthBottom(2f);
            introHeader.setBorderWidthTop(0);
            introHeader.setBorderWidthLeft(0);
            introHeader.setBorderWidthRight(0);
            introTable.addCell(introHeader);
            PdfPCell introContent = new PdfPCell(new Paragraph(introduction, normalFont));
            introContent.setBorder(Rectangle.NO_BORDER);
            introContent.setPadding(5);
            introTable.addCell(introContent);
            document.add(introTable);
            document.add(new Paragraph(" "));
        }

        addSection(document, "PROFESSIONAL EXPERIENCE", experience, headingFont, normalFont, accentColor);
        addSection(document, "EDUCATION", education, headingFont, normalFont, accentColor);
        if (!certifications.isEmpty()) addSection(document, "CERTIFICATIONS", certifications, headingFont, normalFont, accentColor);
        addSection(document, "SKILLS", skills, headingFont, normalFont, accentColor);
        if (!languages.isEmpty()) addSection(document, "LANGUAGES", languages, headingFont, normalFont, accentColor);
    }

    private void addSection(Document document, String title, String content, Font headingFont, Font normalFont, BaseColor accentColor) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell header = new PdfPCell(new Phrase(title, headingFont));
        header.setBorderColor(accentColor);
        header.setPadding(8);
        header.setBorderWidthBottom(2f);
        header.setBorderWidthTop(0);
        header.setBorderWidthLeft(0);
        header.setBorderWidthRight(0);
        table.addCell(header);
        PdfPCell contentCell = new PdfPCell();
        contentCell.setBorder(Rectangle.NO_BORDER);
        contentCell.setPadding(5);
        for (String item : parseTextToList(content)) {
            contentCell.addElement(new Paragraph("• " + item, normalFont));
        }
        table.addCell(contentCell);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private List<String> parseTextToList(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(text.split("\n")));
    }
}