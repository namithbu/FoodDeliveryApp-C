package edu.classproject.delivery;

/**
 * Demo/Bootstrap for Delivery Partner Management module.
 * 
 * Shows complete workflow:
 * 1. Register delivery partners
 * 2. Toggle availability
 * 3. Query available partners (used by dispatch system)
 * 
 * This is the entry point for integrating with other modules.
 * 
 * Usage Pattern:
 * - Create service once at application startup
 * - Inject service into DispatchService and other consumers
 * - Service manages complete partner lifecycle
 */
public class DeliveryPartnerDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Delivery Partner Management Module Demo ===\n");
        
        // Step 1: Create service instance using factory method
        // Factory Pattern: Encapsulates service creation
        System.out.println("Step 1: Initialize Delivery Partner Service");
        DeliveryPartnerService partnerService = InMemoryDeliveryPartnerService.create();
        System.out.println("✓ Service initialized\n");
        
        // Step 2: Register delivery partners
        // Factory Pattern: register() creates partner records
        System.out.println("Step 2: Register Delivery Partners");
        DeliveryPartner driver1 = partnerService.register("Alice Johnson");
        DeliveryPartner driver2 = partnerService.register("Bob Smith");
        DeliveryPartner driver3 = partnerService.register("Charlie Brown");
        DeliveryPartner driver4 = partnerService.register("Diana Martinez");
        
        System.out.println("Registered Partners:");
        printPartner(driver1);
        printPartner(driver2);
        printPartner(driver3);
        printPartner(driver4);
        System.out.println();
        
        // Step 3: Query all available partners
        System.out.println("Step 3: Get Available Partners (Initial)");
        System.out.println("Available drivers: " + partnerService.getAvailablePartners().size());
        partnerService.getAvailablePartners()
            .forEach(p -> System.out.println("  → " + p.name() + " (" + p.partnerId() + ")"));
        System.out.println();
        
        // Step 4: Simulate partners going offline
        System.out.println("Step 4: Simulate Availability Changes");
        System.out.println("Alice takes a break (offline)...");
        partnerService.setAvailability(driver1.partnerId(), false);
        
        System.out.println("Bob takes a break (offline)...");
        partnerService.setAvailability(driver2.partnerId(), false);
        System.out.println();
        
        // Step 5: Query available partners again
        System.out.println("Step 5: Get Available Partners (After Changes)");
        var availablePartners = partnerService.getAvailablePartners();
        System.out.println("Available drivers: " + availablePartners.size());
        availablePartners.forEach(p -> System.out.println("  → " + p.name() + " (" + p.partnerId() + ")"));
        System.out.println();
        
        // Step 6: Dispatch system assigns order to available driver
        System.out.println("Step 6: Dispatch Assignment (Example)");
        System.out.println("Order needs delivery...");
        if (!availablePartners.isEmpty()) {
            DeliveryPartner assignedDriver = availablePartners.get(0);
            System.out.println("✓ Order assigned to: " + assignedDriver.name() + " (" + assignedDriver.partnerId() + ")");
        } else {
            System.out.println("✗ No available drivers for assignment");
        }
        System.out.println();
        
        // Step 7: Alice comes back online
        System.out.println("Step 7: Partners Back Online");
        System.out.println("Alice returns from break (online)...");
        partnerService.setAvailability(driver1.partnerId(), true);
        System.out.println();
        
        // Step 8: Final state
        System.out.println("Step 8: Final Available Partners");
        var finalAvailable = partnerService.getAvailablePartners();
        System.out.println("Available drivers: " + finalAvailable.size());
        finalAvailable.forEach(p -> System.out.println("  → " + p.name() + " (" + p.partnerId() + ")"));
        System.out.println();
        
        // Step 9: Module features summary
        System.out.println("=== Module Features Summary ===");
        System.out.println("✓ Partner Registration: Unique IDs, default online status");
        System.out.println("✓ Availability Management: Toggle online/offline status");
        System.out.println("✓ Partner Discovery: Query only available partners");
        System.out.println("✓ Thread-Safe: Concurrent access support");
        System.out.println("✓ Immutable: DeliveryPartner records prevent mutation");
        System.out.println("✓ Repository Pattern: Swappable storage implementation");
        System.out.println("✓ Factory Pattern: create() method for service instantiation");
        System.out.println("✓ Dependency Injection: Interface-based contracts");
        System.out.println();
        
        // Step 10: Integration example
        System.out.println("=== Integration with Dispatch Module ===");
        System.out.println("DispatchService depends on DeliveryPartnerService interface");
        System.out.println("Usage in DispatchService:");
        System.out.println("  1. Get available partners: availablePartners = partnerService.getAvailablePartners()");
        System.out.println("  2. Select partner: selectedDriver = availablePartners.get(0)");
        System.out.println("  3. Create dispatch: dispatchService.assign(order, selectedDriver)");
        System.out.println();
        
        System.out.println("=== Demo Complete ===");
    }
    
    /**
     * Helper method to print partner details.
     */
    private static void printPartner(DeliveryPartner partner) {
        String status = partner.available() ? "● Online" : "● Offline";
        System.out.println("  " + status + " | " + partner.name() + " | " + partner.partnerId());
    }
}
