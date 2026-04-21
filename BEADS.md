# Fast Eat Backend - Implementation BEADS
---

##  Status Legend

| Symbol | Status | Meaning |
|--------|--------|---------|
|  | **READY** | No blockers, can start immediately |
|  | **BLOCKED** | Waiting on dependencies |
|  | **IN_PROGRESS** | Currently being worked on |
|  | **COMPLETED** | Done and verified |

---

##  Project Overview

**Total Beads**: 37  
**Phases**: 4  

---

##  Table of Contents

- [Phase 1: Foundation Layer (7 beads)](#phase-1-foundation-layer)
- [Phase 2: Domain Layer (8 beads)](#phase-2-domain-layer)
- [Phase 3: Business Logic Layer (12 beads)](#phase-3-business-logic-layer)
- [Phase 4: Polish & Testing (10 beads)](#phase-4-polish--testing)
- [Progress Tracking](#progress-tracking)
- [Dependency Graph](#dependency-graph)
- [Usage Instructions](#usage-instructions)
- [Notes](#notes)

---

# Phase 1: Foundation Layer

**Focus**: Project scaffolding, infrastructure, database setup, core configuration

---

## BEAD-1.1: Maven Project Initialization

**Status**:  READY  
**Dependencies**: None  
**Phase**: 1 - Foundation  

### Description
Initialize the Spring Boot Maven project with all necessary dependencies, plugins, and configurations. Set up the base project structure following Spring Boot best practices.

### Tasks
- [x] Create Maven project with Spring Initializr configuration
- [x] Add Spring Boot Starter dependencies (Web, Data JPA, Security, Validation)
- [x] Add PostgreSQL driver dependency
- [x] Add JWT dependencies (jjwt-api, jjwt-impl, jjwt-jackson)
- [x] Add Lombok for reducing boilerplate
- [x] Add SpringDoc OpenAPI for API documentation
- [x] Add Spring Boot DevTools for development
- [x] Add testing dependencies (JUnit 5, Mockito, TestContainers)
- [x] Configure Maven compiler plugin (Java 17+)
- [x] Configure Maven Surefire plugin for tests
- [x] Configure Maven Failsafe plugin for integration tests
- [ ] Set up project package structure (controller, service, repository, model, dto, config, security, exception)
- [ ] Create application.properties/application.yml skeleton
- [ ] Verify project builds successfully with `mvn clean install`

### Acceptance Criteria
- [ ] Project compiles without errors
- [ ] All dependencies resolve correctly
- [ ] Maven build succeeds
- [ ] Package structure follows Spring Boot conventions
- [ ] Can run with `mvn spring-boot:run`
- [ ] Application starts without errors (may fail DB connection - expected)

### Related Files
```
pom.xml
src/main/java/com/fasteat/
├── FastEatApplication.java
├── controller/
├── service/
├── repository/
├── model/
│   ├── entity/
│   └── enums/
├── dto/
│   ├── request/
│   └── response/
├── config/
├── security/
├── exception/
└── util/
src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
```

---

## BEAD-1.2: Docker Compose Setup

**Status**:  READY  
**Dependencies**: None  
**Phase**: 1 - Foundation  

### Description
Create Docker Compose configuration for local development environment including PostgreSQL database, optional PgAdmin, and future application container.

### Tasks
- [ ] Create `docker-compose.yml` in project root
- [ ] Define PostgreSQL 15+ service with persistent volume
- [ ] Configure PostgreSQL environment variables (POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD)
- [ ] Expose PostgreSQL on port 5432
- [ ] Add PgAdmin service for database management (optional)
- [ ] Configure network bridge for service communication
- [ ] Create `.env.example` file for environment variables
- [ ] Add `docker-compose.override.yml` to .gitignore
- [ ] Create `scripts/docker-init.sh` for initial setup
- [ ] Document Docker commands in README
- [ ] Test database connectivity with `psql` or PgAdmin

### Acceptance Criteria
- [ ] `docker-compose up -d` starts all services successfully
- [ ] PostgreSQL is accessible on localhost:5432
- [ ] Can connect to database with configured credentials
- [ ] Data persists between container restarts
- [ ] PgAdmin (if included) accessible on configured port
- [ ] Services can be stopped and restarted without data loss

### Related Files
```
docker-compose.yml
docker-compose.override.yml.example
.env.example
scripts/docker-init.sh
scripts/docker-clean.sh
```

---

## BEAD-1.3: Application Configuration

**Status**:  READY  
**Dependencies**: BEAD-1.1, BEAD-1.2  
**Phase**: 1 - Foundation  

### Description
Configure Spring Boot application properties for different environments (dev, test, prod). Set up database connection, JPA/Hibernate, logging, and profile-specific configurations.

### Tasks
- [ ] Configure database connection properties (URL, username, password, driver)
- [ ] Set up JPA/Hibernate properties (dialect, ddl-auto, show-sql, format-sql)
- [ ] Configure Hibernate naming strategy (SpringPhysicalNamingStrategy)
- [ ] Set up connection pool configuration (HikariCP)
- [ ] Configure logging levels (Spring, Hibernate, application packages)
- [ ] Create profile-specific property files (dev, test, prod)
- [ ] Set up application name and server port
- [ ] Configure Jackson JSON serialization properties
- [ ] Add JWT configuration properties (secret, expiration, issuer)
- [ ] Configure CORS settings
- [ ] Set up actuator endpoints for health checks
- [ ] Create `@ConfigurationProperties` class for custom properties
- [ ] Add validation for required configuration values
- [ ] Document all configuration properties

### Acceptance Criteria
- [ ] Application starts with dev profile successfully
- [ ] Database connection is established
- [ ] JPA repository operations work
- [ ] Logging outputs at correct levels
- [ ] Profile switching works correctly
- [ ] Configuration properties are validated on startup
- [ ] Health check endpoint returns 200 OK

### Related Files
```
src/main/resources/
├── application.yml
├── application-dev.yml
├── application-test.yml
├── application-prod.yml
└── logback-spring.xml
src/main/java/com/fasteat/config/
├── ApplicationProperties.java
├── JpaConfig.java
└── LoggingConfig.java
```

---

## BEAD-1.4: Core Entity Base Classes

**Status**:  BLOCKED (by BEAD-1.3)  
**Dependencies**: BEAD-1.3  
**Phase**: 1 - Foundation  

### Description
Create base entity classes and common entity utilities. Implement audit fields (createdAt, updatedAt) using JPA lifecycle callbacks and entity listeners.

### Tasks
- [ ] Create `BaseEntity` abstract class with common fields (id, createdAt, updatedAt)
- [ ] Add `@MappedSuperclass` annotation
- [ ] Implement `@EntityListeners` with `AuditingEntityListener`
- [ ] Add `@CreatedDate` and `@LastModifiedDate` annotations
- [ ] Configure `@EnableJpaAuditing` in configuration class
- [ ] Implement `equals()` and `hashCode()` using entity identifier
- [ ] Create `SoftDeletableEntity` for soft delete pattern (optional)
- [ ] Add `@PrePersist` and `@PreUpdate` hooks for validation
- [ ] Create UUID generation strategy for IDs
- [ ] Document entity design patterns
- [ ] Create unit tests for base entity behavior

### Acceptance Criteria
- [ ] BaseEntity compiles without errors
- [ ] Audit fields are automatically populated
- [ ] Entity equality works correctly
- [ ] UUID generation is consistent
- [ ] Extending classes inherit base functionality
- [ ] Tests verify audit behavior
- [ ] Timestamps use UTC timezone

### Related Files
```
src/main/java/com/fasteat/model/entity/
├── BaseEntity.java
├── SoftDeletableEntity.java (optional)
└── listener/
    └── AuditEntityListener.java
src/main/java/com/fasteat/config/
└── JpaAuditingConfig.java
src/test/java/com/fasteat/model/entity/
└── BaseEntityTest.java
```

---

## BEAD-1.5: Database Schema Design

**Status**:  BLOCKED (by BEAD-1.4)  
**Dependencies**: BEAD-1.4  
**Phase**: 1 - Foundation  

### Description
Design and document the complete database schema for Fast Eat. Create SQL migration scripts or Liquibase/Flyway changesets for version-controlled schema evolution.

### Tasks
- [ ] Design Users table with authentication fields
- [ ] Design Restaurants table with business information
- [ ] Design MenuItems table with pricing and categories
- [ ] Design Ingredients table with allergen information
- [ ] Design MenuItemIngredients junction table
- [ ] Design Orders table with status tracking
- [ ] Design OrderItems table with customizations
- [ ] Design OrderItemCustomizations junction table
- [ ] Design Reviews table with ratings
- [ ] Design Addresses table for delivery
- [ ] Define all primary keys, foreign keys, and indexes
- [ ] Create database constraints (unique, not null, check)
- [ ] Add database-level enums or check constraints for status fields
- [ ] Create ER diagram documenting relationships
- [ ] Choose migration tool (Flyway recommended)
- [ ] Create initial migration script (V1__init_schema.sql)
- [ ] Test migration rollback capability
- [ ] Document schema versioning strategy

### Acceptance Criteria
- [ ] All tables defined with correct columns and types
- [ ] Foreign key relationships established
- [ ] Indexes created for query optimization
- [ ] Constraints enforce data integrity
- [ ] Migration script runs successfully
- [ ] Schema can be rolled back
- [ ] ER diagram accurately represents schema
- [ ] No orphan records possible

### Related Files
```
src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__add_indexes.sql
└── V3__seed_data.sql
docs/
├── database-schema.md
└── er-diagram.png
```

---

## BEAD-1.6: JPA Entity Mapping

**Status**:  BLOCKED (by BEAD-1.5)  
**Dependencies**: BEAD-1.5  
**Phase**: 1 - Foundation  

### Description
Create JPA entity classes mapping to the database schema. Define all relationships (OneToMany, ManyToOne, ManyToMany), cascading rules, and fetch strategies.

### Tasks
- [ ] Create User entity with roles and authentication fields
- [ ] Create Restaurant entity with business fields
- [ ] Create MenuItem entity with pricing and categories
- [ ] Create Ingredient entity with allergen information
- [ ] Create Order entity with status and timestamps
- [ ] Create OrderItem entity with customizations
- [ ] Create Review entity with ratings
- [ ] Create Address entity for deliveries
- [ ] Define @OneToMany relationships (User→Orders, Restaurant→MenuItems)
- [ ] Define @ManyToOne relationships (Order→User, MenuItem→Restaurant)
- [ ] Define @ManyToMany relationships (MenuItemIngredient)
- [ ] Configure cascade types appropriately
- [ ] Set fetch types (LAZY vs EAGER) strategically
- [ ] Add @JsonIgnore or @JsonManagedReference/@JsonBackReference to prevent cycles
- [ ] Create enum classes (OrderStatus, UserRole, MenuCategory, AllergenType)
- [ ] Add javax.validation annotations (@NotNull, @Size, @Email, @Min, @Max)
- [ ] Implement toString() methods safely (avoid lazy loading)
- [ ] Write unit tests for entity relationships

### Acceptance Criteria
- [ ] All entities compile without errors
- [ ] Relationships are bidirectional where needed
- [ ] Cascade operations work correctly
- [ ] Lazy loading doesn't cause N+1 queries
- [ ] Validation annotations enforce rules
- [ ] Enums are properly mapped
- [ ] JSON serialization doesn't cause infinite recursion
- [ ] Tests verify entity behavior
- [ ] Schema validation passes (validate mode)

### Related Files
```
src/main/java/com/fasteat/model/entity/
├── User.java
├── Restaurant.java
├── MenuItem.java
├── Ingredient.java
├── Order.java
├── OrderItem.java
├── Review.java
└── Address.java
src/main/java/com/fasteat/model/enums/
├── OrderStatus.java
├── UserRole.java
├── MenuCategory.java
├── AllergenType.java
└── DeliveryStatus.java
src/test/java/com/fasteat/model/entity/
└── EntityRelationshipTest.java
```

---

## BEAD-1.7: Spring Data JPA Repositories

**Status**:  BLOCKED (by BEAD-1.6)  
**Dependencies**: BEAD-1.6  
**Phase**: 1 - Foundation  

### Description
Create Spring Data JPA repository interfaces for all entities. Define custom query methods using method naming conventions and @Query annotations.

### Tasks
- [ ] Create UserRepository extending JpaRepository
- [ ] Create RestaurantRepository extending JpaRepository
- [ ] Create MenuItemRepository extending JpaRepository
- [ ] Create IngredientRepository extending JpaRepository
- [ ] Create OrderRepository extending JpaRepository
- [ ] Create OrderItemRepository extending JpaRepository
- [ ] Create ReviewRepository extending JpaRepository
- [ ] Create AddressRepository extending JpaRepository
- [ ] Add custom finder methods (findByEmail, findByStatus, etc.)
- [ ] Write @Query methods for complex queries
- [ ] Add @Query with JOIN FETCH to avoid N+1 problems
- [ ] Create projection interfaces for read-only queries
- [ ] Add pagination and sorting support
- [ ] Use @Modifying for update/delete queries
- [ ] Add @Lock annotations for pessimistic locking where needed
- [ ] Write repository integration tests with @DataJpaTest
- [ ] Test custom queries with test data
- [ ] Verify query performance with explain plans

### Acceptance Criteria
- [ ] All repository interfaces compile
- [ ] Basic CRUD operations work
- [ ] Custom queries return correct results
- [ ] Pagination works correctly
- [ ] JOIN FETCH queries optimize N+1 scenarios
- [ ] Integration tests pass
- [ ] No unnecessary database calls
- [ ] Query performance is acceptable

### Related Files
```
src/main/java/com/fasteat/repository/
├── UserRepository.java
├── RestaurantRepository.java
├── MenuItemRepository.java
├── IngredientRepository.java
├── OrderRepository.java
├── OrderItemRepository.java
├── ReviewRepository.java
└── AddressRepository.java
src/test/java/com/fasteat/repository/
├── UserRepositoryTest.java
├── OrderRepositoryTest.java
└── MenuItemRepositoryTest.java
```

---

# Phase 2: Domain Layer

**Duration**: 4-5 weeks  
**Focus**: Refining entities, adding business constraints, creating seed data, comprehensive testing

---

## BEAD-2.1: User Entity & Authentication Fields

**Status**:  BLOCKED (by BEAD-1.7)  
**Dependencies**: BEAD-1.7  
**Phase**: 2 - Domain  

### Description
Enhance the User entity with complete authentication and authorization fields. Implement password encoding, role management, and user profile information.

### Tasks
- [ ] Add username field with unique constraint
- [ ] Add email field with validation and unique constraint
- [ ] Add password field (will store BCrypt hash)
- [ ] Add enabled/disabled flag for account status
- [ ] Add accountNonExpired, accountNonLocked, credentialsNonExpired flags
- [ ] Add Set<Role> or enum for user roles (CUSTOMER, RESTAURANT_OWNER, ADMIN)
- [ ] Add profile fields (firstName, lastName, phoneNumber, avatar)
- [ ] Add @OneToMany relationship to Orders
- [ ] Add @OneToMany relationship to Addresses
- [ ] Add @OneToMany relationship to Reviews
- [ ] Add lastLoginAt timestamp
- [ ] Add email verification fields (emailVerified, verificationToken, tokenExpiry)
- [ ] Add password reset fields (resetToken, resetTokenExpiry)
- [ ] Implement UserDetails interface for Spring Security
- [ ] Add @PrePersist method to validate email format
- [ ] Create UserRole enum (ROLE_CUSTOMER, ROLE_OWNER, ROLE_ADMIN)
- [ ] Write unit tests for User entity
- [ ] Write repository tests for user queries

### Acceptance Criteria
- [ ] User entity compiles without errors
- [ ] All validation annotations work
- [ ] UserDetails interface implemented correctly
- [ ] Roles are properly mapped
- [ ] Relationships to other entities work
- [ ] Repository saves and retrieves users
- [ ] Custom queries (findByEmail, findByUsername) work
- [ ] Tests verify all functionality
- [ ] Password field is never serialized to JSON

### Related Files
```
src/main/java/com/fasteat/model/entity/User.java
src/main/java/com/fasteat/model/enums/UserRole.java
src/main/java/com/fasteat/repository/UserRepository.java
src/test/java/com/fasteat/model/entity/UserTest.java
src/test/java/com/fasteat/repository/UserRepositoryTest.java
```

---

## BEAD-2.2: Restaurant Entity & Business Logic

**Status**:  BLOCKED (by BEAD-1.7)  
**Dependencies**: BEAD-1.7  
**Phase**: 2 - Domain  

### Description
Enhance the Restaurant entity with comprehensive business information, operating hours, location data, and relationships to menu items and orders.

### Tasks
- [ ] Add restaurant name with validation
- [ ] Add description/bio field (TEXT type)
- [ ] Add cuisine type/category (Italian, Chinese, FastFood, etc.)
- [ ] Add address fields (street, city, state, zipCode, coordinates)
- [ ] Add phone number and email contact
- [ ] Add logo/image URL
- [ ] Add rating and reviewCount fields
- [ ] Add isActive flag for open/closed status
- [ ] Add operatingHours as JSON or separate entity
- [ ] Add deliveryFee and minimumOrder amounts
- [ ] Add @ManyToOne relationship to owner (User)
- [ ] Add @OneToMany relationship to MenuItems
- [ ] Add @OneToMany relationship to Orders
- [ ] Add estimatedDeliveryTime field
- [ ] Create RestaurantCategory enum
- [ ] Add business methods (isOpenNow(), acceptsOrders(), etc.)
- [ ] Add geospatial indexing hints for location queries
- [ ] Write unit tests for business logic
- [ ] Write repository tests with custom queries (findByCategory, findNearLocation)

### Acceptance Criteria
- [ ] Restaurant entity compiles without errors
- [ ] All fields properly validated
- [ ] Relationships work correctly
- [ ] Business methods return correct results
- [ ] Repository queries work (findByCategory, findByCity)
- [ ] Operating hours validation works
- [ ] Location queries can find nearby restaurants
- [ ] Tests verify all functionality
- [ ] Can calculate if restaurant is currently open

### Related Files
```
src/main/java/com/fasteat/model/entity/Restaurant.java
src/main/java/com/fasteat/model/entity/OperatingHours.java (optional)
src/main/java/com/fasteat/model/enums/RestaurantCategory.java
src/main/java/com/fasteat/repository/RestaurantRepository.java
src/test/java/com/fasteat/model/entity/RestaurantTest.java
src/test/java/com/fasteat/repository/RestaurantRepositoryTest.java
```

---

## BEAD-2.3: MenuItem Entity & Ingredient Relationships

**Status**:  BLOCKED (by BEAD-2.2)  
**Dependencies**: BEAD-2.2  
**Phase**: 2 - Domain  

### Description
Enhance MenuItem entity with pricing, categories, customization options, and many-to-many relationship with Ingredients. Implement allergen tracking.

### Tasks
- [ ] Add item name and description
- [ ] Add price field (BigDecimal for precision)
- [ ] Add category (Appetizer, MainCourse, Dessert, Beverage)
- [ ] Add image URL for item photo
- [ ] Add isAvailable flag for sold-out items
- [ ] Add preparationTime in minutes
- [ ] Add isVegetarian, isVegan, isGlutenFree flags
- [ ] Add spiceLevel enum (NONE, MILD, MEDIUM, HOT, EXTRA_HOT)
- [ ] Add calories and nutritional information (optional)
- [ ] Add @ManyToOne relationship to Restaurant
- [ ] Add @ManyToMany relationship to Ingredients
- [ ] Create junction table configuration (@JoinTable)
- [ ] Add customizationOptions as JSON or separate entity
- [ ] Create Ingredient entity (name, allergenType, extraCost)
- [ ] Create AllergenType enum (DAIRY, GLUTEN, NUTS, SOY, etc.)
- [ ] Add business method to calculate total price with extras
- [ ] Add method to get all allergens from ingredients
- [ ] Write unit tests for price calculations
- [ ] Write repository tests for menu item queries

### Acceptance Criteria
- [ ] MenuItem entity compiles without errors
- [ ] Ingredient relationship works bidirectionally
- [ ] Price calculations are accurate
- [ ] Allergen aggregation works correctly
- [ ] Category and dietary flags work
- [ ] Customization options can be added/removed
- [ ] Repository queries work (findByRestaurant, findByCategory)
- [ ] Tests verify all business logic
- [ ] Junction table properly created

### Related Files
```
src/main/java/com/fasteat/model/entity/MenuItem.java
src/main/java/com/fasteat/model/entity/Ingredient.java
src/main/java/com/fasteat/model/entity/MenuItemCustomization.java (optional)
src/main/java/com/fasteat/model/enums/MenuCategory.java
src/main/java/com/fasteat/model/enums/AllergenType.java
src/main/java/com/fasteat/model/enums/SpiceLevel.java
src/main/java/com/fasteat/repository/MenuItemRepository.java
src/main/java/com/fasteat/repository/IngredientRepository.java
src/test/java/com/fasteat/model/entity/MenuItemTest.java
```

---

## BEAD-2.4: Order Entity & Order Items

**Status**:  BLOCKED (by BEAD-2.3)  
**Dependencies**: BEAD-2.3  
**Phase**: 2 - Domain  

### Description
Enhance Order entity with complete order lifecycle management, status tracking, and order item details with customizations.

### Tasks
- [ ] Add order number (unique, human-readable)
- [ ] Add orderStatus enum (PENDING, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED)
- [ ] Add orderDate timestamp
- [ ] Add scheduledDeliveryTime timestamp
- [ ] Add actualDeliveryTime timestamp
- [ ] Add subtotal, tax, deliveryFee, total (BigDecimal)
- [ ] Add paymentMethod enum (CASH, CARD, ONLINE)
- [ ] Add paymentStatus enum (PENDING, PAID, FAILED, REFUNDED)
- [ ] Add deliveryAddress (embedded or relationship)
- [ ] Add specialInstructions text field
- [ ] Add @ManyToOne relationship to User (customer)
- [ ] Add @ManyToOne relationship to Restaurant
- [ ] Add @OneToMany relationship to OrderItems (cascade ALL)
- [ ] Create OrderItem entity (quantity, price, subtotal)
- [ ] Add @ManyToOne OrderItem→MenuItem relationship
- [ ] Add customizations to OrderItem (List<String> or separate entity)
- [ ] Add business methods (calculateTotal(), canCancel(), isLate())
- [ ] Add @PrePersist to generate unique order number
- [ ] Implement order status state machine validation
- [ ] Write unit tests for order calculations
- [ ] Write repository tests for order queries (findByUser, findByStatus)

### Acceptance Criteria
- [ ] Order entity compiles without errors
- [ ] Order items cascade correctly
- [ ] Total calculations are accurate (subtotal + tax + delivery)
- [ ] Status transitions follow business rules
- [ ] Order number generation is unique
- [ ] Relationships work correctly
- [ ] Repository queries work (findByUserAndStatus, findRecentOrders)
- [ ] Tests verify calculations and state transitions
- [ ] Cannot modify order in certain statuses

### Related Files
```
src/main/java/com/fasteat/model/entity/Order.java
src/main/java/com/fasteat/model/entity/OrderItem.java
src/main/java/com/fasteat/model/entity/OrderItemCustomization.java (optional)
src/main/java/com/fasteat/model/enums/OrderStatus.java
src/main/java/com/fasteat/model/enums/PaymentMethod.java
src/main/java/com/fasteat/model/enums/PaymentStatus.java
src/main/java/com/fasteat/repository/OrderRepository.java
src/main/java/com/fasteat/repository/OrderItemRepository.java
src/test/java/com/fasteat/model/entity/OrderTest.java
```

---

## BEAD-2.5: Review & Rating System

**Status**:  BLOCKED (by BEAD-2.4)  
**Dependencies**: BEAD-2.4  
**Phase**: 2 - Domain  

### Description
Create Review entity for customers to rate restaurants and menu items. Implement rating aggregation and review moderation features.

### Tasks
- [ ] Create Review entity with rating (1-5 stars)
- [ ] Add comment/review text field
- [ ] Add @ManyToOne relationship to User (reviewer)
- [ ] Add @ManyToOne relationship to Restaurant
- [ ] Add @ManyToOne relationship to Order (optional - verify purchase)
- [ ] Add helpful count for review upvotes
- [ ] Add isVerifiedPurchase flag
- [ ] Add response text from restaurant owner
- [ ] Add responseDate timestamp
- [ ] Add isHidden flag for moderation
- [ ] Add validation (rating between 1-5, comment max length)
- [ ] Create business method to verify user ordered from restaurant
- [ ] Add repository method to calculate average rating
- [ ] Add repository method to find recent reviews
- [ ] Implement review reporting system (optional)
- [ ] Write unit tests for review validation
- [ ] Write repository tests for aggregation queries

### Acceptance Criteria
- [ ] Review entity compiles without errors
- [ ] Rating validation works (1-5 only)
- [ ] Relationships to User, Restaurant, Order work
- [ ] Can calculate average rating for restaurant
- [ ] Can retrieve paginated reviews
- [ ] Verified purchase flag works correctly
- [ ] Restaurant response functionality works
- [ ] Tests verify all functionality
- [ ] Cannot review without ordering (if enforced)

### Related Files
```
src/main/java/com/fasteat/model/entity/Review.java
src/main/java/com/fasteat/repository/ReviewRepository.java
src/test/java/com/fasteat/model/entity/ReviewTest.java
src/test/java/com/fasteat/repository/ReviewRepositoryTest.java
```

---

## BEAD-2.6: Address & Delivery Management

**Status**:  BLOCKED (by BEAD-2.4)  
**Dependencies**: BEAD-2.4  
**Phase**: 2 - Domain  

### Description
Create Address entity for delivery locations. Implement address validation, default address management, and geospatial features.

### Tasks
- [ ] Create Address entity with street, city, state, zipCode
- [ ] Add label field (Home, Work, Other)
- [ ] Add building/apartment number
- [ ] Add landmark/instructions for driver
- [ ] Add latitude and longitude (for mapping)
- [ ] Add isDefault flag
- [ ] Add @ManyToOne relationship to User
- [ ] Add validation annotations for address fields
- [ ] Add business method to format full address string
- [ ] Add geospatial methods (distance calculation)
- [ ] Create AddressType enum
- [ ] Add constraint: only one default address per user
- [ ] Write unit tests for address validation
- [ ] Write repository tests for address queries (findByUser, findDefault)
- [ ] Implement address geocoding integration (optional)

### Acceptance Criteria
- [ ] Address entity compiles without errors
- [ ] All fields properly validated
- [ ] Default address logic works (only one default per user)
- [ ] Relationship to User works
- [ ] Address formatting method works
- [ ] Repository queries work correctly
- [ ] Tests verify validation rules
- [ ] Can calculate distance between addresses (optional)

### Related Files
```
src/main/java/com/fasteat/model/entity/Address.java
src/main/java/com/fasteat/model/enums/AddressType.java
src/main/java/com/fasteat/repository/AddressRepository.java
src/test/java/com/fasteat/model/entity/AddressTest.java
src/test/java/com/fasteat/repository/AddressRepositoryTest.java
```

---

## BEAD-2.7: Repository Custom Queries & Optimization

**Status**:  BLOCKED (by BEAD-2.6)  
**Dependencies**: BEAD-2.6  
**Phase**: 2 - Domain  

### Description
Implement advanced repository queries, query optimization, and performance tuning. Create custom repository implementations for complex queries.

### Tasks
- [ ] Identify N+1 query scenarios across all entities
- [ ] Add @Query with JOIN FETCH for eager loading
- [ ] Create @EntityGraph for fine-grained fetch control
- [ ] Implement custom repository interfaces for complex queries
- [ ] Create query DTOs/projections for read-only queries
- [ ] Add full-text search queries for menu items and restaurants
- [ ] Implement geospatial queries (findRestaurantsNearLocation)
- [ ] Add pagination and sorting to all list queries
- [ ] Create Specification API implementations for dynamic queries
- [ ] Add query hints for database-specific optimizations
- [ ] Create database indexes based on query patterns
- [ ] Write performance tests for critical queries
- [ ] Profile queries with explain plans
- [ ] Optimize slow queries
- [ ] Document query patterns and best practices

### Acceptance Criteria
- [ ] No N+1 query issues in main use cases
- [ ] JOIN FETCH queries work correctly
- [ ] Custom queries return expected results
- [ ] Pagination works for all list endpoints
- [ ] Full-text search works (if implemented)
- [ ] Geospatial queries return correct results
- [ ] Query performance meets requirements (<100ms for simple, <500ms for complex)
- [ ] Performance tests pass
- [ ] Documentation is complete

### Related Files
```
src/main/java/com/fasteat/repository/
├── custom/
│   ├── CustomMenuItemRepository.java
│   ├── CustomMenuItemRepositoryImpl.java
│   ├── CustomOrderRepository.java
│   └── CustomOrderRepositoryImpl.java
├── projection/
│   ├── MenuItemSummary.java
│   ├── OrderSummary.java
│   └── RestaurantSummary.java
└── specification/
    ├── MenuItemSpecification.java
    └── OrderSpecification.java
src/test/java/com/fasteat/repository/
└── RepositoryPerformanceTest.java
docs/query-optimization.md
```

---

## BEAD-2.8: Database Seed Data & Test Fixtures

**Status**:  BLOCKED (by BEAD-2.7)  
**Dependencies**: BEAD-2.7  
**Phase**: 2 - Domain  

### Description
Create seed data for development and testing. Implement data fixtures with realistic data for users, restaurants, menu items, and orders.

### Tasks
- [ ] Create DataLoader component with @PostConstruct
- [ ] Generate realistic seed data for Users (customers, owners, admins)
- [ ] Generate seed data for Restaurants (various cuisines, locations)
- [ ] Generate seed data for MenuItems with ingredients
- [ ] Generate seed data for Ingredients with allergens
- [ ] Generate seed data for sample Orders (various statuses)
- [ ] Generate seed data for Reviews
- [ ] Generate seed data for Addresses
- [ ] Use BCrypt to hash seed user passwords
- [ ] Create dev profile conditional loading (@Profile("dev"))
- [ ] Create Flyway migration script for seed data (optional)
- [ ] Create test fixtures for integration tests
- [ ] Use Faker library for generating realistic data (optional)
- [ ] Add ability to reset database to seed state
- [ ] Document seed data credentials
- [ ] Create SQL script alternative (V3__seed_data.sql)

### Acceptance Criteria
- [ ] Seed data loads successfully on dev profile
- [ ] All relationships properly established
- [ ] Can login with seed user credentials
- [ ] Can browse seed restaurants and menus
- [ ] Can view sample orders
- [ ] Test fixtures work in integration tests
- [ ] Seed data is realistic and usable
- [ ] Database can be reset easily
- [ ] Documentation lists all seed credentials

### Related Files
```
src/main/java/com/fasteat/config/DataLoader.java
src/main/resources/db/migration/V3__seed_data.sql
src/test/java/com/fasteat/fixtures/
├── UserFixtures.java
├── RestaurantFixtures.java
├── MenuItemFixtures.java
└── OrderFixtures.java
src/test/resources/
└── test-data.sql
docs/seed-data.md
```

---

# Phase 3: Business Logic Layer

**Duration**: 6-8 weeks  
**Focus**: Services, business rules, security, authentication, validation, error handling

---

## BEAD-3.1: Global Exception Handling

**Status**:  BLOCKED (by BEAD-2.8)  
**Dependencies**: BEAD-2.8  
**Phase**: 3 - Business Logic  

### Description
Implement global exception handling using @ControllerAdvice. Create custom exceptions and standardized error response format.

### Tasks
- [ ] Create custom exception hierarchy (base BusinessException)
- [ ] Create ResourceNotFoundException
- [ ] Create UnauthorizedException
- [ ] Create ForbiddenException
- [ ] Create BadRequestException
- [ ] Create ValidationException
- [ ] Create DuplicateResourceException
- [ ] Create OrderException, PaymentException, etc.
- [ ] Create ErrorResponse DTO with timestamp, status, message, path
- [ ] Create @ControllerAdvice class (GlobalExceptionHandler)
- [ ] Add @ExceptionHandler for each custom exception
- [ ] Add @ExceptionHandler for MethodArgumentNotValidException (validation)
- [ ] Add @ExceptionHandler for DataIntegrityViolationException
- [ ] Add @ExceptionHandler for AccessDeniedException
- [ ] Add @ExceptionHandler for generic Exception (500 Internal Server Error)
- [ ] Include validation field errors in error response
- [ ] Add request ID for error tracking
- [ ] Log exceptions appropriately (debug vs error level)
- [ ] Write unit tests for exception handlers

### Acceptance Criteria
- [ ] All custom exceptions compile
- [ ] GlobalExceptionHandler catches all exception types
- [ ] Error responses follow consistent format
- [ ] HTTP status codes are correct
- [ ] Validation errors include field-level details
- [ ] Stack traces not exposed in production
- [ ] Exceptions are logged appropriately
- [ ] Tests verify exception handling
- [ ] Client receives actionable error messages

### Related Files
```
src/main/java/com/fasteat/exception/
├── BusinessException.java
├── ResourceNotFoundException.java
├── UnauthorizedException.java
├── ForbiddenException.java
├── BadRequestException.java
├── ValidationException.java
├── DuplicateResourceException.java
├── OrderException.java
└── GlobalExceptionHandler.java
src/main/java/com/fasteat/dto/response/ErrorResponse.java
src/test/java/com/fasteat/exception/GlobalExceptionHandlerTest.java
```

---

## BEAD-3.2: DTO Layer & Request/Response Objects

**Status**:  BLOCKED (by BEAD-3.1)  
**Dependencies**: BEAD-3.1  
**Phase**: 3 - Business Logic  

### Description
Create Data Transfer Objects (DTOs) for all API requests and responses. Separate domain entities from API contract.

### Tasks
- [ ] Create request DTOs for User (RegisterRequest, LoginRequest, UpdateProfileRequest)
- [ ] Create response DTOs for User (UserResponse, UserProfileResponse)
- [ ] Create request DTOs for Restaurant (CreateRestaurantRequest, UpdateRestaurantRequest)
- [ ] Create response DTOs for Restaurant (RestaurantResponse, RestaurantDetailResponse)
- [ ] Create request DTOs for MenuItem (CreateMenuItemRequest, UpdateMenuItemRequest)
- [ ] Create response DTOs for MenuItem (MenuItemResponse, MenuItemDetailResponse)
- [ ] Create request DTOs for Order (CreateOrderRequest, UpdateOrderStatusRequest)
- [ ] Create response DTOs for Order (OrderResponse, OrderDetailResponse, OrderSummaryResponse)
- [ ] Create request/response DTOs for Review, Address
- [ ] Add validation annotations to all request DTOs
- [ ] Use @JsonProperty for field name mapping
- [ ] Add @JsonIgnore for sensitive fields
- [ ] Create pagination response wrapper (PageResponse<T>)
- [ ] Create API response wrapper (ApiResponse<T>) with success, message, data
- [ ] Document all DTOs with Javadoc
- [ ] Group DTOs in request/response packages
- [ ] Write unit tests for DTO validation

### Acceptance Criteria
- [ ] All request DTOs have proper validation
- [ ] All response DTOs compile without errors
- [ ] Entities are never exposed directly in API
- [ ] Validation rules match business requirements
- [ ] JSON serialization/deserialization works
- [ ] Pagination wrapper works correctly
- [ ] API response format is consistent
- [ ] Tests verify validation rules
- [ ] DTOs are well-documented

### Related Files
```
src/main/java/com/fasteat/dto/request/
├── auth/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── RefreshTokenRequest.java
├── user/
│   └── UpdateProfileRequest.java
├── restaurant/
│   ├── CreateRestaurantRequest.java
│   └── UpdateRestaurantRequest.java
├── menu/
│   ├── CreateMenuItemRequest.java
│   └── UpdateMenuItemRequest.java
└── order/
    ├── CreateOrderRequest.java
    ├── CreateOrderItemRequest.java
    └── UpdateOrderStatusRequest.java
src/main/java/com/fasteat/dto/response/
├── auth/
│   ├── AuthResponse.java
│   └── TokenResponse.java
├── user/
│   ├── UserResponse.java
│   └── UserProfileResponse.java
├── restaurant/
│   ├── RestaurantResponse.java
│   └── RestaurantDetailResponse.java
├── menu/
│   ├── MenuItemResponse.java
│   └── MenuItemDetailResponse.java
├── order/
│   ├── OrderResponse.java
│   ├── OrderDetailResponse.java
│   └── OrderSummaryResponse.java
├── PageResponse.java
└── ApiResponse.java
src/test/java/com/fasteat/dto/DtoValidationTest.java
```

---

## BEAD-3.3: Entity-DTO Mappers

**Status**:  BLOCKED (by BEAD-3.2)  
**Dependencies**: BEAD-3.2  
**Phase**: 3 - Business Logic  

### Description
Create mapper classes or use MapStruct to convert between entities and DTOs. Implement bidirectional mapping logic.

### Tasks
- [ ] Choose mapping strategy (manual mappers vs MapStruct)
- [ ] Create UserMapper (entity  DTO)
- [ ] Create RestaurantMapper (entity  DTO)
- [ ] Create MenuItemMapper (entity  DTO)
- [ ] Create OrderMapper (entity  DTO)
- [ ] Create ReviewMapper (entity  DTO)
- [ ] Create AddressMapper (entity  DTO)
- [ ] Handle nested relationships (Order → OrderItems → MenuItem)
- [ ] Implement custom mapping logic for complex fields
- [ ] Add null-safety checks
- [ ] Create partial update mappers (updateEntityFromDto)
- [ ] Handle collection mappings (List<Entity> → List<DTO>)
- [ ] Configure MapStruct (if using) with componentModel = "spring"
- [ ] Add @Mapper annotation with dependency injection
- [ ] Write unit tests for all mappers
- [ ] Test bidirectional mapping consistency
- [ ] Verify no sensitive data leaks through mapping

### Acceptance Criteria
- [ ] All mappers compile without errors
- [ ] Entity to DTO mapping works correctly
- [ ] DTO to Entity mapping works correctly
- [ ] Nested relationships map correctly
- [ ] Collection mappings work
- [ ] Partial updates work without overwriting unchanged fields
- [ ] No sensitive data (passwords) in response DTOs
- [ ] Tests verify mapping accuracy
- [ ] MapStruct generates code correctly (if using)

### Related Files
```
src/main/java/com/fasteat/mapper/
├── UserMapper.java
├── RestaurantMapper.java
├── MenuItemMapper.java
├── OrderMapper.java
├── ReviewMapper.java
└── AddressMapper.java
src/test/java/com/fasteat/mapper/
├── UserMapperTest.java
├── OrderMapperTest.java
└── MenuItemMapperTest.java
```

**If using MapStruct, add to pom.xml:**
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
```

---

## BEAD-3.4: Service Layer - User Management

**Status**:  BLOCKED (by BEAD-3.3)  
**Dependencies**: BEAD-3.3  
**Phase**: 3 - Business Logic  

### Description
Implement UserService with business logic for user management, registration, profile updates, and password management.

### Tasks
- [ ] Create UserService interface
- [ ] Create UserServiceImpl with @Service annotation
- [ ] Implement registerUser(RegisterRequest) method
- [ ] Implement getUserById(UUID) method
- [ ] Implement getUserByEmail(String) method
- [ ] Implement getUserByUsername(String) method
- [ ] Implement updateProfile(UUID, UpdateProfileRequest) method
- [ ] Implement changePassword(UUID, ChangePasswordRequest) method
- [ ] Implement deleteUser(UUID) method (soft delete)
- [ ] Add password encoding with BCryptPasswordEncoder
- [ ] Add duplicate email/username validation
- [ ] Implement email verification workflow
- [ ] Implement password reset workflow
- [ ] Add business rule validation (password strength, age restrictions)
- [ ] Use mappers to convert entities to DTOs
- [ ] Add transaction boundaries with @Transactional
- [ ] Add logging for important operations
- [ ] Write unit tests with Mockito
- [ ] Write integration tests with @SpringBootTest

### Acceptance Criteria
- [ ] All service methods compile
- [ ] User registration works with password hashing
- [ ] Duplicate email/username prevented
- [ ] Profile updates work correctly
- [ ] Password change validates old password
- [ ] Email verification workflow works
- [ ] Password reset workflow works
- [ ] Transactions rollback on error
- [ ] Unit tests cover all methods
- [ ] Integration tests verify database operations

### Related Files
```
src/main/java/com/fasteat/service/
├── UserService.java
└── impl/
    └── UserServiceImpl.java
src/test/java/com/fasteat/service/
├── UserServiceTest.java (unit)
└── UserServiceIntegrationTest.java
```

---

## BEAD-3.5: Service Layer - Restaurant & Menu Management

**Status**:  BLOCKED (by BEAD-3.4)  
**Dependencies**: BEAD-3.4  
**Phase**: 3 - Business Logic  

### Description
Implement RestaurantService and MenuItemService with business logic for restaurant operations, menu management, and availability.

### Tasks
- [ ] Create RestaurantService interface and implementation
- [ ] Implement createRestaurant(CreateRestaurantRequest) method
- [ ] Implement getRestaurantById(UUID) method
- [ ] Implement getAllRestaurants(Pageable) method
- [ ] Implement searchRestaurants(String query, Pageable) method
- [ ] Implement updateRestaurant(UUID, UpdateRestaurantRequest) method
- [ ] Implement deleteRestaurant(UUID) method
- [ ] Implement getRestaurantsByCategory(RestaurantCategory, Pageable)
- [ ] Implement getNearbyRestaurants(lat, lng, radius) method
- [ ] Create MenuItemService interface and implementation
- [ ] Implement createMenuItem(CreateMenuItemRequest) method
- [ ] Implement getMenuItemsByRestaurant(UUID restaurantId) method
- [ ] Implement updateMenuItem(UUID, UpdateMenuItemRequest) method
- [ ] Implement deleteMenuItem(UUID) method
- [ ] Implement toggleAvailability(UUID) method
- [ ] Add authorization checks (only owner can modify their restaurant)
- [ ] Add validation (restaurant must be active to accept orders)
- [ ] Calculate and update restaurant average rating
- [ ] Add transaction boundaries
- [ ] Write unit and integration tests

### Acceptance Criteria
- [ ] Restaurant CRUD operations work
- [ ] Menu item CRUD operations work
- [ ] Authorization checks prevent unauthorized modifications
- [ ] Search and filtering work correctly
- [ ] Nearby restaurant queries return correct results
- [ ] Average rating calculation works
- [ ] Availability toggling works
- [ ] Tests cover all service methods
- [ ] Integration tests verify database operations

### Related Files
```
src/main/java/com/fasteat/service/
├── RestaurantService.java
├── MenuItemService.java
├── IngredientService.java
└── impl/
    ├── RestaurantServiceImpl.java
    ├── MenuItemServiceImpl.java
    └── IngredientServiceImpl.java
src/test/java/com/fasteat/service/
├── RestaurantServiceTest.java
├── MenuItemServiceTest.java
└── RestaurantServiceIntegrationTest.java
```

---

## BEAD-3.6: Service Layer - Order Management

**Status**:  BLOCKED (by BEAD-3.5)  
**Dependencies**: BEAD-3.5  
**Phase**: 3 - Business Logic  

### Description
Implement OrderService with complete order lifecycle management, status transitions, payment processing, and business rule enforcement.

### Tasks
- [ ] Create OrderService interface and implementation
- [ ] Implement createOrder(CreateOrderRequest) method
- [ ] Implement getOrderById(UUID) method
- [ ] Implement getOrdersByUser(UUID userId, Pageable) method
- [ ] Implement getOrdersByRestaurant(UUID restaurantId, Pageable) method
- [ ] Implement updateOrderStatus(UUID, OrderStatus) method
- [ ] Implement cancelOrder(UUID) method
- [ ] Implement calculateOrderTotal(CreateOrderRequest) method
- [ ] Validate menu item availability before creating order
- [ ] Validate restaurant is open and accepting orders
- [ ] Implement order status state machine (PENDING→CONFIRMED→PREPARING→etc.)
- [ ] Add business rules (minimum order amount, delivery radius)
- [ ] Generate unique order number
- [ ] Calculate taxes and delivery fees
- [ ] Send order confirmation (mock email/notification)
- [ ] Implement order history with filtering
- [ ] Add authorization (customer can only see their orders)
- [ ] Add concurrent order handling (optimistic locking)
- [ ] Write comprehensive unit tests
- [ ] Write integration tests for order flow

### Acceptance Criteria
- [ ] Order creation works with all validations
- [ ] Order total calculation is accurate
- [ ] Status transitions follow state machine rules
- [ ] Cannot create order from closed restaurant
- [ ] Cannot order unavailable items
- [ ] Order cancellation works with rules (time limit)
- [ ] Order history filtering works
- [ ] Authorization prevents unauthorized access
- [ ] Concurrent orders handled safely
- [ ] Tests verify all business rules

### Related Files
```
src/main/java/com/fasteat/service/
├── OrderService.java
└── impl/
    └── OrderServiceImpl.java
src/main/java/com/fasteat/service/validation/
├── OrderValidator.java
└── OrderStatusValidator.java
src/test/java/com/fasteat/service/
├── OrderServiceTest.java
└── OrderServiceIntegrationTest.java
```

---

## BEAD-3.7: Service Layer - Review & Rating

**Status**:  BLOCKED (by BEAD-3.6)  
**Dependencies**: BEAD-3.6  
**Phase**: 3 - Business Logic  

### Description
Implement ReviewService for rating and review management with verification and aggregation logic.

### Tasks
- [ ] Create ReviewService interface and implementation
- [ ] Implement createReview(CreateReviewRequest) method
- [ ] Implement getReviewById(UUID) method
- [ ] Implement getReviewsByRestaurant(UUID, Pageable) method
- [ ] Implement getReviewsByUser(UUID, Pageable) method
- [ ] Implement updateReview(UUID, UpdateReviewRequest) method
- [ ] Implement deleteReview(UUID) method
- [ ] Implement addRestaurantResponse(UUID, String response) method
- [ ] Validate user has ordered from restaurant before reviewing
- [ ] Prevent duplicate reviews for same order
- [ ] Calculate and update restaurant average rating
- [ ] Implement helpful votes/upvotes for reviews
- [ ] Add review moderation capabilities
- [ ] Add authorization (only customer can review, only owner can respond)
- [ ] Write unit tests
- [ ] Write integration tests

### Acceptance Criteria
- [ ] Review creation works with validation
- [ ] Cannot review without verified order (if enforced)
- [ ] Cannot create duplicate reviews
- [ ] Restaurant average rating updates automatically
- [ ] Restaurant owner can respond to reviews
- [ ] Review filtering and pagination work
- [ ] Authorization prevents unauthorized actions
- [ ] Tests verify all business rules

### Related Files
```
src/main/java/com/fasteat/service/
├── ReviewService.java
└── impl/
    └── ReviewServiceImpl.java
src/test/java/com/fasteat/service/
└── ReviewServiceTest.java
```

---

## BEAD-3.8: JWT Authentication Implementation

**Status**:  BLOCKED (by BEAD-3.7)  
**Dependencies**: BEAD-3.7  
**Phase**: 3 - Business Logic  

### Description
Implement JWT token generation, validation, and refresh logic for stateless authentication.

### Tasks
- [ ] Create JwtUtil or JwtTokenProvider class
- [ ] Implement generateAccessToken(UserDetails) method
- [ ] Implement generateRefreshToken(UserDetails) method
- [ ] Implement validateToken(String token) method
- [ ] Implement getUsernameFromToken(String token) method
- [ ] Implement getClaimsFromToken(String token) method
- [ ] Implement isTokenExpired(String token) method
- [ ] Configure JWT secret from application properties
- [ ] Configure token expiration times (access: 15min, refresh: 7 days)
- [ ] Add user roles/authorities to JWT claims
- [ ] Create RefreshToken entity for refresh token storage (optional)
- [ ] Implement token refresh endpoint logic
- [ ] Implement token revocation (blacklist or database tracking)
- [ ] Add token rotation on refresh
- [ ] Handle token expiration gracefully
- [ ] Write unit tests for token operations
- [ ] Test token expiration and validation

### Acceptance Criteria
- [ ] Access token generation works
- [ ] Refresh token generation works
- [ ] Token validation correctly identifies valid/invalid tokens
- [ ] Claims extraction works correctly
- [ ] Expired tokens are rejected
- [ ] User roles included in token
- [ ] Token refresh works
- [ ] Token revocation works
- [ ] Tests verify all token operations
- [ ] Secret key is externalized in config

### Related Files
```
src/main/java/com/fasteat/security/
├── jwt/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtAuthenticationEntryPoint.java
└── service/
    └── RefreshTokenService.java (optional)
src/main/java/com/fasteat/model/entity/
└── RefreshToken.java (optional)
src/test/java/com/fasteat/security/jwt/
└── JwtTokenProviderTest.java
```

---

## BEAD-3.9: Spring Security Configuration

**Status**:  BLOCKED (by BEAD-3.8)  
**Dependencies**: BEAD-3.8  
**Phase**: 3 - Business Logic  

### Description
Configure Spring Security with JWT authentication, authorization rules, CORS, and security filters.

### Tasks
- [ ] Create SecurityConfig class with @EnableWebSecurity
- [ ] Configure SecurityFilterChain bean
- [ ] Disable CSRF (stateless JWT authentication)
- [ ] Configure session management (STATELESS)
- [ ] Create JwtAuthenticationFilter extending OncePerRequestFilter
- [ ] Extract JWT from Authorization header
- [ ] Validate token and set SecurityContext
- [ ] Create JwtAuthenticationEntryPoint for unauthorized access
- [ ] Configure CORS with allowed origins, methods, headers
- [ ] Define authorization rules for endpoints
- [ ] Allow public access to /api/auth/**, /api/public/**
- [ ] Require authentication for /api/user/**, /api/orders/**
- [ ] Require OWNER role for /api/restaurants/{id}/edit
- [ ] Require ADMIN role for /api/admin/**
- [ ] Configure password encoder bean (BCryptPasswordEncoder)
- [ ] Configure AuthenticationManager bean
- [ ] Create UserDetailsService implementation loading from UserRepository
- [ ] Write integration tests for security configuration
- [ ] Test unauthorized access returns 401
- [ ] Test forbidden access returns 403
- [ ] Test successful authentication

### Acceptance Criteria
- [ ] Security configuration compiles without errors
- [ ] JWT authentication filter works
- [ ] Public endpoints accessible without token
- [ ] Protected endpoints require valid token
- [ ] Role-based authorization works
- [ ] CORS configured correctly
- [ ] Unauthorized requests return 401
- [ ] Forbidden requests return 403
- [ ] UserDetailsService loads users correctly
- [ ] Integration tests verify security rules

### Related Files
```
src/main/java/com/fasteat/security/
├── SecurityConfig.java
├── CorsConfig.java
├── jwt/
│   ├── JwtAuthenticationFilter.java
│   └── JwtAuthenticationEntryPoint.java
└── service/
    └── CustomUserDetailsService.java
src/test/java/com/fasteat/security/
└── SecurityConfigIntegrationTest.java
```

---

## BEAD-3.10: Authentication Endpoints

**Status**:  BLOCKED (by BEAD-3.9)  
**Dependencies**: BEAD-3.9  
**Phase**: 3 - Business Logic  

### Description
Implement authentication REST endpoints for login, registration, token refresh, and logout.

### Tasks
- [ ] Create AuthService interface and implementation
- [ ] Implement login(LoginRequest) method returning AuthResponse with tokens
- [ ] Implement register(RegisterRequest) method
- [ ] Implement refreshToken(RefreshTokenRequest) method
- [ ] Implement logout(String token) method
- [ ] Implement getCurrentUser() method
- [ ] Use AuthenticationManager for authentication
- [ ] Generate JWT tokens on successful authentication
- [ ] Validate credentials and return appropriate errors
- [ ] Hash passwords on registration
- [ ] Implement account activation via email (optional)
- [ ] Implement forgot password workflow (optional)
- [ ] Create AuthController with endpoints
- [ ] POST /api/auth/login
- [ ] POST /api/auth/register
- [ ] POST /api/auth/refresh
- [ ] POST /api/auth/logout
- [ ] GET /api/auth/me
- [ ] Add request validation
- [ ] Write unit tests for AuthService
- [ ] Write integration tests for authentication flow

### Acceptance Criteria
- [ ] Login endpoint authenticates and returns JWT tokens
- [ ] Register endpoint creates user with hashed password
- [ ] Refresh endpoint returns new access token
- [ ] Logout endpoint invalidates tokens
- [ ] /me endpoint returns current user info
- [ ] Invalid credentials return 401
- [ ] Duplicate email/username returns 400
- [ ] Validation errors return 400 with details
- [ ] Tests verify all authentication flows
- [ ] Integration tests verify end-to-end flows

### Related Files
```
src/main/java/com/fasteat/service/
├── AuthService.java
└── impl/
    └── AuthServiceImpl.java
src/main/java/com/fasteat/controller/
└── AuthController.java
src/main/java/com/fasteat/dto/request/auth/
├── LoginRequest.java
├── RegisterRequest.java
└── RefreshTokenRequest.java
src/main/java/com/fasteat/dto/response/auth/
├── AuthResponse.java
└── TokenResponse.java
src/test/java/com/fasteat/service/
└── AuthServiceTest.java
src/test/java/com/fasteat/controller/
└── AuthControllerIntegrationTest.java
```

---

## BEAD-3.11: Event System & Notifications

**Status**:  BLOCKED (by BEAD-3.10)  
**Dependencies**: BEAD-3.10  
**Phase**: 3 - Business Logic  

### Description
Implement application event system for decoupled notifications (order updates, reviews, etc.) using Spring's event mechanism.

### Tasks
- [ ] Create custom event classes extending ApplicationEvent
- [ ] Create OrderCreatedEvent
- [ ] Create OrderStatusChangedEvent
- [ ] Create ReviewCreatedEvent
- [ ] Create UserRegisteredEvent
- [ ] Create event listener classes with @EventListener
- [ ] Create OrderEventListener
- [ ] Create ReviewEventListener
- [ ] Create UserEventListener
- [ ] Publish events using ApplicationEventPublisher in services
- [ ] Implement email notification handlers (mock implementation)
- [ ] Implement push notification handlers (mock implementation)
- [ ] Add async processing with @Async (optional)
- [ ] Create notification templates
- [ ] Add event logging
- [ ] Write unit tests for event publishing
- [ ] Write integration tests for event handling

### Acceptance Criteria
- [ ] Events are published when actions occur
- [ ] Event listeners receive and handle events
- [ ] Notifications triggered by events (logged/mocked)
- [ ] Async processing works (if implemented)
- [ ] Event handling doesn't block main flow
- [ ] Failed event handling doesn't break main operation
- [ ] Tests verify event publishing and handling
- [ ] Event listeners are decoupled from services

### Related Files
```
src/main/java/com/fasteat/event/
├── OrderCreatedEvent.java
├── OrderStatusChangedEvent.java
├── ReviewCreatedEvent.java
└── UserRegisteredEvent.java
src/main/java/com/fasteat/event/listener/
├── OrderEventListener.java
├── ReviewEventListener.java
└── UserEventListener.java
src/main/java/com/fasteat/service/notification/
├── NotificationService.java
├── EmailNotificationService.java
└── PushNotificationService.java
src/main/java/com/fasteat/config/
└── AsyncConfig.java
src/test/java/com/fasteat/event/
└── EventPublishingTest.java
```

---

## BEAD-3.12: Validation & Business Rules Engine

**Status**:  BLOCKED (by BEAD-3.11)  
**Dependencies**: BEAD-3.11  
**Phase**: 3 - Business Logic  

### Description
Centralize and enhance validation logic. Create custom validators and business rule engine for complex validation scenarios.

### Tasks
- [ ] Create custom validation annotations (@ValidEmail, @ValidPhone, etc.)
- [ ] Create @ValidOrderTime annotation with validator
- [ ] Create @ValidDeliveryAddress annotation with validator
- [ ] Create @ValidMenuItemPrice annotation with validator
- [ ] Create @ValidOrderStatus transition validator
- [ ] Implement ConstraintValidator for each custom annotation
- [ ] Create BusinessRulesEngine for complex multi-field validation
- [ ] Implement restaurant operating hours validation
- [ ] Implement delivery radius validation
- [ ] Implement minimum order amount validation
- [ ] Implement order time slot validation
- [ ] Create validation groups for different scenarios (Create, Update)
- [ ] Add cross-field validation in DTOs
- [ ] Create ValidationService for reusable validation logic
- [ ] Write unit tests for all validators
- [ ] Write integration tests for validation in API

### Acceptance Criteria
- [ ] Custom validators compile and work
- [ ] Validation annotations enforce rules
- [ ] Complex business rules validated correctly
- [ ] Validation errors return descriptive messages
- [ ] Validation groups work correctly
- [ ] Cross-field validation works
- [ ] Tests verify all validation scenarios
- [ ] Validation logic is centralized and reusable

### Related Files
```
src/main/java/com/fasteat/validation/
├── annotation/
│   ├── ValidEmail.java
│   ├── ValidPhone.java
│   ├── ValidOrderTime.java
│   ├── ValidDeliveryAddress.java
│   └── ValidOrderStatus.java
├── validator/
│   ├── EmailValidator.java
│   ├── PhoneValidator.java
│   ├── OrderTimeValidator.java
│   ├── DeliveryAddressValidator.java
│   └── OrderStatusValidator.java
└── BusinessRulesEngine.java
src/main/java/com/fasteat/service/validation/
└── ValidationService.java
src/test/java/com/fasteat/validation/
└── CustomValidatorTest.java
```

---

# Phase 4: Polish & Testing

**Duration**: 5-6 weeks  
**Focus**: REST controllers, API documentation, testing, deployment preparation, optimization

---

## BEAD-4.1: REST Controllers - User & Auth

**Status**:  BLOCKED (by BEAD-3.12)  
**Dependencies**: BEAD-3.12  
**Phase**: 4 - Polish  

### Description
Implement REST controllers for user management endpoints with proper HTTP methods, status codes, and documentation.

### Tasks
- [ ] Create UserController with @RestController and @RequestMapping
- [ ] Implement GET /api/users/me (get current user profile)
- [ ] Implement PUT /api/users/me (update profile)
- [ ] Implement POST /api/users/me/change-password
- [ ] Implement GET /api/users/{id} (admin only)
- [ ] Implement DELETE /api/users/me (delete account)
- [ ] Create AddressController
- [ ] Implement GET /api/users/me/addresses
- [ ] Implement POST /api/users/me/addresses
- [ ] Implement PUT /api/users/me/addresses/{id}
- [ ] Implement DELETE /api/users/me/addresses/{id}
- [ ] Implement PATCH /api/users/me/addresses/{id}/set-default
- [ ] Add @Valid annotation for request body validation
- [ ] Return appropriate HTTP status codes (200, 201, 204, 400, 404)
- [ ] Add @PreAuthorize for role-based access control
- [ ] Use ResponseEntity for flexible responses
- [ ] Add pagination support with Pageable parameter
- [ ] Write controller unit tests with MockMvc
- [ ] Write integration tests with @SpringBootTest

### Acceptance Criteria
- [ ] All endpoints compile and run
- [ ] Proper HTTP methods used (GET, POST, PUT, DELETE, PATCH)
- [ ] Correct status codes returned
- [ ] Request validation works
- [ ] Authorization rules enforced
- [ ] Pagination works for list endpoints
- [ ] Controller tests pass
- [ ] Integration tests verify end-to-end flow
- [ ] Error responses follow standard format

### Related Files
```
src/main/java/com/fasteat/controller/
├── UserController.java
└── AddressController.java
src/test/java/com/fasteat/controller/
├── UserControllerTest.java
└── UserControllerIntegrationTest.java
```

---

## BEAD-4.2: REST Controllers - Restaurant & Menu

**Status**:  BLOCKED (by BEAD-4.1)  
**Dependencies**: BEAD-4.1  
**Phase**: 4 - Polish  

### Description
Implement REST controllers for restaurant and menu management with public browsing and owner management endpoints.

### Tasks
- [ ] Create RestaurantController
- [ ] Implement GET /api/restaurants (public - browse all)
- [ ] Implement GET /api/restaurants/{id} (public - view details)
- [ ] Implement GET /api/restaurants/search (public - search)
- [ ] Implement GET /api/restaurants/nearby (public - geospatial search)
- [ ] Implement POST /api/restaurants (owner - create)
- [ ] Implement PUT /api/restaurants/{id} (owner - update)
- [ ] Implement DELETE /api/restaurants/{id} (owner/admin - delete)
- [ ] Implement GET /api/restaurants/{id}/menu (public - view menu)
- [ ] Create MenuItemController
- [ ] Implement GET /api/menu-items/{id} (public)
- [ ] Implement POST /api/restaurants/{restaurantId}/menu-items (owner)
- [ ] Implement PUT /api/menu-items/{id} (owner)
- [ ] Implement DELETE /api/menu-items/{id} (owner)
- [ ] Implement PATCH /api/menu-items/{id}/availability (owner)
- [ ] Add filtering by category, dietary restrictions
- [ ] Add sorting by price, rating, popularity
- [ ] Add pagination for all list endpoints
- [ ] Verify ownership before allowing modifications
- [ ] Write controller tests
- [ ] Write integration tests

### Acceptance Criteria
- [ ] All endpoints work correctly
- [ ] Public endpoints accessible without auth
- [ ] Owner endpoints require authentication and authorization
- [ ] Cannot modify other owner's restaurants
- [ ] Search and filtering work correctly
- [ ] Geospatial queries return nearby restaurants
- [ ] Pagination and sorting work
- [ ] Tests pass
- [ ] Integration tests verify workflows

### Related Files
```
src/main/java/com/fasteat/controller/
├── RestaurantController.java
└── MenuItemController.java
src/test/java/com/fasteat/controller/
├── RestaurantControllerTest.java
└── MenuItemControllerTest.java
```

---

## BEAD-4.3: REST Controllers - Order Management

**Status**:  BLOCKED (by BEAD-4.2)  
**Dependencies**: BEAD-4.2  
**Phase**: 4 - Polish  

### Description
Implement REST controllers for order management with customer and restaurant owner perspectives.

### Tasks
- [ ] Create OrderController
- [ ] Implement POST /api/orders (customer - create order)
- [ ] Implement GET /api/orders/{id} (customer/owner - view order)
- [ ] Implement GET /api/orders (customer - my orders)
- [ ] Implement GET /api/restaurants/{restaurantId}/orders (owner - restaurant orders)
- [ ] Implement PATCH /api/orders/{id}/cancel (customer - cancel order)
- [ ] Implement PATCH /api/orders/{id}/status (owner - update status)
- [ ] Implement GET /api/orders/{id}/track (customer - track order)
- [ ] Add order filtering (status, date range)
- [ ] Add order history with pagination
- [ ] Validate order request (items available, restaurant open)
- [ ] Calculate order total and return in response
- [ ] Verify authorization (customer sees only their orders)
- [ ] Verify authorization (owner sees only their restaurant's orders)
- [ ] Add real-time order updates (WebSocket - optional)
- [ ] Write controller tests
- [ ] Write integration tests for order flow

### Acceptance Criteria
- [ ] Order creation works with validation
- [ ] Order status updates follow state machine
- [ ] Customers can only see/modify their orders
- [ ] Owners can only see/modify their restaurant's orders
- [ ] Order cancellation has time restrictions
- [ ] Order filtering and pagination work
- [ ] Order tracking returns current status
- [ ] Tests pass
- [ ] Integration tests verify order lifecycle

### Related Files
```
src/main/java/com/fasteat/controller/
└── OrderController.java
src/test/java/com/fasteat/controller/
├── OrderControllerTest.java
└── OrderControllerIntegrationTest.java
```

---

## BEAD-4.4: REST Controllers - Reviews

**Status**:  BLOCKED (by BEAD-4.3)  
**Dependencies**: BEAD-4.3  
**Phase**: 4 - Polish  

### Description
Implement REST controllers for review and rating management.

### Tasks
- [ ] Create ReviewController
- [ ] Implement POST /api/restaurants/{restaurantId}/reviews (customer)
- [ ] Implement GET /api/restaurants/{restaurantId}/reviews (public)
- [ ] Implement GET /api/reviews/{id} (public)
- [ ] Implement PUT /api/reviews/{id} (customer - update own review)
- [ ] Implement DELETE /api/reviews/{id} (customer - delete own review)
- [ ] Implement POST /api/reviews/{id}/response (owner - respond to review)
- [ ] Implement POST /api/reviews/{id}/helpful (customer - upvote review)
- [ ] Add filtering (rating, verified purchase)
- [ ] Add sorting (date, helpfulness)
- [ ] Add pagination
- [ ] Verify customer ordered from restaurant before allowing review
- [ ] Prevent duplicate reviews
- [ ] Verify authorization (customer can only modify their reviews)
- [ ] Verify authorization (owner can only respond to their restaurant's reviews)
- [ ] Write controller tests
- [ ] Write integration tests

### Acceptance Criteria
- [ ] Review creation works with validation
- [ ] Cannot review without order (if enforced)
- [ ] Cannot create duplicate reviews
- [ ] Can update/delete own reviews
- [ ] Owner can respond to reviews
- [ ] Filtering and sorting work
- [ ] Pagination works
- [ ] Authorization enforced
- [ ] Tests pass

### Related Files
```
src/main/java/com/fasteat/controller/
└── ReviewController.java
src/test/java/com/fasteat/controller/
└── ReviewControllerTest.java
```

---

## BEAD-4.5: OpenAPI / Swagger Documentation

**Status**:  BLOCKED (by BEAD-4.4)  
**Dependencies**: BEAD-4.4  
**Phase**: 4 - Polish  

### Description
Add comprehensive OpenAPI 3.0 documentation using SpringDoc. Document all endpoints, schemas, security, and examples.

### Tasks
- [ ] Add springdoc-openapi-ui dependency
- [ ] Create OpenApiConfig class
- [ ] Configure OpenAPI metadata (title, version, description, contact, license)
- [ ] Configure security scheme (JWT Bearer)
- [ ] Add @Operation annotations to all controller methods
- [ ] Add @ApiResponse annotations for all response codes
- [ ] Add @Parameter annotations for path/query parameters
- [ ] Add @Schema annotations to all DTOs
- [ ] Add example values with @Schema(example = "...")
- [ ] Group endpoints by tags (@Tag annotation)
- [ ] Add descriptions to all request/response models
- [ ] Document authentication flows
- [ ] Add server URLs for different environments
- [ ] Configure Swagger UI customization
- [ ] Generate OpenAPI JSON/YAML spec
- [ ] Test Swagger UI at /swagger-ui.html
- [ ] Export OpenAPI spec for frontend team
- [ ] Document all error responses

### Acceptance Criteria
- [ ] Swagger UI accessible and functional
- [ ] All endpoints documented with descriptions
- [ ] Request/response schemas complete
- [ ] Security scheme configured for JWT
- [ ] Examples provided for complex requests
- [ ] Error responses documented
- [ ] API can be tested via Swagger UI
- [ ] OpenAPI spec exports successfully
- [ ] Documentation is accurate and helpful

### Related Files
```
src/main/java/com/fasteat/config/
└── OpenApiConfig.java
pom.xml (springdoc dependency)
docs/
├── api-spec.json
└── api-spec.yaml
```

**Access Swagger UI at**: `http://localhost:8080/swagger-ui.html`  
**Access OpenAPI spec at**: `http://localhost:8080/v3/api-docs`

---

## BEAD-4.6: Application Dockerfile & Optimization

**Status**:  BLOCKED (by BEAD-4.5)  
**Dependencies**: BEAD-4.5  
**Phase**: 4 - Polish  

### Description
Create optimized Dockerfile for the Spring Boot application using multi-stage builds and best practices.

### Tasks
- [ ] Create Dockerfile with multi-stage build
- [ ] Stage 1: Build with Maven (maven:3.8-openjdk-17)
- [ ] Copy pom.xml and download dependencies (layer caching)
- [ ] Copy source and build application
- [ ] Stage 2: Runtime with JRE (eclipse-temurin:17-jre-alpine)
- [ ] Copy JAR from build stage
- [ ] Create non-root user for security
- [ ] Set working directory
- [ ] Expose port 8080
- [ ] Configure ENTRYPOINT with java -jar
- [ ] Add health check
- [ ] Optimize image size (use Alpine, clean cache)
- [ ] Add .dockerignore file
- [ ] Update docker-compose.yml to include app service
- [ ] Configure environment variables
- [ ] Configure depends_on for database
- [ ] Test Docker build and run
- [ ] Verify application connects to PostgreSQL
- [ ] Document Docker commands

### Acceptance Criteria
- [ ] Dockerfile builds successfully
- [ ] Image size is optimized (<300MB)
- [ ] Application runs in container
- [ ] Can connect to PostgreSQL container
- [ ] Health check works
- [ ] Non-root user configured
- [ ] Environment variables work
- [ ] docker-compose starts entire stack
- [ ] Documentation complete

### Related Files
```
Dockerfile
.dockerignore
docker-compose.yml (updated)
scripts/docker-build.sh
scripts/docker-run.sh
docs/docker-deployment.md
```

---

## BEAD-4.7: Integration Testing Suite

**Status**:  BLOCKED (by BEAD-4.6)  
**Dependencies**: BEAD-4.6  
**Phase**: 4 - Polish  

### Description
Create comprehensive integration testing suite using TestContainers for real database testing.

### Tasks
- [ ] Add TestContainers dependencies (postgresql, junit-jupiter)
- [ ] Create BaseIntegrationTest abstract class
- [ ] Configure TestContainers PostgreSQL
- [ ] Configure test profile (application-test.yml)
- [ ] Create test data builders and fixtures
- [ ] Write integration tests for AuthController
- [ ] Write integration tests for UserController
- [ ] Write integration tests for RestaurantController
- [ ] Write integration tests for MenuItemController
- [ ] Write integration tests for OrderController (full lifecycle)
- [ ] Write integration tests for ReviewController
- [ ] Test authentication and authorization
- [ ] Test database transactions and rollbacks
- [ ] Test concurrent requests
- [ ] Test error scenarios
- [ ] Use @SpringBootTest with RANDOM_PORT
- [ ] Use TestRestTemplate or WebTestClient
- [ ] Measure test coverage
- [ ] Configure test execution order (if needed)
- [ ] Ensure tests are independent and idempotent

### Acceptance Criteria
- [ ] All integration tests pass
- [ ] TestContainers PostgreSQL works
- [ ] Tests use real database operations
- [ ] Authentication/authorization tested
- [ ] Order lifecycle tested end-to-end
- [ ] Error scenarios covered
- [ ] Tests are fast (<2 minutes total)
- [ ] Tests are reliable (no flakiness)
- [ ] Code coverage >70%

### Related Files
```
src/test/java/com/fasteat/integration/
├── BaseIntegrationTest.java
├── AuthIntegrationTest.java
├── UserIntegrationTest.java
├── RestaurantIntegrationTest.java
├── OrderIntegrationTest.java
└── ReviewIntegrationTest.java
src/test/resources/
├── application-test.yml
└── test-data.sql
pom.xml (testcontainers dependencies)
```

---

## BEAD-4.8: Unit Testing & Test Coverage

**Status**:  BLOCKED (by BEAD-4.7)  
**Dependencies**: BEAD-4.7  
**Phase**: 4 - Polish  

### Description
Enhance unit test coverage across all layers. Achieve target coverage metrics and identify gaps.

### Tasks
- [ ] Review existing unit tests across all packages
- [ ] Write missing unit tests for services (target: >80%)
- [ ] Write missing unit tests for repositories (target: >70%)
- [ ] Write missing unit tests for mappers (target: >90%)
- [ ] Write missing unit tests for validators (target: >90%)
- [ ] Write missing unit tests for controllers (target: >70%)
- [ ] Write missing unit tests for utilities (target: >90%)
- [ ] Use Mockito for mocking dependencies
- [ ] Use @MockBean for Spring beans in tests
- [ ] Test happy paths and error scenarios
- [ ] Test edge cases and boundary conditions
- [ ] Configure JaCoCo plugin for coverage reports
- [ ] Generate coverage reports
- [ ] Review coverage reports and identify gaps
- [ ] Add tests to fill critical gaps
- [ ] Configure coverage thresholds in build
- [ ] Fail build if coverage drops below threshold

### Acceptance Criteria
- [ ] Unit test coverage >75% overall
- [ ] Service layer coverage >80%
- [ ] Critical business logic coverage >90%
- [ ] All public methods tested
- [ ] Error scenarios covered
- [ ] Edge cases tested
- [ ] Tests are fast (<30 seconds)
- [ ] JaCoCo reports generated
- [ ] Build fails if coverage drops

### Related Files
```
src/test/java/com/fasteat/service/
src/test/java/com/fasteat/repository/
src/test/java/com/fasteat/mapper/
src/test/java/com/fasteat/validation/
src/test/java/com/fasteat/controller/
src/test/java/com/fasteat/util/
pom.xml (jacoco plugin)
jacoco.xml (coverage thresholds)
```

**JaCoCo Reports Location**: `target/site/jacoco/index.html`

---

## BEAD-4.9: Performance Testing & Optimization

**Status**:  BLOCKED (by BEAD-4.8)  
**Dependencies**: BEAD-4.8  
**Phase**: 4 - Polish  

### Description
Perform performance testing, identify bottlenecks, and optimize critical paths.

### Tasks
- [ ] Set up performance testing framework (JMeter or Gatling)
- [ ] Create performance test scenarios (browse, order, search)
- [ ] Test concurrent users (10, 50, 100, 500)
- [ ] Measure response times (p50, p95, p99)
- [ ] Measure throughput (requests per second)
- [ ] Identify slow queries with query logging
- [ ] Add database indexes for slow queries
- [ ] Optimize N+1 queries with JOIN FETCH
- [ ] Add caching for frequently accessed data (Spring Cache)
- [ ] Configure Redis for distributed caching (optional)
- [ ] Optimize connection pool settings
- [ ] Add compression for API responses
- [ ] Test with production-like data volume
- [ ] Profile application with JProfiler or VisualVM
- [ ] Document performance benchmarks
- [ ] Create performance regression tests

### Acceptance Criteria
- [ ] Response times meet requirements (p95 <500ms)
- [ ] System handles target load (100 concurrent users)
- [ ] No N+1 query issues
- [ ] Database indexes optimized
- [ ] Caching improves performance
- [ ] Connection pool tuned
- [ ] Performance tests pass
- [ ] Benchmarks documented
- [ ] Performance regression tests in CI

### Related Files
```
src/test/java/com/fasteat/performance/
├── BrowsePerformanceTest.java
├── OrderPerformanceTest.java
└── SearchPerformanceTest.java
src/main/java/com/fasteat/config/
├── CacheConfig.java
└── RedisConfig.java (optional)
docs/
└── performance-benchmarks.md
performance-tests/
└── jmeter/
    └── fasteat-test-plan.jmx
```

---

## BEAD-4.10: Production Deployment Preparation

**Status**:  BLOCKED (by BEAD-4.9)  
**Dependencies**: BEAD-4.9  
**Phase**: 4 - Polish  

### Description
Prepare application for production deployment with proper configuration, monitoring, and deployment automation.

### Tasks
- [ ] Create production application-prod.yml
- [ ] Externalize all secrets (database password, JWT secret)
- [ ] Configure environment variables for production
- [ ] Set up proper logging configuration (logback-spring.xml)
- [ ] Configure log aggregation (ELK or CloudWatch)
- [ ] Add Spring Boot Actuator for monitoring
- [ ] Expose health, metrics, info endpoints
- [ ] Secure actuator endpoints
- [ ] Configure database connection pooling for production
- [ ] Configure graceful shutdown
- [ ] Add application versioning
- [ ] Create deployment scripts (start, stop, restart)
- [ ] Create systemd service file (for Linux deployment)
- [ ] Create Kubernetes manifests (deployment, service, ingress) - optional
- [ ] Set up CI/CD pipeline (GitHub Actions or Jenkins)
- [ ] Create database backup strategy
- [ ] Create rollback procedure
- [ ] Document deployment process
- [ ] Create operational runbook

### Acceptance Criteria
- [ ] Production configuration complete
- [ ] No hardcoded secrets
- [ ] Logging works in production
- [ ] Actuator endpoints accessible and secured
- [ ] Health checks work
- [ ] Metrics exposed for monitoring
- [ ] Graceful shutdown works
- [ ] Deployment scripts work
- [ ] CI/CD pipeline deploys successfully
- [ ] Documentation complete
- [ ] Rollback procedure tested

### Related Files
```
src/main/resources/
├── application-prod.yml
└── logback-spring.xml
scripts/
├── deploy.sh
├── start.sh
├── stop.sh
└── restart.sh
deployment/
├── fasteat-backend.service (systemd)
└── kubernetes/
    ├── deployment.yaml
    ├── service.yaml
    ├── configmap.yaml
    └── secret.yaml
.github/workflows/
└── deploy-prod.yml
docs/
├── deployment-guide.md
├── monitoring-guide.md
└── operational-runbook.md
```

---

# Progress Tracking

## Overall Progress

**Completed**: 0 / 37 beads (0%)  
**In Progress**: 0 / 37 beads (0%)  
**Blocked**: 36 / 37 beads (97%)  
**Ready**: 1 / 37 beads (3%)

---

## Phase Progress

| Phase | Completed | Total | Percentage |
|-------|-----------|-------|------------|
| Phase 1: Foundation | 0 | 7 | 0% |
| Phase 2: Domain | 0 | 8 | 0% |
| Phase 3: Business Logic | 0 | 12 | 0% |
| Phase 4: Polish & Testing | 0 | 10 | 0% |

---

## Bead Checklist

### Phase 1: Foundation Layer
- [ ] BEAD-1.1: Maven Project Initialization
- [ ] BEAD-1.2: Docker Compose Setup
- [ ] BEAD-1.3: Application Configuration
- [ ] BEAD-1.4: Core Entity Base Classes
- [ ] BEAD-1.5: Database Schema Design
- [ ] BEAD-1.6: JPA Entity Mapping
- [ ] BEAD-1.7: Spring Data JPA Repositories

### Phase 2: Domain Layer
- [ ] BEAD-2.1: User Entity & Authentication Fields
- [ ] BEAD-2.2: Restaurant Entity & Business Logic
- [ ] BEAD-2.3: MenuItem Entity & Ingredient Relationships
- [ ] BEAD-2.4: Order Entity & Order Items
- [ ] BEAD-2.5: Review & Rating System
- [ ] BEAD-2.6: Address & Delivery Management
- [ ] BEAD-2.7: Repository Custom Queries & Optimization
- [ ] BEAD-2.8: Database Seed Data & Test Fixtures

### Phase 3: Business Logic Layer
- [ ] BEAD-3.1: Global Exception Handling
- [ ] BEAD-3.2: DTO Layer & Request/Response Objects
- [ ] BEAD-3.3: Entity-DTO Mappers
- [ ] BEAD-3.4: Service Layer - User Management
- [ ] BEAD-3.5: Service Layer - Restaurant & Menu Management
- [ ] BEAD-3.6: Service Layer - Order Management
- [ ] BEAD-3.7: Service Layer - Review & Rating
- [ ] BEAD-3.8: JWT Authentication Implementation
- [ ] BEAD-3.9: Spring Security Configuration
- [ ] BEAD-3.10: Authentication Endpoints
- [ ] BEAD-3.11: Event System & Notifications
- [ ] BEAD-3.12: Validation & Business Rules Engine

### Phase 4: Polish & Testing
- [ ] BEAD-4.1: REST Controllers - User & Auth
- [ ] BEAD-4.2: REST Controllers - Restaurant & Menu
- [ ] BEAD-4.3: REST Controllers - Order Management
- [ ] BEAD-4.4: REST Controllers - Reviews
- [ ] BEAD-4.5: OpenAPI / Swagger Documentation
- [ ] BEAD-4.6: Application Dockerfile & Optimization
- [ ] BEAD-4.7: Integration Testing Suite
- [ ] BEAD-4.8: Unit Testing & Test Coverage
- [ ] BEAD-4.9: Performance Testing & Optimization
- [ ] BEAD-4.10: Production Deployment Preparation

---

# Dependency Graph

```
Phase 1: Foundation Layer
==========================

BEAD-1.1 (Maven Project)
    ├─→ BEAD-1.3 (App Config)
    └─→ BEAD-1.2 (Docker Compose)

BEAD-1.2 (Docker)
    └─→ BEAD-1.3 (App Config)

BEAD-1.3 (App Config)
    └─→ BEAD-1.4 (Base Entity)
        └─→ BEAD-1.5 (Schema Design)
            └─→ BEAD-1.6 (Entity Mapping)
                └─→ BEAD-1.7 (Repositories)


Phase 2: Domain Layer
======================

BEAD-1.7 (Repositories)
    ├─→ BEAD-2.1 (User Entity)
    ├─→ BEAD-2.2 (Restaurant Entity)
    └─→ BEAD-2.3 (MenuItem Entity)

BEAD-2.3 (MenuItem)
    └─→ BEAD-2.4 (Order Entity)
        ├─→ BEAD-2.5 (Review System)
        └─→ BEAD-2.6 (Address)

BEAD-2.6 (Address)
    └─→ BEAD-2.7 (Custom Queries)
        └─→ BEAD-2.8 (Seed Data)


Phase 3: Business Logic Layer
==============================

BEAD-2.8 (Seed Data)
    └─→ BEAD-3.1 (Exception Handling)
        └─→ BEAD-3.2 (DTO Layer)
            └─→ BEAD-3.3 (Mappers)
                └─→ BEAD-3.4 (User Service)
                    └─→ BEAD-3.5 (Restaurant Service)
                        └─→ BEAD-3.6 (Order Service)
                            └─→ BEAD-3.7 (Review Service)
                                └─→ BEAD-3.8 (JWT Auth)
                                    └─→ BEAD-3.9 (Security Config)
                                        └─→ BEAD-3.10 (Auth Endpoints)
                                            └─→ BEAD-3.11 (Events)
                                                └─→ BEAD-3.12 (Validation)


Phase 4: Polish & Testing
==========================

BEAD-3.12 (Validation)
    └─→ BEAD-4.1 (User Controllers)
        └─→ BEAD-4.2 (Restaurant Controllers)
            └─→ BEAD-4.3 (Order Controllers)
                └─→ BEAD-4.4 (Review Controllers)
                    └─→ BEAD-4.5 (OpenAPI Docs)
                        └─→ BEAD-4.6 (Dockerfile)
                            └─→ BEAD-4.7 (Integration Tests)
                                └─→ BEAD-4.8 (Unit Tests)
                                    └─→ BEAD-4.9 (Performance)
                                        └─→ BEAD-4.10 (Production Prep)


Critical Path (Longest Chain)
==============================

BEAD-1.1 → BEAD-1.3 → BEAD-1.4 → BEAD-1.5 → BEAD-1.6 → BEAD-1.7 →
BEAD-2.1 → BEAD-2.3 → BEAD-2.4 → BEAD-2.6 → BEAD-2.7 → BEAD-2.8 →
BEAD-3.1 → BEAD-3.2 → BEAD-3.3 → BEAD-3.4 → BEAD-3.5 → BEAD-3.6 →
BEAD-3.7 → BEAD-3.8 → BEAD-3.9 → BEAD-3.10 → BEAD-3.11 → BEAD-3.12 →
BEAD-4.1 → BEAD-4.2 → BEAD-4.3 → BEAD-4.4 → BEAD-4.5 → BEAD-4.6 →
BEAD-4.7 → BEAD-4.8 → BEAD-4.9 → BEAD-4.10

Total: 37 beads in critical path
```

---

# Usage Instructions

## Getting Started

1. **Start with Phase 1**: Complete all foundation beads before moving to Phase 2
2. **Follow Dependencies**: Only start a bead when all its dependencies are complete
3. **Update Status**: Change bead status as you progress ( →  → )
4. **Check Tasks**: Use task checkboxes to track granular progress
5. **Verify Acceptance Criteria**: Ensure all criteria met before marking complete

## Working with Beads

### Starting a Bead
1. Verify all dependencies are  COMPLETED
2. Change status to  IN_PROGRESS
3. Review tasks and acceptance criteria
4. Create a feature branch (e.g., `feature/bead-1.1-maven-setup`)

### Completing a Bead
1. Check all task checkboxes
2. Verify all acceptance criteria met
3. Run tests to ensure no regressions
4. Update status to  COMPLETED
5. Check the bead checkbox in [Progress Tracking](#progress-tracking)
6. Update dependent beads from  BLOCKED to  READY

### Parallel Work
Some beads can be worked on in parallel if they share the same dependencies:
- BEAD-2.1, BEAD-2.2 (both depend on BEAD-1.7)
- BEAD-2.5, BEAD-2.6 (both depend on BEAD-2.4)

## Testing Strategy

- **Unit Tests**: Write as you implement each bead
- **Integration Tests**: Phase 4, BEAD-4.7
- **Performance Tests**: Phase 4, BEAD-4.9
- **Coverage Goals**: Defined in BEAD-4.8

## Best Practices

1. **Commit Often**: Commit after completing each task
2. **Write Tests**: Write tests as you code, not after
3. **Document**: Update comments and docs as you go
4. **Review Code**: Self-review before marking complete
5. **Ask for Help**: If stuck, consult team or documentation

## Tracking Progress

- Update this file as you complete beads
- Use checkboxes in [Progress Tracking](#progress-tracking)
- Generate progress reports: `grep -E "^- \[x\]" BEADS.md | wc -l`

## Tools

### Check Next Available Beads
```bash
grep "🟢 READY" BEADS.md
```

### Count Completed Beads
```bash
grep "✅ COMPLETED" BEADS.md | wc -l
```

### Show Current Phase Progress
```bash
grep -A 7 "## Phase Progress" BEADS.md
```

---

# Notes

## Estimations

All time estimates are approximate and assume:
- One full-time developer
- Familiarity with Spring Boot ecosystem
- Access to necessary tools and documentation
- No major unexpected issues

Actual time may vary based on:
- Team experience level
- Requirements changes
- Technical complexity discovered during implementation
- Testing rigor

## Flexibility

This breakdown is a guide, not a strict contract. Feel free to:
- Split large beads into smaller ones
- Merge small related beads
- Adjust estimates based on actual progress
- Reorder non-dependent beads if needed

## Communication

- Use bead IDs (e.g., BEAD-3.4) in commits, PRs, and discussions
- Reference this file in code review comments
- Update estimates if significantly off

## Version Control

- Keep this file in version control
- Update it as part of normal development workflow
- Consider it living documentation

---

## Document Metadata

**Version**: 1.0  
**Created**: 2026-02-23  
**Last Updated**: 2026-02-23  
**Maintainer**: Development Team  
**Total Lines**: ~1850  

---

**Happy Coding! **
