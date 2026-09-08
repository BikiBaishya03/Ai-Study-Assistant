// src/Dashboard.jsx
import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import ChatWindow from './components/ChatWindow';
import MessageInput from './components/MessageInput';

export default function Dashboard() {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [documents, setDocuments] = useState([]);
  const [messages, setMessages] = useState([
    {
      id: 'welcome-msg',
      sender: 'assistant',
      text: "Hello! I am your AI Study Assistant. Upload your PDFs on the left, and ask me anything. I'll search your materials and provide citations.",
      sources: []
    }
  ]);
  const [isLoading, setIsLoading] = useState(false);

  const chatEndRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  // Initial Data Load
  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        const token = localStorage.getItem('jwt_token');

        const response = await fetch(
          'http://localhost:8080/api/v1/study/documents',
          {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          }
        );

        if (response.status === 401 || response.status === 403) {
          localStorage.removeItem('jwt_token');
          navigate('/login');
          return;
        }

        if (response.ok) {
          const data = await response.json();
          setDocuments(data);
        }
      } catch (error) {
        console.error('Could not load initial documents:', error);
      }
    };

    fetchInitialData();
  }, [navigate]);

  const handleUploadSuccess = (newDoc) => {
    setDocuments(prev => [...prev, newDoc]);
  };

  const handleSendMessage = async (userQuery) => {
    const userMsg = {
      id: Date.now().toString(),
      sender: 'user',
      text: userQuery
    };

    setMessages(prev => [...prev, userMsg]);
    setIsLoading(true);

    try {
      const token = localStorage.getItem('jwt_token');

      const response = await fetch(
        'http://localhost:8080/api/v1/study/ask',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({
            question: userQuery
          })
        }
      );

      // Handle expired/invalid token
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('jwt_token');
        navigate('/login');
        return;
      }

      const data = await response.json();

      const aiMsg = {
        id: (Date.now() + 1).toString(),
        sender: 'assistant',
        text: data.answer || "I couldn't generate an answer.",
        sources: data.sources || []
      };

      setMessages(prev => [...prev, aiMsg]);

    } catch (err) {
      console.error('Query Error:', err);

      setMessages(prev => [
        ...prev,
        {
          id: (Date.now() + 1).toString(),
          sender: 'assistant',
          text: 'Error contacting the backend. Is Spring Boot running?',
          sources: []
        }
      ]);
    } finally {
      setIsLoading(false);
    }
  }; // <-- THIS WAS MISSING

  const handleDeleteDocument = async (documentId) => {
    // 1. ADD THIS SAFETY CHECK
    if (!documentId) {
      console.error("Cannot delete: Document ID is undefined!");
      return; 
    }
    try {
      const token = localStorage.getItem('jwt_token');

      const response = await fetch(
        `http://localhost:8080/api/v1/study/document/${documentId}`,
        {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      if (response.ok) {
        // Remove deleted document from React state
        setDocuments(prev =>
          prev.filter(doc => doc.Id !== documentId)
        );
      } else if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('jwt_token');
        navigate('/login');
      } else {
        console.error('Failed to delete document on the server.');
      }

    } catch (error) {
      console.error('Error deleting document:', error);
    }
  };
  console.log(documents)
  return (
    <div className="flex h-screen bg-[#131314] text-zinc-100 font-sans antialiased overflow-hidden">

      <Sidebar
        documents={documents}
        onUploadSuccess={handleUploadSuccess}
        onDeleteDocument={handleDeleteDocument}
        isOpen={sidebarOpen}
      />

      <div className="flex-1 flex flex-col">

        <ChatWindow
          messages={messages}
          isLoading={isLoading}
          chatEndRef={chatEndRef}
          sidebarOpen={sidebarOpen}
          setSidebarOpen={setSidebarOpen}
        />

        <MessageInput
          onSendMessage={handleSendMessage}
          isLoading={isLoading}
        />

      </div>
    </div>
  );
}