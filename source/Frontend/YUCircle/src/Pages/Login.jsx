import { useState } from "react";
import useFetch from "../Hooks/useFetch";
import { useNavigate } from "react-router-dom";


export default function Register() {
  const { post } = useFetch("http://localhost:8080/auth/");
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    password: "",
  });

  const navigate = useNavigate();

  const [errorMessage, setErrorMessage] = useState("");

  function handleChange(e) {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  }


  async function onSubmit(e) {
    e.preventDefault(); 
     post("authenticate", {  
        username: formData.username,
        password: formData.password,}).then(data => {
        if (data.status === 401) {
          setErrorMessage(data.message);
        }

        else {
        localStorage.setItem("token", data.token); 
        localStorage.setItem("username", data.username);
        window.dispatchEvent(new Event("localStorageChange"));
        navigate("/profile");
        }
    })

    }

  return (
    <div className="min-h-screen flex justify-center items-center text-white">
      <form
        onSubmit={onSubmit}
        className="bg-(--yorku-red) max-w-md w-full p-10 pb-20 pt-15"
      >
        <h1 className="text-center">YUCircle</h1>
        <div className="flex flex-col gap-5 mt-2">
          <h2 className="text-center font-bold">Login</h2>
          <p className="text-xs">
            Login to your existing account.
          </p>

    

          {/* Username */}
          <div className="flex flex-col mb-6">
            <label className="mb-1">Username</label>
            <input
              name="username"
              type="text"
              value={formData.username}
              onChange={handleChange}
              className="border-b-2 border-gray-400 outline-none p-1"
              style={{
                "--tw-border-opacity": 1,
                borderBottomColor: "var(--yorku-blue)",
              }}
            />
          </div>

          {/* Password */}
          <div className="flex flex-col mb-6">
            <label className="mb-1">Password</label>
            <input
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              className="border-b-2 border-gray-400 outline-none p-1"
              style={{
                "--tw-border-opacity": 1,
                borderBottomColor: "var(--yorku-blue)",
              }}
            />
          </div>
        </div>

        <div className="flex justify-center">
          <button
            type="submit"
            className="bg-white text-black px-4 py-2 rounded"
          >
            Login
          </button>
        </div>
        {errorMessage && (
          <p className="text-white text-center mt-4">{errorMessage}</p>
        )}
      </form>
    </div>

    
  );
}
