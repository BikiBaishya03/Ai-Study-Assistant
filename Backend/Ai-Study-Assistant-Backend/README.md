# AI Study Assistant — Backend

Spring Boot service that authenticates users, handles PDF uploads, and routes chat requests.

> Part of the [AI Study Assistant](../../README.md) monorepo. See the root README for the full-stack architecture.

## Responsibilities

- Authenticate/authorize incoming requests.
- On upload: store the raw PDF in **MinIO**, then push a processing job (PDF reference + user email) onto the **Valkey** queue.
- On chat: forward the request directly to the Python **FastAPI** ask endpoint (synchronous, no queueing).
- On history: return the list of documents previously uploaded by the current user.

## Prerequisites

- Java 17+
- Maven (or use the bundled `mvnw`)
- A running PostgreSQL instance

## Setup

```bash
cd Backend/Ai-Study-Assistant-Backend
```

### Environment variables

Set these in `application.properties` / `application.yml`, or as environment variables (e.g. via `docker-compose.yml`):

```env
SERVER_PORT=8080
JWT_SECRET=your-jwt-secret

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/studyassistant
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-db-password
SPRING_JPA_HIBERNATE_DDL_AUTO=update

#Object Storage
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET=study-materials

#Queue
VALKEY_HOST=valkey
VALKEY_PORT=6379
VALKEY_QUEUE_NAME=pdf_ingestion_queue

#Ai Service
AI_SERVICE_URL=http://ai-service:8000
```

### Run in development

```bash
./mvnw spring-boot:run
```
Runs on http://localhost:8080 by default.

### Build

```bash
./mvnw clean package
java -jar target/*.jar
```

## API Reference

> Paths below reflect the described flow — replace with your actual `@RestController` mappings.

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/study/login` | — | Authenticate a user |
| POST | `/api/v1/study/upload` | ✅ | Upload a PDF; stores it in MinIO and enqueues a processing job |
| GET | `api/v1/study/documents` | ✅ | List the current user's uploaded documents |
| POST | `api/v1/study/ask` | ✅ | Ask a question; routed to the Python `/ask` endpoint |
| GET | `api/v1/study/history` | ✅ | List the current user's chat history |


## Data Model (PostgreSQL)

**Users Table** (Handles authentication)

| Column | Type | Constraints |
|---|---|---|
| `id` | bigserial | Primary key, Auto-increment |
| `email` | varchar / text | Unique, Not Null |
| `password` | varchar / text | Not Null (Hashed) |

## Docker

This service is built and run as part of the root [`docker-compose.yml`](../../docker-compose.yml).
