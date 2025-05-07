package com.santhrupthi.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Data
public class DonationStatsDTO {
    private long totalDonors;
    private BigDecimal totalAmount;
    private long targetDonors;
    private BigDecimal targetAmount;
    private double progressPercentage;
} 