package com.faos.enums;

public enum TimeSlot {
    MORNING_SLOT("07:00 AM - 10:00 AM"),
    MID_MORNING_SLOT("10:00 AM - 01:00 PM"),
    AFTERNOON_SLOT("01:00 PM - 04:00 PM"),
    EVENING_SLOT("04:00 PM - 07:00 PM"),
    NIGHT_SLOT("07:00 PM - 10:00 PM");

    private final String label;

    TimeSlot(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
