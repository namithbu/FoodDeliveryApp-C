package edu.classproject.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryDeliveryPartnerService.
 * 
 * Tests cover:
 * - Happy path scenarios
 * - Edge cases (repeated toggles, unknown partners)
 * - Failure cases (invalid inputs, not found)
 * - Integration with repository
 * 
 * SOLID Principles Applied:
 * - Tests follow Single Responsibility Principle
 * - Each test method tests one behavior
 * - Use of fixtures and setup for consistency
 */
@DisplayName("Delivery Partner Service Tests")
public class InMemoryDeliveryPartnerServiceTest {
    
    private DeliveryPartnerService service;
    private DeliveryPartnerRepository repository;
    
    @BeforeEach
    void setUp() {
        // Set up fresh repository and service for each test
        repository = new InMemoryDeliveryPartnerRepository();
        service = new InMemoryDeliveryPartnerService(repository);
    }
    
    // ==================== REGISTRATION TESTS ====================
    
    @Nested
    @DisplayName("Partner Registration")
    class RegistrationTests {
        
        @Test
        @DisplayName("Happy Path: Should register partner successfully")
        void testRegisterPartnerSuccess() {
            // Given: Valid partner details
            String partnerName = "Alice Johnson";
            String email = "alice@delivery.com";
            String phone = "9876543210";
            String vehicle = "DL-2024-001";
            
            // When: Register partner
            DeliveryPartner registered = service.register(partnerName, email, phone, vehicle);
            
            // Then: Partner registered with correct data
            assertNotNull(registered);
            assertNotNull(registered.partnerId());
            assertTrue(registered.partnerId().startsWith("DP"));
            assertEquals(partnerName, registered.name());
            assertEquals(email, registered.email());
            assertEquals(phone, registered.phoneNumber());
            assertEquals(vehicle, registered.vehicleNumber());
            assertTrue(registered.available());
        }
        
        @Test
        @DisplayName("Happy Path: Multiple partners have unique IDs")
        void testMultiplePartnersHaveUniqueIds() {
            // When: Register multiple partners
            DeliveryPartner partner1 = service.register("Alice", "alice@delivery.com", "9876543210", "DL-001");
            DeliveryPartner partner2 = service.register("Bob", "bob@delivery.com", "9876543211", "DL-002");
            DeliveryPartner partner3 = service.register("Charlie", "charlie@delivery.com", "9876543212", "DL-003");
            
            // Then: All have unique IDs
            assertNotEquals(partner1.partnerId(), partner2.partnerId());
            assertNotEquals(partner2.partnerId(), partner3.partnerId());
            assertNotEquals(partner1.partnerId(), partner3.partnerId());
        }
        
        @Test
        @DisplayName("Failure Case: Null name throws exception")
        void testRegisterWithNullNameThrowsException() {
            // When: Try to register with null name
            // Then: IllegalArgumentException thrown
            assertThrows(IllegalArgumentException.class, () -> {
                service.register(null, "email@delivery.com", "9876543210", "DL-001");
            });
        }
        
        @Test
        @DisplayName("Failure Case: Blank name throws exception")
        void testRegisterWithBlankNameThrowsException() {
            // When: Try to register with empty/blank name
            // Then: IllegalArgumentException thrown
            assertThrows(IllegalArgumentException.class, () -> {
                service.register("", "email@delivery.com", "9876543210", "DL-001");
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                service.register("   ", "email@delivery.com", "9876543210", "DL-001");
            });
        }
        
        @Test
        @DisplayName("Edge Case: Whitespace trimming")
        void testRegisterWithWhitespaceTrims() {
            // When: Register partner with leading/trailing whitespace
            DeliveryPartner partner = service.register("  John Smith  ", "  john@delivery.com  ", "  9876543210  ", "  DL-001  ");
            
            // Then: All fields are trimmed
            assertEquals("John Smith", partner.name());
            assertEquals("john@delivery.com", partner.email());
            assertEquals("9876543210", partner.phoneNumber());
            assertEquals("DL-001", partner.vehicleNumber());
        }
    }
    
    // ==================== AVAILABILITY TESTS ====================
    
    @Nested
    @DisplayName("Availability Management")
    class AvailabilityTests {
        
        @Test
        @DisplayName("Happy Path: Set availability to offline")
        void testSetAvailabilityOfflineSuccess() {
            // Given: Registered partner
            DeliveryPartner partner = service.register("Alice", "alice@delivery.com", "9876543210", "DL-001");
            String partnerId = partner.partnerId();
            
            // When: Set availability to false
            service.setAvailability(partnerId, false);
            
            // Then: Partner is offline
            Optional<DeliveryPartner> retrieved = repository.findById(partnerId);
            assertTrue(retrieved.isPresent());
            assertFalse(retrieved.get().available());
        }
        
        @Test
        @DisplayName("Happy Path: Set availability back to online")
        void testSetAvailabilityOnlineSuccess() {
            // Given: Partner set to offline
            DeliveryPartner partner = service.register("Alice", "alice@delivery.com", "9876543210", "DL-001");
            String partnerId = partner.partnerId();
            service.setAvailability(partnerId, false);
            
            // When: Set availability to true
            service.setAvailability(partnerId, true);
            
            // Then: Partner is online
            Optional<DeliveryPartner> retrieved = repository.findById(partnerId);
            assertTrue(retrieved.isPresent());
            assertTrue(retrieved.get().available());
        }
        
        @Test
        @DisplayName("Failure Case: Unknown partner throws exception")
        void testSetAvailabilityForUnknownPartnerThrowsException() {
            // When: Try to set availability for non-existent partner
            // Then: IllegalArgumentException thrown
            assertThrows(IllegalArgumentException.class, () -> {
                service.setAvailability("DP_UNKNOWN", false);
            });
        }
        
        @Test
        @DisplayName("Failure Case: Null partner ID throws exception")
        void testSetAvailabilityWithNullIdThrowsException() {
            // When: Try to set availability with null ID
            // Then: IllegalArgumentException thrown
            assertThrows(IllegalArgumentException.class, () -> {
                service.setAvailability(null, false);
            });
        }
        
        @Test
        @DisplayName("Failure Case: Blank partner ID throws exception")
        void testSetAvailabilityWithBlankIdThrowsException() {
            // When: Try to set availability with empty ID
            // Then: IllegalArgumentException thrown
            assertThrows(IllegalArgumentException.class, () -> {
                service.setAvailability("", false);
            });
        }
        
        @Test
        @DisplayName("Edge Case: Repeated toggles remain stable")
        void testRepeatedTogglesCauseNoCorruption() {
            // Given: Registered partner
            DeliveryPartner partner = service.register("Alice", "alice@delivery.com", "9876543210", "DL-001");
            String partnerId = partner.partnerId();
            
            // When: Toggle availability multiple times
            service.setAvailability(partnerId, false); // offline
            service.setAvailability(partnerId, true);  // online
            service.setAvailability(partnerId, false); // offline
            service.setAvailability(partnerId, true);  // online
            
            // Then: Partner still exists and is online
            Optional<DeliveryPartner> retrieved = repository.findById(partnerId);
            assertTrue(retrieved.isPresent());
            assertTrue(retrieved.get().available());
            
            // And: Data is not corrupted
            assertEquals("Alice", retrieved.get().name());
        }
        
        @Test
        @DisplayName("Edge Case: Set same availability twice (idempotent)")
        void testSetAvailabilityIdempotence() {
            // Given: Partner online
            DeliveryPartner partner = service.register("Bob", "bob@delivery.com", "9876543210", "DL-001");
            String partnerId = partner.partnerId();
            
            // When: Set to false twice
            service.setAvailability(partnerId, false);
            service.setAvailability(partnerId, false);
            
            // Then: Status is offline (no error)
            Optional<DeliveryPartner> retrieved = repository.findById(partnerId);
            assertTrue(retrieved.isPresent());
            assertFalse(retrieved.get().available());
        }
    }
    
    // ==================== QUERY TESTS ====================
    
    @Nested
    @DisplayName("Get Available Partners")
    class QueryTests {
        
        @Test
        @DisplayName("Happy Path: Get available partners returns online only")
        void testGetAvailablePartnersReturnsOnlineOnly() {
            // Given: Mix of online and offline partners
            DeliveryPartner partner1 = service.register("Alice", "alice@delivery.com", "9876543210", "DL-001");
            DeliveryPartner partner2 = service.register("Bob", "bob@delivery.com", "9876543211", "DL-002");
            DeliveryPartner partner3 = service.register("Charlie", "charlie@delivery.com", "9876543212", "DL-003");
            
            service.setAvailability(partner2.partnerId(), false); // Bob goes offline
            
            // When: Get available partners
            List<DeliveryPartner> available = service.getAvailablePartners();
            
            // Then: Only online partners returned
            assertEquals(2, available.size());
            assertTrue(available.stream().allMatch(DeliveryPartner::available));
            assertTrue(available.stream()
                .noneMatch(p -> p.partnerId().equals(partner2.partnerId())));
        }
        
        @Test
        @DisplayName("Happy Path: Empty list when no partners registered")
        void testGetAvailablePartnersReturnsEmptyWhenNone() {
            // When: Get available partners with no registrations
            List<DeliveryPartner> available = service.getAvailablePartners();
            
            // Then: Empty list returned (not null)
            assertNotNull(available);
            assertEquals(0, available.size());
        }
        
        @Test
        @DisplayName("Happy Path: Empty list when all partners offline")
        void testGetAvailablePartnersReturnsEmptyWhenAllOffline() {
            // Given: All partners offline
            DeliveryPartner partner1 = service.register("Alice", "alice@delivery.com", "9876543210", "DL-001");
            DeliveryPartner partner2 = service.register("Bob", "bob@delivery.com", "9876543211", "DL-002");
            
            service.setAvailability(partner1.partnerId(), false);
            service.setAvailability(partner2.partnerId(), false);
            
            // When: Get available partners
            List<DeliveryPartner> available = service.getAvailablePartners();
            
            // Then: Empty list returned
            assertNotNull(available);
            assertEquals(0, available.size());
        }
        
        @Test
        @DisplayName("Failure Case: List cannot be modified externally")
        void testGetAvailablePartnersReturnsUnmodifiableList() {
            // Given: Some available partners
            service.register("Alice", "alice@delivery.com", "9876543210", "DL-001");
            service.register("Bob", "bob@delivery.com", "9876543211", "DL-002");
            
            // When: Get available partners
            List<DeliveryPartner> available = service.getAvailablePartners();
            
            // Then: List is unmodifiable
            assertThrows(UnsupportedOperationException.class, () -> {
                available.add(new DeliveryPartner("DP_TEST", "Test", "test@delivery.com", "9876543210", "DL-TEST", true));
            });
        }
        
        @Test
        @DisplayName("Edge Case: Multiple calls return consistent view")
        void testGetAvailablePartnersConsistentCalls() {
            // Given: Some partners
            service.register("Alice", "alice@delivery.com", "9876543210", "DL-001");
            service.register("Bob", "bob@delivery.com", "9876543211", "DL-002");
            
            // When: Call getAvailablePartners multiple times
            List<DeliveryPartner> call1 = service.getAvailablePartners();
            List<DeliveryPartner> call2 = service.getAvailablePartners();
            
            // Then: Same results
            assertEquals(call1.size(), call2.size());
            assertEquals(call1, call2);
        }
        
        @Test
        @DisplayName("Edge Case: Availability changes reflected in subsequent queries")
        void testGetAvailablePartnersReflectsAvailabilityChanges() {
            // Given: Partner registered
            DeliveryPartner partner = service.register("Alice", "alice@delivery.com", "9876543210", "DL-001");
            
            // Initially available
            assertEquals(1, service.getAvailablePartners().size());
            
            // When: Partner goes offline
            service.setAvailability(partner.partnerId(), false);
            
            // Then: No longer in available list
            assertEquals(0, service.getAvailablePartners().size());
            
            // When: Partner comes back online
            service.setAvailability(partner.partnerId(), true);
            
            // Then: Back in available list
            assertEquals(1, service.getAvailablePartners().size());
        }
    }
    
    // ==================== INTEGRATION TESTS ====================
    
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationTests {
        
        @Test
        @DisplayName("Complete workflow: Register, toggle, query")
        void testCompleteWorkflow() {
            // Step 1: Register multiple partners
            DeliveryPartner alice = service.register("Alice Johnson", "alice@delivery.com", "9876543210", "DL-001");
            DeliveryPartner bob = service.register("Bob Smith", "bob@delivery.com", "9876543211", "DL-002");
            DeliveryPartner charlie = service.register("Charlie Brown", "charlie@delivery.com", "9876543212", "DL-003");
            
            // Step 2: Verify all registered and available
            assertEquals(3, service.getAvailablePartners().size());
            
            // Step 3: Some go offline
            service.setAvailability(bob.partnerId(), false);
            service.setAvailability(charlie.partnerId(), false);
            
            // Step 4: Verify only one available
            List<DeliveryPartner> available = service.getAvailablePartners();
            assertEquals(1, available.size());
            assertEquals("Alice Johnson", available.get(0).name());
            
            // Step 5: Bring one back online
            service.setAvailability(bob.partnerId(), true);
            
            // Step 6: Verify two available
            available = service.getAvailablePartners();
            assertEquals(2, available.size());
        }
        
        @Test
        @DisplayName("Dispatch scenario: Find partners for assignment")
        void testDispatchScenario() {
            // Simulate dispatch system finding available drivers
            
            // Register drivers
            DeliveryPartner driver1 = service.register("Driver1", "driver1@delivery.com", "9876543210", "DL-001");
            DeliveryPartner driver2 = service.register("Driver2", "driver2@delivery.com", "9876543211", "DL-002");
            DeliveryPartner driver3 = service.register("Driver3", "driver3@delivery.com", "9876543212", "DL-003");
            
            // Some drivers are busy
            service.setAvailability(driver2.partnerId(), false);
            
            // Dispatch checks for available drivers
            List<DeliveryPartner> candidates = service.getAvailablePartners();
            
            // Assigns to first available
            assertTrue(candidates.size() >= 1);
            DeliveryPartner assignedDriver = candidates.get(0);
            assertTrue(assignedDriver.available());
        }
    }
    
    // ==================== CONCURRENCY TESTS ====================
    
    @Nested
    @DisplayName("Thread Safety")
    class ConcurrencyTests {
        
        @Test
        @DisplayName("Thread Safe: Multiple registrations concurrent")
        void testConcurrentRegistrations() throws InterruptedException {
            // When: Register partners from multiple threads
            Thread t1 = new Thread(() -> service.register("Thread1"));
            Thread t2 = new Thread(() -> service.register("Thread2"));
            Thread t3 = new Thread(() -> service.register("Thread3"));
            
            t1.start(); t2.start(); t3.start();
            t1.join(); t2.join(); t3.join();
            
            // Then: All registered successfully
            assertEquals(3, service.getAvailablePartners().size());
        }
        
        @Test
        @DisplayName("Thread Safe: Concurrent availability updates")
        void testConcurrentAvailabilityUpdates() throws InterruptedException {
            // Given: Partners
            DeliveryPartner partner = service.register("TestDriver");
            String partnerId = partner.partnerId();
            
            // When: Multiple threads toggle availability
            Thread t1 = new Thread(() -> service.setAvailability(partnerId, false));
            Thread t2 = new Thread(() -> service.setAvailability(partnerId, true));
            Thread t3 = new Thread(() -> service.setAvailability(partnerId, false));
            
            t1.start(); t2.start(); t3.start();
            t1.join(); t2.join(); t3.join();
            
            // Then: No corruption, partner still exists
            Optional<DeliveryPartner> retrieved = repository.findById(partnerId);
            assertTrue(retrieved.isPresent());
        }
    }
    
    // ==================== FACTORY TESTS ====================
    
    @Nested
    @DisplayName("Factory Method")
    class FactoryTests {
        
        @Test
        @DisplayName("Factory: Create service via factory method")
        void testCreateServiceViaFactory() {
            // When: Create service using factory method
            DeliveryPartnerService factoryService = InMemoryDeliveryPartnerService.create();
            
            // Then: Service is ready to use
            assertNotNull(factoryService);
            DeliveryPartner partner = factoryService.register("Alice");
            assertNotNull(partner);
        }
        
        @Test
        @DisplayName("Factory: Null repository in constructor throws exception")
        void testConstructorRejectsNullRepository() {
            // When: Try to create service with null repository
            // Then: IllegalArgumentException thrown
            assertThrows(IllegalArgumentException.class, () -> {
                new InMemoryDeliveryPartnerService(null);
            });
        }
    }
}
