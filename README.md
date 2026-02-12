# Test Data Management (TDM) Tool

An AI-powered test data management tool built with Spring Boot and Spring AI, enabling automatic generation of realistic, structured test data using Large Language Models.

## 🚀 Features

- **AI-Powered Data Generation**: Leverage OpenAI GPT-4 or local Ollama models for intelligent test data creation
- **Type-Safe Structured Output**: Use Java records/classes for guaranteed data structure compliance
- **Schema Management**: Define and manage reusable data schemas
- **Flexible Data Models**: Support for Java classes, JSON schemas, and CSV templates
- **Business Rules**: Apply custom constraints and validation rules
- **Data Persistence**: Store and retrieve generated data with automatic expiration
- **RESTful API**: Easy integration with existing test frameworks
- **Batch Processing**: Generate multiple records efficiently

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- OpenAI API key (or Ollama for local LLM)
- Spring Boot 3.2+

## 🔧 Installation

1. Clone the repository
2. Configure your AI provider in `application.yml`:

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

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 🎯 Quick Start

### 1. Register a Schema

```bash
curl -X POST http://localhost:8080/api/v1/schemas \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "Customer",
    "description": "Customer entity",
    "schemaDefinition": "public record Customer(String name, String email, Integer age) {}",
    "schemaType": "JAVA_CLASS",
    "businessRules": "Age between 18-65, valid email format",
    "active": true
  }'
```

### 2. Generate Test Data

```bash
curl -X POST http://localhost:8080/api/v1/data/generate \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "User",
    "recordCount": 10,
    "persistData": true
  }'
```

Response:
```json
{
  "batchId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "schemaName": "User",
  "recordCount": 10,
  "status": "COMPLETED",
  "data": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "age": 32,
      "phoneNumber": "+1-555-0123",
      "address": {
        "street": "123 Main St",
        "city": "San Francisco",
        "state": "CA",
        "zipCode": "94102",
        "country": "USA"
      },
      "occupation": "Software Engineer",
      "company": "Google"
    }
    // ... 9 more records
  ],
  "executionTimeMs": 3450
}
```

### 3. Quick Generate (No Persistence)

```bash
curl -X POST http://localhost:8080/api/v1/data/quick-generate/User/5
```

## 📚 API Endpoints

### Schema Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/schemas` | Register a new schema |
| GET | `/api/v1/schemas` | List all schemas |
| GET | `/api/v1/schemas?activeOnly=true` | List active schemas only |
| GET | `/api/v1/schemas/{name}` | Get schema by name |

### Data Generation

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/data/generate` | Generate test data |
| POST | `/api/v1/data/quick-generate/{schema}/{count}` | Quick generation (no persistence) |
| GET | `/api/v1/data/batch/{batchId}` | Retrieve generated data |
| DELETE | `/api/v1/data/cleanup` | Clean expired data |

## 🔍 Usage Examples

### Example 1: Generate E-commerce Orders

```json
{
  "schemaName": "Order",
  "recordCount": 20,
  "additionalConstraints": {
    "order_value": "Between $50 and $500",
    "payment_method": "Credit Card or PayPal only",
    "status": "Mix of statuses except CANCELLED"
  },
  "temperature": 0.8,
  "persistData": true
}
```

### Example 2: Generate Users with Custom Rules

```json
{
  "schemaName": "User",
  "recordCount": 50,
  "additionalConstraints": {
    "location": "New York City only",
    "age_range": "25-40",
    "occupation": "Tech industry only"
  },
  "customPrompt": "Generate diverse user profiles for a tech startup in NYC",
  "temperature": 0.7
}
```

### Example 3: JSON Schema-Based Generation

First, register a JSON schema:

```json
{
  "schemaName": "Product",
  "schemaType": "JSON_SCHEMA",
  "schemaDefinition": "{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\"},\"name\":{\"type\":\"string\"},\"price\":{\"type\":\"number\"},\"category\":{\"type\":\"string\"}},\"required\":[\"id\",\"name\",\"price\"]}",
  "businessRules": "Prices between $10-$1000, realistic product names"
}
```

Then generate:

```json
{
  "schemaName": "Product",
  "recordCount": 30
}
```

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (REST Controllers - DataGeneration,    │
│   SchemaManagement)                     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Service Layer                   │
│  (TestDataManagementService)            │
└──────┬───────────────────────┬──────────┘
       │                       │
┌──────▼────────┐    ┌────────▼─────────┐
│  AI Layer     │    │   Data Layer     │
│ (Spring AI    │    │ (JPA Repos)      │
│  ChatClient)  │    │                  │
└───────────────┘    └──────────────────┘
```

### Core Components

1. **Controllers**
   - `DataGenerationController`: Handles data generation requests
   - `SchemaManagementController`: Manages schema definitions

2. **Services**
   - `TestDataManagementService`: Orchestrates business logic
   - `AIDataGeneratorService`: Integrates with Spring AI

3. **Entities**
   - `DataSchema`: Stores schema definitions
   - `GeneratedData`: Persists generated test data

4. **Domain Models**
   - `User`: Sample user entity with address
   - `Order`: E-commerce order with line items

## 🎨 Customization

### Adding a New Domain Model

1. Create a record class:

```java
public record Employee(
    String employeeId,
    String firstName,
    String lastName,
    String department,
    BigDecimal salary,
    LocalDate hireDate
) {}
```

2. Register the schema:

```java
SchemaDefinitionRequest request = SchemaDefinitionRequest.builder()
    .schemaName("Employee")
    .schemaDefinition("public record Employee(...) {}")
    .schemaType(DataSchema.SchemaType.JAVA_CLASS)
    .businessRules("Salary between $30k-$200k, hire date in past 10 years")
    .build();
```

3. Add specialized generation method (optional):

```java
public List<Employee> generateEmployees(int count, String rules, Double temp) {
    return generateTypedData(Employee.class, count, rules, temp);
}
```

### Using Local LLM (Ollama)

Update `application.yml`:

```yaml
tdm:
  ai:
    provider: ollama

spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama2
```

Start Ollama:
```bash
ollama run llama2
```

## 🧪 Testing

Run all tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

## 📊 Monitoring & Logging

The application provides detailed logging at DEBUG level:

```yaml
logging:
  level:
    com.example.tdm: DEBUG
    org.springframework.ai: DEBUG
```

View H2 console (development):
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:tdmdb`
- Username: `sa`
- Password: (empty)

## 🔒 Security Considerations

1. **API Key Protection**: Never commit API keys to version control
2. **Rate Limiting**: Implement rate limiting for production use
3. **Data Validation**: Always validate generated data before use
4. **Access Control**: Add authentication/authorization for production
5. **Data Retention**: Configure appropriate retention policies

## 🚀 Production Deployment

1. Switch to production database (PostgreSQL):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tdmdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

2. Enable production profile:
```bash
java -jar target/test-data-management-1.0.0.jar --spring.profiles.active=prod
```

3. Configure external properties:
```bash
export OPENAI_API_KEY=your-key-here
export DB_USERNAME=tdm_user
export DB_PASSWORD=secure_password
```

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch
3. Write tests for new features
4. Ensure all tests pass
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- Spring AI Team for the excellent AI integration framework
- OpenAI for GPT models
- Ollama for local LLM support

## 📧 Support

For issues and questions:
- GitHub Issues: [Create an issue]
- Email: support@example.com
- Documentation: [Wiki]

---

**Happy Testing! 🎉**
