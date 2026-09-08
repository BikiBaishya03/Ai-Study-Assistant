# AI Study Assistant — Frontend

React + Vite single-page app providing the Upload, Chat, and History views for AI Study Assistant.

> Part of the [AI Study Assistant](../README.md) monorepo. See the root README for the full-stack architecture.

## Features

- **Authentication** — user login/registration to secure documents and chat history.
- **Upload** — pick a PDF and send it to the backend for ingestion.
- **Chat** — ask questions about uploaded documents and view answers.
- **History** — list previously uploaded documents.

## Prerequisites

- Node.js 18+
- npm

## Setup

```bash
cd react-frontend
npm install
```

### Environment variables

Create `react-frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### Run in development

```bash
npm run dev
```
Runs on http://localhost:5173 by default.

### Build for production

```bash
npm run build
npm run preview   # optional: preview the production build locally
```

### Lint

```bash
npm run lint
```

## Notes

- All API calls go through the Spring Boot backend (`VITE_API_BASE_URL`) — this app never talks to MinIO, Valkey, or the Python AI service directly.
- Auth tokens are implemented via JWT. The token is stored locally (e.g., in localStorage or sessionStorage) and attached as a Bearer token to all outgoing API requests.

## Docker

This service is built and run as part of the root [`docker-compose.yml`](../docker-compose.yml). To build the image standalone:

```bash
docker build -t ai-study-assistant-frontend .
```
