import pytest

from fastapi.testclient import TestClient
from unittest.mock import MagicMock, patch

from src.api import app

client = TestClient(app)

@patch("src.api.generate_answer")
@patch("srcx.api.search_pgvector")
def test_ask_endpoint_success(mock_search, mock_generate):
    mock_search.return_value = ["Mocked chunk about AI from pgvector"]
    
    mock_generate.return_value = "This is the generated answer based in the pg vector content"
    
    response = client.post("/ask", json={"query": "Explain the architecture."})
    
    assert response.status_code == 200
    assert "answer" in response.json()
    mock_search.asert_called_once()
    mock_generate.assert_called_once()
    

def test_ask_endpoint_missing_query():
    response = client.post("/ask", json={})
    assert response.status_code == 422
    

@patch("src.worker.send_to_processor")
@patch("src.worker.valkey_client.lpop")
def test_worker_pops_and_processes_job(mock_lpop, mock_send_to_processor):
    mock_lpop.return_value = b'{"file_name": "sample_test_document1.pdf"}'
    
    from src.worker import process_queue_once
    process_queue_once()
    
    mock_send_to_processor.assert_called_once_with("sample_test_document1.pdf")

@patch("src.worker.send_to_processor")
@patch("src.worker.valkey_client.lpop")
def test_worker_skips_empty_queue(mock_lpop, mock_send_to_processor):
    mock_lpop.return_value = None
    
    from src.worker import process_queue_once
    process_queue_once()
    
    mock_send_to_processor.assert_not_called()

@patch("src.processor.store_in_pgvector")
@patch("src.processor.chunk_document")
@patch("src.processor.download_from_minio")
def test_processor_pipeline(mock_chunk, mock_download, mock_store):
    
    mock_chunk.return_value = ["chunk_1", "chunk_2"]
    mock_download.return_value = "raw_pdf_text_content"
    
    from src.processor import process_file
    process_file("sample_test_document1.pdf")
    
    mock_download.assert_called_once_with("sample_test_document1.pdf")
    mock_chunk.assert_called_once_with("raw_pdf_text_content")
    mock_store.assert_called_once_with(["chunk_1", "chunk_2"])