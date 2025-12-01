import { Link } from "react-router-dom";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";


export default function Sidebar() {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const navigate = useNavigate();


  useEffect(() => {
  const checkLoginStatus = () => {
    const token = localStorage.getItem("token");
    setIsLoggedIn(!!token);
  };

  checkLoginStatus();

  window.addEventListener("storage", checkLoginStatus);
  window.addEventListener("localStorageChange", checkLoginStatus);

  return () => {
    window.removeEventListener("storage", checkLoginStatus);
    window.removeEventListener("localStorageChange", checkLoginStatus);
  };
}, []);

  return (
    <>
      {/* Hamburger button - only visible on small/medium screens */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="xl:hidden fixed top-4 left-4 z-50 p-2 text-white rounded bg-(--yorku-red)"
      >
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          {isOpen ? (
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          ) : (
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
          )}
        </svg>
      </button>

      <div
        className={`${
          isOpen ? "translate-x-0" : "-translate-x-full"
        } xl:translate-x-0 fixed xl:static h-screen w-64 xl:w-1/7 text-white transition-transform duration-300 ease-in-out z-40 bg-(--yorku-red)`}
      >
        <div className="flex flex-col">
          <Link to="/" className="py-10 border-b border-white pb-2 pl-4 pb-10">
            Welcome {isLoggedIn ? "back, " + localStorage.getItem("username") : "new user!"}
          </Link>

          {/* Show Register/Login only when user NOT logged in */}
          {!isLoggedIn && (
            <>
              <Link
                to="/register"
                className="pb-2 pl-4 pt-2 border-b border-white hover:bg-(--yorku-blue) transition-colors duration-300 cursor-pointer"
              >
                Register
              </Link>
              <Link
                to="/login"
                className="pb-2 pl-4 pt-2 border-b border-white hover:bg-(--yorku-blue) transition-colors duration-300 cursor-pointer"
              >
                Login
              </Link>
            </>
          )}
        {isLoggedIn && (
            <>
            <Link to="/profile" className="pb-2 pl-4 pt-2 border-b border-white hover:bg-(--yorku-blue) transition-colors duration-300 cursor-pointer">
            Your Profile
          </Link>
            <Link to="/make-post" className="pb-2 pl-4 pt-2 border-b border-white hover:bg-(--yorku-blue) transition-colors duration-300 cursor-pointer">
            Make a Post
          </Link>
            <Link to="/profile" className="pb-2 pl-4 pt-2 border-b border-white hover:bg-(--yorku-blue) transition-colors duration-300 cursor-pointer">
            Upload Schedule
          </Link>
          </>
        )}

          {/* Optional logout button */}
          {isLoggedIn && (
            
            <button
              onClick={() => {
                setIsLoggedIn(false);
                localStorage.removeItem("token");
                window.dispatchEvent(new Event("localStorageChange"));
                navigate("/");
              }}
              className="pb-2 pl-4 pt-2 border-b border-white hover:bg-red-700 transition-colors duration-300 text-left"
            >
              Logout
            </button>
          )}
        </div>
      </div>

      {isOpen && (
        <div
          className="fixed inset-0 bg-black z-30 xl:hidden"
          onClick={() => setIsOpen(false)}
        />
      )}
    </>
  );
}
