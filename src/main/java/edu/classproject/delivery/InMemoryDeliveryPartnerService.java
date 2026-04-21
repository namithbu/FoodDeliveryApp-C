package edu.classproject.delivery;

import java.util.Collections;
import java.util.List;

import edu.classproject.common.IdGenerator;

/**
 * In-memory implementation of DeliveryPartnerService.
 * 
 * Manages delivery partner registration, availability updates, and discovery.
 * Uses dependency injection for repository and ID generation.
 * 
 * Implements Patterns:
 * - Factory Pattern: Creates DeliveryPartner objects via register()
 * - Facade Pattern: Provides simplified interface for service consumers
 * - Repository Pattern: Delegates data access to repository
 * 
 * SOLID Principles:
 * - Single Responsibility: Only business logic, not data persistence
 * - Open/Closed: Open for extension, closed for modification
 * - Liskov Substitution: Interchangeable with other service implementations
 * - Interface Segregation: Implements only required interface methods
 * - Dependency Inversion: Depends on repository abstraction, not concrete impl
 * 
 * GRASP Patterns:
 * - Information Expert: Service knows how to manage partners
 * - Creator: Factory for creating valid DeliveryPartner objects
 * - Controller: Service coordinates partner operations
 * - High Cohesion: All methods work toward partner management
 * - Low Coupling: Depends only on repository interface and IdGenerator
 */
public class InMemoryDeliveryPartnerService implements DeliveryPartnerService {
    
    private final DeliveryPartnerRepository repository;
    
    /**
     * Factory method to create a service instance.
     * Demonstrates Factory Pattern for service instantiation.
     * 
     * @return new instance of InMemoryDeliveryPartnerService
     */
    public static InMemoryDeliveryPartnerService create() {
        DeliveryPartnerRepository repository = new InMemoryDeliveryPartnerRepository();
        return new InMemoryDeliveryPartnerService(repository);
    }
    
    /**
     * Constructor with dependency injection.
     * 
     * Demonstrates Dependency Inversion Principle:
     * Service depends on repository interface, not concrete implementation.
     * Enables testing with mock repositories.
     * 
     * @param repository the partner repository implementation (non-null)
     * @throws IllegalArgumentException if repository is null
     */
    public InMemoryDeliveryPartnerService(DeliveryPartnerRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        this.repository = repository;
    }
    
    @Override
    public DeliveryPartner register(String name) {
        // Input validation
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Partner name cannot be null or empty");
        }
        
        // Generate unique ID using factory method
        String partnerId = generatePartnerId();
        
        // Create immutable partner record
        // Demonstrates Immutable Value Object pattern
        DeliveryPartner newPartner = new DeliveryPartner(partnerId, name.trim(), true);
        
        // Persist to repository
        repository.save(newPartner);
        
        return newPartner;
    }
    
    @Override
    public void setAvailability(String partnerId, boolean available) {
        // Input validation
        if (partnerId == null || partnerId.isBlank()) {
            throw new IllegalArgumentException("Partner ID cannot be null or empty");
        }
        
        // Validate partner exists
        DeliveryPartner existingPartner = repository.findById(partnerId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Partner with ID '" + partnerId + "' not found"
            ));
        
        // Create new immutable record with updated availability
        // Demonstrates Immutable Update Pattern (no mutation)
        DeliveryPartner updatedPartner = new DeliveryPartner(
            existingPartner.partnerId(),
            existingPartner.name(),
            available
        );
        
        // Persist updated state
        repository.save(updatedPartner);
    }
    
    @Override
    public List<DeliveryPartner> getAvailablePartners() {
        // Query repository for available partners only
        List<DeliveryPartner> availablePartners = repository.findAllAvailable();
        
        // Return unmodifiable copy to prevent external mutation
        // Guarantees: 
        // - Safe against concurrent modification
        // - Repository contract protected
        // - Returns empty list if no partners available (never null)
        return Collections.unmodifiableList(availablePartners);
    }
    
    /**
     * Generate unique partner ID.
     * 
     * Demonstrates Factory Pattern for ID generation.
     * Uses IdGenerator from common package.
     * 
     * @return unique partner ID with "DP" prefix
     */
    private String generatePartnerId() {
        return IdGenerator.nextId("DP");
    }
}
