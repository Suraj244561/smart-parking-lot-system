package com.parking.service;

import com.parking.dto.FeeBreakdown;
import com.parking.enums.VehicleType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service for dynamic parking fee calculation.
 * 
 * Implements time-based pricing with occupancy-based surcharges:
 * - Hourly rates vary by vehicle type
 * - Daily caps for long-term parking
 * - Dynamic pricing multiplier based on occupancy
 * - Discounts for subscribers and off-peak hours
 */
@Service
public class FeeCalculationService {
    
    // Rate card constants (in Indian Rupees)
    private static final double MINIMUM_CHARGE = 30.0;
    private static final double SUBSCRIBER_DISCOUNT = 0.15;  // 15% discount
    
    /**
     * Main fee calculation algorithm.
     * 
     * Algorithm: CalculateParkingFee
     * Step 1: Calculate parking duration in minutes
     * Step 2: Determine rate based on duration (hourly vs daily)
     * Step 3: Apply vehicle-type base rate
     * Step 4: Apply dynamic pricing multiplier based on occupancy
     * Step 5: Apply loyalty discounts
     * Step 6: Ensure minimum charge and return breakdown
     * 
     * @param entryTime Vehicle entry timestamp
     * @param exitTime Vehicle exit timestamp
     * @param vehicleType Type of vehicle
     * @param occupancyRate Parking lot occupancy (0.0 - 1.0)
     * @param isSubscriber Is customer a subscriber
     * @return FeeBreakdown with detailed charges
     */
    public FeeBreakdown calculateFee(LocalDateTime entryTime, LocalDateTime exitTime,
                                     VehicleType vehicleType, double occupancyRate,
                                     boolean isSubscriber) {
        
        // Step 1: Calculate duration
        long durationMinutes = ChronoUnit.MINUTES.between(entryTime, exitTime);
        
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Exit time must be after entry time");
        }
        
        // Step 2: Calculate base fare
        double baseFare = calculateBaseFare(durationMinutes, vehicleType);
        
        // Step 3: Apply dynamic pricing multiplier
        double multiplier = calculateOccupancyMultiplier(occupancyRate);
        double adjustedFare = baseFare * multiplier;
        
        // Step 4: Apply subscriber discount if applicable
        double discount = 0.0;
        if (isSubscriber) {
            discount = adjustedFare * SUBSCRIBER_DISCOUNT;
        }
        
        // Step 5: Calculate total and ensure minimum charge
        double totalFare = Math.max(adjustedFare - discount, MINIMUM_CHARGE);
        
        // Return detailed breakdown
        return new FeeBreakdown(
            baseFare,
            multiplier,
            adjustedFare,
            discount,
            totalFare,
            durationMinutes,
            occupancyRate
        );
    }
    
    /**
     * Calculate base fare based on duration and vehicle type.
     * 
     * Pricing Strategy:
     * - First 30 min: Grace period or minimal charge
     * - 30 min - 24 hours: Hourly rates
     * - > 24 hours: Daily rates with hourly overage
     * 
     * @param durationMinutes Total parking duration
     * @param vehicleType Type of vehicle
     * @return Base fare before multipliers
     */
    private double calculateBaseFare(long durationMinutes, VehicleType vehicleType) {
        double hourlyRate = vehicleType.getHourlyRate();
        double dailyRate = vehicleType.getDailyRate();
        
        // Case 1: Less than 1 hour - charge for full hour
        if (durationMinutes <= 60) {
            return hourlyRate;
        }
        
        // Case 2: 1-24 hours - charge by hour (round up)
        if (durationMinutes < 1440) {  // 1440 min = 24 hours
            long hours = (durationMinutes + 59) / 60;  // Round up
            return hours * hourlyRate;
        }
        
        // Case 3: > 24 hours - apply daily rate + hourly overage
        long fullDays = durationMinutes / 1440;
        long remainingMinutes = durationMinutes % 1440;
        long remainingHours = (remainingMinutes + 59) / 60;  // Round up
        
        return (fullDays * dailyRate) + (remainingHours * hourlyRate);
    }
    
    /**
     * Calculate dynamic pricing multiplier based on occupancy rate.
     * 
     * Pricing Strategy:
     * - > 90% occupied: 1.5x surcharge (Peak pricing)
     * - 75-90% occupied: 1.25x surcharge (High occupancy)
     * - 50-75% occupied: 1.0x (Normal pricing)
     * - 25-50% occupied: 0.9x (Moderate discount)
     * - < 25% occupied: 0.75x (Off-peak discount)
     * 
     * @param occupancyRate Current occupancy rate (0.0 - 1.0)
     * @return Price multiplier
     */
    private double calculateOccupancyMultiplier(double occupancyRate) {
        if (occupancyRate > 0.90) {
            return 1.5;  // 50% surcharge for peak
        } else if (occupancyRate > 0.75) {
            return 1.25;  // 25% surcharge for high
        } else if (occupancyRate > 0.50) {
            return 1.0;  // Normal pricing
        } else if (occupancyRate > 0.25) {
            return 0.9;  // 10% discount
        } else {
            return 0.75;  // 25% discount for off-peak
        }
    }
    
    /**
     * Estimate parking fee before exit.
     * Useful for showing estimated charges to customers.
     * 
     * @param entryTime Entry time
     * @param vehicleType Vehicle type
     * @param currentOccupancy Current occupancy rate
     * @return Estimated fee
     */
    public double estimateFee(LocalDateTime entryTime, VehicleType vehicleType,
                            double currentOccupancy) {
        LocalDateTime now = LocalDateTime.now();
        return calculateFee(entryTime, now, vehicleType, currentOccupancy, false)
            .getTotalFare();
    }
}
