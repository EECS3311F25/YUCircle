import { useState } from "react";
import useFetch from "../Hooks/useFetch";

export default function Register() {
  const { post } = useFetch("http://localhost:8080/auth/");
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    password: "",
  });

  const [isPostSuccess, setIsPostSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  function handleChange(e) {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  }


  async function onSubmit(e) {
    e.preventDefault(); 
     post("register", {  
        username: formData.username,
        email: formData.email,
        firstName: formData.firstName,
        lastName: formData.lastName,
        password: formData.password,}).then(data => {
        if (data.status === 409) {
          setErrorMessage(data.message);
        }
        else if (data.status === 400) {
            const firstError = Object.values(data.errors)[0];
            setErrorMessage(firstError);
        }

        else {
          setIsPostSuccess(true);
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
        {isPostSuccess && <p className="text-center pt-1">Check your email for verification.</p>}
        {!isPostSuccess && (
            <>
        <div className="flex flex-col gap-5 mt-2">
          <h2 className="text-center font-bold">Register</h2>
          <p className="text-xs">
            Sign up for an account, verify using your York University email.
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

          {/* Email */}
          <div className="flex flex-col mb-6">
            <label className="mb-1">Email</label>
            <input
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              className="border-b-2 border-gray-400 outline-none p-1"
              style={{
                "--tw-border-opacity": 1,
                borderBottomColor: "var(--yorku-blue)",
              }}
            />
          </div>

          {/* First Name */}
          <div className="flex flex-col mb-6">
            <label className="mb-1">First Name</label>
            <input
              name="firstName"
              type="text"
              value={formData.firstName}
              onChange={handleChange}
              className="border-b-2 border-gray-400 outline-none p-1"
              style={{
                "--tw-border-opacity": 1,
                borderBottomColor: "var(--yorku-blue)",
              }}
            />
          </div>

          {/* Last Name */}
          <div className="flex flex-col mb-6">
            <label className="mb-1">Last Name</label>
            <input
              name="lastName"
              type="text"
              value={formData.lastName}
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
            Verify Email
          </button>
        </div>
            </>
        )}
        {errorMessage && (
          <p className="text-white text-center mt-4">{errorMessage}</p>
        )}
      </form>
    </div>
    
  );
}
