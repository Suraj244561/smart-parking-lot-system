package com.parking.service;

import com.parking.enums.VehicleType;
import com.parking.exception.NoAvailableSpotException;
import com.parking.model.ParkingSpace;
import com.parking.repository.ParkingSpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for allocating parking spaces to vehicles.
 * Implements greedy algorithm for spot allocation:
 * - Prioritize closest spots to entrance (lower floor, lower spot number)
 * - Match vehicle type with spot size requirements
 * - Thread-safe using database-level pessimistic locking
 */
@Service
public class AllocationService {
    
    @Autowired
    private ParkingSpaceRepository parkingSpaceRepository;
    
    /**
     * Algorithm: FindAvailableParkingSpot
     * 
     * Strategy: Greedy Nearest Available
     * - Find first available spot matching vehicle type
     * - Prioritize lower floors (closer to entrance)
     * - Use SERIALIZABLE isolation for atomic allocation
     * 
     * Time Complexity: O(log N) with database indexing
     * Space Complexity: O(1)
     * 
     * @param vehicleType Type of vehicle requiring parking
     * @return Allocated ParkingSpace
     * @throws NoAvailableSpotException if no spots available
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized ParkingSpace allocateSpot(VehicleType vehicleType) {
        // Query database for first available spot matching vehicle type
        // Ordered by floor (ascending) for nearest spot to entrance
        Optional<ParkingSpace> availableSpot = parkingSpaceRepository
            .findFirstAvailableSpotByVehicleType(vehicleType);
        
        if (!availableSpot.isPresent()) {
            throw new NoAvailableSpotException(
                "No available parking spot for " + vehicleType.getDisplayName()
            );
        }
        
        return availableSpot.get();
    }
    
    /**
     * Find available spots on preferred floor (if available)
     * Fallback to other floors if preferred floor is full
     * 
     * @param vehicleType Type of vehicle
     * @param preferredFloorId Preferred floor ID
     * @return Allocated ParkingSpace
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized ParkingSpace allocateSpotWithFloorPreference(
            VehicleType vehicleType, Long preferredFloorId) {
        
        // Try preferred floor first
        Optional<ParkingSpace> preferredSpot = parkingSpaceRepository
            .findFirstAvailableSpotByVehicleTypeAndFloor(vehicleType, preferredFloorId);
        
        if (preferredSpot.isPresent()) {
            return preferredSpot.get();
        }
        
        // Fallback to any available spot
        return allocateSpot(vehicleType);
    }
    
    /**
     * Get count of available spots for a specific vehicle type
     * 
     * @param vehicleType Type of vehicle
     * @return Count of available spots
     */
    public long getAvailableSpotCount(VehicleType vehicleType) {
        return parkingSpaceRepository.countAvailableSpots(vehicleType);
    }
    
    /**
     * Get total spots for a vehicle type
     * 
     * @param vehicleType Type of vehicle
     * @return Total spots for that vehicle type
     */
    public long getTotalSpotCount(VehicleType vehicleType) {
        return parkingSpaceRepository.countByVehicleType(vehicleType);
    }
    
    /**
     * Get occupancy percentage for a vehicle type
     * 
     * @param vehicleType Type of vehicle
     * @return Occupancy rate (0.0 - 1.0)
     */
    public double getOccupancyRate(VehicleType vehicleType) {
        long total = getTotalSpotCount(vehicleType);
        if (total == 0) return 0.0;
        
        long available = getAvailableSpotCount(vehicleType);
        return (double) (total - available) / total;
    }
}
