package com.santhrupthi.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.itextpdf.text.pdf.PdfStream;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Service
public class CertificateService {
    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);

    public byte[] generateCertificate(String donorName, double amount, String paymentId) {
        logger.info("Starting certificate generation for donor: {}, amount: {}", donorName, amount);
        try {
            // Load HTML template as stream (works in JAR and dev)
            InputStream is = getClass().getClassLoader().getResourceAsStream("templates/certificate-template.html");
            if (is == null) throw new RuntimeException("certificate-template.html not found in classpath");
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Replace placeholders
            html = html.replace("${donorName}", donorName != null ? donorName : "YOUR NAME HERE");

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            // Use classpath root as base URI for images
            String baseUri = getClass().getResource("/").toString();
            builder.withHtmlContent(html, baseUri);
            builder.useDefaultPageSize(361, 325, PdfRendererBuilder.PageSizeUnits.MM);
            builder.toStream(os);
            builder.run();
            logger.info("Certificate generated successfully for payment ID: {}", paymentId);
            return os.toByteArray();
        } catch (Exception e) {
            logger.error("Failed to generate certificate: {}", e.getMessage(), e);
            return null;
        }
    }
}