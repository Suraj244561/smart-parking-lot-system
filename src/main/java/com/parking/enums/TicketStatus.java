package com.parking.enums;

public enum TicketStatus {
    ACTIVE("Vehicle currently parked"),
    COMPLETED("Vehicle has exited"),
    CANCELLED("Ticket cancelled");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
