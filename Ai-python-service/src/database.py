import os
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

# Fetch the Postgres URL from the environment variables
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://ai_user:secure_password@postgres:5432/study_assistant_db")

def init_db():
    print("Connecting to Postgres to initialize vector storage...", flush=True)
    
    try:
        # Connect to the database
        conn = psycopg2.connect(DATABASE_URL)
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        cur = conn.cursor()
        
        # 1. Enable the pgvector extension for AI search
        cur.execute("CREATE EXTENSION IF NOT EXISTS vector;")
        
        # 2. Create the main table for storing PDF chunks and embeddings
        # (Assuming 1536 dimensions for standard OpenAI/common embeddings)
        cur.execute("""
            CREATE TABLE IF NOT EXISTS document_chunks (
                id SERIAL PRIMARY KEY,
                document_name TEXT NOT NULL,
                chunk_text TEXT NOT NULL,
                embedding vector(1536) 
            );
        """)
        
        print("Success: Database initialized with pgvector and tables created!", flush=True)
        
        cur.close()
        conn.close()
        
    except Exception as e:
        print(f"Database initialization failed: {e}", flush=True)

if __name__ == "__main__":
    init_db()