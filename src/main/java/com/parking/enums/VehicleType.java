package com.parking.enums;

public enum VehicleType {
    MOTORCYCLE("Motorcycle", "SMALL", 50.0, 300.0),
    CAR("Car", "MEDIUM", 100.0, 800.0),
    SUV("SUV", "LARGE", 150.0, 1200.0),
    BUS("Bus", "XLARGE", 200.0, 1500.0);

    private final String displayName;
    private final String spotSize;
    private final Double hourlyRate;  // in Indian Rupees
    private final Double dailyRate;

    VehicleType(String displayName, String spotSize, Double hourlyRate, Double dailyRate) {
        this.displayName = displayName;
        this.spotSize = spotSize;
        this.hourlyRate = hourlyRate;
        this.dailyRate = dailyRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSpotSize() {
        return spotSize;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public Double getDailyRate() {
        return dailyRate;
    }
}
