package com.nextbigtool.backend.model.payment;

import com.nextbigtool.backend.entity.user.BillingCycle;
import com.nextbigtool.backend.entity.user.PlanType;

public class PaymentVerifyDto {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private PlanType planType;
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }

    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
}
