package com.parking.model;

import com.parking.enums.VehicleType;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_space", indexes = {
    @Index(name = "idx_floor_available", columnList = "floor_id,is_available"),
    @Index(name = "idx_available", columnList = "is_available"),
    @Index(name = "idx_vehicle_type", columnList = "vehicle_type_id"),
    @Index(name = "idx_available_spots", columnList = "is_available,vehicle_type_id,floor_id")
})
public class ParkingSpace {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spotId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;
    
    @Column(nullable = false, length = 10)
    private String spotNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type_id", nullable = false)
    private VehicleType vehicleType;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
    
    @Column(name = "current_vehicle_id")
    private Long currentVehicleId;
    
    @Column(name = "entry_time")
    private LocalDateTime entryTime;
    
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Version
    private Long version;  // For optimistic locking
    
    // Constructors
    public ParkingSpace() {}
    
    public ParkingSpace(Floor floor, String spotNumber, VehicleType vehicleType) {
        this.floor = floor;
        this.spotNumber = spotNumber;
        this.vehicleType = vehicleType;
    }
    
    // Getters and Setters
    public Long getSpotId() {
        return spotId;
    }
    
    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }
    
    public Floor getFloor() {
        return floor;
    }
    
    public void setFloor(Floor floor) {
        this.floor = floor;
    }
    
    public String getSpotNumber() {
        return spotNumber;
    }
    
    public void setSpotNumber(String spotNumber) {
        this.spotNumber = spotNumber;
    }
    
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public Boolean getAvailable() {
        return isAvailable;
    }
    
    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
    
    public Long getCurrentVehicleId() {
        return currentVehicleId;
    }
    
    public void setCurrentVehicleId(Long currentVehicleId) {
        this.currentVehicleId = currentVehicleId;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
}
