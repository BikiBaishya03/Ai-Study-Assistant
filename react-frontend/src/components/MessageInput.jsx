import  React, { useState } from 'react';
import { Send } from 'lucide-react';

export default function MessageInput({ onSendMessage, isLoading }) {
  const [input, setInput] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;
    
    onSendMessage(input.trim()); // Send data to parent
    setInput('');                // Clear the input box
  };

  const handleKeyDown = (e) => {
    // If user presses Enter without Shift, send the message
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <div className="p-4 bg-[#131314] border-t border-zinc-800/40">
      <form 
        onSubmit={handleSubmit} 
        className="max-w-3xl mx-auto relative flex items-center bg-[#1e1f20] border border-zinc-700/50 focus-within:border-blue-500/60 rounded-2xl p-1.5 transition-all shadow-lg shadow-black/20"
      >
        <textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask anything about your study materials..."
          rows={1}
          className="flex-1 bg-transparent border-0 resize-none px-3 py-2 text-sm text-zinc-100 placeholder-zinc-500 focus:outline-none focus:ring-0 max-h-32"
        />
        <button
          type="submit"
          disabled={!input.trim() || isLoading}
          className="p-2.5 bg-blue-600 hover:bg-blue-500 disabled:bg-zinc-800 disabled:text-zinc-600 text-white rounded-xl transition-all shrink-0 ml-2"
        >
          <Send className="w-4 h-4" />
        </button>
      </form>
      <p className="text-[11px] text-center text-zinc-600 mt-2">
        Answers are grounded strictly in your uploaded PDF vector embeddings.
      </p>
    </div>
  );
}