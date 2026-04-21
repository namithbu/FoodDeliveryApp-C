package edu.classproject.delivery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of DeliveryPartnerRepository.
 * 
 * Thread-safe storage using ConcurrentHashMap.
 * Suitable for MVP and testing.
 * 
 * Implements Patterns:
 * - Repository Pattern: Data access abstraction
 * - Singleton Pattern: Single instance manages all partner data
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles in-memory data persistence
 * - Liskov Substitution: Can be replaced with DatabaseDeliveryPartnerRepository
 * - Dependency Inversion: Implements DeliveryPartnerRepository interface
 */
public class InMemoryDeliveryPartnerRepository implements DeliveryPartnerRepository {
    
    private final Map<String, DeliveryPartner> partners;
    
    /**
     * Creates a new in-memory repository with thread-safe storage.
     * Uses ConcurrentHashMap for safe concurrent access.
     */
    public InMemoryDeliveryPartnerRepository() {
        this.partners = new ConcurrentHashMap<>();
    }
    
    @Override
    public void save(DeliveryPartner partner) {
        if (partner == null) {
            throw new IllegalArgumentException("Partner cannot be null");
        }
        partners.put(partner.partnerId(), partner);
    }
    
    @Override
    public Optional<DeliveryPartner> findById(String partnerId) {
        if (partnerId == null || partnerId.isBlank()) {
            throw new IllegalArgumentException("Partner ID cannot be null or empty");
        }
        return Optional.ofNullable(partners.get(partnerId));
    }
    
    @Override
    public List<DeliveryPartner> findAll() {
        return Collections.unmodifiableList(
            new ArrayList<>(partners.values())
        );
    }
    
    @Override
    public List<DeliveryPartner> findAllAvailable() {
        return Collections.unmodifiableList(
            partners.values().stream()
                .filter(DeliveryPartner::available)
                .collect(Collectors.toList())
        );
    }
    
    @Override
    public boolean existsById(String partnerId) {
        if (partnerId == null || partnerId.isBlank()) {
            throw new IllegalArgumentException("Partner ID cannot be null or empty");
        }
        return partners.containsKey(partnerId);
    }
}
