package com.nextbigtool.backend.controller.payment;

import com.nextbigtool.backend.model.payment.PaymentOrderRequestDto;
import com.nextbigtool.backend.model.payment.PaymentVerifyDto;
import com.nextbigtool.backend.model.payment.SubscriptionVerifyDto;
import com.nextbigtool.backend.service.payment.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PaymentController {

    @Autowired private PaymentService paymentService;

    /** Creates either a Razorpay Subscription (monthly) or Order (yearly) */
    @PostMapping("/create-order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrder(@RequestBody PaymentOrderRequestDto request) {
        return paymentService.createOrderOrSubscription(request);
    }

    /** Verify mandate + first payment for monthly subscription */
    @PostMapping("/verify-subscription")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> verifySubscription(@RequestBody SubscriptionVerifyDto dto) {
        return paymentService.verifySubscription(dto);
    }

    /** Verify one-time yearly order payment */
    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerifyDto dto) {
        return paymentService.verifyYearlyPayment(dto);
    }
}
