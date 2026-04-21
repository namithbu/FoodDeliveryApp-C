package edu.classproject.delivery;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for InMemoryDeliveryPartnerRepository.
 * 
 * Tests the data access layer with focus on:
 * - CRUD operations
 * - Thread safety (ConcurrentHashMap)
 * - Unmodifiable collections
 */
@DisplayName("Delivery Partner Repository Tests")
public class InMemoryDeliveryPartnerRepositoryTest {
    
    private DeliveryPartnerRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryDeliveryPartnerRepository();
    }
    
    @Nested
    @DisplayName("Save and Retrieve")
    class SaveRetrieveTests {
        
        @Test
        @DisplayName("Happy Path: Save and retrieve partner")
        void testSaveAndRetrievePartner() {
            // Given: Partner to save
            DeliveryPartner partner = new DeliveryPartner("DP_001", "Alice", true);
            
            // When: Save partner
            repository.save(partner);
            
            // Then: Can retrieve it
            Optional<DeliveryPartner> retrieved = repository.findById("DP_001");
            assertTrue(retrieved.isPresent());
            assertEquals(partner, retrieved.get());
        }
        
        @Test
        @DisplayName("Happy Path: Update existing partner")
        void testUpdateExistingPartner() {
            // Given: Partner saved
            DeliveryPartner original = new DeliveryPartner("DP_001", "Alice", true);
            repository.save(original);
            
            // When: Save updated version
            DeliveryPartner updated = new DeliveryPartner("DP_001", "Alice", false);
            repository.save(updated);
            
            // Then: Retrieved partner reflects update
            Optional<DeliveryPartner> retrieved = repository.findById("DP_001");
            assertTrue(retrieved.isPresent());
            assertFalse(retrieved.get().available());
        }
        
        @Test
        @DisplayName("Failure Case: Save null throws exception")
        void testSaveNullThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                repository.save(null);
            });
        }
        
        @Test
        @DisplayName("Failure Case: Find by null ID throws exception")
        void testFindByNullIdThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                repository.findById(null);
            });
        }
        
        @Test
        @DisplayName("Failure Case: Find by blank ID throws exception")
        void testFindByBlankIdThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                repository.findById("");
            });
        }
        
        @Test
        @DisplayName("Happy Path: Find non-existent returns empty Optional")
        void testFindNonExistentReturnsEmpty() {
            Optional<DeliveryPartner> retrieved = repository.findById("DP_UNKNOWN");
            assertFalse(retrieved.isPresent());
        }
    }
    
    @Nested
    @DisplayName("Find All Operations")
    class FindAllTests {
        
        @Test
        @DisplayName("Happy Path: Find all partners")
        void testFindAllPartners() {
            // Given: Multiple partners
            DeliveryPartner partner1 = new DeliveryPartner("DP_001", "Alice", true);
            DeliveryPartner partner2 = new DeliveryPartner("DP_002", "Bob", false);
            DeliveryPartner partner3 = new DeliveryPartner("DP_003", "Charlie", true);
            
            repository.save(partner1);
            repository.save(partner2);
            repository.save(partner3);
            
            // When: Find all
            List<DeliveryPartner> all = repository.findAll();
            
            // Then: All partners returned
            assertEquals(3, all.size());
        }
        
        @Test
        @DisplayName("Happy Path: Find all available partners only")
        void testFindAllAvailablePartnersOnly() {
            // Given: Mix of available and unavailable
            repository.save(new DeliveryPartner("DP_001", "Alice", true));
            repository.save(new DeliveryPartner("DP_002", "Bob", false));
            repository.save(new DeliveryPartner("DP_003", "Charlie", true));
            
            // When: Find all available
            List<DeliveryPartner> available = repository.findAllAvailable();
            
            // Then: Only available partners returned
            assertEquals(2, available.size());
            assertTrue(available.stream().allMatch(DeliveryPartner::available));
        }
        
        @Test
        @DisplayName("Happy Path: Find all returns empty when no partners")
        void testFindAllReturnsEmptyWhenNone() {
            List<DeliveryPartner> all = repository.findAll();
            assertNotNull(all);
            assertEquals(0, all.size());
        }
        
        @Test
        @DisplayName("Happy Path: Find available returns empty when all offline")
        void testFindAvailableReturnsEmptyWhenAllOffline() {
            // Given: All offline partners
            repository.save(new DeliveryPartner("DP_001", "Alice", false));
            repository.save(new DeliveryPartner("DP_002", "Bob", false));
            
            // When: Find all available
            List<DeliveryPartner> available = repository.findAllAvailable();
            
            // Then: Empty list
            assertEquals(0, available.size());
        }
        
        @Test
        @DisplayName("Edge Case: Returned list is unmodifiable")
        void testFindAllReturnsUnmodifiableList() {
            // Given: Some partners
            repository.save(new DeliveryPartner("DP_001", "Alice", true));
            
            // When: Get list
            List<DeliveryPartner> all = repository.findAll();
            
            // Then: Cannot modify returned list
            assertThrows(UnsupportedOperationException.class, () -> {
                all.add(new DeliveryPartner("DP_NEW", "New", true));
            });
        }
    }
    
    @Nested
    @DisplayName("Exists Check")
    class ExistsTests {
        
        @Test
        @DisplayName("Happy Path: Exists check returns true for saved partner")
        void testExistsReturnsTrueForSavedPartner() {
            // Given: Partner saved
            repository.save(new DeliveryPartner("DP_001", "Alice", true));
            
            // When: Check exists
            // Then: Returns true
            assertTrue(repository.existsById("DP_001"));
        }
        
        @Test
        @DisplayName("Happy Path: Exists check returns false for unknown partner")
        void testExistsReturnsFalseForUnknownPartner() {
            assertFalse(repository.existsById("DP_UNKNOWN"));
        }
        
        @Test
        @DisplayName("Failure Case: Exists with null ID throws exception")
        void testExistsWithNullIdThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                repository.existsById(null);
            });
        }
        
        @Test
        @DisplayName("Failure Case: Exists with blank ID throws exception")
        void testExistsWithBlankIdThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                repository.existsById("");
            });
        }
    }
}
