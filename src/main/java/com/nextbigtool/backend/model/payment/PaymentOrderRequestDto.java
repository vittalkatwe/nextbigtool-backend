package com.nextbigtool.backend.model.payment;

import com.nextbigtool.backend.entity.user.BillingCycle;
import com.nextbigtool.backend.entity.user.PlanType;

public class PaymentOrderRequestDto {
    private PlanType planType;
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
}
