# Idempotent Payment API

An enterprise-grade REST API built with Spring Boot that processes payments while strictly preventing duplicate transactions. The architecture implements a distributed gatekeeper pattern using an in-memory Redis cache to protect the primary PostgreSQL database from race conditions and double-charging.

## Architecture & Data Flow

To ensure data integrity, this system utilizes an **Idempotency Key** passed via HTTP headers.

1. **Request Interception:** The client submits a payment request with a unique `Idempotency-Key`.
2. **The Redis Gatekeeper:** The application attempts to write the key to Redis using an atomic `SETNX` (Set if Not Exists) operation. 
3. **RAM-Level Validation:** 
    * If the key exists: Redis instantly rejects the request, returning a `409 Conflict`. The database is never queried, saving CPU cycles and network overhead.
    * If the key is new: The transaction proceeds.
4. **Persistent Storage:** The transaction is safely written to the PostgreSQL database, returning a `200 OK`. 

## Tech Stack
* **Language:** Java 17
* **Framework:** Spring Boot 3
* **Database:** PostgreSQL
* **Cache:** Redis / Memurai
* **ORM:** Spring Data JPA / Hibernate

## Local Setup

**Prerequisites:**
* PostgreSQL running locally on port `5432`
* Redis (or Memurai) running locally on port `6379`

**1. Clone the repository**
```bash
git clone [https://github.com/yourusername/idempotent-payment-api.git](https://github.com/yourusername/idempotent-payment-api.git)
cd idempotent-payment-api
**2. Configure Environment Variables**
To keep database credentials secure, this application uses environment variables. Set the following variable in your IDE run configuration or OS before executing:

DB_PASSWORD = Your PostgreSQL password

**3. Run the Application**
./mvnw spring-boot:run

API Endpoints
1. Process a Payment
URL: /api/postPayment

Method: POST

Headers: Idempotency-Key: [Unique-String]

Body:(example)
{
  "userId": "asApplicable",
  "amount": 50.00
}
Responses:

200 OK: Payment processed and saved.

409 Conflict: "Error: Payment already processed for this key."

2. Retrieve a Payment
URL: /api/getPayment/{id}

Method: GET

Responses:

200 OK: Returns the payment JSON object.

404 Not Found: "Error: Payment not found."
