package com.santhrupthi.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Service
public class CertificateService {
    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);
    private static final String BACKGROUND_IMAGE = "img887.jpg";

    public byte[] generateCertificate(String donorName, double amount, String paymentId) throws DocumentException {
        logger.info("Starting certificate generation for donor: {}, amount: {}", donorName, amount);
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Create document in landscape mode with custom size (1200x675 to match UI)
            Rectangle pageSize = new Rectangle(1200, 675);
            Document document = new Document(pageSize, 0, 0, 0, 0); // Remove margins
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add background image
            try {
                String imagePath = "/static/images/" + BACKGROUND_IMAGE;
                logger.info("Loading background image from: {}", imagePath);
                
                InputStream imageStream = getClass().getResourceAsStream(imagePath);
                if (imageStream == null) {
                    throw new Exception("Image not found: " + imagePath);
                }
                
                Image background = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(imageStream));
                background.setAbsolutePosition(0, 0);
                background.scaleAbsolute(pageSize.getWidth(), pageSize.getHeight());
                document.add(background);
            } catch (Exception e) {
                logger.error("Failed to add background image: {}", e.getMessage());
                e.printStackTrace();
            }

            PdfContentByte canvas = writer.getDirectContent();
            float centerX = pageSize.getWidth() / 2;
            float centerY = pageSize.getHeight() * 0.62f;
            
            // Draw green circle (smaller and higher)
            float circleRadius = 90; // smaller
            float circleCenterY = pageSize.getHeight() * 0.62f + 40; // move up
            canvas.saveState();
            canvas.setColorFill(new BaseColor(34, 197, 94)); // #22c55e
            canvas.circle(centerX, circleCenterY, circleRadius);
            canvas.fill();
            canvas.restoreState();

            // Use Helvetica (or Bahnschrift if available)
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
            
            // Main title
            canvas.beginText();
            canvas.setFontAndSize(baseFont, 54); // Large title font
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_CENTER, "THANK YOU FOR YOUR DONATION!", centerX, centerY - 120, 0);
            // Donor name (large, bold, uppercase)
            canvas.setFontAndSize(baseFont, 64); // Even larger for donor name
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_CENTER, donorName != null ? donorName.toUpperCase() : "", centerX, centerY - 170, 0);
            // Subtitle (single line, smaller)
            canvas.setFontAndSize(baseFont, 36); // Subtitle font size
            canvas.showTextAligned(Element.ALIGN_CENTER, "YOU JUST FED A FAMILY. AND GAVE HOPE.", centerX, centerY - 220, 0);
            // Instructions
            canvas.setFontAndSize(baseFont, 22); // Instructions font size
            canvas.showTextAligned(Element.ALIGN_CENTER, "We will contact you with further instructions.", centerX, centerY - 260, 0);
            canvas.endText();

            // Remove pill bars. Add social icons and contact at bottom corners.
            // Socials at bottom right (flush to right edge)
            float socialStartX = pageSize.getWidth() - 48;
            float socialStartY = 48;
            float iconRadius = 18;
            float iconSpacingY = 54;
            String[] socialNames = {"Facebook", "Instagram", "LinkedIn"};
            String[] socialUrls = {
                "https://facebook.com/groups/1531070753832737/",
                "https://www.instagram.com/awo_india",
                "https://www.linkedin.com/company/theawofoundation/"
            };
            for (int i = 0; i < socialNames.length; i++) {
                // Draw icon circle (placeholder)
                canvas.saveState();
                canvas.setColorFill(BaseColor.WHITE);
                canvas.circle(socialStartX - iconRadius, socialStartY + i * iconSpacingY, iconRadius);
                canvas.fill();
                canvas.restoreState();
                // Draw URL text, right-aligned
                canvas.beginText();
                canvas.setFontAndSize(baseFont, 18);
                canvas.setColorFill(BaseColor.WHITE);
                canvas.showTextAligned(Element.ALIGN_RIGHT, socialNames[i], socialStartX - iconRadius - 24, socialStartY + i * iconSpacingY + 6, 0);
                canvas.endText();
                // Add clickable link annotation
                float textWidth = baseFont.getWidthPoint(socialNames[i], 18);
                float llx = socialStartX - iconRadius - 24 - textWidth;
                float lly = socialStartY + i * iconSpacingY;
                float urx = socialStartX - iconRadius - 24;
                float ury = socialStartY + i * iconSpacingY + 18;
                PdfAnnotation link = PdfAnnotation.createLink(writer, new Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_INVERT, new PdfAction(socialUrls[i]));
                writer.addAnnotation(link);
            }

            // Need Help at bottom left
            float helpX = 48;
            float helpY = 48;
            canvas.beginText();
            canvas.setFontAndSize(baseFont, 22);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_LEFT, "Need Help?", helpX, helpY + 32, 0);
            canvas.setFontAndSize(baseFont, 22);
            canvas.setColorFill(new BaseColor(255, 106, 0)); // #ff6a00
            canvas.showTextAligned(Element.ALIGN_LEFT, "+91 888 4666 653", helpX, helpY, 0);
            canvas.endText();

            // Overlay tick.png image LAST so it is always on top
            try {
                String tickImagePath = "/static/images/tick.png";
                logger.info("Loading tick image from: {} (drawn last)", tickImagePath);
                InputStream tickImageStream = getClass().getResourceAsStream(tickImagePath);
                if (tickImageStream == null) {
                    logger.error("Tick image stream is null. Image not found at path: {}", tickImagePath);
                    throw new Exception("Tick image not found: " + tickImagePath);
                }
                byte[] imageBytes = org.apache.commons.io.IOUtils.toByteArray(tickImageStream);
                Image tickImage = Image.getInstance(imageBytes);
                // Position the tick image at the center of the green circle
                float tickWidth = 80;
                float tickHeight = 80;
                tickImage.scaleAbsolute(tickWidth, tickHeight);
                tickImage.setAbsolutePosition(centerX - tickWidth / 2, circleCenterY - tickHeight / 2);
                document.add(tickImage);
                logger.info("Successfully added tick.png to document at ({}, {}) (drawn last)", centerX - tickWidth / 2, circleCenterY - tickHeight / 2);
            } catch (Exception e) {
                logger.error("Failed to add tick.png: {} - {}", e.getClass().getName(), e.getMessage());
                e.printStackTrace();
            }

            document.close();
            logger.info("Certificate generated successfully for payment ID: {}", paymentId);
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Failed to generate certificate: {}", e.getMessage(), e);
            throw new DocumentException("Failed to generate certificate: " + e.getMessage());
        }
    }
}