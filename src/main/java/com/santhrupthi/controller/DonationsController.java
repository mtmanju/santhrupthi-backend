package com.santhrupthi.controller;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.santhrupthi.dto.DonationsRequest;
import com.santhrupthi.dto.DonationsResponse;
import com.santhrupthi.model.Donations;
import com.santhrupthi.dto.DonationStatsDTO;
import com.santhrupthi.repository.DonationsRepository;
import com.santhrupthi.service.CertificateService;
import com.santhrupthi.service.InvoiceService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import com.santhrupthi.dto.DonationDetailsDTO;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@RestController
@RequestMapping("/api/donations")
public class DonationsController {
    private static final Logger logger = LoggerFactory.getLogger(DonationsController.class);

    private final CertificateService certificateService;
    private final DonationsRepository donationRepository;
    private final InvoiceService invoiceService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${donation.targetDonors:15000}")
    private long targetDonors;

    @Value("${donation.targetAmount:1500000}")
    private BigDecimal targetAmount;

    @PersistenceContext
    private EntityManager entityManager;

    public DonationsController(CertificateService certificateService, DonationsRepository donationRepository, InvoiceService invoiceService) {
        this.certificateService = certificateService;
        this.donationRepository = donationRepository;
        this.invoiceService = invoiceService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<DonationsResponse> createOrder(@RequestBody DonationsRequest request) {
        try {
            // 1. Get next sequence value for donations_id_seq
            Long nextId = ((Number) entityManager.createNativeQuery("SELECT nextval('public.donations_id_seq')").getSingleResult()).longValue();
            String donationIdSeq = String.format("SANT-%05d", nextId);

            // Join firstName and lastName with a space for the name
            String fullName = (request.getFirstName() != null ? request.getFirstName() : "") +
                (request.getLastName() != null && !request.getLastName().isEmpty() ? " " + request.getLastName() : "");

            // 2. Save Donation entity with donation_id set
            Donations donation = Donations.builder()
                .donationId(donationIdSeq)
                .name(fullName.trim())
                .email(request.getEmail())
                .phone(request.getPhone())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .panCard(request.getPanCard())
                .address(request.getAddress())
                .message(request.getMessage())
                .status("CREATED")
                .donationDate(new Date())
                .build();
            donation = donationRepository.save(donation);

            // 3. Create Razorpay order with this donationId
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // paise
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", donationIdSeq);
            orderRequest.put("notes", new JSONObject()
                .put("name", request.getName())
                .put("email", request.getEmail())
                .put("phone", request.getPhone())
                .put("panCard", request.getPanCard())
                .put("address", request.getAddress())
                .put("message", request.getMessage())
            );

            Order order = razorpay.orders.create(orderRequest);
            logger.info("Created Razorpay order with ID: " + order.get("id"));

            // Set the orderId field and save again
            donation.setOrderId(order.get("id"));
            donation = donationRepository.save(donation);

            DonationsResponse response = new DonationsResponse();
            response.setDonationId(donationIdSeq);
            response.setName(fullName.trim());
            response.setEmail(request.getEmail());
            response.setPhone(request.getPhone());
            response.setAmount(request.getAmount());
            response.setCurrency(request.getCurrency());
            response.setStatus("CREATED");
            response.setCreatedBy("system");
            response.setModifiedBy("system");
            response.setOrderId(order.get("id"));
            response.setRazorpayKey(razorpayKeyId);
            // No payment info yet

            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            logger.error("Error creating Razorpay order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestBody Map<String, String> paymentData) {
        try {
            String donationId = paymentData.get("razorpay_order_id");
            String razorpayPaymentId = paymentData.get("razorpay_payment_id");
            String razorpaySignature = paymentData.get("razorpay_signature");

            logger.info("/verify-payment called with: order_id={}, payment_id={}, signature={}", donationId, razorpayPaymentId, razorpaySignature);

            if (donationId == null || razorpayPaymentId == null || razorpaySignature == null) {
                String errorMsg = String.format(
                    "Missing required payment data. OrderId: %s, PaymentId: %s, Signature: %s",
                    donationId != null ? "present" : "missing",
                    razorpayPaymentId != null ? "present" : "missing",
                    razorpaySignature != null ? "present" : "missing"
                );
                logger.error(errorMsg + " Full payload: " + paymentData);
                return ResponseEntity.badRequest().body(null);
            }

            logger.info("Verifying payment - Order ID: {} Payment ID: {}", donationId, razorpayPaymentId);

            // Signature verification
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", donationId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);
            boolean isValidSignature = Utils.verifyPaymentSignature(options, razorpayKeySecret);
            logger.info("Signature verification result: {}", isValidSignature);
            if (!isValidSignature) {
                String errorMsg = "Invalid payment signature.";
                logger.error(errorMsg + " Verification options: " + options.toString());
                return ResponseEntity.badRequest().body(null);
            }

            // Fetch payment details from Razorpay
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            Payment razorpayPayment = razorpay.payments.fetch(razorpayPaymentId);
            String paymentStatus = razorpayPayment.get("status");
            logger.info("Fetched payment status from Razorpay: {}", paymentStatus);
            if (!"captured".equals(paymentStatus)) {
                String errorMsg = String.format("Payment not captured. Current status: %s.", paymentStatus);
                logger.error(errorMsg);
                return ResponseEntity.badRequest().body(null);
            }

            // Update donation entity
            Optional<Donations> donationOpt = donationRepository.findByOrderId(donationId);
            logger.info("Result of findByOrderId({}): present? {}", donationId, donationOpt.isPresent());
            if (donationOpt.isEmpty()) {
                logger.error("Donation not found for orderId: " + donationId);
                return ResponseEntity.badRequest().body(null);
            }
            Donations donation = donationOpt.get();
            donation.setStatus("SUCCESS");
            donation.setPaymentMethod(razorpayPayment.get("method") != null ? razorpayPayment.get("method").toString() : null);
            donation.setSignature(razorpaySignature);
            donation.setDonationDate(new Date());
            donation.setPaymentId(razorpayPaymentId);
            donation.setOrderId(donationId);
            donationRepository.save(donation);

            logger.info("Donation payment verified and updated for orderId: {}", donationId);
            // Return JSON with donationId and paymentId
            Map<String, String> result = new HashMap<>();
            result.put("donationId", donation.getDonationId());
            result.put("paymentId", razorpayPaymentId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            String errorMsg = "Error verifying payment: " + e.getMessage();
            logger.error(errorMsg, e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/download-certificate/{donationId}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable String donationId) {
        try {
            Optional<Donations> donationOpt = donationRepository.findByDonationId(donationId);
            if (donationOpt.isEmpty()) {
                logger.error("Could not find donation for ID: " + donationId);
                return ResponseEntity.notFound().build();
            }
            Donations donation = donationOpt.get();
            if (donation.getName() == null || donation.getAmount() == null) {
                logger.error("Invalid donation details - Name: {}, Amount: {}", donation.getName(), donation.getAmount());
                return ResponseEntity.badRequest().build();
            }
            byte[] certificateBytes = certificateService.generateCertificate(
                donation.getName(),
                donation.getAmount().doubleValue(),
                donationId
            );
            if (certificateBytes == null || certificateBytes.length == 0) {
                logger.error("Generated certificate is empty for donation ID: {}", donationId);
                return ResponseEntity.internalServerError().build();
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "donation-certificate.pdf");
            logger.info("Certificate generated successfully for donation ID: {}", donationId);
            return new ResponseEntity<>(certificateBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating certificate for donation {}: {}", donationId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/details/{donationId}")
    @ResponseBody
    public ResponseEntity<DonationDetailsDTO> getDonationDetails(@PathVariable String donationId) {
        Optional<Donations> donationOpt = donationRepository.findByDonationId(donationId);
        if (donationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Donations donation = donationOpt.get();
        DonationDetailsDTO response = new DonationDetailsDTO();
        response.setDonationId(donation.getDonationId());
        response.setDonationDate(donation.getDonationDate());
        response.setName(donation.getName());
        response.setPhone(donation.getPhone());
        response.setEmail(donation.getEmail());
        response.setAmount(donation.getAmount());
        response.setPanCard(donation.getPanCard());
        response.setAddress(donation.getAddress());
        response.setMessage(donation.getMessage());
        response.setCurrency(donation.getCurrency());
        response.setPaymentMethod(donation.getPaymentMethod());
        response.setOrderId(donation.getOrderId());
        response.setPaymentId(donation.getPaymentId());
        response.setStatus(donation.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<DonationStatsDTO> getDonationStats() {
        long totalDonors = donationRepository.countByStatus("SUCCESS");
        BigDecimal totalAmount = donationRepository.sumAmountByStatus("SUCCESS");
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        double progressPercentage = targetAmount.compareTo(BigDecimal.ZERO) > 0 ?
            totalAmount.multiply(BigDecimal.valueOf(100)).divide(targetAmount, 2, BigDecimal.ROUND_HALF_UP).doubleValue() : 0.0;
        DonationStatsDTO stats = new DonationStatsDTO();
        stats.setTotalDonors(totalDonors);
        stats.setTotalAmount(totalAmount);
        stats.setTargetDonors(targetDonors);
        stats.setTargetAmount(targetAmount);
        stats.setProgressPercentage(progressPercentage);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/invoice/{donationId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String donationId) {
        Optional<Donations> donationOpt = donationRepository.findByDonationId(donationId);
        if (donationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Donations d = donationOpt.get();
        try {
            byte[] pdf = invoiceService.generateInvoice(
                d.getDonationId(), d.getDonationDate(), d.getName(), d.getPhone(), d.getEmail(), d.getAmount(),
                d.getPanCard(), d.getAddress(), d.getMessage(), d.getCurrency(), d.getPaymentMethod(),
                d.getOrderId(), d.getPaymentId(), d.getStatus()
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Invoice_" + d.getDonationId() + ".pdf");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating invoice PDF: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-phone/{phone}")
    public ResponseEntity<List<DonationDetailsDTO>> getDonationsByPhone(@PathVariable String phone) {
        List<Donations> donations = donationRepository.findAllByPhoneAndStatusOrderByDonationDateDesc(phone, "SUCCESS");
        List<DonationDetailsDTO> dtos = donations.stream().map(donation -> {
            DonationDetailsDTO dto = new DonationDetailsDTO();
            dto.setDonationId(donation.getDonationId());
            dto.setDonationDate(donation.getDonationDate());
            dto.setName(donation.getName());
            dto.setPhone(donation.getPhone());
            dto.setEmail(donation.getEmail());
            dto.setAmount(donation.getAmount());
            dto.setPanCard(donation.getPanCard());
            dto.setAddress(donation.getAddress());
            dto.setMessage(donation.getMessage());
            dto.setCurrency(donation.getCurrency());
            dto.setPaymentMethod(donation.getPaymentMethod());
            dto.setOrderId(donation.getOrderId());
            dto.setPaymentId(donation.getPaymentId());
            dto.setStatus(donation.getStatus());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/invoice/by-phone/{phone}")
    public ResponseEntity<byte[]> downloadInvoiceByPhone(@PathVariable String phone) {
        List<Donations> donations = donationRepository.findAllByPhoneAndStatusOrderByDonationDateDesc(phone, "SUCCESS");
        if (donations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] pdf = invoiceService.generateCombinedInvoiceForPhone(phone, donations);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Invoice_" + phone + ".pdf");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating combined invoice PDF: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 