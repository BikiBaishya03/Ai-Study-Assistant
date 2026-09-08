import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv
load_dotenv()
from langchain_google_genai import ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings
from langchain_postgres import PGVector
from langchain.chains import create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain_core.prompts import ChatPromptTemplate
import traceback  


from src.database import DATABASE_URL



app = FastAPI(title="AI Study Assistant Retrieval API")

# 1. Initialize Embeddings & Vector Store
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


# 2. Initialize the Gemini LLM for answering
# Using gemini-2.5-flash for fast, efficient text generation
llm = ChatGoogleGenerativeAI(
    model="gemini-3.5-flash-lite",
    google_api_key=os.getenv("GEMINI_API_KEY")
)

# 3. Create the Prompt Template
system_prompt = (
    "You are a helpful AI Study Assistant. Use the following pieces of retrieved context "
    "to answer the user's question. If you don't know the answer based on the context, "
    "just say that you don't know. Keep the answer concise and educational.\n\n"
    "Context: {context}"
)
prompt = ChatPromptTemplate.from_messages([
    ("system", system_prompt),
    ("human", "{input}"),
])

# 4. Build the Retrieval Chain
question_answer_chain = create_stuff_documents_chain(llm, prompt)


# --- API ENDPOINTS ---

class QueryRequest(BaseModel):
    question: str
    userEmail: str

@app.get("/")
async def root():
    return {"message": "RAG Microservice is up and running!"}

@app.post("/ask")
def ask_question(request: QueryRequest):
    try:
        
        # Change search_type to threshold, and add a score_threshold
        retriever = vectorstore.as_retriever(
            search_type="similarity_score_threshold",
            search_kwargs={
                "k": 5, 
                "score_threshold": 0.50,
                "filter": {"user_email": request.userEmail}
            }
        ) # Get top 3 chunks
        
        rag_chain = create_retrieval_chain(retriever, question_answer_chain)
        
        response = rag_chain.invoke({"input": request.question})
        
        # 2. Extract and format sources nicely (e.g., "biki_resume.pdf (Page 1)")
        raw_sources = []
        for doc in response["context"]:
            filename = doc.metadata.get("source", "Unknown")
            page = doc.metadata.get("page", "Unknown")
            raw_sources.append(f"{filename} (Page {page})")
            
        # 3. Use list(set()) to remove any duplicate citations
        unique_sources = list(set(raw_sources))
        
        return {
            "question": request.question,
            "answer": response["answer"],
            "sources": unique_sources
        }
    except Exception as e:
               # <-- 1. Add this import
        traceback.print_exc()     # <-- 2. Add this line to print the error
        raise HTTPException(status_code=500, detail=str(e))