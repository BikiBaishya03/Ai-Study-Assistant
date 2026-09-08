import os
import boto3
import socket

# 1. Bypass Docker underscore rules for Boto3
minio_hostname = "minio" 
try:
    minio_ip = socket.gethostbyname(minio_hostname)
except socket.gaierror:
    minio_hostname = "minio"
    minio_ip = socket.gethostbyname(minio_hostname)

MINIO_ENDPOINT = f"http://{minio_ip}:9000"

# 2. Hardcode your exact credentials here to prevent mismatches
AWS_ACCESS_KEY_ID = "admin"
AWS_SECRET_ACCESS_KEY = "password123"
BUCKET_NAME = os.getenv("MINIO_BUCKET", "study-materials")

def get_s3_client():
    return boto3.client(
        "s3",
        endpoint_url=MINIO_ENDPOINT,
        aws_access_key_id=AWS_ACCESS_KEY_ID,
        aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
        region_name="us-east-1"
    )

# ... inside your process_pdf function, make sure you use get_s3_client() to download the file

def init_storage():
    """Ensure the default bucket for user PDFs exists."""
    print("Connecting to MinIO Object Storage...", flush=True)
    s3 = get_s3_client()
    try:
        response = s3.list_buckets()
        existing_buckets = [b['Name'] for b in response.get('Buckets', [])]
        
        if BUCKET_NAME not in existing_buckets:
            s3.create_bucket(Bucket=BUCKET_NAME)
            print(f"Bucket '{BUCKET_NAME}' created successfully!", flush=True)
        else:
            print(f"Bucket '{BUCKET_NAME}' already exists.", flush=True)
            
    except Exception as e:
        print(f"MinIO initialization failed: {e}", flush=True)

def download_file(object_name: str, download_path: str):
    """Download a PDF file from MinIO to a temporary local path inside the worker."""
    s3 = get_s3_client()
    s3.download_file(BUCKET_NAME, object_name, download_path)
    print(f"Downloaded {object_name} to {download_path}", flush=True)

if __name__ == "__main__":
    init_storage()