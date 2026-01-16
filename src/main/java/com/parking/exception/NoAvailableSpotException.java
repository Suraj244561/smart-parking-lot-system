package com.parking.exception;

/**
 * Exception thrown when no parking spot is available
 * for the requested vehicle type or floor.
 */
public class NoAvailableSpotException extends RuntimeException {
    
    public NoAvailableSpotException(String message) {
        super(message);
    }
    
    public NoAvailableSpotException(String message, Throwable cause) {
        super(message, cause);
    }
}
