import os
import json
import redis
import time
from processor import process_pdf
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
from database import DATABASE_URL

VALKEY_URL = os.getenv("VALKEY_URL", "redis://localhost:6379/0")
QUEUE_NAME = "pdf-ingestion-queue" # Matches Spring Boot queue name

def main():
    print("Starting AI Python Worker...", flush=True)
    
    # Initial Connection Setup
    while True:
        try:
            client = redis.from_url(VALKEY_URL)
            client.ping()
            print(f"Successfully connected to Valkey at {VALKEY_URL}", flush=True)
            break
        except Exception as e:
            print(f"Waiting for Valkey to start... retrying in 3 seconds. ({e})", flush=True)
            time.sleep(3)

    print(f"Listening for jobs on queue: '{QUEUE_NAME}'...", flush=True)
    
    # Polling Loop
    while True:
        try:
            # THE FIX: Use standard lpop (non-blocking) instead of blpop!
            #print(client.llen("pdf-ingestion-queue"))
            message = client.lpop(QUEUE_NAME)
            
            # If the queue is empty, sleep for 2 seconds and loop again
            if not message:
                time.sleep(2)
                continue
                
            # If we get here, a PDF job was found!
            try:
                job_data = json.loads(message.decode("utf-8"))
            except json.JSONDecodeError:
                print(f"Received invalid JSON payload, discarding: {message}", flush=True)
                continue
            
            # Extract the fields sent by Spring Boot
            object_name = job_data.get("objectName")
            original_filename = job_data.get("originalFileName") or job_data.get("originalFilename") 
            user_email = job_data.get("userEmail")
            doc_id = job_data.get("docId")
            
            if not object_name:
                print("Job missing 'objectName', skipping...", flush=True)
                continue
            
            print(f"\n--- New Job Received: {original_filename} ---", flush=True)
            
            # Pass all fields to the processor
            chunks = process_pdf(object_name, original_filename, user_email)
            
            conn = psycopg2.connect(DATABASE_URL)
            conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
            
            cursor = conn.cursor()
            if chunks:
                
                cursor.execute(
                    "UPDATE user_documents SET status = 'READY', processed_at = NOW() WHERE id = %s",
                    (doc_id,),
                )
                
                print(f"Finished pipeline for {original_filename}.", flush=True)
            else:
                cursor.execute(
                    "UPDATE user_documents SET status = 'FAiled', processed_at = NOW() WHERE id = %s",
                        (doc_id,),
                )
                print(f"Failed pipeline for {original_filename}.", flush=True)
            cursor.close()
            conn.close()
        except KeyboardInterrupt:
            print("\nWorker shutting down.")
            break
        except (redis.exceptions.ConnectionError, redis.exceptions.TimeoutError) as e:
            # Added 'as e' so we can see the exact error if the network drops
            print(f"Connection issue ({e}). Reconnecting...", flush=True)
            time.sleep(2)
            # Force a brand-new connection to Valkey to recover gracefully
            client = redis.from_url(VALKEY_URL)
        except Exception as e:
            print(f"Worker encountered an error: {e}", flush=True)
            time.sleep(2)

if __name__ == "__main__":
    main()