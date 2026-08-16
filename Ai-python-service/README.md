# AI Study Assistant — AI Service

Python service responsible for PDF ingestion (chunking + embedding) and RAG-based chat answers.

> Part of the [AI Study Assistant](../README.md) monorepo. See the root README for the full-stack architecture.

Runs as **two processes** from the same codebase:
- **API** (`src/api.py`) — FastAPI app exposing the `/chat` endpoint, called synchronously by the Spring Boot backend.
- **Worker** (`src/worker.py`) — continuously polls the Valkey queue for new ingestion jobs.

## Source Layout

| File | Responsibility |
|---|---|
| `src/api.py` | FastAPI app — chat endpoint (similarity search + Gemini call) |
| `src/worker.py` | Polls Valkey for new jobs, triggers ingestion |
| `src/processor.py` | Chunks PDF text and generates embeddings |
| `src/storage.py` | MinIO client wrapper (download/upload PDFs) |
| `src/database.py` | PostgreSQL/pgvector connection and queries |
| `src/upload_and_test.py` | Manual script for testing upload/ingestion locally |

## Responsibilities

- **Ingestion:** worker pulls a job → downloads the PDF from MinIO → `processor.py` chunks the text and embeds each chunk via **Gemini Embedding-2** → chunks + vectors + uploader's email are stored in pgvector.
- **Chat:** API embeds the incoming question → similarity search in pgvector filtered by user email → top-k chunks sent as context to **Gemini 3.5 Flash** → answer returned to the backend.

## Prerequisites

- Python 3.11+
- A running PostgreSQL instance with the `pgvector` extension enabled
- A Google Gemini API key

## Setup

```bash
cd Ai-python-service
python -m venv .venv
source .venv/bin/activate   # .venv\Scripts\activate on Windows
pip install -r requirements.txt
```

### Environment variables

Create `Ai-python-service/.env`:

```env
# Object storage
MINIO_ENDPOINT=minio:9000
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET=study-documents

# Queue
VALKEY_HOST=valkey
VALKEY_PORT=6379
VALKEY_QUEUE_NAME=pdf_processing_queue

# Vector DB
DATABASE_URL=postgresql://postgres:postgres@postgres:5432/studyassistant

# Gemini
GEMINI_API_KEY=your-gemini-api-key
GEMINI_CHAT_MODEL=gemini-3.5-flash
GEMINI_EMBEDDING_MODEL=embedding-2

# Chunking
CHUNK_SIZE=1000
CHUNK_OVERLAP=200
TOP_K=5
```

> ⚠️ Names above are inferred from the described flow — check `database.py`, `storage.py`, and `worker.py` for the real variable names and update accordingly.

### Run the API

```bash
uvicorn src.api:app --reload --port 8000
```

### Run the worker

```bash
python src/worker.py
```

Both need to be running for the ingestion → chat pipeline to work end-to-end.

## API Reference

> Paths below reflect the described flow — replace with your actual FastAPI route decorators in `api.py`.

| Method | Endpoint | Called by | Description |
|---|---|---|---|
| POST | `/chat` | Spring Boot backend | Similarity search over the user's chunks + Gemini-generated answer |
| GET | `/health` | — | Health check |

## Data Model (pgvector)

Example shape of the chunks table — update to match `database.py`:

| Column | Type | Notes |
|---|---|---|
| `id` | uuid / serial | Primary key |
| `user_email` | text | Scopes similarity search per user |
| `document_name` | text | Source PDF reference (MinIO object key) |
| `chunk_text` | text | Raw chunk content |
| `embedding` | vector | pgvector column, dimension matches `GEMINI_EMBEDDING_MODEL` |
| `created_at` | timestamp | |

## Docker

This service is built and run as part of the root [`docker-compose.yml`](../docker-compose.yml). The included `Dockerfile` builds a single image — the API and worker are typically run as separate containers/services from that same image, each with a different start command.

## Dev Container (VS Code)

This service includes a `.devcontainer/` config for [VS Code Dev Containers](https://code.visualstudio.com/docs/devcontainers/containers), giving it a ready-to-go containerized editing environment (correct Python version, dependencies, etc.) independent of `docker-compose.yml`. This is specific to the Python AI service — the frontend and backend don't currently have their own dev container configs.

