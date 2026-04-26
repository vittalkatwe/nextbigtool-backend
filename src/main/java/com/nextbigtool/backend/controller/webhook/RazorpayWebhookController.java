package com.nextbigtool.backend.controller.webhook;

import com.nextbigtool.backend.service.payment.PaymentService;
import com.nextbigtool.backend.service.subscription.SubscriptionService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
public class RazorpayWebhookController {

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    @Autowired private PaymentService paymentService;
    @Autowired private SubscriptionService subscriptionService;

    @PostMapping("/razorpay")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        // Verify authenticity of the webhook
        if (signature == null || !paymentService.verifyWebhookSignature(rawBody, signature, webhookSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        try {
            JSONObject payload = new JSONObject(rawBody);
            String event = payload.getString("event");

            // All subscription events carry the subscription object under payload.subscription.entity
            String subscriptionId = null;
            String paymentId = null;

            if (payload.has("payload")) {
                JSONObject eventPayload = payload.getJSONObject("payload");

                if (eventPayload.has("subscription")) {
                    subscriptionId = eventPayload
                            .getJSONObject("subscription")
                            .getJSONObject("entity")
                            .getString("id");
                }
                if (eventPayload.has("payment")) {
                    paymentId = eventPayload
                            .getJSONObject("payment")
                            .getJSONObject("entity")
                            .getString("id");
                }
            }

            switch (event) {
                case "subscription.charged" -> {
                    if (subscriptionId != null && paymentId != null) {
                        subscriptionService.handleSubscriptionCharged(subscriptionId, paymentId);
                    }
                }
                case "subscription.halted" -> {
                    if (subscriptionId != null) {
                        subscriptionService.handleSubscriptionHalted(subscriptionId);
                    }
                }
                case "subscription.cancelled" -> {
                    if (subscriptionId != null) {
                        subscriptionService.handleSubscriptionCancelled(subscriptionId);
                    }
                }
                case "subscription.completed" -> {
                    if (subscriptionId != null) {
                        subscriptionService.handleSubscriptionCompleted(subscriptionId);
                    }
                }
                // Acknowledge but ignore other events
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            // Return 200 so Razorpay doesn't keep retrying malformed payloads
            return ResponseEntity.ok("Processed with error: " + e.getMessage());
        }
    }
}
