package com.nextbigtool.backend.entity.tool;

public enum CampaignDuration {
    ONE_MONTH,
    TWO_MONTHS,
    THREE_MONTHS;

    public int getMonths() {
        return switch (this) {
            case ONE_MONTH -> 1;
            case TWO_MONTHS -> 2;
            case THREE_MONTHS -> 3;
        };
    }

    // Rough impression estimates (customize based on your traffic)
    public String getEstimatedImpressions() {
        return switch (this) {
            case ONE_MONTH -> "~15,000 impressions";
            case TWO_MONTHS -> "~32,000 impressions";
            case THREE_MONTHS -> "~50,000 impressions";
        };
    }

    public String getDisplayPrice() {
        return switch (this) {
            case ONE_MONTH -> "$199 / month";
            case TWO_MONTHS -> "$179 / month";
            case THREE_MONTHS -> "$149 / month";
        };
    }
}