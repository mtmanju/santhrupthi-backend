package com.santhrupthi.repository;

import com.santhrupthi.model.Donations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Repository
public interface DonationsRepository extends JpaRepository<Donations, Long> {
    // Add custom query methods if needed
    Optional<Donations> findByDonationId(String donationId);

    long countByStatus(String status);

    @Query("SELECT SUM(d.amount) FROM Donations d WHERE d.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") String status);

    Optional<Donations> findByOrderId(String orderId);

    List<Donations> findAllByPhoneOrderByDonationDateDesc(String phone);

    List<Donations> findAllByPhoneAndStatusOrderByDonationDateDesc(String phone, String status);
} 