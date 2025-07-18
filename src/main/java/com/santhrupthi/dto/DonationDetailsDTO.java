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
public class DonationDetailsDTO {
    private String donationId;
    private Date donationDate;
    private String name;
    private String phone;
    private String email;
    private BigDecimal amount;
    private String panCard;
    private String address;
    private String message;
    private String currency;
    private String paymentMethod;
    private String orderId;
    private String paymentId;
    private String status;
} 