# Development Guide - Spring Boot Backend

This guide provides detailed information for developers working on the Spring Boot implementation of the Fast Eat backend.

## Table of Contents

- [Development Roadmap](#development-roadmap)
- [Module Implementation Guide](#module-implementation-guide)
- [Development Workflow](#development-workflow)
- [Code Organization](#code-organization)
- [Database Management](#database-management)
- [Debugging](#debugging)
- [Performance Optimization](#performance-optimization)
- [Monitoring](#monitoring)
- [Useful Commands](#useful-commands)

---

## Development Roadmap

### Phase 1: Foundation

**Goals:**
- Set up multi-module Maven project
- Configure PostgreSQL database
- Establish Docker development environment
- Create base entity classes

**Tasks:**
- Create Maven parent POM with module structure
- Configure Spring Boot parent and dependency management
- Set up PostgreSQL schema (tables for User, Menu, Ingredient, Order)
- Create docker-compose.yml with PostgreSQL and application services
- Implement base JPA entities with audit fields (@CreatedDate, @LastModifiedDate)
- Configure Spring Data JPA repositories
- Set up application.yml for local and docker profiles

**Deliverables:**
- Multi-module Maven project structure
- PostgreSQL database running in Docker
- Base entities with JPA annotations
- Repository interfaces extending JpaRepository

### Phase 2: Domain Layer

**Goals:**
- Complete entity modeling
- Implement repository layer
- Create database seed scripts

**Tasks:**
- Finalize User entity with payment card fields
- Implement Menu entity with location fields (latitude, longitude)
- Create Ingredient entity with menu relationship
- Implement Order entity with status enum and tracking fields
- Add custom repository methods (e.g., findMenusByLocation)
- Create @Query annotations for complex queries
- Implement database seeding with realistic test data (Milan locations)
- Add pagination support to repository methods

**Deliverables:**
- Complete JPA entity model
- Spring Data repositories with custom queries
- Database seed script with 5-10 restaurants, 20+ menus

### Phase 3: Business Logic

**Goals:**
- Implement service layer
- Add JWT authentication
- Configure Spring Security
- Implement event-driven order processing

**Tasks:**
- Create service interfaces and implementations
- Implement UserService (register, update profile)
- Implement MenuService (list by location, get details, get ingredients)
- Implement OrderService (create, get status, get history)
- Configure Spring Security with JWT filter chain
- Implement JwtTokenProvider for token generation/validation
- Create custom @Customer and @Admin authorization annotations
- Add @ControllerAdvice for centralized exception handling
- Implement ApplicationEventPublisher for order events
- Create @EventListener for async notifications
- Add DTO classes with Jakarta Validation annotations
- Implement MapStruct mappers for entity-DTO conversion

**Deliverables:**
- Complete service layer with business logic
- JWT authentication and authorization
- Custom security annotations
- Event-driven order processing
- DTO layer with validation

### Phase 4: Polish & Testing

**Goals:**
- Complete Docker setup
- Add API documentation
- Achieve 70%+ test coverage
- Finalize README and documentation

**Tasks:**
- Create multi-stage Dockerfile
- Optimize Docker image size (target 60-70% reduction)
- Add health check endpoint (/actuator/health)
- Configure SpringDoc OpenAPI
- Customize Swagger UI with API info and examples
- Write unit tests for service layer (JUnit 5 + Mockito)
- Write integration tests for controllers (@SpringBootTest)
- Configure TestContainers for PostgreSQL integration tests
- Set up JaCoCo for code coverage reporting
- Create Makefile with common development commands
- Write comprehensive README.md
- Document API endpoints and curl examples

**Deliverables:**
- Production-ready Docker setup
- Swagger UI at /api/docs
- 70%+ test coverage with JaCoCo report
- Complete documentation (README, DEVELOPMENT.md)
- Makefile for developer convenience

---

## Module Implementation Guide

### fast-eat-domain

**Purpose:** Data access layer with JPA entities and repositories

**Contents:**
- Entity classes with JPA annotations (@Entity, @Table, @Column)
- Enum classes (OrderStatus, UserRole)
- Spring Data JPA repository interfaces
- Custom repository implementations (if needed)

**Best Practices:**
- Keep entities focused on data structure
- Use Lombok to reduce boilerplate (@Data, @Entity, @Table)
- Add validation constraints at entity level (@NotNull, @Size)
- Use @EntityListeners for audit fields
- Define bidirectional relationships carefully (avoid circular references)

**Example Entity Structure:**
- User entity with authentication and payment card fields
- Menu entity with restaurant location coordinates
- Ingredient entity with relationship to Menu
- Order entity with delivery tracking fields

### fast-eat-service

**Purpose:** Business logic and service orchestration

**Contents:**
- Service interfaces
- Service implementations with @Service annotation
- DTO classes (Java Records recommended for immutability)
- MapStruct mapper interfaces
- Event classes and event listeners
- Business exception classes

**Best Practices:**
- Keep service methods focused on single responsibility
- Validate business rules before database operations
- Use @Transactional for multi-step operations
- Throw descriptive exceptions (ConflictError, ValidationError)
- Emit events for cross-cutting concerns (notifications)

**Key Services:**
- UserService: Registration, profile management, authentication
- MenuService: Location-based menu queries, ingredient retrieval
- OrderService: Order creation, status tracking, delivery management
- AuthService: Token generation, refresh, validation

### fast-eat-api

**Purpose:** REST API controllers and request/response handling

**Contents:**
- REST controllers with @RestController
- Request/Response DTO classes
- Controller advice for exception handling
- API documentation annotations (@Operation, @ApiResponse)

**Best Practices:**
- Keep controllers thin (delegate to services)
- Use @Valid for automatic validation
- Return appropriate HTTP status codes
- Document all endpoints with Swagger annotations
- Handle exceptions at controller level with @ExceptionHandler

**Controller Organization:**
- AuthController: /auth/register, /auth/login, /auth/refresh
- UserController: /users/me (profile operations)
- MenuController: /menus (list, details, ingredients)
- OrderController: /orders (create, status, history)

### fast-eat-security

**Purpose:** Authentication, authorization, and JWT management

**Contents:**
- Spring Security configuration
- JWT token provider/validator
- Authentication filters
- Custom security annotations
- Password encoder configuration

**Best Practices:**
- Never log JWT tokens or passwords
- Use strong JWT secrets (environment variables)
- Implement token refresh mechanism
- Hash passwords with BCrypt (10+ rounds)
- Configure CORS appropriately for mobile clients

**Security Components:**
- JwtAuthenticationFilter: Validates tokens on each request
- JwtTokenProvider: Generates and validates JWT tokens
- SecurityConfig: Configures security filter chain
- Custom annotations: @Customer, @Admin with SpEL expressions

### fast-eat-common

**Purpose:** Shared utilities, exceptions, and constants

**Contents:**
- Custom exception classes
- Utility classes (DateUtils, StringUtils, etc.)
- Constants and configuration properties
- Shared DTOs or value objects

**Best Practices:**
- Keep utilities stateless
- Document exception hierarchy
- Use constants for magic strings/numbers
- Make utilities reusable across modules

**Common Components:**
- ApplicationException: Base exception class
- ErrorResponse: Standardized error format
- Constants: API paths, validation patterns
- Utils: Date formatting, coordinate calculations

---

## Development Workflow

### Branch Strategy

**Recommended:** Trunk-Based Development or Git Flow

**Main Branches:**
- `main` - Production-ready code
- `develop` - Integration branch for features

**Feature Branches:**
- Naming: `feature/short-description`
- Merge to `develop` via pull request
- Delete after merge

**Example Workflow:**
```bash
# Create feature branch
git checkout -b feature/user-authentication

# Make changes and commit
git add .
git commit -m "feat: implement JWT authentication"

# Push and create pull request
git push origin feature/user-authentication
```

### Commit Conventions

Use [Conventional Commits](https://www.conventionalcommits.org/) format:

**Format:** `<type>(<scope>): <subject>`

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `refactor` - Code refactoring
- `test` - Adding or updating tests
- `chore` - Maintenance tasks
- `perf` - Performance improvements
- `style` - Code style changes (formatting)

**Examples:**
```
feat(auth): add JWT token refresh endpoint
fix(orders): prevent duplicate active orders
docs(readme): update quick start instructions
test(service): add unit tests for MenuService
refactor(domain): simplify Order entity relationships
perf(query): optimize menu location search query
```

### Code Review Checklist

Before submitting a pull request, ensure:

- [ ] Code follows project structure and conventions
- [ ] All tests pass (`mvn verify`)
- [ ] New features have unit and integration tests
- [ ] Test coverage meets minimum thresholds
- [ ] No hardcoded values (use configuration)
- [ ] Exceptions are properly handled
- [ ] Logging is appropriate (debug/info/warn/error)
- [ ] Documentation updated (README, JavaDoc)
- [ ] No sensitive data in commits (passwords, tokens)
- [ ] Git commit messages follow conventions
- [ ] No compilation warnings
- [ ] Code formatted according to project style

---

## Code Organization

### Package Structure

```
com.fasteat
├── api/                 # Controllers, DTOs
│   ├── controller/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   └── advice/
├── service/             # Business logic
│   ├── impl/
│   ├── mapper/
│   └── event/
├── domain/              # Entities, repositories
│   ├── entity/
│   ├── repository/
│   └── enums/
├── security/            # Security configuration
│   ├── jwt/
│   ├── annotation/
│   ├── filter/
│   └── config/
└── common/              # Shared utilities
    ├── exception/
    ├── util/
    └── constant/
```

### Naming Conventions

**Classes:**
- **Entities:** Singular noun (User, Menu, Order)
- **Repositories:** EntityRepository (UserRepository)
- **Services:** EntityService (UserService)
- **Controllers:** EntityController (UserController)
- **DTOs:** Purpose + Entity + Request/Response (CreateOrderRequest, UserResponse)
- **Exceptions:** Purpose + Error (NotFoundError, ValidationError)

**Methods:**
- Use verb-noun pattern (findUserById, createOrder, validateCard)
- Boolean methods start with `is`, `has`, `can` (isOrderActive, hasCompleteProfile)
- Avoid generic names (process, handle, execute)

**Constants:**
- ALL_CAPS_WITH_UNDERSCORES
- Group by purpose (API_BASE_PATH, JWT_EXPIRATION_DAYS)

---

## Database Management

### Schema Migrations

**Option 1: Flyway (Recommended)**

Add Flyway dependency to pom.xml and create migration scripts:

```
src/main/resources/db/migration/
├── V1__Initial_schema.sql
├── V2__Add_ingredients_table.sql
└── V3__Add_order_tracking_fields.sql
```

**Naming Convention:** `V{version}__{description}.sql`

**Option 2: Manual Scripts**

Connect to database and run SQL scripts manually:

```bash
# Using Makefile
make db-shell

# Or directly with docker-compose
docker-compose exec postgres psql -U fasteat -d fasteat

# Run SQL script
\i /path/to/schema.sql
```

### Seeding Data

**Development Seed Script Example:**

Create realistic test data for development and showcase:

```sql
-- Insert test users
INSERT INTO users (email, password, first_name, last_name, card_full_name, card_number, card_expire_month, card_expire_year, card_cvv) 
VALUES ('john.doe@example.com', '$2a$10$...', 'John', 'Doe', 'John Doe', '1234567812345678', 12, 2028, '123');

-- Insert restaurants and menus (Milan area)
INSERT INTO menus (name, short_description, long_description, price, delivery_time, image_url, image_version, restaurant_lat, restaurant_lng)
VALUES 
  ('Margherita Pizza', 'Classic Italian pizza', 'Traditional Neapolitan pizza with tomato, mozzarella, and basil', 12.50, 25, '/images/margherita.jpg', 1, 45.4642, 9.1900),
  ('Carbonara Pasta', 'Creamy Roman pasta', 'Spaghetti with eggs, pecorino, guanciale, and black pepper', 14.00, 20, '/images/carbonara.jpg', 1, 45.4668, 9.1905);

-- Insert ingredients
INSERT INTO ingredients (menu_id, name, description, origin, bio)
VALUES 
  ((SELECT id FROM menus WHERE name = 'Margherita Pizza'), 'Mozzarella', 'Fresh buffalo mozzarella', 'Campania, Italy', true),
  ((SELECT id FROM menus WHERE name = 'Margherita Pizza'), 'Tomato Sauce', 'San Marzano tomatoes', 'Campania, Italy', true);
```

**Run Seed Script:**
```bash
make db-seed
# or
docker-compose exec postgres psql -U fasteat -d fasteat -f /path/to/seed.sql
```

### Database Best Practices

- Use indexes for frequently queried fields (email, location coordinates)
- Add foreign key constraints for referential integrity
- Use appropriate data types (DECIMAL for prices, TIMESTAMP for dates)
- Avoid NULL values where possible (use default values)
- Document schema with comments

---

## Debugging

### Local Development (IDE)

**IntelliJ IDEA Setup:**
1. Import project as Maven project
2. Set Spring profile to `local` in Run Configuration
3. Set breakpoints in code
4. Run in Debug mode (Shift+F9)

**Environment Variables:**
```
SPRING_PROFILES_ACTIVE=local
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fasteat
JWT_SECRET=local-dev-secret
LOG_LEVEL=DEBUG
```

### Docker Debugging

**Remote Debugging Setup:**

1. **Modify Dockerfile to enable debugging:**
```dockerfile
CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]
```

2. **Expose debug port in docker-compose.yml:**
```yaml
backend:
  ports:
    - "8080:8080"
    - "5005:5005"
```

3. **Configure IDE remote debugger:**
   - Host: localhost
   - Port: 5005
   - Debugger: Java Remote Debugging

### Logging

**Configure Log Levels in application.yml:**

```yaml
logging:
  level:
    root: INFO
    com.fasteat: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

**View Logs:**
```bash
# Docker logs
docker-compose logs -f backend

# Tail specific service
docker-compose logs -f postgres

# Follow logs from specific timestamp
docker-compose logs --since 30m backend
```

---

## Performance Optimization

### Database Optimization

**Indexing Strategy:**

Create indexes for frequently queried fields:

```sql
-- Location-based menu queries
CREATE INDEX idx_menus_location ON menus(restaurant_lat, restaurant_lng);

-- User orders lookup
CREATE INDEX idx_orders_user_status ON orders(user_id, status);

-- Email lookup for authentication
CREATE INDEX idx_users_email ON users(email);
```

**Query Optimization:**
- Use `@EntityGraph` to avoid N+1 queries
- Use DTOs for read-only queries (no lazy loading overhead)
- Implement pagination for large result sets
- Use native queries for complex aggregations

### Caching Strategy (Optional: Redis)

**When to Cache:**
- Menu images (large Base64 payloads)
- Menu listings by location (high query frequency)
- User profiles (frequently accessed)
- Authentication tokens (reduce database lookups)

**Cache Configuration Example:**

```yaml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
```

**Usage:**
```java
@Cacheable(value = "menuImages", key = "#menuId")
public String getMenuImage(Long menuId) { ... }

@CacheEvict(value = "menuImages", key = "#menuId")
public void updateMenuImage(Long menuId, String image) { ... }
```

### JVM Tuning

**Production JVM Flags:**

```dockerfile
CMD ["java", 
     "-Xms512m",           # Initial heap size
     "-Xmx1024m",          # Maximum heap size
     "-XX:+UseG1GC",       # Use G1 garbage collector
     "-XX:MaxGCPauseMillis=200",  # Target max GC pause
     "-jar", "app.jar"]
```

**Memory Considerations:**
- Set Xms = Xmx for consistent performance
- Monitor heap usage with Actuator metrics
- Adjust based on container memory limits

---

## Monitoring

### Spring Boot Actuator

**Enable Actuator Endpoints:**

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

**Health Check:**
```bash
curl http://localhost:8080/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

**Metrics:**
```bash
# JVM memory metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP request metrics
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Application Metrics

**Key Metrics to Monitor:**
- Response times (percentiles: p50, p95, p99)
- Error rates (4xx, 5xx responses)
- Database connection pool usage
- JVM memory and GC metrics
- Active requests

---

## Useful Commands

### Maven Commands

```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run -pl fast-eat-api

# Run tests
mvn test                    # Unit tests only
mvn verify                  # Unit + integration tests
mvn clean verify jacoco:report  # With coverage report

# Skip tests during build
mvn clean install -DskipTests

# Run specific test class
mvn test -Dtest=UserServiceTest

# Debug tests
mvn test -Dmaven.surefire.debug
```

### Docker Commands

```bash
# Start services
docker-compose up -d        # Detached mode
docker-compose up --build   # Rebuild images

# View logs
docker-compose logs -f backend  # Follow logs
docker-compose logs --tail=100 backend  # Last 100 lines

# Execute commands in containers
docker-compose exec backend sh  # Shell into backend
docker-compose exec postgres psql -U fasteat -d fasteat  # PostgreSQL shell

# Stop and cleanup
docker-compose down         # Stop services
docker-compose down -v      # Stop and remove volumes
docker-compose down --rmi all  # Stop and remove images

# View resource usage
docker stats
```

### Database Commands

```bash
# Connect to database
docker-compose exec postgres psql -U fasteat -d fasteat

# Common PostgreSQL commands (inside psql)
\dt                         # List tables
\d users                    # Describe users table
\q                          # Exit psql

# Backup database
docker-compose exec postgres pg_dump -U fasteat fasteat > backup.sql

# Restore database
docker-compose exec -T postgres psql -U fasteat -d fasteat < backup.sql
```

### Makefile Commands (if implemented)

```bash
make dev          # Start development environment
make test         # Run all tests
make test-unit    # Run unit tests only
make test-int     # Run integration tests only
make logs         # View logs
make shell        # Shell into backend container
make db-shell     # PostgreSQL shell
make db-seed      # Seed database
make clean        # Stop and cleanup
make rebuild      # Rebuild without cache
```

---

## Additional Resources

- **Architecture & API Specification:** [fast-eat-architecture](https://github.com/gerolori/fast-eat-architecture)
- **Spring Boot Documentation:** [spring.io](https://spring.io/projects/spring-boot)
- **Spring Data JPA Guide:** [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- **Spring Security Documentation:** [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- **TestContainers Documentation:** [testcontainers.org](https://www.testcontainers.org/)

---

*For API specification and cross-implementation architecture, see the [Architecture README](https://github.com/gerolori/fast-eat-architecture).*
