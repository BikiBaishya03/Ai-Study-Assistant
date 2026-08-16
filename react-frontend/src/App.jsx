// src/App.jsx
import React, { useState, useRef, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import ChatWindow from './components/ChatWindow';
import MessageInput from './components/MessageInput';

export default function App() {
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

  // Auto-scroll to the bottom whenever a new message appears
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  // Initial Data Load (Endpoint 2: Get History/Documents)
  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        // Adjust this URL to match your Spring Boot GET documents endpoint
        const response = await fetch('http://localhost:8080/api/v1/study/history'); 
        if (response.ok) {
          const data = await response.json();
          console.log("My Backend Data:", data);
          setDocuments(data); // Assuming data is an array of document objects
        }
      } catch (error) {
        console.error("Could not load initial documents:", error);
      }
    };
    fetchInitialData();
  }, []);

  // Callback when Sidebar successfully uploads a file
  const handleUploadSuccess = (newDoc) => {
    setDocuments(prev => [...prev, newDoc]);
  };

  // Callback when user hits Send (Endpoint 3: Ask Question)
  const handleSendMessage = async (userQuery) => {
    // 1. Add User Message to screen
    const userMsg = { id: Date.now().toString(), sender: 'user', text: userQuery };
    setMessages(prev => [...prev, userMsg]);
    setIsLoading(true);

    try {
      // 2. Call Spring Boot backend
      const response = await fetch('http://localhost:8080/api/v1/study/ask', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question: userQuery }),
      });

      const data = await response.json();

      // 3. Add AI Message to screen
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
  };

  return (
    <div className="flex h-screen bg-[#131314] text-zinc-100 font-sans antialiased overflow-hidden">
      <Sidebar 
        documents={documents} 
        onUploadSuccess={handleUploadSuccess}
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