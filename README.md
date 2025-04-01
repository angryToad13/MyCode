# Detailed Inventory Management System Specifications

## Database Structure & Entity Relationships

### User Entity
```
Table: users
- id (Long, PK)
- username (String, unique, not null)
- password (String, not null, encrypted)
- email (String, unique, not null)
- role (Enum: ADMIN, USER, LOGISTICS_COORDINATOR)
- firstName (String)
- lastName (String)
- phoneNumber (String)
- createdAt (LocalDateTime)
- lastLogin (LocalDateTime)
- active (Boolean)
```

### Vehicle Entity
```
Table: vehicles
- id (Long, PK)
- registrationNumber (String, unique, not null)
- vehicleType (String)
- capacity (Double) // in tons or cubic meters
- model (String)
- manufacturer (String)
- manufacturingYear (Integer)
- currentStatus (Enum: AVAILABLE, IN_TRANSIT, MAINTENANCE)
- lastMaintenanceDate (LocalDate)
- owner (User, FK to users.id)
- currentLocation (String)
- gpsEnabled (Boolean)
- fuelEfficiency (Double)
- createdAt (LocalDateTime)
- updatedAt (LocalDateTime)
```

### Category Entity
```
Table: categories
- id (Long, PK)
- name (String, unique, not null)
- description (String)
- storageRequirements (String)
- isPerishable (Boolean)
- shelfLife (Integer) // in days, null if not perishable
- createdBy (User, FK to users.id)
- createdAt (LocalDateTime)
- updatedAt (LocalDateTime)
- active (Boolean)
```

### Inventory Entity
```
Table: inventory_items
- id (Long, PK)
- name (String, not null)
- category (Category, FK to categories.id)
- quantity (Integer, not null)
- unitOfMeasure (String)
- location (String) // warehouse location code
- batchNumber (String)
- expiryDate (LocalDate) // null if not applicable
- qualityStatus (Enum: GOOD, DAMAGED, EXPIRED, UNDER_INSPECTION)
- acquisitionDate (LocalDate)
- lastQualityCheck (LocalDateTime)
- minimumStockLevel (Integer)
- price (BigDecimal)
- supplier (String)
- notes (String)
- createdAt (LocalDateTime)
- updatedAt (LocalDateTime)
```

### VehicleMovement Entity
```
Table: vehicle_movements
- id (Long, PK)
- vehicle (Vehicle, FK to vehicles.id)
- departureLocation (String, not null)
- destinationLocation (String, not null)
- departureTime (LocalDateTime)
- estimatedArrivalTime (LocalDateTime)
- actualArrivalTime (LocalDateTime)
- status (Enum: SCHEDULED, IN_TRANSIT, COMPLETED, DELAYED)
- assignedCoordinator (User, FK to users.id)
- inventoryItems (List<InventoryItem>) // many-to-many relationship through a join table
- notes (String)
- createdAt (LocalDateTime)
- updatedAt (LocalDateTime)
```

### Entity Relationships
- One User can register/own multiple Vehicles (One-to-Many)
- A Category can have multiple Inventory Items (One-to-Many)
- A Vehicle can have multiple VehicleMovements (One-to-Many)
- A VehicleMovement can involve multiple Inventory Items (Many-to-Many)
- Each User has one Role (One-to-One)
- A LogisticsCoordinator (User) can monitor multiple VehicleMovements (One-to-Many)

## Form Validations

### User Registration Form
```
- Username: Required, 5-20 characters, alphanumeric
- Password: Required, min 8 characters, must contain at least one uppercase, one lowercase, one number, one special character
- Email: Required, valid email format
- First Name: Required, 2-50 characters, letters only
- Last Name: Required, 2-50 characters, letters only
- Phone Number: Required, valid phone format (with country code validation)
```

### Vehicle Registration Form
```
- Registration Number: Required, unique, format validation based on country standards
- Vehicle Type: Required, dropdown selection
- Capacity: Required, positive number
- Model: Required, 2-50 characters
- Manufacturer: Required, dropdown selection
- Manufacturing Year: Required, between 1950 and current year
- Current Status: Required, dropdown selection
- Last Maintenance Date: Optional, date not in future
- Current Location: Required if status is not MAINTENANCE
- GPS Enabled: Boolean checkbox
- Fuel Efficiency: Optional, positive number
```

### Category Creation Form (Admin)
```
- Name: Required, 3-50 characters, unique
- Description: Optional, max 500 characters
- Storage Requirements: Optional, max 500 characters
- Is Perishable: Boolean checkbox
- Shelf Life: Required if isPerishable is true, positive integer
```

### Inventory Item Form
```
- Name: Required, 3-100 characters
- Category: Required, dropdown selection from available categories
- Quantity: Required, positive integer
- Unit of Measure: Required, dropdown selection
- Location: Required, valid warehouse location code format
- Batch Number: Optional, alphanumeric
- Expiry Date: Required if category is perishable, date in future
- Quality Status: Required, dropdown selection
- Acquisition Date: Required, not in future
- Minimum Stock Level: Optional, positive integer
- Price: Optional, positive decimal number
- Supplier: Optional, 3-100 characters
- Notes: Optional, max 1000 characters
```

### Vehicle Movement Form
```
- Vehicle: Required, dropdown selection from available vehicles
- Departure Location: Required, valid location format
- Destination Location: Required, valid location format
- Departure Time: Required, date-time not in past
- Estimated Arrival Time: Required, date-time after departure time
- Status: Required, dropdown selection
- Assigned Coordinator: Required, dropdown selection from users with LOGISTICS_COORDINATOR role
- Inventory Items: Optional multiple selection from available inventory
- Notes: Optional, max 1000 characters
```

## Java Stream Filters to be Implemented

1. **Filter available vehicles:**
```java
List<Vehicle> availableVehicles = vehicles.stream()
    .filter(v -> v.getCurrentStatus() == VehicleStatus.AVAILABLE)
    .collect(Collectors.toList());
```

2. **Filter vehicles by owner:**
```java
List<Vehicle> userVehicles = vehicles.stream()
    .filter(v -> v.getOwner().getId().equals(userId))
    .collect(Collectors.toList());
```

3. **Filter inventory by category:**
```java
List<InventoryItem> categoryItems = inventory.stream()
    .filter(item -> item.getCategory().getId().equals(categoryId))
    .collect(Collectors.toList());
```

4. **Filter low stock inventory items:**
```java
List<InventoryItem> lowStockItems = inventory.stream()
    .filter(item -> item.getQuantity() < item.getMinimumStockLevel())
    .sorted(Comparator.comparing(item -> 
        (double)item.getQuantity() / item.getMinimumStockLevel()))
    .collect(Collectors.toList());
```

5. **Filter expired or soon-to-expire inventory:**
```java
LocalDate today = LocalDate.now();
LocalDate warningDate = today.plusDays(7);

List<InventoryItem> expiringItems = inventory.stream()
    .filter(item -> item.getExpiryDate() != null 
        && (item.getExpiryDate().isBefore(today) 
            || item.getExpiryDate().isBefore(warningDate)))
    .sorted(Comparator.comparing(InventoryItem::getExpiryDate))
    .collect(Collectors.toList());
```

6. **Filter vehicles by maintenance requirement:**
```java
LocalDate maintenanceThreshold = LocalDate.now().minusMonths(3);

List<Vehicle> vehiclesNeedingMaintenance = vehicles.stream()
    .filter(v -> v.getLastMaintenanceDate() == null 
        || v.getLastMaintenanceDate().isBefore(maintenanceThreshold))
    .sorted(Comparator.comparing(
        v -> v.getLastMaintenanceDate() == null ? 
            LocalDate.of(1900, 1, 1) : v.getLastMaintenanceDate()))
    .collect(Collectors.toList());
```

7. **Filter active vehicle movements by coordinator:**
```java
List<VehicleMovement> coordinatorMovements = movements.stream()
    .filter(m -> m.getAssignedCoordinator().getId().equals(coordinatorId))
    .filter(m -> m.getStatus() == MovementStatus.IN_TRANSIT 
        || m.getStatus() == MovementStatus.SCHEDULED)
    .sorted(Comparator.comparing(VehicleMovement::getDepartureTime))
    .collect(Collectors.toList());
```

8. **Group inventory by quality status for analytics:**
```java
Map<QualityStatus, Long> inventoryByQuality = inventory.stream()
    .collect(Collectors.groupingBy(
        InventoryItem::getQualityStatus, 
        Collectors.counting()));
```

9. **Calculate total inventory value by category:**
```java
Map<Category, BigDecimal> valueByCategory = inventory.stream()
    .collect(Collectors.groupingBy(
        InventoryItem::getCategory,
        Collectors.reducing(
            BigDecimal.ZERO,
            item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())),
            BigDecimal::add)));
```

10. **Find delayed vehicles:**
```java
LocalDateTime now = LocalDateTime.now();

List<VehicleMovement> delayedMovements = movements.stream()
    .filter(m -> m.getStatus() == MovementStatus.IN_TRANSIT)
    .filter(m -> m.getEstimatedArrivalTime().isBefore(now))
    .sorted(Comparator.comparing(VehicleMovement::getEstimatedArrivalTime))
    .collect(Collectors.toList());
```

These specifications provide a solid foundation for developing the inventory management system with Spring Boot and Angular, focusing on the data structure, validations, and business logic required for the three roles: User, Admin, and Logistics Coordinator.​​​​​​​​​​​​​​​​


#CLAUDE

# 15-Day Inventory Management System Development Plan

Here's a comprehensive 15-day plan for developing an inventory management system with Spring Boot backend and Angular frontend:

## Day 1: Project Setup and Requirements Analysis
- Set up Spring Boot project structure with necessary dependencies
- Set up Angular project with required modules
- Create Git repository for version control
- Define detailed requirements for each user role (User, Admin, Logistics Coordinator)
- Create database schema design

## Day 2: Database Design and User Authentication
- Implement database models/entities (Users, Vehicles, Categories, Inventory, Shipments)
- Set up Spring Security for authentication
- Create user registration and login API endpoints
- Implement role-based authorization

## Day 3: User Management Backend
- Develop user CRUD operations
- Implement role management system
- Create API endpoints for user profile management
- Write unit tests for user services

## Day 4: Vehicle Registration Backend
- Implement vehicle registration functionality
- Create vehicle tracking data models
- Develop API endpoints for vehicle management
- Write unit tests for vehicle services

## Day 5: Category and Inventory Backend
- Implement category management for admin
- Develop inventory tracking system
- Create API endpoints for inventory operations
- Write unit tests for inventory services

## Day 6: Logistics and Monitoring Backend
- Implement vehicle movement tracking
- Develop goods quality assessment system
- Create API endpoints for logistics operations
- Write unit tests for logistics services

## Day 7: Analytics Backend
- Implement data aggregation for analytics
- Create dashboard data API endpoints
- Develop reporting functionality
- Write unit tests for analytics services

## Day 8: Frontend Authentication & User Management
- Create login and registration pages
- Implement authentication service in Angular
- Develop user profile management components
- Design responsive layouts for user management

## Day 9: Vehicle Management Frontend
- Create vehicle registration components
- Implement vehicle listing and details views
- Develop vehicle tracking interface
- Design responsive layouts for vehicle management

## Day 10: Inventory Management Frontend
- Create category management components for admin
- Implement inventory listing and details views
- Develop inventory CRUD operations interface
- Design responsive layouts for inventory management

## Day 11: Logistics Monitoring Frontend
- Implement vehicle movement tracking interface
- Create goods quality assessment components
- Develop shipment monitoring dashboard
- Design responsive layouts for logistics operations

## Day 12: Analytics Dashboard Frontend
- Create admin analytics dashboard
- Implement data visualization components
- Develop reporting interface
- Design responsive layouts for analytics views

## Day 13: Integration Testing
- Perform full integration testing of backend and frontend
- Test all user roles and functionalities
- Fix identified bugs and issues
- Optimize API performance

## Day 14: UI/UX Refinement and Final Testing
- Refine user interface and experience
- Implement responsive design for mobile compatibility
- Conduct final round of testing for all functionality
- Fix any remaining issues

## Day 15: Deployment and Documentation
- Prepare deployment packages for backend and frontend
- Deploy application to target environment
- Create user documentation for each role
- Prepare technical documentation for future maintenance

Would you like me to elaborate on any specific part of this plan or provide more details for any particular phase?​​​​​​​​​​​​​​​​
