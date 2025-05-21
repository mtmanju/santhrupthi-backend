package com.santhrupthi.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.itextpdf.text.pdf.PdfStream;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Service
public class CertificateService {
    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);
    private static final String BACKGROUND_IMAGE = "certificate-background.jpg";

    public byte[] generateCertificate(String donorName, double amount, String paymentId) throws DocumentException {
        logger.info("Starting certificate generation for donor: {}, amount: {}", donorName, amount);
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Create document in landscape mode with custom size (1200x675 to match UI)
            Rectangle pageSize = new Rectangle(1200, 675);
            Document document = new Document(pageSize, 0, 0, 0, 0); // Remove margins
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setCompressionLevel(PdfStream.BEST_COMPRESSION);
            document.open();

            // Add background image (img42.jpg)
            try {
                String imagePath = "images/" + BACKGROUND_IMAGE;
                logger.info("[DEBUG] Attempting to load background image from: {}", imagePath);
                InputStream imageStream = getClass().getClassLoader().getResourceAsStream(imagePath);
                if (imageStream == null) {
                    logger.error("[DEBUG] Background image NOT FOUND: {}", imagePath);
                    throw new Exception("Image not found: " + imagePath);
                } else {
                    logger.info("[DEBUG] Background image loaded successfully: {}", imagePath);
                }
                Image background = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(imageStream));
                background.setAbsolutePosition(0, 0);
                background.scaleAbsolute(pageSize.getWidth(), pageSize.getHeight());
                background.setCompressionLevel(9);
                document.add(background);
            } catch (Exception e) {
                logger.error("Failed to add background image: {}", e.getMessage());
                e.printStackTrace();
            }

            // Add semi-transparent black overlay
            PdfContentByte canvas = writer.getDirectContent();
            canvas.saveState();
            canvas.setColorFill(new BaseColor(0, 0, 0, 102)); // 40% opacity
            canvas.rectangle(0, 0, pageSize.getWidth(), pageSize.getHeight());
            canvas.fill();
            canvas.restoreState();

            // Draw green circle (success icon) at top center (REMOVED)
            float circleRadius = 72; // 144px diameter
            float circleCenterX = pageSize.getWidth() / 2;
            float circleMarginTop = 60; // margin from the top
            float circleCenterY = pageSize.getHeight() - circleRadius - circleMarginTop;
            // canvas.saveState();
            // canvas.setColorFill(new BaseColor(0, 200, 83)); // #00C853
            // canvas.circle(circleCenterX, circleCenterY, circleRadius);
            // canvas.fill();
            // canvas.restoreState();

            // Overlay tick.png image (centered at the top, scaled to 144px)
            try {
                String tickImagePath = "images/certificate-background-tick.png";
                InputStream tickImageStream = getClass().getClassLoader().getResourceAsStream(tickImagePath);
                if (tickImageStream == null) {
                    logger.error("[DEBUG] Tick image not found: {}", tickImagePath);
                } else {
                    logger.info("[DEBUG] Tick image loaded successfully: {}", tickImagePath);
                    byte[] imageBytes = org.apache.commons.io.IOUtils.toByteArray(tickImageStream);
                    Image tickImage = Image.getInstance(imageBytes);
                    // IMPORTANT: Use a tick PNG with a transparent background
                    tickImage.setTransparency(new int[]{0xFF, 0xFF}); // Make fully opaque
                    float tickWidth = 144;
                    float tickHeight = 144;
                    float tickX = circleCenterX - tickWidth / 2;
                    float tickY = circleCenterY - tickHeight / 2;
                    tickImage.scaleAbsolute(tickWidth, tickHeight);
                    tickImage.setAbsolutePosition(tickX, tickY);
                    tickImage.setCompressionLevel(9);
                    PdfContentByte directContent = writer.getDirectContent();
                    directContent.addImage(tickImage);
                }
            } catch (Exception e) {
                logger.error("Failed to add tick.png: {} - {}", e.getClass().getName(), e.getMessage());
            }

            // Main content (centered)
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.EMBEDDED);
            float contentCenterY = pageSize.getHeight() * 0.55f - 80; // Shift text down by 80px
            canvas.beginText();
            canvas.setFontAndSize(baseFont, 54);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_CENTER, "THANK YOU FOR YOUR DONATION!", circleCenterX, contentCenterY + 80, 0);
            // Donor name (large, bold, uppercase)
            canvas.setFontAndSize(baseFont, 48);
            canvas.showTextAligned(Element.ALIGN_CENTER, donorName != null ? donorName.toUpperCase() : "", circleCenterX, contentCenterY + 30, 0);
            canvas.setFontAndSize(baseFont, 36);
            canvas.showTextAligned(Element.ALIGN_CENTER, "YOU JUST FED A FAMILY. AND GAVE HOPE.", circleCenterX, contentCenterY - 20, 0);
            canvas.setFontAndSize(baseFont, 22);
            canvas.showTextAligned(Element.ALIGN_CENTER, "We will contact you with further instructions.", circleCenterX, contentCenterY - 60, 0);
            canvas.endText();

            // Footer bar (orange, rounded) -- REMOVED
            // Social icons and "Follow us on:"
            float socialStartX = 80;
            float socialStartY = 60; // Place near the bottom, adjust as needed
            float iconSize = 36;
            float iconSpacing = 44; // Adjusted for 5 icons
            String[] socialIcons = {
                "facebook.png",
                "instagram.png",
                "linkedin.png"
            };
            String[] socialUrls = {
                "https://facebook.com/groups/1531070753832737/",
                "https://www.instagram.com/awo_india",
                "https://www.linkedin.com/company/theawofoundation/"
            };
            // "Follow us on:" text
            canvas.beginText();
            canvas.setFontAndSize(baseFont, 22);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_LEFT, "Follow us on:", socialStartX, socialStartY + iconSize + 18, 0);
            canvas.endText();
            // Social icons (icons only, clickable)
            for (int i = 0; i < socialIcons.length; i++) {
                try {
                    String iconPath = "images/" + socialIcons[i];
                    logger.info("[DEBUG] Attempting to load social icon from: {}", iconPath);
                    InputStream iconStream = getClass().getClassLoader().getResourceAsStream(iconPath);
                    if (iconStream == null) {
                        logger.error("[DEBUG] Social icon NOT FOUND: {}", iconPath);
                        continue;
                    } else {
                        logger.info("[DEBUG] Social icon loaded successfully: {}", iconPath);
                    }
                    float iconX = socialStartX + 140 + i * iconSpacing;
                    // Load and configure the image
                    byte[] imageBytes = org.apache.commons.io.IOUtils.toByteArray(iconStream);
                    Image socialIcon = Image.getInstance(imageBytes);
                    // Configure image properties
                    socialIcon.setTransparency(new int[]{0xFF, 0xFF}); // Make fully opaque
                    socialIcon.scaleAbsolute(iconSize, iconSize);
                    socialIcon.setAbsolutePosition(iconX, socialStartY);
                    socialIcon.setCompressionLevel(9);
                    // Force the image to be rendered
                    PdfContentByte directContent = writer.getDirectContent();
                    directContent.addImage(socialIcon);
                    // Add clickable link annotation for the icon
                    PdfAnnotation link = PdfAnnotation.createLink(writer, 
                        new Rectangle(iconX, socialStartY, iconX + iconSize, socialStartY + iconSize),
                        PdfAnnotation.HIGHLIGHT_INVERT,
                        new PdfAction(socialUrls[i]));
                    writer.addAnnotation(link);
                } catch (Exception e) {
                    logger.error("Failed to add social icon {}: {} - Stack trace: ", 
                        socialIcons[i], e.getMessage(), e);
                }
            }

            // Need Help? text and phone number (no black pill)
            float helpTextX = pageSize.getWidth() - 400;
            float helpTextY = socialStartY + iconSize + 18;
            canvas.beginText();
            canvas.setFontAndSize(baseFont, 18);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_LEFT, "\u260E  Need Help?", helpTextX, helpTextY, 0);
            canvas.setFontAndSize(baseFont, 18);
            canvas.showTextAligned(Element.ALIGN_LEFT, "+91 888 4666 653", helpTextX + 150, helpTextY, 0);
            canvas.endText();

            document.close();
            logger.info("Certificate generated successfully for payment ID: {}", paymentId);
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Failed to generate certificate: {}", e.getMessage(), e);
            throw new DocumentException("Failed to generate certificate: " + e.getMessage());
        }
    }
}