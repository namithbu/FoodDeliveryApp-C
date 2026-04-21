package edu.classproject.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests showing how other modules use DeliveryPartnerService.
 * 
 * Demonstrates the module's external API contract and typical usage patterns.
 * 
 * This is what Team 12 (Dispatch) and other modules will use.
 */
@DisplayName("Delivery Partner - Integration Tests")
public class DeliveryPartnerIntegrationTest {
    
    private DeliveryPartnerService deliveryPartnerService;
    
    @BeforeEach
    void setUp() {
        // Bootstrap service using factory pattern
        deliveryPartnerService = InMemoryDeliveryPartnerService.create();
    }
    
    @Test
    @DisplayName("Integration: Dispatch System finds available partners")
    void testDispatchSystemFindingAvailablePartners() {
        // Setup: Register delivery partners as admin
        DeliveryPartner driver1 = deliveryPartnerService.register("Driver 1");
        DeliveryPartner driver2 = deliveryPartnerService.register("Driver 2");
        DeliveryPartner driver3 = deliveryPartnerService.register("Driver 3");
        
        // Simulate: Some drivers are busy
        deliveryPartnerService.setAvailability(driver2.partnerId(), false);
        
        // Dispatch System: Get available partners for assignment
        // This is how Team 12 (Dispatch) will use this service
        List<DeliveryPartner> candidates = deliveryPartnerService.getAvailablePartners();
        
        // Verify: Only available partners in candidate list
        assertEquals(2, candidates.size());
        assertTrue(candidates.stream().allMatch(DeliveryPartner::available));
        
        // Dispatch can now use first available for assignment
        if (!candidates.isEmpty()) {
            DeliveryPartner assignedDriver = candidates.get(0);
            assertTrue(assignedDriver.available());
            System.out.println("Assigned driver: " + assignedDriver.name());
        }
    }
    
    @Test
    @DisplayName("Integration: Driver toggles availability from mobile app")
    void testDriverTogglingAvailabilityFromApp() {
        // Setup: Driver registered
        DeliveryPartner driver = deliveryPartnerService.register("John Doe");
        String driverId = driver.partnerId();
        
        // Driver goes offline via mobile app
        deliveryPartnerService.setAvailability(driverId, false);
        
        // Verify: Driver no longer in available list
        assertEquals(0, deliveryPartnerService.getAvailablePartners().size());
        
        // Driver logs back in and goes online
        deliveryPartnerService.setAvailability(driverId, true);
        
        // Verify: Driver now available for orders
        assertEquals(1, deliveryPartnerService.getAvailablePartners().size());
    }
    
    @Test
    @DisplayName("Integration: Registration and discovery workflow")
    void testRegistrationAndDiscoveryWorkflow() {
        // Phase 1: Admin registration flow
        // Represents admin/restaurant manager registering delivery partners
        DeliveryPartner[] drivers = new DeliveryPartner[5];
        for (int i = 0; i < 5; i++) {
            drivers[i] = deliveryPartnerService.register("Driver " + (i + 1));
        }
        
        // Phase 2: Query available (all initially online)
        assertEquals(5, deliveryPartnerService.getAvailablePartners().size());
        
        // Phase 3: Simulate peak hours - partners come online/offline
        for (int i = 0; i < 3; i++) {
            deliveryPartnerService.setAvailability(drivers[i].partnerId(), false);
        }
        
        // Phase 4: Dispatch checks availability multiple times
        List<DeliveryPartner> available1 = deliveryPartnerService.getAvailablePartners();
        List<DeliveryPartner> available2 = deliveryPartnerService.getAvailablePartners();
        
        // Verify: Consistent results
        assertEquals(available1.size(), available2.size());
        assertEquals(2, available1.size());
    }
    
    @Test
    @DisplayName("Integration: Handle no available partners scenario")
    void testHandleNoAvailablePartnersScenario() {
        // Setup: Partners registered but all offline
        DeliveryPartner driver1 = deliveryPartnerService.register("Driver 1");
        DeliveryPartner driver2 = deliveryPartnerService.register("Driver 2");
        
        deliveryPartnerService.setAvailability(driver1.partnerId(), false);
        deliveryPartnerService.setAvailability(driver2.partnerId(), false);
        
        // Dispatch queries available partners
        List<DeliveryPartner> available = deliveryPartnerService.getAvailablePartners();
        
        // Verify: Empty list (not null) returned
        assertNotNull(available);
        assertEquals(0, available.size());
        
        // Dispatch can handle this case
        if (available.isEmpty()) {
            System.out.println("No drivers available - order queued for later");
        }
    }
    
    @Test
    @DisplayName("Contract: Service is dependency-injectable interface")
    void testServiceAsDependency() {
        // This demonstrates how other modules inject the service
        
        // The interface contract is DeliveryPartnerService
        // Actual implementation is InMemoryDeliveryPartnerService
        DeliveryPartnerService service = deliveryPartnerService;
        
        // External modules use only the interface, not the implementation
        assertNotNull(service);
        
        // Any implementation of DeliveryPartnerService works:
        // - InMemoryDeliveryPartnerService (current)
        // - DatabaseDeliveryPartnerService (future)
        // - CachedDeliveryPartnerService (optimization)
        
        // Verify interface contract
        DeliveryPartner partner = service.register("Test");
        assertNotNull(partner);
        
        service.setAvailability(partner.partnerId(), false);
        assertEquals(0, service.getAvailablePartners().size());
    }
    
    @Test
    @DisplayName("Contract: Guarantees about returned list")
    void testContractGuaranteesAboutReturnedList() {
        // Register partners
        deliveryPartnerService.register("Driver 1");
        deliveryPartnerService.register("Driver 2");
        
        // Get available partners
        List<DeliveryPartner> partners = deliveryPartnerService.getAvailablePartners();
        
        // Guarantee 1: Never returns null
        assertNotNull(partners);
        
        // Guarantee 2: Only returns available partners
        partners.forEach(p -> assertTrue(p.available()));
        
        // Guarantee 3: List is unmodifiable (external parties can't corrupt state)
        assertThrows(UnsupportedOperationException.class, () -> {
            partners.add(new DeliveryPartner("FAKE", "Fake", true));
        });
        
        // Guarantee 4: Safe for concurrent consumers
        // Multiple threads can safely read the list simultaneously
    }
    
    @Test
    @DisplayName("Contract: Partner ID and availability status immutable")
    void testPartnerRecordImmutability() {
        // Register partner
        DeliveryPartner partner = deliveryPartnerService.register("Test Driver");
        
        // The returned record is immutable - fields cannot change
        // This is guaranteed by using Java record
        assertNotNull(partner.partnerId());
        assertNotNull(partner.name());
        assertTrue(partner.available());
        
        // To change availability, must call setAvailability on service
        // Cannot just mutate the record
        deliveryPartnerService.setAvailability(partner.partnerId(), false);
        
        // Original reference is still available=true
        assertTrue(partner.available());
        
        // New query returns updated state
        List<DeliveryPartner> available = deliveryPartnerService.getAvailablePartners();
        assertEquals(0, available.size());
    }
    
    @Test
    @DisplayName("Contract: Error handling for external modules")
    void testErrorHandlingForExternalModules() {
        // Unknown partner
        assertThrows(IllegalArgumentException.class, () -> {
            deliveryPartnerService.setAvailability("UNKNOWN_ID", false);
        });
        
        // Invalid input
        assertThrows(IllegalArgumentException.class, () -> {
            deliveryPartnerService.register(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            deliveryPartnerService.register("");
        });
        
        // These exceptions are clear, testable, and document the contract
    }
    
    @Test
    @DisplayName("Scaling: Can handle many partners")
    void testScalingWithManyPartners() {
        // Register many partners
        int partnerCount = 100;
        for (int i = 0; i < partnerCount; i++) {
            deliveryPartnerService.register("Driver " + i);
        }
        
        // Query should still be fast
        long start = System.currentTimeMillis();
        List<DeliveryPartner> available = deliveryPartnerService.getAvailablePartners();
        long duration = System.currentTimeMillis() - start;
        
        // All available initially
        assertEquals(partnerCount, available.size());
        
        // Query completes quickly (milliseconds)
        assertTrue(duration < 100, "Query took too long: " + duration + "ms");
    }
}
