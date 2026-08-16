import os
import json
import redis
import boto3
import socket
from fitz import open as fitz_open  # PyMuPDF

# --- Dynamic Network Resolution ---
# Boto3 rejects underscores in hostnames. We bypass this by resolving the Docker hostname 
# to a raw IP address before handing it to the Boto3 client.
minio_hostname = "rag_minio"
try:
    minio_ip = socket.gethostbyname(minio_hostname)
except socket.gaierror:
    # Fallback just in case the service is named 'minio' in the network
    minio_hostname = "minio"
    minio_ip = socket.gethostbyname(minio_hostname)

MINIO_ENDPOINT = f"http://{minio_ip}:9000"
print(f"Resolved MinIO endpoint to: {MINIO_ENDPOINT}")

# --- Configuration ---
AWS_ACCESS_KEY_ID = "admin"
AWS_SECRET_ACCESS_KEY = "password123"
BUCKET_NAME = os.getenv("MINIO_BUCKET", "pdf-documents")

VALKEY_URL = os.getenv("VALKEY_URL", "redis://localhost:6379/0")
QUEUE_NAME = "pdf_jobs"
TEST_FILE_NAME = "sample_test_document1.pdf"

# 1. Create a minimal PDF file locally
print("\n1. Creating dummy PDF file...")
doc = fitz_open()
page = doc.new_page()
page.insert_text((50, 50), "Hello from the AI Study Assistant! This is page 1 testing RAG chunking.")
page = doc.new_page()
page.insert_text((50, 50), "This is page 2 containing additional text for testing recursive character splitting.")
doc.save(TEST_FILE_NAME)
doc.close()

# 2. Upload the PDF to MinIO
print(f"2. Uploading {TEST_FILE_NAME} to MinIO bucket '{BUCKET_NAME}'...")
s3_client = boto3.client(
    "s3",
    endpoint_url=MINIO_ENDPOINT,
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name="us-east-1"
)

# Ensure bucket exists
try:
    s3_client.head_bucket(Bucket=BUCKET_NAME)
except Exception:
    print(f"   Bucket '{BUCKET_NAME}' not found. Creating it now...")
    s3_client.create_bucket(Bucket=BUCKET_NAME)

s3_client.upload_file(TEST_FILE_NAME, BUCKET_NAME, TEST_FILE_NAME)
print("   File uploaded successfully to MinIO!")

# 3. Push job payload to Valkey queue
print(f"\n3. Pushing job to Valkey queue '{QUEUE_NAME}'...")
valkey_client = redis.from_url(VALKEY_URL)
job_payload = {"file_name": TEST_FILE_NAME}
valkey_client.rpush(QUEUE_NAME, json.dumps(job_payload))

print("\nDone! Check your worker terminal now.")