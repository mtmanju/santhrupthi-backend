package com.santhrupthi.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.santhrupthi.dto.DonationsRequest;
import com.santhrupthi.dto.DonationsResponse;

import java.util.Date;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public DonationsResponse createOrder(DonationsRequest paymentRequest) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", paymentRequest.getAmount()); // amount in the smallest currency unit
        orderRequest.put("currency", paymentRequest.getCurrency());
        orderRequest.put("receipt", "order_rcptid_" + System.currentTimeMillis());

        Order order = razorpay.orders.create(orderRequest);

        DonationsResponse paymentResponse = new DonationsResponse();
        paymentResponse.setOrderId(order.get("id"));
        paymentResponse.setRazorpayKey(razorpayKeyId);
        paymentResponse.setAmount(paymentRequest.getAmount());
        paymentResponse.setCurrency(paymentRequest.getCurrency());
        paymentResponse.setName(paymentRequest.getName());
        paymentResponse.setEmail(paymentRequest.getEmail());
        paymentResponse.setPhone(paymentRequest.getPhone());
        paymentResponse.setDonationDate(new Date());
        paymentResponse.setSignature(paymentRequest.getSignature());
        paymentResponse.setStatus("CREATED");
        paymentResponse.setPaymentMethod(paymentRequest.getPaymentMethod());
        paymentResponse.setCreatedBy("system");
        paymentResponse.setCreatedOn(new Date());
        paymentResponse.setModifiedBy("system");
        paymentResponse.setModifiedOn(new Date());  
        return paymentResponse;
    }
} 