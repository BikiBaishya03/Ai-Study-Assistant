import os
import json
import redis
import time
from processor import process_pdf

VALKEY_URL = os.getenv("VALKEY_URL", "redis://localhost:6379/0")
QUEUE_NAME = "pdf-ingestion-queue" # Matches Spring Boot queue name

def main():
    print("Starting AI Python Worker...", flush=True)
    
    while True:
        try:
            client = redis.from_url(VALKEY_URL, health_check_interval=30)
            client.ping()
            print(f"Successfully connected to Valkey at {VALKEY_URL}", flush=True)
            break
        except Exception as e:
            print(f"Waiting for Valkey to start... retrying in 3 seconds. ({e})", flush=True)
            time.sleep(3)

    print(f"Listening for jobs on queue: '{QUEUE_NAME}'...", flush=True)
    
    while True:
        try:
            result = client.blpop(QUEUE_NAME, timeout=2)
            
            if result:
                _, message = result
                
                try:
                    job_data = json.loads(message.decode("utf-8"))
                except json.JSONDecodeError:
                    print(f"Received invalid JSON payload, discarding: {message}", flush=True)
                    continue
                
                # Extract the fields sent by Spring Boot
                object_name = job_data.get("objectName")
                original_filename = job_data.get("originalFileName")
                
                if not object_name:
                    print("Job missing 'objectName', skipping...", flush=True)
                    continue
                
                print(f"\n--- New Job Received: {original_filename} ---", flush=True)
                
                # Pass both the MinIO name and the clean name to the processor
                chunks = process_pdf(object_name, original_filename)
                
                if chunks:
                    print(f"Finished pipeline for {original_filename}.", flush=True)
                else:
                    print(f"Failed pipeline for {original_filename}.", flush=True)
                    
        except KeyboardInterrupt:
            print("\nWorker shutting down.")
            break
        except (redis.exceptions.ConnectionError, redis.exceptions.TimeoutError):
            print("Connection to Valkey temporarily lost. Reconnecting...", flush=True)
            time.sleep(2)
        except Exception as e:
            print(f"Worker encountered an error: {e}", flush=True)
            time.sleep(2)

if __name__ == "__main__":
    main()