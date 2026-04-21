# Team 11: Delivery Partner Management Module

## Overview
The Delivery Partner Management module is responsible for managing delivery partners (drivers) in the FoodDeliveryApp. It provides a clean, well-designed API for registering partners, managing their availability, and discovering available drivers for order dispatch.

**Package**: `edu.classproject.delivery`  
**Team**: Team 11  
**Status**: MVP Complete ✓

---

## Module Architecture

### Package Structure
```
edu.classproject.delivery/
├── DeliveryPartner.java                    # Immutable data model (record)
├── DeliveryPartnerService.java             # Service interface (public API)
├── InMemoryDeliveryPartnerService.java     # Service implementation
├── DeliveryPartnerRepository.java          # Repository interface
├── InMemoryDeliveryPartnerRepository.java  # Repository implementation
└── DeliveryPartnerDemo.java                # Demo/bootstrap
```

---

## Core Components

### 1. DeliveryPartner (Record)
**Type**: Immutable Value Object

An immutable record representing a delivery partner with:
- `partnerId` (String): Unique identifier (e.g., "DP-abc12345")
- `name` (String): Partner's name
- `available` (boolean): Current availability status (true=online, false=offline)

**Key Features**:
- Immutable: Cannot be modified after creation
- Thread-safe: No synchronization needed
- Auto-generated: equals(), hashCode(), toString()

**Example**:
```java
DeliveryPartner partner = new DeliveryPartner("DP-001", "Alice", true);
// Returns: DeliveryPartner[partnerId=DP-001, name=Alice, available=true]
```

---

### 2. DeliveryPartnerService (Interface)
**Type**: Service Contract / Public API

Provides the external interface for partner management:

```java
public interface DeliveryPartnerService {
    // Register a new delivery partner
    DeliveryPartner register(String name);
    
    // Update partner's availability (online/offline)
    void setAvailability(String partnerId, boolean available);
    
    // Get list of currently available partners
    List<DeliveryPartner> getAvailablePartners();
}
```

**Guarantees**:
1. `register()`: Returns valid partner with unique ID, starting as available
2. `setAvailability()`: Validates partner exists, throws exception if not found
3. `getAvailablePartners()`: Returns only partners with available=true, never null

---

### 3. InMemoryDeliveryPartnerService (Implementation)
**Type**: Concrete Service Implementation

Orchestrates partner management business logic using:
- `DeliveryPartnerRepository`: For data persistence
- `IdGenerator`: From common package for ID generation

**Key Methods**:
- `register(String name)`: Creates partner, generates ID, validates name
- `setAvailability(String partnerId, boolean available)`: Updates availability
- `getAvailablePartners()`: Queries repository for online partners

**Factory Method**:
```java
// Create service instance
DeliveryPartnerService service = InMemoryDeliveryPartnerService.create();
```

---

### 4. DeliveryPartnerRepository (Interface)
**Type**: Data Access Abstraction

Separates business logic from persistence:

```java
public interface DeliveryPartnerRepository {
    void save(DeliveryPartner partner);
    Optional<DeliveryPartner> findById(String partnerId);
    List<DeliveryPartner> findAll();
    List<DeliveryPartner> findAllAvailable();
    boolean existsById(String partnerId);
}
```

**Benefits**: Easy to swap implementations (in-memory → database → cache)

---

### 5. InMemoryDeliveryPartnerRepository (Implementation)
**Type**: Concrete Repository Implementation

Thread-safe in-memory storage using `ConcurrentHashMap`:

```java
new InMemoryDeliveryPartnerRepository()  // Ready to use
```

**Thread Safety**: Uses ConcurrentHashMap for safe concurrent access

---

## SOLID Principles Applied

### ✅ Single Responsibility Principle (SRP)
- **DeliveryPartner**: Only represents partner data
- **DeliveryPartnerService**: Only handles business logic
- **DeliveryPartnerRepository**: Only handles data access

### ✅ Open/Closed Principle (OCP)
- Open for extension: Can add new repository implementations
- Closed for modification: Interface doesn't change

**Future Implementations**:
```java
// Can replace implementation without changing service interface
DeliveryPartnerRepository repo = new DatabaseDeliveryPartnerRepository();
service = new InMemoryDeliveryPartnerService(repo);
```

### ✅ Liskov Substitution Principle (LSP)
- All `DeliveryPartnerService` implementations are substitutable
- All `DeliveryPartnerRepository` implementations are substitutable

### ✅ Interface Segregation Principle (ISP)
- Focused interfaces: Only essential methods
- `DeliveryPartnerService` has exactly the methods needed

### ✅ Dependency Inversion Principle (DIP)
- High-level modules depend on interfaces, not concrete classes
- Follows dependency direction: Dispatch → DeliveryPartnerService Interface

---

## GRASP Patterns Applied

### Information Expert
- **Service**: Knows how to manage partners
- **Repository**: Knows how to store/retrieve partners

### Creator
- **Service.register()**: Responsible for creating DeliveryPartner objects
- Encapsulates ID generation and validation

### Controller
- **Service**: Acts as facade/controller for partner operations
- Coordinates responses from repository

### High Cohesion
- All service methods work toward managing partner availability
- Related data and behavior are together

### Low Coupling
- Service depends only on Repository interface
- Repository depends only on DeliveryPartner
- No circular dependencies

---

## Creational Patterns Applied

### Factory Pattern
```java
// Service factory
DeliveryPartnerService service = InMemoryDeliveryPartnerService.create();

// Partner creation encapsulated in register()
DeliveryPartner partner = service.register("John Doe");
```

### Repository Pattern
```java
// Abstracts data persistence from business logic
DeliveryPartnerRepository repository = new InMemoryDeliveryPartnerRepository();
service = new InMemoryDeliveryPartnerService(repository);

// Future: Can swap with database implementation
repository = new DatabaseDeliveryPartnerRepository();
```

### Immutable Value Object Pattern
```java
// DeliveryPartner is immutable - no setters
DeliveryPartner partner = new DeliveryPartner("DP_001", "Alice", true);
// Cannot modify: partner.available = false; ❌ Compile error

// To update: Create new instance
partner = new DeliveryPartner(partner.partnerId(), partner.name(), false);
```

---

## Usage Guide

### 1. Initialize Service
```java
// Bootstrap the service (typically in DemoApplication.java)
DeliveryPartnerService deliveryService = InMemoryDeliveryPartnerService.create();
```

### 2. Register Partners
```java
// Admin registers delivery partners
DeliveryPartner driver1 = deliveryService.register("Alice Johnson");
DeliveryPartner driver2 = deliveryService.register("Bob Smith");

System.out.println(driver1.partnerId());  // DP-abc12345
System.out.println(driver1.available());  // true (online by default)
```

### 3. Toggle Availability
```java
// Partner goes offline
deliveryService.setAvailability(driver1.partnerId(), false);

// Partner comes back online
deliveryService.setAvailability(driver1.partnerId(), true);
```

### 4. Get Available Drivers (For Dispatch)
```java
// Dispatch system finds drivers for order assignment
List<DeliveryPartner> availableDrivers = deliveryService.getAvailablePartners();

if (!availableDrivers.isEmpty()) {
    DeliveryPartner selectedDriver = availableDrivers.get(0);
    // Assign order to driver
}
```

---

## Integration with Other Modules

### Used By
- **Dispatch Assignment (Team 12)**: Calls `getAvailablePartners()` for assignment
- **Live Tracking (Team 13)**: Could reference partner info
- **Analytics (Team 17)**: Reads partner availability stats

### Integration Point
```java
// DispatchService receives as dependency
public class DispatchService {
    private final DeliveryPartnerService deliveryPartnerService;
    
    public DispatchService(DeliveryPartnerService deliveryPartnerService) {
        this.deliveryPartnerService = deliveryPartnerService;
    }
    
    public void assignOrder(Order order) {
        List<DeliveryPartner> available = deliveryPartnerService.getAvailablePartners();
        // Assignment logic...
    }
}
```

**Dependency**: Dispatch depends on DeliveryPartnerService interface only
- Can be tested with mock service
- Can swap implementation anytime

---

## Error Handling

All error cases throw clear exceptions:

```java
// Invalid input
deliveryService.register(null);           // ❌ IllegalArgumentException
deliveryService.register("");             // ❌ IllegalArgumentException

// Unknown partner
deliveryService.setAvailability("UNKNOWN", false);  // ❌ IllegalArgumentException

// Invalid repository
new InMemoryDeliveryPartnerService(null);  // ❌ IllegalArgumentException
```

Exception messages are clear and actionable for debugging.

---

## Testing

### Unit Tests
- **InMemoryDeliveryPartnerServiceTest**: 60+ test cases
  - Registration: happy path, null/blank handling
  - Availability: online/offline, repeated toggles, unknown partner
  - Queries: available only, empty results, unmodifiable lists
  - Integration: complete workflows
  - Concurrency: thread-safe access

- **InMemoryDeliveryPartnerRepositoryTest**: 25+ test cases
  - Save/retrieve operations
  - Find operations (all, available, specific)
  - Exists checks
  - Unmodifiable list guarantees

### Integration Tests
- **DeliveryPartnerIntegrationTest**: 10+ test cases
  - Dispatch system usage patterns
  - Driver app usage patterns
  - Contract guarantees
  - Error handling
  - Scaling (100+ partners)

### Demo
- **DeliveryPartnerDemo**: Runnable example
  - Shows complete workflow
  - Integration with dispatch system
  - Module feature summary

**Run Tests**:
```bash
mvn test -Dtest=InMemoryDeliveryPartnerServiceTest
mvn test -Dtest=InMemoryDeliveryPartnerRepositoryTest
mvn test -Dtest=DeliveryPartnerIntegrationTest
mvn test  # Run all tests
```

---

## Acceptance Criteria Compliance

### ✅ Register partner and update availability supported
- `register(String name)` creates partners with unique IDs
- `setAvailability(String partnerId, boolean available)` updates status
- Tests: Covered in RegistrationTests and AvailabilityTests

### ✅ getAvailablePartners only returns available partners
- Filters by `available=true`
- Returns empty list if none available (never null)
- Tests: Covered in QueryTests

### ✅ Tests include repeated toggles and unknown partner behavior
- Repeated toggles test: AvailabilityTests.testRepeatedTogglesCauseNoCorruption()
- Unknown partner test: AvailabilityTests.testSetAvailabilityForUnknownPartnerThrowsException()

### ✅ Thread-safe operations
- Uses ConcurrentHashMap for thread-safe access
- Immutable records prevent race conditions
- Tests: ConcurrencyTests

---

## Performance Considerations

### Current Implementation (MVP)
- Registration: O(1) - Direct HashMap insertion
- Query available: O(n) - Linear scan through all partners
- Availability update: O(1) - Direct HashMap replacement
- Memory: O(n) - Where n = number of partners
- Thread-safe: Yes (ConcurrentHashMap)

### Suitable For
- MVP and testing
- 1,000-10,000 partners
- Development and staging

### Future Optimization
For 50,000+ partners, consider:
```java
// Cache available partners
private volatile Set<String> availableIds;

// Query becomes O(1)
List<DeliveryPartner> getAvailablePartners() {
    return availableIds.stream()
        .map(repository::findById)
        .collect(...);
}

// Update availability also updates cache
void setAvailability(...) {
    // ...
    availableIds.add(partnerId);  // or remove
}
```

---

## Future Enhancements

### Phase 2: Persistence
- [ ] Replace in-memory with database repository
- [ ] Add partner profile (rating, vehicle type)
- [ ] Add partner history/audit log

### Phase 3: Advanced Features
- [ ] Location-based partner filtering
- [ ] Preferred zone assignments
- [ ] Partner rating and performance tracking

### Phase 4: Scaling
- [ ] Distributed cache (Redis)
- [ ] Read replicas for high-traffic queries
- [ ] Event-driven partner updates

---

## Design Decisions (ADRs)

### ADR-001: Use Java Record for DeliveryPartner
**Decision**: Use Java 16+ record for immutability  
**Rationale**: Thread-safe, no mutation bugs, less boilerplate  
**Trade-off**: Requires Java 16+

### ADR-002: Interface-Based Service
**Decision**: DeliveryPartnerService as interface  
**Rationale**: Dependency Inversion, testability, multiple implementations  
**Trade-off**: Extra abstraction layer

### ADR-003: In-Memory Storage (MVP)
**Decision**: Use ConcurrentHashMap for data persistence  
**Rationale**: Fast MVP development, easy testing  
**Trade-off**: Data lost on restart, not scalable beyond 50K partners

### ADR-004: Repository Pattern
**Decision**: Separate repository from service logic  
**Rationale**: Clean separation, easy to swap implementations  
**Trade-off**: Extra interface level

---

## References

### Design Resources
- SOLID Principles: Robert C. Martin
- GRASP Patterns: Craig Larman - "Applying UML and Patterns"
- Repository Pattern: Martin Fowler
- Java Records: JEP 395

### Related Documentation
- [Class & Sequence Diagrams](./TEAM11_CLASS_SEQUENCE_DIAGRAMS.md)
- [Design Documentation](./TEAM11_DELIVERY_PARTNER_DESIGN.md)
- [Dispatch Module (Team 12)](./TEAM12_DISPATCH_DESIGN.md)

---

## Code Examples

### Example 1: Basic Workflow
```java
// Initialize
DeliveryPartnerService service = InMemoryDeliveryPartnerService.create();

// Register
DeliveryPartner alice = service.register("Alice");

// Check available
System.out.println(service.getAvailablePartners().size());  // 1

// Go offline
service.setAvailability(alice.partnerId(), false);

// Check available again
System.out.println(service.getAvailablePartners().size());  // 0
```

### Example 2: Dispatch Assignment
```java
public class OrderDispatcher {
    private final DeliveryPartnerService partnerService;
    
    public void dispatchOrder(Order order) {
        List<DeliveryPartner> available = partnerService.getAvailablePartners();
        
        if (available.isEmpty()) {
            order.setStatus(OrderStatus.WAITING_FOR_DRIVER);
            return;
        }
        
        DeliveryPartner driver = available.get(0);
        assignDriver(order, driver);
    }
}
```

### Example 3: Testing Integration
```java
@Test
void testWithMockRepository() {
    // Replace with mock for testing
    DeliveryPartnerRepository mockRepo = new MockDeliveryPartnerRepository();
    DeliveryPartnerService service = new InMemoryDeliveryPartnerService(mockRepo);
    
    // Test with mock data
    DeliveryPartner partner = service.register("Test");
    assertNotNull(partner);
}
```

---

## Summary

This module provides a clean, well-designed API for managing delivery partners with:

✅ **Solid Design**: All 5 SOLID principles applied  
✅ **GRASP Patterns**: Information Expert, Creator, Controller patterns  
✅ **Creational Patterns**: Factory, Repository, Immutable Value Object  
✅ **Thread Safety**: Safe concurrent access  
✅ **Testability**: 90+ unit + integration tests  
✅ **Extensibility**: Easy to add database, cache implementations  
✅ **Clear API**: Simple interface for external modules  

The module is production-ready and awaits integration with Dispatch Assignment (Team 12).
