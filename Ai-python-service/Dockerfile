# ai-python-service/Dockerfile
FROM python:3.11-slim

# Prevent Python from writing pyc files and keep stdout unbuffered
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Install OS-level dependencies for PDF processing and psycopg2 (Postgres)
RUN apt-get update && apt-get install -y \
    build-essential \
    libpq-dev \
    poppler-utils \
    tesseract-ocr \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Install dependencies first to leverage Docker cache
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application code
COPY . .

# Since this is a background queue worker (Valkey listener), we just run the main script.
# (If using FastAPI for synchronous endpoints, you would use uvicorn here instead)
CMD ["python", "src/worker.py"]