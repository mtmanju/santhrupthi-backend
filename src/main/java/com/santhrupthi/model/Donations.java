package com.santhrupthi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

import com.santhrupthi.model.base.BaseEntity;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Entity
@Table(name = "donations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Donations extends BaseEntity<Donations> {
    
    @Column(name = "donation_id", unique = true, nullable = false)
    private String donationId;

    @Column(name = "donation_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date donationDate;

    @Column(name = "order_id", unique = true)
    private String orderId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "pan_card", length = 20)
    private String panCard;
    
    @Column(name = "address", columnDefinition = "text")
    private String address;
    
    @Column(name = "message", columnDefinition = "text")
    private String message;
    
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;
    
    @Column(name = "status", nullable = false, length = 50)
    private String status;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "signature")
    private String signature;

    @Column(name = "payment_id")
    private String paymentId;

} 