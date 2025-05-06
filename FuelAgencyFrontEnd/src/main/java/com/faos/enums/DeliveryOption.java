package com.faos.enums;


public enum DeliveryOption {
    STANDARD("Standard Delivery (3-5 days)"),
    EXPRESS("Express Delivery (1-2 days)"),
    SAME_DAY("Same-Day Delivery"),
    SCHEDULED("Scheduled Delivery");

    private final String label;

    DeliveryOption(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
