package com.santhrupthi.service;

import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Service
public class InvoiceService {
    public byte[] generateInvoice(
        String donationId, Date donationDate, String name, String phone, String email, BigDecimal amount,
        String panCard, String address, String message, String currency, String paymentMethod, String orderId, String paymentId, String status
    ) throws java.io.IOException {
        String html = loadInvoiceHtmlTemplate();
        String addressHtml = (address != null && !address.trim().isEmpty()) ? "<div><span style=\"font-weight:700\">Address:</span> " + address + "</div>" : "";
        String panHtml = (panCard != null && !panCard.trim().isEmpty()) ? "<div><span style=\"font-weight:700\">PAN:</span> " + panCard + "</div>" : "";
        html = html.replace("${donorName}", name != null ? name : "-")
                   .replace("${email}", email != null ? email : "-")
                   .replace("${phone}", phone != null ? phone : "-")
                   .replace("${addressBlock}", addressHtml)
                   .replace("${panBlock}", panHtml)
                   .replace("${orderId}", orderId != null ? orderId : "-")
                   .replace("${paymentId}", paymentId != null ? paymentId : "-")
                   .replace("${status}", status != null ? status : "-")
                   .replace("${paymentMethod}", paymentMethod != null ? paymentMethod : "-")
                   .replace("${message}", message != null ? message : "-")
                   .replace("${donationId}", donationId != null ? donationId : "-")
                   .replace("${donationDate}", donationDate != null ? new SimpleDateFormat("dd/MM/yyyy, hh:mm:ss a").format(donationDate) : "-")
                   .replace("${amount}", amount != null ? amount.toString() : "0");
        return renderHtmlToPdf(html);
    }

    private String[] loadInvoiceTemplateParts() throws java.io.IOException {
        String template = loadInvoiceHtmlTemplate();
        int headStart = template.indexOf("<head>");
        int headEnd = template.indexOf("</head>") + 7;
        String head = template.substring(headStart, headEnd);
        String body = template.substring(template.indexOf("<body>"), template.indexOf("<!--INVOICE_START-->") + "<!--INVOICE_START-->".length());
        String invoiceContent = template.substring(template.indexOf("<!--INVOICE_START-->") + "<!--INVOICE_START-->".length(), template.indexOf("<!--INVOICE_END-->"));
        return new String[] { head, body, invoiceContent };
    }

    public byte[] generateCombinedInvoiceForPhone(String phone, List<com.santhrupthi.model.Donations> donations) throws java.io.IOException {
        String[] templateParts = loadInvoiceTemplateParts();
        String head = templateParts[0];
        String invoiceContent = templateParts[2];
        StringBuilder bodyBuilder = new StringBuilder();
        for (int i = 0; i < donations.size(); i++) {
            com.santhrupthi.model.Donations d = donations.get(i);
            String html = invoiceContent;
            String addressHtml = (d.getAddress() != null && !d.getAddress().trim().isEmpty()) ? "<div><span style=\"font-weight:700\">Address:</span> " + d.getAddress() + "</div>" : "";
            String panHtml = (d.getPanCard() != null && !d.getPanCard().trim().isEmpty()) ? "<div><span style=\"font-weight:700\">PAN:</span> " + d.getPanCard() + "</div>" : "";
            html = html.replace("${donorName}", d.getName() != null ? d.getName() : "-")
                       .replace("${email}", d.getEmail() != null ? d.getEmail() : "-")
                       .replace("${phone}", d.getPhone() != null ? d.getPhone() : "-")
                       .replace("${addressBlock}", addressHtml)
                       .replace("${panBlock}", panHtml)
                       .replace("${orderId}", d.getOrderId() != null ? d.getOrderId() : "-")
                       .replace("${paymentId}", d.getPaymentId() != null ? d.getPaymentId() : "-")
                       .replace("${status}", d.getStatus() != null ? d.getStatus() : "-")
                       .replace("${paymentMethod}", d.getPaymentMethod() != null ? d.getPaymentMethod() : "-")
                       .replace("${message}", d.getMessage() != null ? d.getMessage() : "-")
                       .replace("${donationId}", d.getDonationId() != null ? d.getDonationId() : "-")
                       .replace("${donationDate}", d.getDonationDate() != null ? new SimpleDateFormat("dd/MM/yyyy, hh:mm:ss a").format(d.getDonationDate()) : "-")
                       .replace("${amount}", d.getAmount() != null ? d.getAmount().toString() : "0");
            bodyBuilder.append(html);
            if (i < donations.size() - 1) {
                bodyBuilder.append("<div style='page-break-after: always'></div>");
            }
        }
        String combinedHtml = "<!DOCTYPE html><html>" + head + "<body>" + bodyBuilder.toString() + "</body></html>";
        return renderHtmlToPdf(combinedHtml);
    }

    private String loadInvoiceHtmlTemplate() throws java.io.IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("templates/invoice-template.html")) {
            if (is == null) throw new RuntimeException("invoice-template.html not found in classpath");
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private byte[] renderHtmlToPdf(String html) throws java.io.IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            String baseUri = getClass().getResource("/templates/").toString();
            builder.withHtmlContent(html, baseUri);
            builder.toStream(os);
            builder.useDefaultPageSize(210, 297, PdfRendererBuilder.PageSizeUnits.MM);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to render invoice PDF: " + e.getMessage(), e);
        }
    }
} 