import { useState } from "react";
import useFetch from "../Hooks/useFetch";

export default function MakePost() {
  const { post } = useFetch("http://localhost:8080/api/posts/");
  const [formData, setFormData] = useState({
    username: "",
    content: "",
    imageUrl: "",
  });
  const [isPostSuccess, setIsPostSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  function handleChange(e) {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  }

  async function onSubmit(e) {
    e.preventDefault();
    post("create", {
      username: localStorage.getItem("username"),
      content: formData.content,
      imageUrl: formData.imageUrl,
    })
      .then(data => {
        //  "Post created successfully", id: 3 
        if (data?.message === "Post created successfully") {
          setIsPostSuccess(true);
          setErrorMessage("");
          //  clear the form after successful post
          setFormData({ username: "", content: "", imageUrl: "" });
        } 
        else if (data?.status === 400 || data?.status === 500) {
          setErrorMessage("Server error: Could not create post.");
        } 
        else {
          console.log("Unexpected data:", data);
          setErrorMessage("Unexpected response from server.");
        }
      })
      .catch(err => {
        console.error("Network error:", err);
        setErrorMessage("Network error: " + err.message);
      });
  }

  return (
    <div className="p-6 text-black">
      <h1 className="text-2xl font-bold mb-4">Create a Post</h1>
      <form onSubmit={onSubmit} className="flex flex-col gap-4 max-w-md">
        <textarea
          name="content"
          placeholder="Write your post..."
          value={formData.content}
          onChange={handleChange}
          className="border p-2 rounded"
        />
        <input
          name="imageUrl"
          type="text"
          placeholder="Image URL (optional)"
          value={formData.imageUrl}
          onChange={handleChange}
          className="border p-2 rounded"
        />
        <button
          type="submit"
          className="bg-red-600 text-white py-2 px-4 rounded hover:bg-red-700"
        >
          Post
        </button>
      </form>

      {isPostSuccess && (
        <p className="mt-4 text-green-600">Post created successfully!</p>
      )}
      {errorMessage && (
        <p className="mt-4 text-red-600">{errorMessage}</p>
      )}
    </div>
  );
}
