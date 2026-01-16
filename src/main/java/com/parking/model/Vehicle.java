package com.parking.model;

import com.parking.enums.VehicleType;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle", indexes = {
    @Index(name = "idx_license_plate", columnList = "license_plate")
})
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;
    
    @Column(nullable = false, unique = true, length = 20)
    private String licensePlate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;
    
    @Column(nullable = false, length = 100)
    private String ownerName;
    
    @Column(length = 100)
    private String ownerEmail;
    
    @Column(length = 20)
    private String ownerPhone;
    
    @Column(name = "registered_date", nullable = false, updatable = false)
    private LocalDateTime registeredDate = LocalDateTime.now();
    
    // Constructors
    public Vehicle() {}
    
    public Vehicle(String licensePlate, VehicleType vehicleType, String ownerName) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.ownerName = ownerName;
    }
    
    // Getters and Setters
    public Long getVehicleId() {
        return vehicleId;
    }
    
    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public String getOwnerEmail() {
        return ownerEmail;
    }
    
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
    
    public String getOwnerPhone() {
        return ownerPhone;
    }
    
    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }
    
    public LocalDateTime getRegisteredDate() {
        return registeredDate;
    }
    
    public void setRegisteredDate(LocalDateTime registeredDate) {
        this.registeredDate = registeredDate;
    }
}
