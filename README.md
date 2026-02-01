# Mini Sticker Engine

A sticker-style loyalty campaign system that awards shoppers "stickers" based on their purchase transactions.

## Overview

This system:
- Accepts purchase transactions via REST API
- Calculates stickers earned using campaign rules
- Stores transactions and sticker balances (PostgreSQL)
- Provides shopper status lookup with balance and transaction history
- Handles duplicate transactions (idempotency via Redis)
- Uses distributed locking for concurrent request handling

## Campaign Rules

| Rule | Description |
|------|-------------|
| **Base earn rate** | 1 sticker per $10 of total basket spend (floor division) |
| **Promo bonus** | +1 extra sticker per unit of items with `category = "promo"` |
| **Per-transaction cap** | Maximum 5 stickers per transaction |

### Examples
- $19 spend → 1 sticker
- $21 spend → 2 stickers
- $25 spend + 1 promo item → 2 base + 1 promo = 3 stickers

## Running the Application

### Prerequisites
- Java 17+
- Gradle 8.x (or use the wrapper)
- Redis (for idempotency and distributed locking)
- PostgreSQL (or H2 for local development)

### Build and Run
```bash
# Build
./gradlew build

# Run (ensure Redis and PostgreSQL are running)
./gradlew bootRun

# Or run the JAR directly
java -jar build/libs/sticker-engine-1.0.0.jar
```

The server starts on `http://localhost:8080`

### Configuration
Configure in `application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/stickerengine
spring.datasource.username=postgres
spring.datasource.password=postgres

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Logging (optional - enable debug for service layer)
logging.level.com.looplink.stickerengine.service=DEBUG
```

## API Endpoints

### POST /api/transactions
Submit a purchase transaction to earn stickers.

**Request:**
```json
{
  "transactionId": "tx-1001",
  "shopperId": "shopper-123",
  "storeId": "store-01",
  "timestamp": "2025-01-10T10:15:00Z",
  "items": [
    { "sku": "SKU-MILK", "name": "Milk", "quantity": 2, "unitPrice": 5, "category": "grocery" },
    { "sku": "SKU-PLUSH", "name": "Promo Plush Toy", "quantity": 1, "unitPrice": 15, "category": "promo" }
  ]
}
```

**Response (200 OK):**
```json
{
  "transactionId": "tx-1001",
  "shopperId": "shopper-123",
  "stickersEarned": 3,
  "newTotalBalance": 3,
  "duplicate": false,
  "message": "Transaction processed successfully. Earned 3 sticker(s)."
}
```

**Duplicate Transaction Response:**
```json
{
  "transactionId": "tx-1001",
  "shopperId": "shopper-123",
  "stickersEarned": 3,
  "newTotalBalance": 3,
  "duplicate": true,
  "message": "Duplicate transaction. Previously awarded 3 sticker(s)."
}
```

### GET /api/shoppers/{shopperId}
Get a shopper's sticker balance and transaction history.

**Response (200 OK):**
```json
{
  "shopperId": "shopper-123",
  "totalStickers": 3,
  "transactions": [
    {
      "transactionId": "tx-1001",
      "storeId": "store-01",
      "timestamp": "2025-01-10T10:15:00Z",
      "totalAmount": "$25",
      "stickersEarned": 3
    }
  ]
}
```

**Response (404 Not Found):** Shopper not found

## Error Handling

### Validation Errors (400 Bad Request)
```json
{
  "timestamp": "2025-01-10T10:15:00Z",
  "status": 400,
  "error": "Validation Failed",
  "messages": [
    "shopperId: Shopper ID is required",
    "items: Items list cannot be empty"
  ]
}
```

### Invalid JSON (400 Bad Request)
```json
{
  "timestamp": "2025-01-10T10:15:00Z",
  "status": 400,
  "error": "Invalid Request Body",
  "message": "Unable to parse request. Please check JSON format and data types."
}
```

## Testing with cURL

```bash
# Submit a transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "tx-1001",
    "shopperId": "shopper-123",
    "storeId": "store-01",
    "timestamp": "2025-01-10T10:15:00Z",
    "items": [
      { "sku": "SKU-MILK", "name": "Milk", "quantity": 2, "unitPrice": 5, "category": "grocery" },
      { "sku": "SKU-PLUSH", "name": "Promo Plush Toy", "quantity": 1, "unitPrice": 15, "category": "promo" }
    ]
  }'

# Get shopper status
curl http://localhost:8080/api/shoppers/shopper-123
```

## Running Tests

```bash
./gradlew test
```

## Project Structure

```
src/main/java/com/looplink/stickerengine/
├── StickerEngineApplication.java    # Spring Boot entry point
├── controller/
│   └── TransactionController.java   # REST endpoints
├── service/
│   ├── TransactionService.java      # Core business logic
│   ├── StickerCalculator.java       # Rule engine orchestrator
│   ├── IdempotencyService.java      # Redis-based idempotency
│   ├── DistributedLockService.java  # Redis-based distributed locking
│   └── rule/                        # Sticker calculation rules
│       ├── StickerRule.java         # Rule interface
│       ├── BaseStickersRule.java    # 1 sticker per $10 spend
│       ├── PromoBonusRule.java      # +1 per promo item
│       └── MaxCapRule.java          # Cap at 5 stickers
├── repository/
│   ├── TransactionRepository.java   # Transaction storage
│   └── ShopperRepository.java       # Shopper balance storage
├── entity/
│   ├── TransactionEntity.java       # Transaction JPA entity
│   ├── TransactionItemEntity.java   # Transaction item JPA entity
│   └── ShopperEntity.java           # Shopper JPA entity
├── model/
│   ├── Item.java                    # Line item in a transaction
│   ├── Transaction.java             # Stored transaction record
│   ├── TransactionRequest.java      # Incoming request DTO
│   ├── TransactionResponse.java     # Response DTO
│   └── ShopperStatus.java           # Shopper lookup response
└── exception/
    └── GlobalExceptionHandler.java  # Error handling
```

## Design Decisions

1. **Rule Engine Pattern**: Sticker calculation uses a pluggable rule engine. Each rule implements `StickerRule` interface and is auto-discovered by Spring. Rules are executed in order based on `getOrder()` priority.

2. **Distributed Locking**: Uses Redis for distributed locks to prevent race conditions when multiple requests for the same shopper arrive concurrently.

3. **Idempotency**: Redis-based idempotency check ensures duplicate `transactionId` submissions return the original result without re-awarding stickers.

4. **Validation**: Uses Jakarta Bean Validation annotations for declarative input validation.

5. **Separation of concerns**: 
   - `StickerCalculator` orchestrates rule execution
   - `StickerRule` implementations handle individual rules (easily testable/extensible)
   - `TransactionService` orchestrates storage, locking, and calculation
   - `TransactionController` handles HTTP concerns

6. **Records**: Uses Java records for immutable DTOs with minimal boilerplate.

## Adding New Sticker Rules

To add a new rule, create a class implementing `StickerRule`:

```java
@Component
public class WeekendBonusRule implements StickerRule {

    @Override
    public int apply(TransactionRequest request, int currentStickers) {
        DayOfWeek day = request.timestamp().atZone(ZoneId.systemDefault()).getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return currentStickers + 1; // +1 bonus on weekends
        }
        return currentStickers;
    }

    @Override
    public int getOrder() {
        return 30; // Runs after promo (20), before cap (100)
    }
}
```

No changes needed to `StickerCalculator` — the new rule is auto-discovered.

## Potential Extensions

- Add sticker redemption endpoint
- Configurable campaign rules (externalize thresholds to config)
- Add store-specific bonus rules
- Rate limiting per shopper
- Admin API for rule management
