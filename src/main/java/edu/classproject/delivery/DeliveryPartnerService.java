package edu.classproject.delivery;

import java.util.List;

public interface DeliveryPartnerService {
    /**
     * Register a new delivery partner.
     * 
     * @param name the partner's full name (non-null, non-empty)
     * @param email the partner's email address (non-null, non-empty)
     * @param phoneNumber the partner's phone number (non-null, non-empty)
     * @param vehicleNumber the partner's vehicle number/plate (non-null, non-empty)
     * @return the newly registered DeliveryPartner
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    DeliveryPartner register(String name, String email, String phoneNumber, String vehicleNumber);

    void setAvailability(String partnerId, boolean available);

    List<DeliveryPartner> getAvailablePartners();
}
