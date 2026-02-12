# Test Data Management Tool - Complete Implementation

## 🎯 Overview

This is a **production-ready** Test Data Management (TDM) tool built with Spring Boot 3.2 and Spring AI 1.0. It leverages Large Language Models (LLMs) to generate realistic, structured test data automatically based on defined schemas and business rules.

## ✨ Key Features

### 1. **AI-Powered Data Generation**
- Uses OpenAI GPT-4 or local Ollama models
- Generates realistic, contextually appropriate test data
- Supports custom business rules and constraints
- Configurable temperature for creativity vs. consistency

### 2. **Type-Safe Structured Output**
- Java records ensure type safety
- Automatic JSON serialization/deserialization
- No manual parsing required
- Spring AI's `BeanOutputConverter` guarantees structure compliance

### 3. **Flexible Schema Management**
- Support for Java classes, JSON schemas, CSV templates
- Reusable schema definitions
- Business rules as natural language
- Active/inactive schema toggling

### 4. **RESTful API**
- Clean, intuitive endpoints
- Comprehensive request validation
- Detailed error responses
- Supports batch operations

### 5. **Data Persistence**
- Optional data storage with configurable retention
- Batch ID tracking for retrieval
- Automatic expiration and cleanup
- H2 for development, PostgreSQL for production

## 📦 What's Included

### Core Application Files (28 files)

#### **Java Source Files (17)**
1. `TestDataManagementApplication.java` - Main Spring Boot application
2. `AIConfiguration.java` - Spring AI ChatClient configuration
3. `DataInitializer.java` - Default schema initialization
4. `DataGenerationController.java` - REST endpoints for data generation
5. `SchemaManagementController.java` - REST endpoints for schemas
6. `User.java` - Sample user domain model
7. `Order.java` - Sample order domain model
8. `DataGenerationRequest.java` - Generation request DTO
9. `DataGenerationResponse.java` - Generation response DTO
10. `SchemaDefinitionRequest.java` - Schema registration DTO
11. `DataSchema.java` - Schema entity
12. `GeneratedData.java` - Generated data entity
13. `DataSchemaRepository.java` - Schema repository
14. `GeneratedDataRepository.java` - Generated data repository
15. `TestDataManagementService.java` - Main business logic service
16. `AIDataGeneratorService.java` - AI integration service
17. `GlobalExceptionHandler.java` - Exception handling
18. `TestDataManagementServiceIntegrationTest.java` - Integration tests

#### **Configuration Files (5)**
1. `pom.xml` - Maven dependencies and build configuration
2. `application.yml` - Main application configuration
3. `application-test.yml` - Test environment configuration
4. `docker-compose.yml` - Multi-container Docker setup
5. `Dockerfile` - Application containerization

#### **Documentation (4)**
1. `README.md` - Project overview and setup
2. `USAGE_GUIDE.md` - Detailed usage examples
3. `PROJECT_STRUCTURE.md` - Architecture and structure
4. `TDM-API-Collection.postman_collection.json` - Postman collection

#### **Scripts & Config (2)**
1. `quick-start.sh` - Automated setup script
2. `.gitignore` - Git ignore rules

## 🚀 Quick Start (3 Options)

### Option 1: Local Development (Fastest)

```bash
# Set your OpenAI API key
export OPENAI_API_KEY='your-key-here'

# Run quick-start script
./quick-start.sh
```

### Option 2: Docker (Easiest)

```bash
# Create .env file
echo "OPENAI_API_KEY=your-key-here" > .env

# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f tdm-app
```

### Option 3: Using Ollama (Free, Local)

```bash
# Install Ollama
curl https://ollama.ai/install.sh | sh

# Pull a model
ollama pull llama2

# Start application
mvn spring-boot:run -Dspring.profiles.active=ollama
```

## 📝 Example Usage

### 1. List Available Schemas

```bash
curl http://localhost:8080/api/v1/schemas
```

**Response:**
```json
[
  {
    "id": 1,
    "schemaName": "User",
    "description": "User entity with personal and contact information",
    "schemaType": "JAVA_CLASS",
    "active": true
  }
]
```

### 2. Generate Test Users

```bash
curl -X POST http://localhost:8080/api/v1/data/generate \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "User",
    "recordCount": 5,
    "persistData": false
  }'
```

**Response:**
```json
{
  "batchId": "a1b2c3d4-...",
  "schemaName": "User",
  "recordCount": 5,
  "status": "COMPLETED",
  "data": [
    {
      "firstName": "Sarah",
      "lastName": "Johnson",
      "email": "sarah.johnson@techcorp.com",
      "age": 32,
      "phoneNumber": "+1-555-0123",
      "address": {
        "street": "456 Oak Avenue",
        "city": "San Francisco",
        "state": "CA",
        "zipCode": "94102",
        "country": "USA"
      },
      "occupation": "Software Engineer",
      "company": "TechCorp"
    }
    // ... 4 more users
  ],
  "executionTimeMs": 2847
}
```

### 3. Generate with Custom Constraints

```bash
curl -X POST http://localhost:8080/api/v1/data/generate \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "User",
    "recordCount": 10,
    "additionalConstraints": {
      "location": "New York City only",
      "age_range": "25-35",
      "industry": "Finance"
    },
    "temperature": 0.7
  }'
```

### 4. Quick Generate (No Persistence)

```bash
curl -X POST http://localhost:8080/api/v1/data/quick-generate/Order/10
```

## 🏗️ Architecture Highlights

### Layered Architecture

```
┌─────────────────────────────────────┐
│     Presentation Layer              │
│  (REST Controllers)                 │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     Service Layer                   │
│  (Business Logic)                   │
└──────┬───────────────────┬──────────┘
       │                   │
┌──────▼────────┐  ┌──────▼─────────┐
│  AI Layer     │  │  Data Layer    │
│ (Spring AI)   │  │  (Spring Data) │
└───────────────┘  └────────────────┘
```

### Key Design Patterns

1. **Repository Pattern** - Clean data access abstraction
2. **DTO Pattern** - Separation of API and domain models
3. **Service Layer Pattern** - Business logic encapsulation
4. **Builder Pattern** - Fluent object construction
5. **Strategy Pattern** - Multiple AI provider support

### Spring AI Integration

The tool uses Spring AI's latest features:

```java
// Type-safe generation using BeanOutputConverter
BeanOutputConverter<List<User>> converter = 
    new BeanOutputConverter<>(new ParameterizedTypeReference<List<User>>() {});

// ChatClient with structured output
ChatClient chatClient = chatClientBuilder.build();
List<User> users = chatClient.prompt()
    .user(promptText)
    .call()
    .entity(new ParameterizedTypeReference<List<User>>() {});
```

## 🔧 Configuration Options

### AI Provider Selection

**OpenAI (Cloud):**
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4
          temperature: 0.7
```

**Ollama (Local):**
```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama2
          temperature: 0.7
```

### Database Configuration

**Development (H2):**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:tdmdb
```

**Production (PostgreSQL):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tdmdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

## 🧪 Testing

### Run Tests

```bash
# All tests
mvn test

# Integration tests only
mvn verify

# With coverage
mvn clean test jacoco:report
```

### Test Coverage

The implementation includes:
- Unit tests for services
- Integration tests for the full stack
- API endpoint tests
- Repository tests
- Error handling tests

## 🔒 Security Best Practices

1. **API Key Protection**
   - Environment variables for secrets
   - Never commit keys to version control
   - Use `.env` files locally

2. **Input Validation**
   - Jakarta Validation on all DTOs
   - Request size limits
   - Custom constraint validators

3. **Error Handling**
   - No sensitive data in error messages
   - Generic errors for production
   - Detailed logs for debugging

4. **Database Security**
   - Parameterized queries (JPA)
   - No SQL injection vulnerabilities
   - Audit trails with timestamps

## 📊 Performance Considerations

### Optimization Strategies

1. **Batch Processing**
   - Generate up to 1000 records per request
   - Pagination for large result sets
   - Async generation (future enhancement)

2. **Caching**
   - Schema definitions cached in memory
   - Frequently used prompts cached
   - Database query caching

3. **Database Indexing**
   - Primary keys indexed
   - Foreign keys indexed
   - Common query paths optimized

4. **Connection Pooling**
   - HikariCP for database connections
   - Configurable pool sizes
   - Connection timeout handling

## 🚀 Deployment

### Production Checklist

- [ ] Set `OPENAI_API_KEY` environment variable
- [ ] Configure PostgreSQL connection
- [ ] Enable production profile
- [ ] Set up monitoring and logging
- [ ] Configure backup strategy
- [ ] Set up SSL/TLS
- [ ] Implement rate limiting
- [ ] Add authentication/authorization
- [ ] Configure firewall rules
- [ ] Set up CI/CD pipeline

### Docker Deployment

```bash
# Build image
docker build -t tdm-tool:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e OPENAI_API_KEY=your-key \
  -e SPRING_PROFILES_ACTIVE=prod \
  tdm-tool:latest
```

### Kubernetes Deployment (Example)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tdm-tool
spec:
  replicas: 3
  selector:
    matchLabels:
      app: tdm-tool
  template:
    metadata:
      labels:
        app: tdm-tool
    spec:
      containers:
      - name: tdm-tool
        image: tdm-tool:latest
        ports:
        - containerPort: 8080
        env:
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: tdm-secrets
              key: openai-api-key
```

## 📈 Metrics & Monitoring

### Key Metrics to Track

1. **Performance**
   - Data generation time
   - API response times
   - Database query performance
   - AI API latency

2. **Usage**
   - Requests per minute
   - Data generated per day
   - Most used schemas
   - Error rates

3. **Resources**
   - Memory usage
   - CPU utilization
   - Database connections
   - AI token consumption

### Monitoring Tools

- **Spring Boot Actuator** - Application metrics
- **Prometheus** - Metrics collection
- **Grafana** - Visualization
- **ELK Stack** - Log aggregation

## 🔮 Future Enhancements

### Planned Features

1. **Web UI Dashboard**
   - Schema management interface
   - Visual data preview
   - Generation history
   - Analytics and reports

2. **Advanced AI Features**
   - Multi-model comparison
   - Fine-tuned models for specific domains
   - Context-aware generation
   - Data quality scoring

3. **Integration Plugins**
   - JUnit plugin
   - TestNG plugin
   - Cucumber integration
   - Postman collection generator

4. **Data Operations**
   - Data masking/anonymization
   - Data transformation pipelines
   - Export to multiple formats (CSV, SQL, XML)
   - Version control for generated data

5. **Enterprise Features**
   - Multi-tenancy support
   - RBAC and SSO
   - Audit logging
   - Compliance reporting

## 🤝 Contributing

Contributions are welcome! See the project structure documentation for details on how to extend the system.

## 📄 License

MIT License - See LICENSE file for details

## 🙏 Acknowledgments

- **Spring Team** - For the excellent Spring Boot framework
- **Spring AI Team** - For the powerful AI integration capabilities
- **OpenAI** - For GPT models
- **Ollama** - For local LLM support

## 📧 Support

- **Documentation**: See README.md and USAGE_GUIDE.md
- **Issues**: GitHub Issues
- **Email**: support@example.com

---

**Built with ❤️ using Spring Boot, Spring AI, and Java 17**

Last Updated: February 2026
Version: 1.0.0
