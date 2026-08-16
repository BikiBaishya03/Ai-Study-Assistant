// src/components/Sidebar.jsx
import  React, { useRef, useState } from 'react';
import { Plus, FileText, Sparkles, Loader2 } from 'lucide-react';

export default function Sidebar({ 
  documents, 
  onUploadSuccess, 
  isOpen, 
}) {
  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setIsUploading(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      // Endpoint 1: Upload PDF
      const response = await fetch('http://localhost:8080/api/v1/study/upload', {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        const uploadedDoc = await response.json(); // Assuming Spring returns doc details
        onUploadSuccess(uploadedDoc);
      } else {
        console.error('Upload failed with status:', response.status);
      }
    } catch (err) {
      console.error('Network error uploading file:', err);
    } finally {
      setIsUploading(false);
      e.target.value = null; // Reset file input
    }
  };

  return (
    <aside 
      className={`${
        isOpen ? 'w-72' : 'w-0'
      } transition-all duration-300 ease-in-out bg-[#1e1f20] border-r border-zinc-800/60 flex flex-col z-20 relative overflow-hidden`}
    >
      {/* Header */}
      <div className="p-4 border-b border-zinc-800/60 flex items-center justify-between">
        <div className="flex items-center gap-2 text-blue-400 font-medium">
          <Sparkles className="w-5 h-5" />
          <span className="text-zinc-100 font-semibold tracking-wide">Study Assistant</span>
        </div>
      </div>

      {/* Hidden File Input & Upload Button */}
      <div className="p-4">
        <input 
          type="file" 
          ref={fileInputRef} 
          onChange={handleFileChange} 
          accept="application/pdf" 
          className="hidden" 
        />
        <button 
          onClick={() => fileInputRef.current?.click()}
          disabled={isUploading}
          className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-500 disabled:bg-zinc-700 text-white py-2.5 px-4 rounded-xl font-medium transition-all shadow-md shadow-blue-950/20"
        >
          {isUploading ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              <span>Processing PDF...</span>
            </>
          ) : (
            <>
              <Plus className="w-4 h-4" />
              <span>Upload Study Material</span>
            </>
          )}
        </button>
      </div>

      {/* Dynamic Document List */}
      <div className="flex-1 overflow-y-auto px-3 py-2 space-y-1">
        <p className="px-3 text-xs font-semibold uppercase tracking-wider text-zinc-400 mb-2">
          Active Knowledge Base
        </p>
        
        {documents.length === 0 ? (
          <p className="px-3 text-xs text-zinc-500 italic">No documents uploaded yet.</p>
        ) : (
          documents.map((doc, index) => (
            <div 
              key={doc.id || index}
              className="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-zinc-800/40 hover:bg-zinc-800/80 border border-zinc-700/30 transition-all cursor-pointer"
            >
              <FileText className="w-4 h-4 text-blue-400 shrink-0" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-zinc-200 truncate">
                  {doc.originalFileName || doc.name || 'Untitled Document'}
                </p>
              </div>
            </div>
          ))
        )}
      </div>
    </aside>
  );
}