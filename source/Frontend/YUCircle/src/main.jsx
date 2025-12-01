import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import Sidebar from './Components/Sidebar'
import Register from "./Pages/Register"
import Login from "./Pages/Login"
import { BrowserRouter, Routes, Route } from "react-router-dom"
import Landing from "./Pages/Landing"
import MakePost from "./Pages/MakePost"
import UserProfile from "./Pages/UserProfile"

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <div className="flex">
        <Sidebar />
        <div className="flex-1">
          <Routes>
            <Route path="/" element={<Landing />} />
            <Route path="/register" element={<Register />} />
            <Route path="/login" element={<Login />} />
            <Route path="/make-post" element={<MakePost />} />
            <Route path="/profile" element={<UserProfile />} />
          </Routes>
        </div>
      </div>
    </BrowserRouter>
  </StrictMode>,
)
