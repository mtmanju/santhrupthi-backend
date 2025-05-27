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

    public byte[] generateCertificate(String donorName, double amount, String paymentId) throws DocumentException {
        logger.info("Starting certificate generation for donor: {}, amount: {}", donorName, amount);
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Rectangle pageSize = new Rectangle(1280, 721);
            Document document = new Document(pageSize, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setCompressionLevel(PdfStream.BEST_COMPRESSION);
            document.open();

            // --- Background image ---
            try {
                String imagePath = "images/certificate-background.jpg";
                InputStream imageStream = getClass().getClassLoader().getResourceAsStream(imagePath);
                if (imageStream == null) throw new Exception("Image not found: " + imagePath);
                Image background = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(imageStream));
                background.setAbsolutePosition(0, 0);
                background.scaleAbsolute(pageSize.getWidth(), pageSize.getHeight());
                background.setCompressionLevel(9);
                document.add(background);
            } catch (Exception e) {
                logger.error("Failed to add background image: {}", e.getMessage());
            }

            // --- Orange gradient overlay (simulate with semi-transparent orange rectangle) ---
            PdfContentByte canvas = writer.getDirectContent();
            canvas.saveState();
            canvas.setColorFill(new BaseColor(255, 120, 0, 90)); // orange, alpha
            canvas.rectangle(0, 0, pageSize.getWidth(), pageSize.getHeight());
            canvas.fill();
            canvas.restoreState();

            // --- Load Bahnschrift font (fallback to Helvetica if not found) ---
            BaseFont bahnFont;
            try {
                InputStream fontStream = getClass().getClassLoader().getResourceAsStream("images/Bahnschrift.ttf");
                if (fontStream != null) {
                    java.nio.file.Path tempFont = java.nio.file.Files.createTempFile("bahnschrift", ".ttf");
                    java.nio.file.Files.copy(fontStream, tempFont, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    bahnFont = BaseFont.createFont(tempFont.toAbsolutePath().toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                } else {
                    logger.warn("Bahnschrift font not found, using Helvetica for all text.");
                    bahnFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.EMBEDDED);
                }
            } catch (Exception e) {
                logger.warn("Could not load Bahnschrift font, using Helvetica for all text: {}", e.getMessage());
                bahnFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.EMBEDDED);
            }

            // --- Layout to match template exactly ---
            float centerX = pageSize.getWidth() / 2f;
            float y = pageSize.getHeight() - 60; // Start much higher

            // --- Tick icon at top center ---
            float tickWidth = 120, tickHeight = 120;
            float tickX = (pageSize.getWidth() - tickWidth) / 2;
            float tickY = y - tickHeight; // Place tick at the top
            try {
                String tickImagePath = "images/certificate-background-tick.png";
                InputStream tickImageStream = getClass().getClassLoader().getResourceAsStream(tickImagePath);
                if (tickImageStream != null) {
                    Image tickImage = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(tickImageStream));
                    tickImage.scaleAbsolute(tickWidth, tickHeight);
                    tickImage.setAbsolutePosition(tickX, tickY);
                    tickImage.setCompressionLevel(9);
                    canvas.addImage(tickImage);
                }
            } catch (Exception e) {
                logger.error("Failed to add tick.png: {} - {}", e.getClass().getName(), e.getMessage());
            }
            y = tickY - 24; // Add a clear gap below tick

            // --- THANK YOU ---
            canvas.beginText();
            canvas.setFontAndSize(bahnFont, 64);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_CENTER, "THANK YOU", centerX, y, 0);
            canvas.endText();
            y -= 54; // Small gap below 'THANK YOU'

            // --- Red bar (with donor name inside) ---
            float barWidth = pageSize.getWidth() * 0.7f;
            float barHeight = 70;
            float barX = (pageSize.getWidth() - barWidth) / 2f;
            float barY = y - barHeight - 18; // 18px gap below 'THANK YOU'
            canvas.saveState();
            canvas.setColorFill(new BaseColor(183, 28, 28)); // #B71C1C
            canvas.roundRectangle(barX, barY, barWidth, barHeight, 35);
            canvas.fill();
            canvas.restoreState();
            // Donor name text centered in bar
            canvas.beginText();
            canvas.setFontAndSize(bahnFont, 48);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_CENTER, donorName != null ? donorName.toUpperCase() : "YOUR NAME HERE", centerX, barY + barHeight / 2, 0);
            canvas.endText();
            y = barY - 36; // Small gap below red bar

            // --- FOR YOUR DONATION! ---
            canvas.beginText();
            canvas.setFontAndSize(bahnFont, 44);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_CENTER, "FOR YOUR DONATION!", centerX, y, 0);
            canvas.endText();
            y -= 48;

            // --- Appreciation text (yellow, two lines) ---
            canvas.beginText();
            canvas.setFontAndSize(bahnFont, 32);
            canvas.setColorFill(new BaseColor(255, 221, 51)); // yellow
            canvas.showTextAligned(Element.ALIGN_CENTER, "THE ARMS WIDE OPEN FOUNDATION", centerX, y, 0);
            canvas.endText();
            y -= 36;
            canvas.beginText();
            canvas.setFontAndSize(bahnFont, 32);
            canvas.setColorFill(new BaseColor(255, 221, 51));
            canvas.showTextAligned(Element.ALIGN_CENTER, "APPRECIATES YOUR DONATION.", centerX, y, 0);
            canvas.endText();
            y -= 50;

            // --- Impact text with orange underline ---
            String impact = "YOU JUST FED A FAMILY. AND GAVE HOPE.";
            canvas.beginText();
            canvas.setFontAndSize(bahnFont, 32);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_CENTER, impact, centerX, y, 0);
            canvas.endText();
            float impactTextWidth = bahnFont.getWidthPoint(impact, 32);
            canvas.saveState();
            canvas.setColorFill(new BaseColor(255, 72, 0)); // #ff4800
            canvas.rectangle(centerX - impactTextWidth / 2, y - 10, impactTextWidth, 5);
            canvas.fill();
            canvas.restoreState();
            y -= 80;

            // --- Footer row (centered, one line, no overlap with logos) ---
            float footerY = 32;
            float footerHeight = 80;
            // Santhrupthi logo background (white rounded rect)
            float logoW = 180, logoH = 70;
            float logoRectX = 32, logoRectY = footerY;
            canvas.saveState();
            canvas.setColorFill(BaseColor.WHITE);
            canvas.roundRectangle(logoRectX, logoRectY, logoW, logoH, 35);
            canvas.fill();
            canvas.restoreState();
            // AWO logo background (white circle)
            float awoSize = 90;
            float awoCircleX = pageSize.getWidth() - 90;
            float awoCircleY = footerY + footerHeight / 2;
            canvas.saveState();
            canvas.setColorFill(BaseColor.WHITE);
            canvas.circle(awoCircleX, awoCircleY, awoSize / 2 + 8);
            canvas.fill();
            canvas.restoreState();
            // Extended orange pill (contains both sections)
            float pillW = 760, pillH = 54;
            float pillX = (pageSize.getWidth() - pillW) / 2f;
            float pillY = footerY + (footerHeight - pillH) / 2f;
            canvas.saveState();
            canvas.setColorFill(new BaseColor(255, 72, 0)); // #ff4800
            canvas.roundRectangle(pillX, pillY, pillW, pillH, 27);
            canvas.fill();
            canvas.restoreState();
            // 'Follow us on:' label (left of icons, inside pill)
            canvas.beginText();
            canvas.setFontAndSize(bahnFont, 20);
            canvas.setColorFill(BaseColor.WHITE);
            float labelX = pillX + 32;
            float labelY = pillY + (pillH / 2) - 7;
            canvas.showTextAligned(Element.ALIGN_LEFT, "Follow us on:", labelX, labelY, 0);
            canvas.endText();
            // Social icons (inside pill)
            String[] socialIcons = {"facebook.png", "instagram.png", "linkedin.png"};
            float iconW = 36, iconH = 36, iconGap = 18;
            float iconsStartX = labelX + 120;
            float iconY = pillY + (pillH - iconH) / 2;
            for (int i = 0; i < socialIcons.length; i++) {
                try {
                    String iconPath = "images/" + socialIcons[i];
                    InputStream iconStream = getClass().getClassLoader().getResourceAsStream(iconPath);
                    float px = iconsStartX + i * (iconW + iconGap);
                    if (iconStream != null) {
                        Image icon = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(iconStream));
                        icon.scaleAbsolute(iconW, iconH);
                        icon.setAbsolutePosition(px, iconY);
                        canvas.addImage(icon);
                    }
                } catch (Exception e) {
                    logger.error("Failed to add social icon {}: {}", socialIcons[i], e.getMessage());
                }
            }
            // Need Help (phone icon + text), inside pill, right side
            float phoneIconX = pillX + pillW - 260;
            float phoneIconY = pillY + (pillH - 28) / 2;
            try {
                String phoneIconPath = "images/phone.png";
                InputStream phoneIconStream = getClass().getClassLoader().getResourceAsStream(phoneIconPath);
                if (phoneIconStream != null) {
                    Image phoneIcon = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(phoneIconStream));
                    phoneIcon.scaleAbsolute(28, 28);
                    phoneIcon.setAbsolutePosition(phoneIconX, phoneIconY);
                    canvas.addImage(phoneIcon);
                }
            } catch (Exception e) {
                logger.error("Failed to add phone icon: {}", e.getMessage());
            }
            // Help text (inside pill, right of phone icon)
            canvas.beginText();
            canvas.setFontAndSize(bahnFont, 20);
            canvas.setColorFill(BaseColor.WHITE);
            float helpTextX = phoneIconX + 36;
            float helpTextY = pillY + (pillH / 2) - 7;
            canvas.showTextAligned(Element.ALIGN_LEFT, "Need Help?  +91 888 4666 653", helpTextX, helpTextY, 0);
            canvas.endText();
            // Santhrupthi logo (left, on top of white rounded rect)
            try {
                String logoPath = "images/Logo.png";
                InputStream logoStream = getClass().getClassLoader().getResourceAsStream(logoPath);
                if (logoStream != null) {
                    float logoImgW = logoW - 24, logoImgH = logoH - 18;
                    float logoImgX = logoRectX + (logoW - logoImgW) / 2, logoImgY = logoRectY + (logoH - logoImgH) / 2;
                    Image logo = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(logoStream));
                    logo.scaleAbsolute(logoImgW, logoImgH);
                    logo.setAbsolutePosition(logoImgX, logoImgY);
                    canvas.addImage(logo);
                }
            } catch (Exception e) {
                logger.error("Failed to add Santhrupthi logo: {}", e.getMessage());
            }
            // AWO logo (right, on top of white circle)
            try {
                String awoPath = "images/AWO-Logo-01.png";
                InputStream awoStream = getClass().getClassLoader().getResourceAsStream(awoPath);
                float px = awoCircleX - awoSize / 2, py = awoCircleY - awoSize / 2;
                if (awoStream != null) {
                    Image awo = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(awoStream));
                    awo.scaleAbsolute(awoSize, awoSize);
                    awo.setAbsolutePosition(px, py);
                    canvas.addImage(awo);
                }
            } catch (Exception e) {
                logger.error("Failed to add AWO logo: {}", e.getMessage());
            }

            // --- Social handles row below the pill (centered, as in UI) ---
            float socialRowY = footerY + 8; // Place just above the bottom
            float socialFontSize = 18;
            float iconSize = 18;
            // Facebook handle
            try {
                String fbIconPath = "images/facebook.png";
                InputStream fbIconStream = getClass().getClassLoader().getResourceAsStream(fbIconPath);
                if (fbIconStream != null) {
                    Image fbIcon = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(fbIconStream));
                    fbIcon.scaleAbsolute(iconSize, iconSize);
                    float fbIconX = centerX - 90;
                    float fbIconY = socialRowY;
                    fbIcon.setAbsolutePosition(fbIconX, fbIconY);
                    canvas.addImage(fbIcon);
                }
            } catch (Exception e) { logger.error("Failed to add Facebook icon: {}", e.getMessage()); }
            canvas.beginText();
            canvas.setFontAndSize(bahnFont, socialFontSize);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_LEFT, "AWO (Arms Wide Open)", centerX - 65, socialRowY + 2, 0);
            canvas.endText();
            // Instagram handle
            try {
                String instaIconPath = "images/instagram.png";
                InputStream instaIconStream = getClass().getClassLoader().getResourceAsStream(instaIconPath);
                if (instaIconStream != null) {
                    Image instaIcon = Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(instaIconStream));
                    instaIcon.scaleAbsolute(iconSize, iconSize);
                    float instaIconX = centerX + 110;
                    float instaIconY = socialRowY;
                    instaIcon.setAbsolutePosition(instaIconX, instaIconY);
                    canvas.addImage(instaIcon);
                }
            } catch (Exception e) { logger.error("Failed to add Instagram icon: {}", e.getMessage()); }
            canvas.beginText();
            canvas.setFontAndSize(bahnFont, socialFontSize);
            canvas.setColorFill(BaseColor.WHITE);
            canvas.showTextAligned(Element.ALIGN_LEFT, "@awo_india", centerX + 135, socialRowY + 2, 0);
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