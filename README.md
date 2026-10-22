# FullStack Challenge 🚀

This project is a complete FullStack solution designed to manage a client's financial transactions. It features a robust Java Spring Boot backend, a modern React frontend, and a PostgreSQL database, all orchestrated via Docker.

## 🏗️ Architecture & Tech Stack

### Backend
*   **Framework:** Java 21 + Spring Boot 3
*   **Database:** PostgreSQL 15
*   **Data Access:** Spring Data JPA / Hibernate
*   **Validation:** Jakarta Bean Validation (Client constraints, non-negative amounts, past/present dates)
*   **Rate Limiting:** Bucket4j (Implements Token Bucket algorithm to limit requests to 3 per minute per client)
*   **API Documentation:** OpenAPI 3 / Swagger UI

### Frontend
*   **Library:** React 18 + TypeScript (Vite)
*   **Data Fetching & Caching:** Axios + React Query (`@tanstack/react-query`)
*   **Form Management:** React Hook Form
*   **Server:** Nginx (for serving the static build)

### DevOps
*   **Containerization:** Docker & Docker Compose
*   **Build Strategy:** Multi-stage Dockerfiles for optimized, lightweight production images.

## ✨ Key Features

1.  **Transaction Management:** Create, list, and delete transactions.
2.  **Business Rules Enforcement:**
    *   A single client can have a maximum of 100 transactions.
    *   Transaction amounts cannot be negative.
    *   Transaction dates cannot be in the future.
3.  **API Protection:** A global Rate Limiter restricts endpoints to a maximum of 3 requests per minute per client (Returns HTTP `429 Too Many Requests`).
4.  **Global Error Handling:** Intercepts exceptions and returns standardized JSON responses (HTTP 400 for bad requests, HTTP 500 for server errors).
5.  **Optimized Client:** Uses React Query to cache GET requests and automatically invalidate/refresh data upon successful POST/DELETE mutations.

## 🚀 Getting Started

### Prerequisites
*   [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

### Installation & Execution

The entire application (Database, Backend, and Frontend) is fully containerized. To spin up the environment, simply run:

```bash
docker-compose up -d --build