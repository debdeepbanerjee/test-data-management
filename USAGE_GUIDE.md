# TDM Tool - Usage Guide

## Table of Contents
1. [Getting Started](#getting-started)
2. [Basic Usage](#basic-usage)
3. [Advanced Features](#advanced-features)
4. [Integration Examples](#integration-examples)
5. [Best Practices](#best-practices)
6. [Troubleshooting](#troubleshooting)

## Getting Started

### Option 1: Local Development

```bash
# Set environment variables
export OPENAI_API_KEY=your-api-key-here

# Run application
mvn spring-boot:run
```

### Option 2: Docker

```bash
# Create .env file
echo "OPENAI_API_KEY=your-api-key-here" > .env

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f tdm-app
```

### Option 3: Using Ollama (Local LLM)

```bash
# Install Ollama
curl https://ollama.ai/install.sh | sh

# Pull a model
ollama pull llama2

# Update application.yml
tdm:
  ai:
    provider: ollama

# Start application
mvn spring-boot:run -Dspring.profiles.active=ollama
```

## Basic Usage

### 1. View Available Schemas

```bash
curl http://localhost:8080/api/v1/schemas
```

Expected output:
```json
[
  {
    "id": 1,
    "schemaName": "User",
    "description": "User entity with personal and contact information",
    "schemaType": "JAVA_CLASS",
    "active": true
  },
  {
    "id": 2,
    "schemaName": "Order",
    "description": "E-commerce order with line items",
    "schemaType": "JAVA_CLASS",
    "active": true
  }
]
```

### 2. Generate Simple Test Data

```bash
curl -X POST http://localhost:8080/api/v1/data/generate \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "User",
    "recordCount": 5,
    "persistData": false
  }'
```

### 3. Save Batch ID for Later Retrieval

```bash
# Generate and save batch ID
BATCH_ID=$(curl -s -X POST http://localhost:8080/api/v1/data/generate \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "User",
    "recordCount": 10,
    "persistData": true
  }' | jq -r '.batchId')

echo "Batch ID: $BATCH_ID"

# Retrieve data later
curl http://localhost:8080/api/v1/data/batch/$BATCH_ID
```

## Advanced Features

### 1. Custom Business Rules

Generate data with specific constraints:

```bash
curl -X POST http://localhost:8080/api/v1/data/generate \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "User",
    "recordCount": 20,
    "additionalConstraints": {
      "age_range": "30-45",
      "location": "West Coast USA (CA, OR, WA)",
      "occupation": "Software Engineering or Data Science",
      "company_size": "Large tech companies (Fortune 500)"
    },
    "temperature": 0.8
  }'
```

### 2. Custom Prompts

Override default generation with custom instructions:

```bash
curl -X POST http://localhost:8080/api/v1/data/generate \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "User",
    "recordCount": 10,
    "customPrompt": "Generate user profiles for senior executives at financial institutions in New York City. Focus on diversity and realistic career progression. Include business-appropriate email addresses.",
    "temperature": 0.7
  }'
```

### 3. Different AI Models

```bash
# Use GPT-3.5 for faster, cheaper generation
curl -X POST http://localhost:8080/api/v1/data/generate \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "User",
    "recordCount": 100,
    "aiModel": "gpt-3.5-turbo",
    "temperature": 0.6
  }'
```

### 4. Register Custom Schema

#### Example: Customer Schema

```bash
curl -X POST http://localhost:8080/api/v1/schemas \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "Customer",
    "description": "Customer entity for CRM system",
    "schemaDefinition": "public record Customer(String customerId, String companyName, String industry, String contactPerson, String email, String phone, Address billingAddress, CustomerTier tier, BigDecimal lifetimeValue, LocalDate firstPurchaseDate) { public record Address(String street, String city, String state, String zipCode, String country) {} public enum CustomerTier { BRONZE, SILVER, GOLD, PLATINUM } }",
    "schemaType": "JAVA_CLASS",
    "businessRules": "- Customer IDs format: CUST-YYYYMMDD-XXXX\n- Lifetime value between $1,000 and $1,000,000\n- Industries: Technology, Finance, Healthcare, Retail, Manufacturing\n- First purchase within past 5 years\n- Contact emails should be corporate domains",
    "active": true,
    "createdBy": "admin"
  }'
```

#### Example: JSON Schema

```bash
curl -X POST http://localhost:8080/api/v1/schemas \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "Transaction",
    "description": "Financial transaction record",
    "schemaType": "JSON_SCHEMA",
    "schemaDefinition": "{\"type\":\"object\",\"properties\":{\"transactionId\":{\"type\":\"string\"},\"accountId\":{\"type\":\"string\"},\"amount\":{\"type\":\"number\"},\"currency\":{\"type\":\"string\"},\"timestamp\":{\"type\":\"string\",\"format\":\"date-time\"},\"type\":{\"type\":\"string\",\"enum\":[\"DEBIT\",\"CREDIT\"]},\"merchant\":{\"type\":\"string\"}},\"required\":[\"transactionId\",\"accountId\",\"amount\",\"currency\",\"timestamp\",\"type\"]}",
    "businessRules": "- Amounts between $1 and $10,000\n- Mix of DEBIT and CREDIT transactions\n- Realistic merchant names\n- Timestamps within past 90 days",
    "active": true
  }'
```

## Integration Examples

### 1. JUnit Test Integration

```java
@SpringBootTest
@TestPropertySource(properties = {
    "tdm.api.url=http://localhost:8080"
})
class UserServiceTest {

    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${tdm.api.url}")
    private String tdmUrl;

    @Test
    void testUserCreation() {
        // Generate test users
        DataGenerationRequest request = DataGenerationRequest.builder()
                .schemaName("User")
                .recordCount(5)
                .persistData(false)
                .build();

        ResponseEntity<DataGenerationResponse> response = restTemplate.postForEntity(
                tdmUrl + "/api/v1/data/generate",
                request,
                DataGenerationResponse.class
        );

        List<User> testUsers = parseUsers(response.getBody().getData());
        
        // Use test users in your tests
        testUsers.forEach(user -> {
            // Your test logic here
            assertNotNull(user.getEmail());
            assertTrue(user.getAge() >= 18 && user.getAge() <= 65);
        });
    }
}
```

### 2. Cucumber/BDD Integration

```java
@Given("I have {int} test users")
public void i_have_test_users(int count) {
    String response = restTemplate.postForObject(
            tdmUrl + "/api/v1/data/quick-generate/User/" + count,
            null,
            String.class
    );
    
    this.testUsers = objectMapper.readValue(
            response, 
            new TypeReference<List<User>>(){}
    );
}
```

### 3. Database Seeding Script

```bash
#!/bin/bash
# seed-database.sh

# Generate users
echo "Generating users..."
curl -s -X POST http://localhost:8080/api/v1/data/quick-generate/User/100 | \
  jq '.data' > users.json

# Generate orders
echo "Generating orders..."
curl -s -X POST http://localhost:8080/api/v1/data/quick-generate/Order/500 | \
  jq '.data' > orders.json

echo "Data generation complete!"
echo "Users: $(jq length users.json) records"
echo "Orders: $(jq length orders.json) records"

# Import to database (example with PostgreSQL)
psql -d mydb -c "COPY users FROM STDIN WITH (FORMAT json)" < users.json
psql -d mydb -c "COPY orders FROM STDIN WITH (FORMAT json)" < orders.json
```

### 4. Performance Testing Data

```bash
# Generate large dataset for load testing
curl -X POST http://localhost:8080/api/v1/data/generate \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "User",
    "recordCount": 1000,
    "persistData": true,
    "additionalConstraints": {
      "distribution": "Even distribution across all states",
      "age": "Bell curve centered at 35"
    }
  }' > load-test-users.json

# Use in JMeter/Gatling
BATCH_ID=$(jq -r '.batchId' load-test-users.json)
echo "Use batch ID in your tests: $BATCH_ID"
```

## Best Practices

### 1. Temperature Settings

- **0.3-0.5**: Consistent, predictable data (unit tests)
- **0.6-0.8**: Balanced creativity and consistency (integration tests)
- **0.8-1.0**: Maximum diversity (exploratory testing)

### 2. Batch Size Recommendations

- **Small batches (1-10)**: Quick iterations, specific test cases
- **Medium batches (10-100)**: Standard test suites
- **Large batches (100-1000)**: Performance testing, data seeding

### 3. Schema Design

✅ **Good Schema**:
```java
public record Product(
    String id,              // Clear, specific field
    String name,            // Self-explanatory
    BigDecimal price,       // Appropriate type
    ProductCategory category // Enum for constraints
) {
    public enum ProductCategory {
        ELECTRONICS, CLOTHING, FOOD, BOOKS
    }
}
```

❌ **Poor Schema**:
```java
public record Product(
    String data,           // Vague field name
    String info,           // Too generic
    int value              // Ambiguous type
) {}
```

### 4. Business Rules

✅ **Good Rules**:
```
- Age between 21 and 65
- Email format: firstname.lastname@company.com
- Salary range: $40,000 - $150,000
- Hire dates within past 10 years
- Even distribution across departments
```

❌ **Poor Rules**:
```
- Make realistic data
- Use good values
- Be diverse
```

### 5. Error Handling

```bash
# Always check response status
response=$(curl -s -w "\n%{http_code}" -X POST \
  http://localhost:8080/api/v1/data/generate \
  -H "Content-Type: application/json" \
  -d '{...}')

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 200 ]; then
    echo "Success: $body"
else
    echo "Error ($http_code): $body"
    exit 1
fi
```

## Troubleshooting

### Common Issues

#### 1. AI Generation Fails

**Symptom**: Status returns `FAILED`

**Solutions**:
- Check API key: `echo $OPENAI_API_KEY`
- Verify network connectivity to OpenAI
- Reduce `recordCount` if timeout
- Lower `temperature` for more reliable generation
- Check logs: `docker-compose logs tdm-app`

#### 2. Invalid Data Structures

**Symptom**: Generated data doesn't match expected format

**Solutions**:
- Review schema definition for correctness
- Add more specific business rules
- Lower temperature (0.3-0.5) for consistency
- Validate using custom prompt

#### 3. Slow Generation

**Symptom**: Request takes >30 seconds

**Solutions**:
- Reduce batch size (max 100 per request)
- Use `gpt-3.5-turbo` instead of `gpt-4`
- Switch to Ollama for local generation
- Implement pagination for large datasets

#### 4. Database Connection Issues

**Symptom**: Cannot persist data

**Solutions**:
```bash
# Check database status
docker-compose ps postgres

# View database logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

### Debug Mode

Enable detailed logging:

```yaml
# application.yml
logging:
  level:
    com.example.tdm: DEBUG
    org.springframework.ai: TRACE
    org.hibernate: DEBUG
```

### Health Checks

```bash
# Application health
curl http://localhost:8080/api/v1/schemas/health

# Database connection
curl http://localhost:8080/actuator/health

# Ollama availability (if using)
curl http://localhost:11434/api/tags
```

## Performance Optimization

### 1. Batch Processing

```java
// Instead of 1000 individual requests
for (int i = 0; i < 1000; i++) {
    generateSingleUser(); // ❌ Slow
}

// Use batch generation
generateUsers(1000); // ✅ Fast
```

### 2. Caching Schemas

```bash
# Cache schema list
SCHEMAS=$(curl -s http://localhost:8080/api/v1/schemas)

# Reuse in multiple requests
echo $SCHEMAS | jq '.[] | select(.schemaName=="User")'
```

### 3. Async Generation (Future Enhancement)

```java
// Submit generation job
CompletableFuture<DataGenerationResponse> future = 
    tdmService.generateDataAsync(request);

// Do other work...

// Get result when ready
DataGenerationResponse response = future.get();
```

## Contact & Support

- GitHub Issues: https://github.com/yourorg/tdm-tool/issues
- Documentation: https://docs.example.com/tdm
- Slack: #tdm-support
