package com.santhrupthi.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Data
public class DonationsRequest {
    private String name;
    private String phone;
    private String email;
    private BigDecimal amount;
    private String panCard;
    private String address;
    private String message;
    private String currency;
    private String paymentMethod;
    private String signature;
    private String firstName;
    private String lastName;
    // createdBy, modifiedBy can be set in service/controller
} 