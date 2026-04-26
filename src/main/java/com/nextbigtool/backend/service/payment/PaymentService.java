package com.nextbigtool.backend.service.payment;

import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.entity.user.BillingCycle;
import com.nextbigtool.backend.entity.user.PlanType;
import com.nextbigtool.backend.model.payment.PaymentOrderRequestDto;
import com.nextbigtool.backend.model.payment.PaymentVerifyDto;
import com.nextbigtool.backend.model.payment.SubscriptionVerifyDto;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import com.nextbigtool.backend.service.subscription.SubscriptionService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.plan-id.basic-monthly}")
    private String basicMonthlyPlanId;

    @Value("${razorpay.plan-id.core-monthly}")
    private String coreMonthlyPlanId;

    @Autowired private CurrentUserService currentUserService;
    @Autowired private SubscriptionService subscriptionService;

    // Yearly one-time prices in paise (₹1900×10 = ₹19000, ₹7900×10 = ₹79000)
    private static final Map<PlanType, Integer> YEARLY_PRICES = Map.of(
            PlanType.BASIC, 1900000,
            PlanType.CORE,  7900000
    );

    /**
     * Unified entry point. Routes to subscription (monthly) or order (yearly).
     */
    public ResponseEntity<?> createOrderOrSubscription(PaymentOrderRequestDto request) {
        PlanType planType = request.getPlanType();
        if (planType == PlanType.FREE) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "FREE plan requires no payment"));
        }

        BillingCycle cycle = request.getBillingCycle() != null ? request.getBillingCycle() : BillingCycle.MONTHLY;

        return cycle == BillingCycle.MONTHLY
                ? createSubscription(planType)
                : createYearlyOrder(planType);
    }

    // ── Monthly: Razorpay Subscription (recurring) ──────────────────────────

    private ResponseEntity<?> createSubscription(PlanType planType) {
        try {
            String planId = planType == PlanType.BASIC ? basicMonthlyPlanId : coreMonthlyPlanId;

            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("plan_id", planId);
            options.put("total_count", 120);   // 10 years — effectively unlimited
            options.put("quantity", 1);
            options.put("customer_notify", 1); // Razorpay sends payment receipts to customer

            com.razorpay.Subscription sub = client.subscriptions.create(options);
            String subscriptionId = sub.get("id").toString();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "type", "SUBSCRIPTION",
                    "data", Map.of(
                            "subscriptionId", subscriptionId,
                            "keyId", keyId,
                            "planType", planType.name(),
                            "billingCycle", "MONTHLY"
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Failed to create subscription: " + e.getMessage()));
        }
    }

    /**
     * Verify mandate/first-payment for a Razorpay Subscription.
     * Signature = HMAC_SHA256(paymentId + "|" + subscriptionId, keySecret)
     */
    public ResponseEntity<?> verifySubscription(SubscriptionVerifyDto dto) {
        try {
            String payload = dto.getRazorpayPaymentId() + "|" + dto.getRazorpaySubscriptionId();
            String generated = hmacSHA256(payload, keySecret);

            if (!generated.equals(dto.getRazorpaySignature())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Subscription verification failed"));
            }

            AppUser user = currentUserService.getCurrentUser();
            subscriptionService.activateMonthlySubscription(
                    user, dto.getPlanType(),
                    dto.getRazorpaySubscriptionId(), dto.getRazorpayPaymentId()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Subscription activated — you will be billed monthly",
                    "planType", dto.getPlanType().name(),
                    "billingCycle", "MONTHLY"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Verification failed: " + e.getMessage()));
        }
    }

    // ── Yearly: Razorpay Order (one-time) ────────────────────────────────────

    private ResponseEntity<?> createYearlyOrder(PlanType planType) {
        try {
            Integer amount = YEARLY_PRICES.get(planType);
            if (amount == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Invalid plan"));
            }

            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", amount);
            options.put("currency", "INR");
            options.put("receipt", "rcpt_" + System.currentTimeMillis());
            options.put("notes", new JSONObject()
                    .put("planType", planType.name())
                    .put("billingCycle", "YEARLY"));

            Order order = client.orders.create(options);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "type", "ORDER",
                    "data", Map.of(
                            "orderId", order.get("id").toString(),
                            "amount", order.get("amount"),
                            "currency", order.get("currency"),
                            "keyId", keyId,
                            "planType", planType.name(),
                            "billingCycle", "YEARLY"
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Failed to create order: " + e.getMessage()));
        }
    }

    /**
     * Verify one-time yearly order payment.
     * Signature = HMAC_SHA256(orderId + "|" + paymentId, keySecret)
     */
    public ResponseEntity<?> verifyYearlyPayment(PaymentVerifyDto dto) {
        try {
            String payload = dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId();
            String generated = hmacSHA256(payload, keySecret);

            if (!generated.equals(dto.getRazorpaySignature())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Payment verification failed"));
            }

            AppUser user = currentUserService.getCurrentUser();
            subscriptionService.activatePlan(user, dto.getPlanType(), BillingCycle.YEARLY,
                    dto.getRazorpayOrderId(), dto.getRazorpayPaymentId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Yearly plan activated",
                    "planType", dto.getPlanType().name(),
                    "billingCycle", "YEARLY"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Verification failed: " + e.getMessage()));
        }
    }

    // ── Webhook signature verification ───────────────────────────────────────

    public boolean verifyWebhookSignature(String rawBody, String razorpaySignatureHeader, String webhookSecret) {
        try {
            String generated = hmacSHA256(rawBody, webhookSecret);
            return generated.equals(razorpaySignatureHeader);
        } catch (Exception e) {
            return false;
        }
    }

    // ── Shared HMAC utility ───────────────────────────────────────────────────

    public String hmacSHA256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        Formatter formatter = new Formatter();
        for (byte b : hash) formatter.format("%02x", b);
        return formatter.toString();
    }
}
