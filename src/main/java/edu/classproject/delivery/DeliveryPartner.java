package edu.classproject.delivery;

/**
 * Immutable record representing a Delivery Partner (Driver).
 * 
 * Fields:
 * - partnerId: Unique identifier for the partner
 * - name: Partner's full name
 * - email: Partner's email address
 * - phoneNumber: Partner's phone number
 * - vehicleNumber: Registered vehicle number/plate
 * - available: Current availability status (online/offline)
 * 
 * Uses Java Record for immutability and automatic equals/hashCode/toString.
 */
public record DeliveryPartner(
    String partnerId, 
    String name, 
    String email,
    String phoneNumber,
    String vehicleNumber,
    boolean available
) {
    
}
