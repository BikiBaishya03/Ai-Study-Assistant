 import React from 'react';
import { Sparkles, Loader2, PanelLeft } from 'lucide-react';
import MessageBubble from './MessageBubble';

export default function ChatWindow({ 
  messages, 
  isLoading, 
  chatEndRef, 
  sidebarOpen, 
  setSidebarOpen 
}) {
  return (
    <div className="flex-1 flex flex-col h-full relative bg-[#131314]">
      {/* Top Navigation Bar */}
      <header className="h-14 border-b border-zinc-800/60 flex items-center px-4 bg-[#131314]/80 backdrop-blur sticky top-0 z-10">
        <div className="flex items-center gap-3">
          <button 
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-2 hover:bg-zinc-800/60 rounded-lg text-zinc-400 hover:text-zinc-100 transition-colors"
          >
            <PanelLeft className="w-5 h-5" />
          </button>
          <span className="text-sm font-medium text-zinc-300">
            AI Study Assistant Workspace
          </span>
        </div>
      </header>

      {/* Messages Thread */}
      <div className="flex-1 overflow-y-auto px-4 py-6 md:px-0">
        <div className="max-w-3xl mx-auto space-y-6">
          
          {/* Map through all messages */}
          {messages.map((msg) => (
            <MessageBubble key={msg.id} message={msg} />
          ))}

          {/* AI Loading State Indicator */}
          {isLoading && (
            <div className="flex gap-4 items-start justify-start">
              <div className="w-8 h-8 rounded-lg bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-blue-400 shrink-0 mt-1">
                <Sparkles className="w-4 h-4 animate-spin" />
              </div>
              <div className="bg-[#1e1f20] border border-zinc-800/80 px-4 py-3 rounded-2xl rounded-bl-none text-sm text-zinc-400 flex items-center gap-2 shadow-sm">
                <Loader2 className="w-4 h-4 animate-spin text-blue-400" />
                <span>Searching document vectors & generating answer...</span>
              </div>
            </div>
          )}
          
          {/* Invisible element to auto-scroll to */}
          <div ref={chatEndRef} />
        </div>
      </div>
    </div>
  );
}