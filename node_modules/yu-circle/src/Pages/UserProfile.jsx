import { useState, useEffect } from "react";
import useFetch from "../Hooks/useFetch";

export default function UserProfile() {
  // API hooks for different resources
  const studentApi = useFetch("http://localhost:8080/api/students/by-username/");
  const postApi = useFetch("http://localhost:8080/api/posts/");
  const commentApi = useFetch("http://localhost:8080/api/comments/");

  const [student, setStudent] = useState(null);
  const [posts, setPosts] = useState([]);
  const [likedPosts, setLikedPosts] = useState(new Set());
  const [editingBio, setEditingBio] = useState(false);
  const [bio, setBio] = useState("");
  const [savingBio, setSavingBio] = useState(false);
  const [bioMessage, setBioMessage] = useState(null);
  const [error, setError] = useState("");

  const [expandedPost, setExpandedPost] = useState(null);
  const [comments, setComments] = useState({});
  const [newComment, setNewComment] = useState({});

  useEffect(() => {
    const username = localStorage.getItem("username");
    if (username) {
      studentApi.get(username).then(setStudent);
    }
    fetchPosts();
  }, []);

  // Fetch all posts (sorted by most recent)
  async function fetchPosts() {
    try {
      const data = await postApi.get("all");
      const sorted = data.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
      setPosts(sorted);
    } catch (err) {
      console.error("Error loading posts:", err);
      setError("Failed to load posts.");
    }
  }

  // Like a post
  async function handleLike(postId) {
    const alreadyLiked = likedPosts.has(postId);
    try {
      // UI update
      setPosts(prev =>
        prev.map(p =>
          p.id === postId
            ? { ...p, likes: p.likes + (alreadyLiked ? -1 : 1) }
            : p
        )
      );

      // Update liked set locally
      setLikedPosts(prev => {
        const updated = new Set(prev);
        if (alreadyLiked) updated.delete(postId);
        else updated.add(postId);
        return updated;
      });

      // Call backend 
	  const username = localStorage.getItem("username");
      await postApi.put(`${postId}/like?username=${username}`);
    } catch (err) {
      console.error("Error liking post:", err);
    }
  }

  // Toggle comment visibility
  async function toggleComments(postId) {
    if (expandedPost === postId) {
      setExpandedPost(null);
      return;
    }

    try {
      const data = await commentApi.get(`${postId}`);
      setComments(prev => ({ ...prev, [postId]: data }));
      setExpandedPost(postId);
    } catch (err) {
      console.error("Error fetching comments:", err);
    }
  }

  // Add a comment to a post
  async function handleAddComment(postId) {
    const username = localStorage.getItem("username");
    const content = newComment[postId];
    if (!content?.trim()) return;

    try {
      const added = await commentApi.post(`${postId}`, { username, content });
      setComments(prev => ({
        ...prev,
        [postId]: [added, ...(prev[postId] || [])],
      }));
      setNewComment(prev => ({ ...prev, [postId]: "" }));
    } catch (err) {
      console.error("Error adding comment:", err);
    }
  }

  // Save bio (currently local)
  function handleSaveBio() {
    setSavingBio(true);
    setBioMessage(null);
    setStudent(prev => ({ ...prev, bio }));
    setBioMessage("Bio saved locally.");
    setEditingBio(false);
    setSavingBio(false);
    setTimeout(() => setBioMessage(null), 3000);
  }

  if (!student) return <p className="text-white p-4">No student data..</p>;

  return (
        <div className="min-h-screen flex gap-6 p-8 text-white">
      <aside className="max-w-sm w-full rounded shadow-md" style={{ backgroundColor: "var(--yorku-red, #B31B1B)" }}>
        <div className="p-6">
          <h2 className="text-2xl font-bold mb-2">Your Profile</h2>
          <p className="text-sm opacity-90 mb-4">
            Welcome back, <span className="font-semibold">{student.firstName + " " + student.lastName}</span>
          </p>

          <div className="space-y-2 mb-4">
            <div>
              <div className="text-xs text-white/80">First name</div>
              <div className="font-medium">{student.firstName}</div>
            </div>
            <div>
              <div className="text-xs text-white/80">Last name</div>
              <div className="font-medium">{student.lastName}</div>
            </div>
            <div>
              <div className="text-xs text-white/80">Email</div>
              <div className="font-medium">{student.email}</div>
            </div>
            <div>
              <div className="text-xs text-white/80">Major</div>
              <div className="font-medium">{student.major === null ? "We don't know your major yet." : student.major}</div>
            </div>
          </div>

          <div className="mt-4">
            <div className="flex items-center justify-between mb-2">
              <div className="text-xs text-white/80">Bio</div>
              <div>
                {!editingBio ? (
                  <button onClick={() => setEditingBio(true)} className="text-sm underline underline-offset-2">Edit</button>
                ) : (
                  <button onClick={() => { setEditingBio(false); setBio(student.bio ?? ""); }} className="text-sm underline underline-offset-2">Cancel</button>
                )}
              </div>
            </div>

            {!editingBio ? (
              <div className="text-sm text-white/90">
                {student.bio || <span className="text-white/60">No bio yet. Click Edit to add one.</span>}
              </div>
            ) : (
              <div className="space-y-2">
                <textarea
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                  rows={4}
                  className="w-full p-2 rounded text-black"
                  placeholder="Tell people about yourself..."
                />
                <div className="flex items-center gap-2">
                  <button onClick={handleSaveBio} disabled={savingBio} className="bg-white text-black px-3 py-1 rounded">
                    {savingBio ? "Saving..." : "Save"}
                  </button>
                  {bioMessage && <div className="text-sm text-white/80 ml-3">{bioMessage}</div>}
                </div>
              </div>
            )}
          </div>
        </div>
      </aside>

      {/* Main Posts Section */}
      <main className="flex-1">
        <h1 className="text-3xl font-bold text-black mb-6">Posts</h1>
        {error && <p className="text-red-400 mb-4">{error}</p>}

        {posts.length === 0 ? (
          <p className="text-gray-400">No posts yet.</p>
        ) : (
          <div className="space-y-6">
            {posts.map(post => (
              <div key={post.id} className="bg-white rounded-lg p-4 shadow">
                <div className="flex justify-between items-center mb-2">
                  <span className="font-semibold text-black">{post.username}</span>
                  <span className="text-xs text-gray-500">
                    {new Date(post.timestamp).toLocaleString()}
                  </span>
                </div>

                <p className="text-gray-800 mb-2">{post.content}</p>
                {post.imageUrl && (
                  <img
                    src={post.imageUrl}
                    alt="Post"
                    className="w-full max-h-60 object-cover rounded mb-2"
                  />
                )}

                {/* Likes & Comments */}
                <div className="flex items-center gap-4 text-sm text-gray-600">
				<button
				  onClick={() => handleLike(post.id)}
				  className={`flex items-center gap-1 transition ${
				    likedPosts.has(post.id)
				      ? "text-red-600"
				      : "text-gray-600 hover:text-red-500"
				  }`}
				>
				  ❤️ {post.likes || 0}
				</button>

                  <button
                    onClick={() => toggleComments(post.id)}
                    className="flex items-center gap-1 hover:text-blue-600 transition"
                  >
                    💬 {post.comments ? post.comments.length : 0}
                  </button>
                </div>

                {/* Expanded Comments */}
                {expandedPost === post.id && (
                  <div className="mt-3 border-t border-gray-300 pt-3">
                    <div className="space-y-2 mb-3">
                      {(comments[post.id] || []).map(c => (
                        <div key={c.id} className="text-gray-700 bg-gray-100 p-2 rounded">
                          <span className="font-semibold">{c.username}</span>: {c.content}
                        </div>
                      ))}
                    </div>

                    <div className="flex gap-2">
                      <input
                        type="text"
                        placeholder="Add a comment..."
                        value={newComment[post.id] || ""}
                        onChange={(e) => setNewComment(prev => ({ ...prev, [post.id]: e.target.value }))}
                        className="flex-1 p-2 border rounded text-black"
                      />
                      <button
                        onClick={() => handleAddComment(post.id)}
                        className="bg-blue-600 text-white px-3 py-1 rounded"
                      >
                        Post
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
