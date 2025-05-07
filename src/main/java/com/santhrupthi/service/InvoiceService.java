package com.santhrupthi.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class InvoiceService {
    public byte[] generateInvoice(
        String donationId, Date donationDate, String name, String phone, String email, BigDecimal amount,
        String panCard, String address, String message, String currency, String paymentMethod, String orderId, String paymentId, String status
    ) throws DocumentException, java.io.IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Header
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph header = new Paragraph("Santhrupthi Foundation\nDonation Receipt", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            // Org details
            Font orgFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
            Paragraph orgDetails = new Paragraph("123 Charity Lane, Bengaluru, KA 560001\nPhone: +91 888 4666 653 | Email: info@santhrupthi.org", orgFont);
            orgDetails.setAlignment(Element.ALIGN_CENTER);
            orgDetails.setSpacingAfter(16);
            document.add(orgDetails);

            // Invoice meta
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(12);
            metaTable.addCell(getCell("Receipt #:", PdfPCell.ALIGN_LEFT, true));
            metaTable.addCell(getCell(donationId, PdfPCell.ALIGN_LEFT, false));
            metaTable.addCell(getCell("Date:", PdfPCell.ALIGN_LEFT, true));
            metaTable.addCell(getCell(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(donationDate), PdfPCell.ALIGN_LEFT, false));
            document.add(metaTable);

            // Donor & Donation details
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingAfter(12);
            detailsTable.addCell(getCell("Donor Details", PdfPCell.ALIGN_LEFT, true));
            detailsTable.addCell(getCell("Donation Details", PdfPCell.ALIGN_LEFT, true));
            detailsTable.addCell(getCell("Name: " + name, PdfPCell.ALIGN_LEFT, false));
            detailsTable.addCell(getCell("Order ID: " + orderId, PdfPCell.ALIGN_LEFT, false));
            detailsTable.addCell(getCell("Email: " + email, PdfPCell.ALIGN_LEFT, false));
            detailsTable.addCell(getCell("Payment ID: " + paymentId, PdfPCell.ALIGN_LEFT, false));
            detailsTable.addCell(getCell("Phone: " + phone, PdfPCell.ALIGN_LEFT, false));
            detailsTable.addCell(getCell("Status: " + status, PdfPCell.ALIGN_LEFT, false));
            detailsTable.addCell(getCell("PAN: " + (panCard != null ? panCard : "-"), PdfPCell.ALIGN_LEFT, false));
            detailsTable.addCell(getCell("Method: " + (paymentMethod != null ? paymentMethod : "-"), PdfPCell.ALIGN_LEFT, false));
            detailsTable.addCell(getCell("Address: " + (address != null ? address : "-"), PdfPCell.ALIGN_LEFT, false));
            detailsTable.addCell(getCell("Message: " + (message != null ? message : "-"), PdfPCell.ALIGN_LEFT, false));
            document.add(detailsTable);

            // Amount Table
            PdfPTable amountTable = new PdfPTable(2);
            amountTable.setWidthPercentage(60);
            amountTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            amountTable.addCell(getCell("Description", PdfPCell.ALIGN_LEFT, true));
            amountTable.addCell(getCell("Amount", PdfPCell.ALIGN_RIGHT, true));
            amountTable.addCell(getCell("Donation to Santhrupthi Foundation", PdfPCell.ALIGN_LEFT, false));
            amountTable.addCell(getCell("₹" + amount, PdfPCell.ALIGN_RIGHT, false));
            amountTable.addCell(getCell("Total", PdfPCell.ALIGN_LEFT, true));
            amountTable.addCell(getCell("₹" + amount, PdfPCell.ALIGN_RIGHT, true));
            document.add(amountTable);

            // Footer
            Paragraph thanks = new Paragraph("Thank you for your generous contribution! This receipt can be used for your records and tax purposes.", orgFont);
            thanks.setSpacingBefore(20);
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            document.close();
            return outputStream.toByteArray();
        }
    }

    public byte[] generateCombinedInvoiceForPhone(String phone, List<com.santhrupthi.model.Donations> donations) throws DocumentException, java.io.IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Header
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph header = new Paragraph("Santhrupthi Foundation\nDonation Invoice (All Donations)", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            // Org details
            Font orgFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
            Paragraph orgDetails = new Paragraph("123 Charity Lane, Bengaluru, KA 560001\nPhone: +91 888 4666 653 | Email: info@santhrupthi.org", orgFont);
            orgDetails.setAlignment(Element.ALIGN_CENTER);
            orgDetails.setSpacingAfter(16);
            document.add(orgDetails);

            // Donor meta
            if (!donations.isEmpty()) {
                com.santhrupthi.model.Donations first = donations.get(0);
                PdfPTable metaTable = new PdfPTable(2);
                metaTable.setWidthPercentage(100);
                metaTable.setSpacingAfter(12);
                metaTable.addCell(getCell("Name:", PdfPCell.ALIGN_LEFT, true));
                metaTable.addCell(getCell(first.getName(), PdfPCell.ALIGN_LEFT, false));
                metaTable.addCell(getCell("Phone:", PdfPCell.ALIGN_LEFT, true));
                metaTable.addCell(getCell(first.getPhone(), PdfPCell.ALIGN_LEFT, false));
                metaTable.addCell(getCell("Email:", PdfPCell.ALIGN_LEFT, true));
                metaTable.addCell(getCell(first.getEmail(), PdfPCell.ALIGN_LEFT, false));
                document.add(metaTable);
            }

            // Donations Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);
            table.addCell(getCell("Date", PdfPCell.ALIGN_LEFT, true));
            table.addCell(getCell("Donation ID", PdfPCell.ALIGN_LEFT, true));
            table.addCell(getCell("Amount", PdfPCell.ALIGN_RIGHT, true));
            table.addCell(getCell("Status", PdfPCell.ALIGN_LEFT, true));
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (com.santhrupthi.model.Donations d : donations) {
                table.addCell(getCell(sdf.format(d.getDonationDate()), PdfPCell.ALIGN_LEFT, false));
                table.addCell(getCell(d.getDonationId(), PdfPCell.ALIGN_LEFT, false));
                table.addCell(getCell("₹" + d.getAmount(), PdfPCell.ALIGN_RIGHT, false));
                table.addCell(getCell(d.getStatus(), PdfPCell.ALIGN_LEFT, false));
                if (d.getAmount() != null) total = total.add(d.getAmount());
            }
            // Total row
            table.addCell(getCell("", PdfPCell.ALIGN_LEFT, false));
            table.addCell(getCell("Total", PdfPCell.ALIGN_RIGHT, true));
            table.addCell(getCell("₹" + total, PdfPCell.ALIGN_RIGHT, true));
            table.addCell(getCell("", PdfPCell.ALIGN_LEFT, false));
            document.add(table);

            // Footer
            Paragraph thanks = new Paragraph("Thank you for your generous contributions! This invoice summarizes all your donations.", orgFont);
            thanks.setSpacingBefore(20);
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            document.close();
            return outputStream.toByteArray();
        }
    }

    private PdfPCell getCell(String text, int alignment, boolean bold) {
        Font font = new Font(Font.FontFamily.HELVETICA, 10, bold ? Font.BOLD : Font.NORMAL);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }
} 