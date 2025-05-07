package com.santhrupthi.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Data
public class DonationsResponse {
    private String donationId;
    private String name;
    private String phone;
    private String email;
    private BigDecimal amount;
    private String panCard;
    private String address;
    private String message;
    private String currency;
    private String status;
    private String paymentMethod;
    private String signature;
    private String orderId;
    private String razorpayKey;
    private Date donationDate;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
    private String paymentId;
} 