import { useState, useEffect } from "react";
import useFetch from "../Hooks/useFetch";

export default function UserProfile() {
  const { get, patch } = useFetch("http://localhost:8080/api/students/");
  const { get: getPosts } = useFetch("http://localhost:8080/api/posts/");

  const username = localStorage.getItem("username");

  const [student, setStudent] = useState(null);
  const [posts, setPosts] = useState([]);
  const { del: deleteComment } = useFetch("http://localhost:8080/api/comments");


  // COMMENT INPUT per post
  const [commentInputs, setCommentInputs] = useState({});

  // OPEN/CLOSE comment sections per post
  const [openComments, setOpenComments] = useState({});
  
  // Edit post modal state
  const [editingPostId, setEditingPostId] = useState(null);
  const [editContent, setEditContent] = useState("");


  // PROFILE FIELDS
  const [editingField, setEditingField] = useState(null);
  const [fieldValues, setFieldValues] = useState({
    firstName: "",
    lastName: "",
    major: "",
    bio: "",
  });

  // PASSWORD RESET
  const [passwordValues, setPasswordValues] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [resettingPassword, setResettingPassword] = useState(false);
  const [passwordMessage, setPasswordMessage] = useState(null);

  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);

  // Load profile
  useEffect(() => {
    if (!username) return;

    get(`by-username/${username}`).then((data) => {
      setStudent(data);
      setFieldValues({
        firstName: data.firstName || "",
        lastName: data.lastName || "",
        major: data.major || "",
        bio: data.bio || "",
      });
    });
  }, []);

  // Load posts belonging to this user
  useEffect(() => {
    if (!username) return;

    getPosts(`user/${username}`)
      .then((posts) => setPosts(posts))
      .catch(() => setPosts([]));
  }, [username]);

  if (!student) return <p className="text-white p-4">Loading...</p>;

  // PROFILE SAVE
  async function handleSaveField(field) {
    setSaving(true);
    setMessage(null);

    try {
      const updated = await patch(`update/${username}`, {
        [field]: fieldValues[field],
      });

      setStudent(updated);
      setEditingField(null);
      setMessage("Saved!");
      setTimeout(() => setMessage(null), 2000);
    } catch {
      setMessage("Error saving.");
    }

    setSaving(false);
  }

  // PASSWORD RESET
  async function handleResetPassword() {
    const { currentPassword, newPassword, confirmPassword } = passwordValues;

    if (!currentPassword || !newPassword || !confirmPassword) {
      setPasswordMessage("Please fill all fields.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordMessage("New passwords do not match.");
      return;
    }

    setResettingPassword(true);
    setPasswordMessage(null);

    try {
      await patch(`update/${username}`, {
        currentPassword,
        newPassword,
      });
      setPasswordMessage("Password updated successfully!");
      setPasswordValues({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } catch {
      setPasswordMessage("Error updating password.");
    }

    setResettingPassword(false);
  }

  // Toggle comment visibility
  function toggleComments(postId) {
    setOpenComments((prev) => ({
      ...prev,
      [postId]: !prev[postId],
    }));
  }

  // Like a post
  async function handleLike(postId) {
    await fetch(`http://localhost:8080/api/posts/${postId}/like?username=${username}`, {
      method: "PUT",
    });

    getPosts(`user/${username}`).then(setPosts);
  }

  //Submit comment
  async function handleComment(postId) {
    const text = commentInputs[postId] || "";
    if (!text.trim()) return;

    try {
      const res = await fetch(`http://localhost:8080/api/comments/${postId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          username,
          content: text,
        }),
      });

      if (!res.ok) {
        console.error("Comment failed:", await res.text());
        return;
      }

      // Clear input field
      setCommentInputs((prev) => ({ ...prev, [postId]: "" }));

      // Reload posts after submitting comment
      getPosts(`user/${username}`).then(setPosts);

    } catch (err) {
      console.error("Error posting comment:", err);
    }
  }
  
  // Delete comment
  const handleDeleteComment = async (commentId, postId) => {
	if (!window.confirm("Delete Comment?")) return;
    try {
      await deleteComment(`/${commentId}`);

      setPosts(prev =>
        prev.map(p =>
          p.id === postId
            ? { ...p, comments: p.comments.filter(c => c.id !== commentId) }
            : p
        )
      );
    } catch (err) {
      console.error("Failed to delete comment:", err);
    }
  };


  //Edit Post
  async function handleEditPost(postId) {
    try {
      const updated = await fetch(`http://localhost:8080/api/posts/update/${postId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ content: editContent })
      });

      if (!updated.ok) {
        console.error("Edit failed");
        return;
      }

      // Refresh posts
      getPosts(`user/${username}`).then(setPosts);
      setEditingPostId(null);

    } catch (err) {
      console.error("Error editing post:", err);
    }
  }

  //Delete Post
  async function handleDeletePost(postId) {
    if (!window.confirm("Are you sure you want to delete this post?")) return;

    try {
      await fetch(`http://localhost:8080/api/posts/${postId}`, {
        method: "DELETE",
      });

      // Remove from UI
      setPosts(prev => prev.filter(p => p.id !== postId));

    } catch (err) {
      console.error("Error deleting post:", err);
    }
  }



  // REUSABLE PROFILE FIELD
  function renderEditableField(label, field) {
    const isEditing = editingField === field;

    return (
      <div className="mb-4">
        <div className="flex items-center justify-between mb-1">
          <div className="text-xs text-white/80">{label}</div>

          {!isEditing ? (
            <button
              onClick={() => setEditingField(field)}
              className="text-sm underline underline-offset-2"
            >
              Edit
            </button>
          ) : (
            <button
              onClick={() => {
                setEditingField(null);
                setFieldValues((prev) => ({
                  ...prev,
                  [field]: student[field] || "",
                }));
              }}
              className="text-sm underline underline-offset-2"
            >
              Cancel
            </button>
          )}
        </div>

        {!isEditing ? (
          <div className="font-medium text-sm text-white/90">
            {student[field] || <span className="text-white/60">Not set</span>}
          </div>
        ) : (
          <div className="space-y-2">
            <input
              type="text"
              value={fieldValues[field]}
              onChange={(e) =>
                setFieldValues((prev) => ({
                  ...prev,
                  [field]: e.target.value,
                }))
              }
              className="w-full p-2 rounded text-black"
            />

            <button
              onClick={() => handleSaveField(field)}
              disabled={saving}
              className="bg-white text-black px-3 py-1 rounded"
            >
              {saving ? "Saving..." : "Save"}
            </button>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="min-h-screen flex gap-6 p-8 text-white">
      {/* SIDEBAR */}
      <aside
        className="max-w-sm w-full rounded shadow-md"
        style={{ backgroundColor: "var(--yorku-red, #B31B1B)" }}
      >
        <div className="p-6">
          <h2 className="text-2xl font-bold mb-2">Your Profile</h2>
          <p className="text-sm opacity-90 mb-4">
            Welcome back,{" "}
            <span className="font-semibold">
              {student.firstName} {student.lastName}
            </span>
          </p>

          {renderEditableField("First name", "firstName")}
          {renderEditableField("Last name", "lastName")}

          {/* Username */}
          <div className="mb-4">
            <div className="text-xs text-white/80">Username</div>
            <div className="font-medium text-sm text-white/90 mt-1">
              {student.username}
            </div>
          </div>

          {/* Email */}
          <div className="mb-4">
            <div className="text-xs text-white/80">Email</div>
            <div className="font-medium text-sm text-white/90 mt-1">
              {student.email}
            </div>
          </div>

          {renderEditableField("Major", "major")}

          {/* BIO */}
          <div className="mt-4">
            <div className="flex items-center justify-between mb-2">
              <div className="text-xs text-white/80">Bio</div>

              {editingField !== "bio" ? (
                <button
                  onClick={() => setEditingField("bio")}
                  className="text-sm underline underline-offset-2"
                >
                  Edit
                </button>
              ) : (
                <button
                  onClick={() => {
                    setEditingField(null);
                    setFieldValues((prev) => ({
                      ...prev,
                      bio: student.bio || "",
                    }));
                  }}
                  className="text-sm underline underline-offset-2"
                >
                  Cancel
                </button>
              )}
            </div>

            {editingField !== "bio" ? (
              <div className="text-sm text-white/90">
                {student.bio || (
                  <span className="text-white/60">
                    No bio yet. Click Edit to add one.
                  </span>
                )}
              </div>
            ) : (
              <div className="space-y-2">
                <textarea
                  value={fieldValues.bio}
                  onChange={(e) =>
                    setFieldValues((prev) => ({
                      ...prev,
                      bio: e.target.value,
                    }))
                  }
                  rows={4}
                  className="w-full p-2 rounded text-black"
                ></textarea>

                <button
                  onClick={() => handleSaveField("bio")}
                  disabled={saving}
                  className="bg-white text-black px-3 py-1 rounded"
                >
                  {saving ? "Saving..." : "Save"}
                </button>
              </div>
            )}
          </div>

          {message && (
            <p className="text-xs text-white/80 mt-3">{message}</p>
          )}
        </div>

        {/* PASSWORD RESET */}
        <div className="mt-6 p-6">
          <h3 className="text-sm font-semibold mb-2 text-white/90">
            Reset Password
          </h3>

          <input
            type="password"
            placeholder="Current Password"
            value={passwordValues.currentPassword}
            onChange={(e) =>
              setPasswordValues((prev) => ({
                ...prev,
                currentPassword: e.target.value,
              }))
            }
            className="w-full p-2 rounded mb-2 text-black"
          />

          <input
            type="password"
            placeholder="New Password"
            value={passwordValues.newPassword}
            onChange={(e) =>
              setPasswordValues((prev) => ({
                ...prev,
                newPassword: e.target.value,
              }))
            }
            className="w-full p-2 rounded mb-2 text-black"
          />

          <input
            type="password"
            placeholder="Confirm New Password"
            value={passwordValues.confirmPassword}
            onChange={(e) =>
              setPasswordValues((prev) => ({
                ...prev,
                confirmPassword: e.target.value,
              }))
            }
            className="w-full p-2 rounded mb-2 text-black"
          />

          <button
            onClick={handleResetPassword}
            disabled={resettingPassword}
            className="bg-white text-black px-3 py-1 rounded"
          >
            {resettingPassword ? "Updating..." : "Reset Password"}
          </button>

          {passwordMessage && (
            <p className="text-xs text-white/80 mt-2">
              {passwordMessage}
            </p>
          )}
        </div>
      </aside>

      {/* MAIN POSTS SECTION */}
      <main className="flex-1">
        <h1 className="text-3xl font-bold text-black mb-6">Posts</h1>

        <section className="space-y-6">
          {posts.length === 0 && (
            <div className="bg-white/10 text-white p-4 rounded shadow">
              No posts yet.
            </div>
          )}

          {posts.map((post) => (
            <div key={post.id} className="bg-white text-black p-4 rounded shadow space-y-3">

              {/* Header */}
              <div className="flex justify-between">
                <span className="font-semibold">{post.username}</span>
                <span className="text-gray-500">
                  {new Date(post.timestamp).toLocaleString()}
                </span>
              </div>

              {/* Content */}
              <p>{post.content}</p>

              {post.imageUrl && (
                <img
                  src={post.imageUrl}
                  alt="Post"
                  className="rounded max-h-96 object-cover"
                />
              )}

              {/* Like + Comment Row */}
              <div className="flex items-center gap-6 text-sm">
			  {post.username === username && (
			    <div className="flex gap-3 ml-auto">
			      <button 
			        className="text-gray-500 hover:text-gray-800"
			        onClick={() => {
			          setEditingPostId(post.id);
			          setEditContent(post.content);
			        }}
			      >
			        Edit
			      </button>

			      <button 
			        className="text-gray-500 hover:text-gray-800"
			        onClick={() => handleDeletePost(post.id)}
			      >
			        Delete
			      </button>
			    </div>
			  )}
			  {editingPostId === post.id && (
			    <div className="border p-3 mt-2 rounded bg-gray-100">
			      <textarea
			        className="w-full p-2 border rounded"
			        rows={3}
			        value={editContent}
			        onChange={(e) => setEditContent(e.target.value)}
			      />

			      <div className="flex gap-3 mt-2">
			        <button
			          className="bg-blue-500 text-white px-3 py-1 rounded"
			          onClick={() => handleEditPost(post.id)}
			        >
			          Save
			        </button>
			        <button
			          className="bg-gray-300 px-3 py-1 rounded"
			          onClick={() => setEditingPostId(null)}
			        >
			          Cancel
			        </button>
			      </div>
			    </div>
			  )}


                <button className="flex items-center gap-1" onClick={() => handleLike(post.id)}>
                  ❤️ <span>{post.likes || 0}</span>
                </button>

                <button
                  className="flex items-center gap-1"
                  onClick={() => toggleComments(post.id)}
                >
                  💬 <span>{post.comments?.length || 0}</span>
                </button>
              </div>

              {/* Comment Section */}
              {openComments[post.id] && (
                <div className="space-y-2 pl-4 border-l">

                  {/* Comment Input */}
                  <div className="flex gap-2">
                    <input
                      type="text"
                      placeholder="Add a comment..."
                      className="flex-1 border rounded p-2"
                      value={commentInputs[post.id] || ""}
                      onChange={(e) =>
                        setCommentInputs((prev) => ({
                          ...prev,
                          [post.id]: e.target.value,
                        }))
                      }
                    />

                    <button
                      className="bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600 text-white"
                      onClick={() => handleComment(post.id)}
                    >
                      Post
                    </button>
                  </div>

                  {/* Comment list */}
				  <div className="mt-2 space-y-2">
				    {post.comments?.map((c) => (
				      <div key={c.id} className="text-sm p-2 bg-white/10 rounded flex justify-between items-start">
				        <div>
				          <span className="font-semibold">{c.username}: </span>
				          {c.content}
				        </div>

				        {/* Delete Comment Button */}
				        <button
				          className="text-gray-500 hover:text-gray-800 text-xs ml-2"
				          onClick={() => handleDeleteComment(c.id, post.id)}
				        >
				          Delete
				        </button>
				      </div>
				    ))}
				  </div>
                </div>
              )}
            </div>
          ))}
        </section>
      </main>
    </div>
  );
}
