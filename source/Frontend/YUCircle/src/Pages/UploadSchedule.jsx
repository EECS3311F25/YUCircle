import { useState } from 'react'
import FileDropZone from '../Components/FileDropZone'
import useFetch from "../Hooks/useFetch";

export default function UploadSchedule() {
  // The file selected by the user (null until chosen)
  const [selectedFile, setSelectedFile] = useState(null)
  // Status: 'idle' | 'uploading' | 'success' | 'error'
  const [status, setStatus] = useState('idle')
  // User-facing message for feedback
  const [message, setMessage] = useState('')

  // POST the selected file to the backend using multipart/form-data
  const handleUpload = async () => {
    // Basic validation
    if (!selectedFile) {
      setStatus('error')
      setMessage('Please select a file first.')
      return
    }

    try {
      setStatus('uploading')
      setMessage('Uploading...')

      // Build the form payload expected by Spring controllers: @RequestParam("file")
      const formData = new FormData()
      formData.append("file", selectedFile)

      // Perform the POST
      const username = localStorage.getItem("username")
      if (!username) {
        setStatus("error");
        setMessage("Username not found. Please log in again.");
        return;
      }
      const response = await fetch(`http://localhost:8080/api/students/${username}/schedule`, {
        method: "POST",
        body: formData,
      })

      // Handle non-2xx statuses explicitly
      if (!response.ok) {
        // Try to read a JSON error body; fall back to status text
        let errorText = `Upload failed with status ${response.status}`
        try {
          const errJson = await response.json()
          if (errJson && errJson.message) errorText = errJson.message
        } catch {
          // ignore JSON parse errors
        }
        throw new Error(errorText)
      }

      // Parse a success JSON response (if backend returns one)
      // Show details like parsed schedule info.
      const result = await response.json()
      setMessage(result.message)

      setStatus('success')
      setMessage('Upload successful!')
    } catch (err) {
      console.error('Upload error:', err)
      setStatus('error')
      setMessage(err.message || 'Upload failed. Please try again.')
    }
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100">
      {/* Centered white box */}
      <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
        {/* Title */}
        <h2 className="text-2xl font-bold text-center mb-6 text-gray-800">
          Upload schedule
        </h2>

        {/* Drag-and-drop zone */}
        <FileDropZone
          selectedFile={selectedFile}
          setSelectedFile={setSelectedFile}
        />

        {/* Done button triggers upload */}
        <button
          className={`mt-6 w-full font-semibold py-2 px-4 rounded transition-colors
            ${status === 'uploading'
              ? 'bg-gray-300 text-gray-600 cursor-not-allowed'
              : 'bg-gray-300 hover:bg-gray-400 text-gray-800'}`}
          onClick={handleUpload}
          disabled={status === 'uploading'}
        >
          {status === 'uploading' ? 'Uploading...' : 'Done'}
        </button>

        {/* Status feedback: success/error message */}
        {message && (
          <p
            className={`mt-4 text-sm ${
              status === 'error' ? 'text-red-600' : 'text-green-600'
            }`}
          >
            {message}
          </p>
        )}
      </div>
    </div>
  )
}