package edu.classproject.delivery;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DeliveryPartner data persistence.
 * 
 * Implements Repository Pattern (Data Access Abstraction)
 * Enables switching between in-memory, database, or cache implementations
 * without changing service logic.
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles data access
 * - Dependency Inversion: Abstraction layer for data persistence
 * 
 * GRASP Patterns:
 * - Information Expert: Repository owns data storage logic
 * - Separation of Concerns: Data access separate from business logic
 */
public interface DeliveryPartnerRepository {
    
    /**
     * Save a delivery partner to the repository.
     * 
     * @param partner the partner to save (non-null)
     * @throws IllegalArgumentException if partner is null
     */
    void save(DeliveryPartner partner);
    
    /**
     * Find a delivery partner by ID.
     * 
     * @param partnerId the partner's unique ID (non-null)
     * @return Optional containing the partner if found, empty otherwise
     * @throws IllegalArgumentException if partnerId is null or empty
     */
    Optional<DeliveryPartner> findById(String partnerId);
    
    /**
     * Retrieve all delivery partners.
     * 
     * @return list of all partners (empty list if none found)
     */
    List<DeliveryPartner> findAll();
    
    /**
     * Retrieve all available (online) delivery partners.
     * 
     * @return list of available partners (empty list if none available)
     */
    List<DeliveryPartner> findAllAvailable();
    
    /**
     * Check if a partner exists by ID.
     * 
     * @param partnerId the partner's ID
     * @return true if partner exists, false otherwise
     */
    boolean existsById(String partnerId);
}
