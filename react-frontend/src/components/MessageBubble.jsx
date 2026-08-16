import React from 'react';
import { User, Sparkles, FileText } from 'lucide-react';

export default function MessageBubble({ message }) {
  const isUser = message.sender === 'user';

  return (
    <div className={`flex gap-4 ${isUser ? 'justify-end' : 'justify-start'}`}>
      
      {/* AI Avatar */}
      {!isUser && (
        <div className="w-8 h-8 rounded-lg bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-blue-400 shrink-0 mt-1">
          <Sparkles className="w-4 h-4" />
        </div>
      )}

      {/* Message Content Area */}
      <div className={`flex flex-col max-w-[85%] ${isUser ? 'items-end' : 'items-start'}`}>
        
        {/* The Text Bubble */}
        <div 
          className={`px-4 py-3 rounded-2xl leading-relaxed text-sm ${
            isUser
              ? 'bg-blue-600 text-white rounded-br-none shadow-sm'
              : 'bg-[#1e1f20] border border-zinc-800/80 text-zinc-200 rounded-bl-none shadow-sm'
          }`}
        >
          {message.text}
        </div>

        {/* Source Citations (Only rendered if it's the AI and sources exist) */}
        {!isUser && message.sources && message.sources.length > 0 && (
          <div className="mt-2 flex flex-wrap gap-1.5 items-center">
            <span className="text-[11px] font-medium text-zinc-500 uppercase tracking-wider mr-1">
              Sources:
            </span>
            {message.sources.map((src, idx) => (
              <span 
                key={idx}
                className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-zinc-800/80 border border-zinc-700/50 text-[11px] font-mono text-zinc-300 hover:border-blue-500/50 transition-colors"
              >
                <FileText className="w-3 h-3 text-blue-400" />
                {src}
              </span>
            ))}
          </div>
        )}
      </div>

      {/* User Avatar */}
      {isUser && (
        <div className="w-8 h-8 rounded-lg bg-zinc-800 border border-zinc-700 flex items-center justify-center text-zinc-300 shrink-0 mt-1">
          <User className="w-4 h-4" />
        </div>
      )}
    </div>
  );
}