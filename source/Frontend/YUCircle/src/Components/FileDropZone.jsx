import { useState } from 'react'

export default function FileDropZone({ selectedFile, setSelectedFile }) {
  const [isDragging, setIsDragging] = useState(false)

  // Handle a file being dropped onto the zone
  const handleDrop = (e) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)

    const file = e.dataTransfer.files && e.dataTransfer.files[0]
    if (file) setSelectedFile(file)
  }

  // Prevent default so the browser doesn’t open the file
  const handleDragOver = (e) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(true)
  }

  const handleDragLeave = () => setIsDragging(false)

  const handleFileSelect = (e) => {
    const file = e.target.files && e.target.files[0]
    if (file) setSelectedFile(file)
  }

  // Trigger the hidden input to open the file picker
  const openFilePicker = () => {
    const input = document.getElementById('fileInput')
    if (input) input.click()
  }

  return (
    <div
      className={`border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors
        ${isDragging ? 'bg-gray-100 border-gray-400' : 'bg-white border-gray-300'}
        text-gray-600`}
      onDrop={handleDrop}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onClick={openFilePicker}
      role="button"
      aria-label="Upload schedule drop zone"
      tabIndex={0}
    >
      {/* Upload icon (arrow into tray).*/}
      <svg
        className="mx-auto mb-4 w-12 h-12 text-gray-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M4 16v1a2 2 0 002 2h12a2 2 0 002-2v-1
             M16 10l-4-4m0 0l-4 4m4-4v10"
        />
      </svg>

      {/* Instruction text */}
      <p className="text-sm">
        Drag and drop <span className="font-semibold">(Or select from your files)</span>
      </p>

      {/* Hidden file input (opened when the box is clicked). Only accept only certain file types. */}
      <input
        id="fileInput"
        type="file"
        className="hidden"
        onChange={handleFileSelect}
        accept=".pdf,.jpeg,.jpg,.png,.bmp,.tiff,.heif"
      />

      {/* Show selected file name for feedback */}
      {selectedFile && (
        <p className="mt-2 text-sm text-green-600">
          Selected: {selectedFile.name}
        </p>
      )}
    </div>
  )
}