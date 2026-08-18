# Online Book Microservice – Member 2

This is a ready-to-run Spring Boot microservices bundle for the Service-Oriented Computing group assignment. It contains Member 2's **Order Service** and **Payment Service**, plus a secured API Gateway, Keycloak, Redis, PostgreSQL databases, and a small frontend for demonstration.

## Included components

| Component | Port | Purpose |
|---|---:|---|
| Frontend | 3000 | Login and demonstrate order/payment flow |
| API Gateway | 8080 | OAuth2/JWT, CORS, rate limiting and routing |
| Keycloak | 8180 | OAuth2 token provider |
| Order Service | 8083 | Order CRUD API |
| Payment Service | 8084 | Payment API and order status update |
| Order PostgreSQL | 5435 | Order database |
| Payment PostgreSQL | 5436 | Payment database |
| Redis | 6379 | Gateway rate-limit counters |

## Project structure

```text
online-book-microservice/
├── member1/
│   └── README.md
├── member2/
│   ├── order-service/
│   └── payment-service/
├── api-gateway/
├── frontend/
├── keycloak/
├── .env                 # Private; ignored by Git
├── .env.example         # Safe placeholders for GitHub
├── docker-compose.yml
└── README.md
```

The `member1` directory is intentionally not populated because Book Service and User Service are owned by Member 1. Copy their existing services into that directory when merging the group repository.

## Prerequisites

- Docker Desktop with Docker Compose
- Ports `3000`, `8080`, `8083`, `8084`, `8180`, `5435`, `5436`, and `6379` must be free

## Run everything

The downloaded ZIP already contains a private `.env` file with generated local secrets. Do not commit that file. If it is missing, create it from the safe template and replace every placeholder:

```powershell
Copy-Item .env.example .env
notepad .env
```

Then run from this project directory:

```powershell
docker compose up -d --build
docker compose ps
```

First startup can take a few minutes because Maven dependencies and Docker images must be downloaded.

Open the frontend:

```text
http://localhost:3000
```

Demo login:

```text
Username: demo
Password: demo123
```

## Security flow

1. The frontend obtains a JWT access token from Keycloak.
2. The frontend sends `Authorization: Bearer <token>` to the API Gateway.
3. The Gateway validates the token and applies CORS and Redis rate limiting.
4. The Gateway removes any API key supplied by the browser and injects the correct private service key.
5. Each service independently rejects direct calls that do not contain its correct API key.

API keys and infrastructure passwords are stored only in the local `.env` file. The real values are not present in `docker-compose.yml`, application source files, `.env.example`, or this README. `.gitignore` prevents `.env` from being committed.

Before pushing to GitHub, confirm this command does not show `.env`:

```powershell
git status
```

For lecturer testing, provide the local test API keys privately rather than posting them in a public repository. The API-key header format is:

```http
X-API-KEY: <private-service-key>
```

## API endpoints

### Order Service

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/orders` | Create an order |
| GET | `/api/orders` | List orders |
| GET | `/api/orders/{id}` | Find an order |
| GET | `/api/orders/user/{userId}` | List a user's orders |
| PATCH | `/api/orders/{id}/status` | Change order status |
| DELETE | `/api/orders/{id}` | Delete a pending order |

### Payment Service

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/payments` | Create a pending payment |
| GET | `/api/payments` | List payments |
| GET | `/api/payments/{id}` | Find a payment |
| GET | `/api/payments/order/{orderId}` | List payments for an order |
| PATCH | `/api/payments/{id}/status` | Complete or fail payment |

## Swagger UI

- Order Service: http://localhost:8083/swagger-ui/index.html
- Payment Service: http://localhost:8084/swagger-ui/index.html

Click **Authorize** and enter the relevant service API key before testing endpoints directly in Swagger.

## Security tests

Direct call without a service key must return `401`:

```powershell
curl.exe -i http://localhost:8083/api/orders
```

Direct call with the service key must return `200`:

```powershell
$line = Get-Content .env | Where-Object { $_ -like "ORDER_SERVICE_API_KEY=*" }
$orderKey = ($line -split "=", 2)[1]
curl.exe -i -H "X-API-KEY: $orderKey" http://localhost:8083/api/orders
```

Gateway call without a JWT must also return `401`:

```powershell
curl.exe -i http://localhost:8080/api/orders
```

## Member 1 integration

Copy Member 1's existing services to:

```text
member1/book-service
member1/user-service
```

Then merge their existing Docker Compose service definitions and Gateway routes with this project. Keep Member 1's service API keys in the same private `.env` pattern.

## Suggested Member 2 commits

```text
Create order service CRUD and PostgreSQL persistence
Add API key security and Swagger to order service
Create payment service and order integration
Containerize Member 2 services
Register order and payment routes in API Gateway
Add Member 2 frontend demonstration flow
```

## Stop the system

```powershell
docker compose down
```

To also remove the project databases:

```powershell
docker compose down -v
```
