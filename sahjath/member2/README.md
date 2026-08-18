# Member 2 – Order and Payment Services

## Ownership

Member 2 owns the following microservices:

1. **Order Service** – creates and manages customer orders and order items.
2. **Payment Service** – validates order totals, records payments, and marks successfully paid orders as `PAID`.

## Main implementation responsibilities

- RESTful CRUD/domain endpoints
- PostgreSQL database per service
- Request validation and consistent JSON error responses
- Independent `X-API-KEY` verification in both services
- Swagger/OpenAPI documentation
- Dockerfiles and Docker Compose integration
- Payment-to-Order inter-service communication
- Gateway route and frontend integration

## Important design choice

The Payment Service never stores a real card number, PIN, or CVV. It stores only the payment method and an internally generated transaction reference. The payment result is simulated for this coursework demonstration.

## Request flow

```text
Frontend -> Gateway (JWT) -> Order Service (Order API key)
Frontend -> Gateway (JWT) -> Payment Service (Payment API key)
Payment Service -> Order Service (Order API key) -> status PAID
```

