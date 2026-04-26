package com.nextbigtool.backend.entity.user;

public enum SubscriptionStatus {
    CREATED,    // subscription created, mandate pending
    ACTIVE,     // mandate done, charging normally
    HALTED,     // payment failed after retries
    CANCELLED,  // user or system cancelled
    COMPLETED   // all billing cycles exhausted
}
