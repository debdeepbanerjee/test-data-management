# Test Data Management Tool - Project Structure

## Directory Layout

```
test-data-management/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── tdm/
│   │   │               ├── TestDataManagementApplication.java
│   │   │               │
│   │   │               ├── config/
│   │   │               │   ├── AIConfiguration.java
│   │   │               │   └── DataInitializer.java
│   │   │               │
│   │   │               ├── controller/
│   │   │               │   ├── DataGenerationController.java
│   │   │               │   └── SchemaManagementController.java
│   │   │               │
│   │   │               ├── model/
│   │   │               │   ├── domain/
│   │   │               │   │   ├── User.java
│   │   │               │   │   └── Order.java
│   │   │               │   │
│   │   │               │   ├── dto/
│   │   │               │   │   ├── DataGenerationRequest.java
│   │   │               │   │   ├── DataGenerationResponse.java
│   │   │               │   │   └── SchemaDefinitionRequest.java
│   │   │               │   │
│   │   │               │   └── entity/
│   │   │               │       ├── DataSchema.java
│   │   │               │       └── GeneratedData.java
│   │   │               │
│   │   │               ├── repository/
│   │   │               │   ├── DataSchemaRepository.java
│   │   │               │   └── GeneratedDataRepository.java
│   │   │               │
│   │   │               ├── service/
│   │   │               │   ├── TestDataManagementService.java
│   │   │               │   └── ai/
│   │   │               │       └── AIDataGeneratorService.java
│   │   │               │
│   │   │               └── exception/
│   │   │                   └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       └── static/
│   │           └── (web UI files - optional)
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── tdm/
│                       └── service/
│                           └── TestDataManagementServiceIntegrationTest.java
│
├── docs/
│   ├── API_DOCUMENTATION.md
│   ├── ARCHITECTURE.md
│   └── DEPLOYMENT.md
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── scripts/
│   ├── seed-database.sh
│   └── run-tests.sh
│
├── .gitignore
├── pom.xml
├── README.md
└── USAGE_GUIDE.md
```

## Component Descriptions

### Core Application

#### `TestDataManagementApplication.java`
- Spring Boot application entry point
- Enables JPA auditing
- Configures component scanning

### Configuration Layer

#### `config/AIConfiguration.java`
- Configures Spring AI ChatClient
- Sets up OpenAI and Ollama integrations
- Manages AI model beans

#### `config/DataInitializer.java`
- Initializes default schemas on startup
- Registers User and Order schemas
- Can be extended for custom initialization

### Controller Layer (REST API)

#### `controller/DataGenerationController.java`
**Endpoints:**
- `POST /api/v1/data/generate` - Generate test data
- `POST /api/v1/data/quick-generate/{schema}/{count}` - Quick generation
- `GET /api/v1/data/batch/{batchId}` - Retrieve generated data
- `DELETE /api/v1/data/cleanup` - Clean expired data

#### `controller/SchemaManagementController.java`
**Endpoints:**
- `POST /api/v1/schemas` - Register schema
- `GET /api/v1/schemas` - List all schemas
- `GET /api/v1/schemas/{name}` - Get specific schema
- `GET /api/v1/schemas/health` - Health check

### Model Layer

#### `model/domain/`
Type-safe domain models for AI generation:
- `User.java` - User entity with address
- `Order.java` - Order entity with line items

#### `model/dto/`
Data Transfer Objects for API communication:
- `DataGenerationRequest.java` - Request for data generation
- `DataGenerationResponse.java` - Response with generated data
- `SchemaDefinitionRequest.java` - Schema registration request

#### `model/entity/`
JPA entities for persistence:
- `DataSchema.java` - Schema definitions
- `GeneratedData.java` - Generated test data records

### Repository Layer

#### `repository/DataSchemaRepository.java`
- CRUD operations for schemas
- Find by name, type, active status
- Check existence

#### `repository/GeneratedDataRepository.java`
- CRUD operations for generated data
- Find by batch ID, schema, status
- Query expired data

### Service Layer

#### `service/TestDataManagementService.java`
**Core business logic:**
- Schema registration and management
- Data generation orchestration
- Persistence coordination
- Error handling and logging

#### `service/ai/AIDataGeneratorService.java`
**AI integration:**
- Type-safe data generation using Spring AI
- JSON schema-based generation
- Custom prompt generation
- Specialized methods for common domains

### Exception Handling

#### `exception/GlobalExceptionHandler.java`
- Centralized error handling
- Validation error formatting
- HTTP status mapping
- Consistent error responses

## Data Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP Request
       ▼
┌─────────────────────────────┐
│  REST Controller            │
│  - Validate request         │
│  - Map to DTO              │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│  Service Layer              │
│  - Business logic           │
│  - Orchestrate operations  │
└──────┬──────────────────────┘
       │
       ├─────────┬──────────────┐
       │         │              │
       ▼         ▼              ▼
┌──────────┐ ┌────────┐  ┌──────────┐
│ AI Layer │ │ Repos  │  │  Utils   │
│ (OpenAI) │ │ (JPA)  │  │          │
└──────────┘ └────────┘  └──────────┘
       │         │              │
       └─────────┴──────────────┘
                 │
                 ▼
         ┌──────────────┐
         │   Database   │
         │   (H2/PG)    │
         └──────────────┘
```

## Technology Stack

### Backend
- **Spring Boot 3.2.2** - Application framework
- **Spring AI 1.0.0-M4** - AI integration
- **Spring Data JPA** - Database access
- **Hibernate** - ORM
- **Jackson** - JSON processing
- **Lombok** - Boilerplate reduction

### Database
- **H2** - In-memory (development)
- **PostgreSQL** - Production

### AI/LLM
- **OpenAI GPT-4** - Cloud-based LLM
- **Ollama** - Local LLM option

### Build & Deploy
- **Maven** - Build tool
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

### Testing
- **JUnit 5** - Unit testing
- **Spring Boot Test** - Integration testing
- **Mockito** - Mocking framework

## Configuration Files

### `application.yml`
Main configuration file containing:
- Database settings
- AI provider configuration
- Server configuration
- Logging levels
- Application-specific properties

### `application-test.yml`
Test environment configuration:
- In-memory database
- Mock AI settings
- Reduced logging

### `pom.xml`
Maven configuration:
- Dependencies
- Build plugins
- Spring AI BOM
- Repository configuration

## API Design Patterns

### RESTful Principles
- Resource-based URLs
- HTTP methods for CRUD
- Status codes for responses
- JSON for data exchange

### Request/Response Pattern
```
Request → Controller → Service → AI/DB → Service → Controller → Response
```

### Error Handling Pattern
```
Exception → GlobalExceptionHandler → ErrorResponse → Client
```

## Database Schema

### `data_schemas` Table
```sql
id                BIGINT PRIMARY KEY
schema_name       VARCHAR(255) UNIQUE NOT NULL
description       VARCHAR(1000)
schema_definition TEXT NOT NULL
schema_type       VARCHAR(50) NOT NULL
business_rules    TEXT
sample_prompt     TEXT
active            BOOLEAN NOT NULL
created_at        TIMESTAMP NOT NULL
updated_at        TIMESTAMP
created_by        VARCHAR(255)
```

### `generated_data` Table
```sql
id              BIGINT PRIMARY KEY
schema_id       BIGINT NOT NULL (FK → data_schemas)
batch_id        VARCHAR(255) NOT NULL
json_data       TEXT NOT NULL
record_count    INTEGER NOT NULL
status          VARCHAR(50) NOT NULL
prompt_used     TEXT
ai_model        VARCHAR(100)
ai_temperature  DOUBLE
error_message   TEXT
generated_at    TIMESTAMP NOT NULL
expires_at      TIMESTAMP
requested_by    VARCHAR(255)
```

## Extension Points

### Adding New Domain Models
1. Create record in `model/domain/`
2. Add specialized method in `AIDataGeneratorService`
3. Update `DataInitializer` to register schema
4. Add integration test

### Custom Schema Types
1. Add enum value to `DataSchema.SchemaType`
2. Implement generation logic in `TestDataManagementService`
3. Update documentation

### Additional AI Providers
1. Add provider configuration in `AIConfiguration`
2. Implement provider-specific logic
3. Update application properties

### UI Integration
1. Add static resources in `src/main/resources/static/`
2. Create web controllers
3. Implement frontend using React/Vue/Angular

## Deployment Configurations

### Development
```yaml
Profile: default
Database: H2 (in-memory)
AI Provider: OpenAI (with API key)
Logging: DEBUG
```

### Testing
```yaml
Profile: test
Database: H2 (in-memory)
AI Provider: Mock
Logging: INFO
```

### Production
```yaml
Profile: prod
Database: PostgreSQL
AI Provider: OpenAI/Ollama
Logging: WARN
Security: Enabled
```

## Future Enhancements

1. **Authentication & Authorization**
   - User management
   - Role-based access control
   - API key authentication

2. **Advanced Features**
   - Async data generation
   - Streaming responses
   - Data versioning
   - Schema validation

3. **UI Dashboard**
   - Schema management interface
   - Data preview
   - Generation history
   - Analytics

4. **Additional Integrations**
   - Database seeding tools
   - CI/CD pipeline integration
   - Testing framework plugins
   - Data masking/anonymization

5. **Performance Optimization**
   - Caching layer
   - Batch processing
   - Connection pooling
   - Query optimization
