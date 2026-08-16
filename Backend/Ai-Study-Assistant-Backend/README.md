# AI Study Assistant — Backend

Spring Boot service that authenticates users, handles PDF uploads, and routes chat requests.

> Part of the [AI Study Assistant](../../README.md) monorepo. See the root README for the full-stack architecture.

## Responsibilities

- Authenticate/authorize incoming requests.
- On upload: store the raw PDF in **MinIO**, then push a processing job (PDF reference + user email) onto the **Valkey** queue.
- On chat: forward the request directly to the Python **FastAPI** chat endpoint (synchronous, no queueing).
- On history: return the list of documents previously uploaded by the current user.

## Prerequisites

- Java 17+
- Maven (or use the bundled `mvnw`)

## Setup

```bash
cd Backend/Ai-Study-Assistant-Backend
```

### Environment variables

Set these in `application.properties` / `application.yml`, or as environment variables (e.g. via `docker-compose.yml`):

```env
SERVER_PORT=8080
JWT_SECRET=your-jwt-secret

MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET=study-documents

VALKEY_HOST=valkey
VALKEY_PORT=6379
VALKEY_QUEUE_NAME=pdf_processing_queue

AI_SERVICE_URL=http://ai-service:8000
```

> ⚠️ Names above are inferred from the described flow — check your actual `application.properties`/`.yml` and `@ConfigurationProperties` classes and update accordingly. Spring typically uses dotted property keys (e.g. `spring.datasource.url`) rather than plain env-var names, so this block may need reformatting to match.

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
| POST | `/api/auth/login` | — | Authenticate a user |
| POST | `/api/documents/upload` | ✅ | Upload a PDF; stores it in MinIO and enqueues a processing job |
| GET | `/api/documents/history` | ✅ | List the current user's uploaded documents |
| POST | `/api/chat` | ✅ | Ask a question; routed to the Python `/chat` endpoint |

## Docker

This service is built and run as part of the root [`docker-compose.yml`](../../docker-compose.yml).
