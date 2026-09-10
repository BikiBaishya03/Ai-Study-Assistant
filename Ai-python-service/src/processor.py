import os

import fitz  # PyMuPDF
from dotenv import load_dotenv
from langchain_core.documents import Document
from langchain_google_genai import GoogleGenerativeAIEmbeddings
from langchain_postgres import PGVector

from database import DATABASE_URL
from storage import BUCKET_NAME, get_s3_client

load_dotenv()


def process_pdf(object_name, original_filename, user_email):
    print(f"Starting processing for: {original_filename}")
    
    s3 = get_s3_client()
    local_path = f"/tmp/{object_name}"
    
    try:
        # Step 1: Download using the UUID object_name
        print(f"Downloading {object_name} from MinIO...")
        s3.download_file(BUCKET_NAME, object_name, local_path)
        
        # Step 2: Extract and Chunk Text using PyMuPDF
        print(f"Extracting text from {original_filename}...")
        doc = fitz.open(local_path)
        chunks = []
        
        for page_num in range(len(doc)):
            page = doc.load_page(page_num)
            text = page.get_text()
            if text.strip():
                # Store the CLEAN original_filename in metadata, not the UUID
                chunks.append(Document(
                    page_content=text, 
                    metadata={  
                            "source": original_filename,
                            "page": page_num + 1,
                            "user_email":user_email
                        }
                ))
                
        doc.close()
        
        if os.path.exists(local_path):
            os.remove(local_path)
            
        # Step 3: Embed and Store in Postgres
        if chunks:
            print(f"Extracted {len(chunks)} chunks. Generating Gemini embeddings...")
            
            # Note: Changed to the standard Google GenAI embedding model name
            embeddings = GoogleGenerativeAIEmbeddings(
                model="models/gemini-embedding-2",
                google_api_key=os.getenv("GEMINI_API_KEY")
            )
            
            vectorstore = PGVector(
                embeddings=embeddings,
                collection_name="pdf_documents",
                connection=DATABASE_URL,
                use_jsonb=True,
            )
            
            print("Storing vectors in Postgres database...")
            vectorstore.add_documents(chunks)
            
            print(f"Success! {len(chunks)} chunks successfully vectorized and saved.")
            return chunks
        else:
            print(f"No text could be extracted from {original_filename}.")
            return None
            
    except Exception as e:
        print(f"Error processing {original_filename}: {e}")
        return None

if __name__ == "__main__":
    print("Processor module ready to be integrated.", flush=True)