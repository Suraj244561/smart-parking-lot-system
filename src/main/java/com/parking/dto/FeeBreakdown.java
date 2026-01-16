package com.parking.dto;

/**
 * Data Transfer Object for fee calculation breakdown.
 * Provides detailed fee information for transparency and reporting.
 */
public class FeeBreakdown {
    
    private double baseFare;           // Initial fare before multipliers
    private double occupancyMultiplier; // Multiplier based on parking lot occupancy
    private double adjustedFare;       // Fare after occupancy adjustment
    private double discountApplied;    // Total discount amount
    private double totalFare;          // Final amount to be paid
    private long durationMinutes;      // Parking duration in minutes
    private double occupancyRate;      // Current lot occupancy rate
    
    // Constructors
    public FeeBreakdown() {}
    
    public FeeBreakdown(double baseFare, double occupancyMultiplier,
                       double adjustedFare, double discountApplied,
                       double totalFare, long durationMinutes,
                       double occupancyRate) {
        this.baseFare = baseFare;
        this.occupancyMultiplier = occupancyMultiplier;
        this.adjustedFare = adjustedFare;
        this.discountApplied = discountApplied;
        this.totalFare = totalFare;
        this.durationMinutes = durationMinutes;
        this.occupancyRate = occupancyRate;
    }
    
    // Getters and Setters
    public double getBaseFare() {
        return baseFare;
    }
    
    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }
    
    public double getOccupancyMultiplier() {
        return occupancyMultiplier;
    }
    
    public void setOccupancyMultiplier(double occupancyMultiplier) {
        this.occupancyMultiplier = occupancyMultiplier;
    }
    
    public double getAdjustedFare() {
        return adjustedFare;
    }
    
    public void setAdjustedFare(double adjustedFare) {
        this.adjustedFare = adjustedFare;
    }
    
    public double getDiscountApplied() {
        return discountApplied;
    }
    
    public void setDiscountApplied(double discountApplied) {
        this.discountApplied = discountApplied;
    }
    
    public double getTotalFare() {
        return totalFare;
    }
    
    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }
    
    public long getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public double getOccupancyRate() {
        return occupancyRate;
    }
    
    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }
    
    /**
     * Get duration in hours (rounded to 2 decimals)
     */
    public double getDurationHours() {
        return Math.round((durationMinutes / 60.0) * 100.0) / 100.0;
    }
    
    @Override
    public String toString() {
        return "FeeBreakdown{" +
                "baseFare=" + baseFare +
                ", occupancyMultiplier=" + occupancyMultiplier +
                ", adjustedFare=" + adjustedFare +
                ", discountApplied=" + discountApplied +
                ", totalFare=" + totalFare +
                ", durationMinutes=" + durationMinutes +
                ", occupancyRate=" + occupancyRate +
                '}';
    }
}
