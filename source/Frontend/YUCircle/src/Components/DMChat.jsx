import { useState, useEffect } from "react";

export default function DMChat({ onSendMessage, onReceiveMessage }) {
  const currentUser = localStorage.getItem("username");

  const [selectedUser, setSelectedUser] = useState(null);
  const [messages, setMessages] = useState({});
  const [text, setText] = useState("");

  // REAL USERS FOR TESTING
  const users = [
    {
      id: 1,
      username: "jdeng7",
      displayName: "Jason Deng",
      avatar: "/default-avatar.png",
    },
    {
      id: 2,
      username: "defrozen7",
      displayName: "DeFroZen",
      avatar: "/default-avatar.png",
    },
  ];

  // Handle incoming WebSocket messages
  useEffect(() => {
    if (!onReceiveMessage) return;

    onReceiveMessage((msg) => {
  if (!msg) return;   // Prevent crash
  if (!msg.sender) return; // Prevent crash if incomplete message

  const key = msg.sender;
  setMessages((prev) => ({
    ...prev,
    [key]: [...(prev[key] || []), msg],
  }));
});
  }, [onReceiveMessage]);

  const handleSend = () => {
    if (!text.trim() || !selectedUser) return;

    const msg = {
      sender: currentUser,
      receiver: selectedUser.username,   // FIXED
      content: text,
    };

    // Update UI immediately
    setMessages((prev) => ({
      ...prev,
      [selectedUser.username]: [
        ...(prev[selectedUser.username] || []),
        msg,
      ],
    }));

    // Send through WebSocket
    if (onSendMessage) onSendMessage(msg);

    setText("");
  };

  return (
    <div className="flex h-screen bg-white text-black">
      {/* LEFT SIDEBAR */}
      <div className="w-72 border-r border-gray-300 p-4 bg-white">
        <h2 className="text-xl font-bold mb-4 text-(--yorku-red)">Messages</h2>

        {users.map((user) => (
          <div
            key={user.id}
            onClick={() => setSelectedUser(user)}
            className={`flex items-center gap-3 p-3 rounded-lg cursor-pointer hover:bg-gray-100 transition ${
              selectedUser?.id === user.id ? "bg-gray-100" : ""
            }`}
          >
            <img
              src={user.avatar}
              className="w-12 h-12 rounded-full object-cover border border-gray-200"
              alt=""
            />
            <div>
              <p className="font-medium">{user.displayName}</p>
              <span className="text-sm text-gray-500">
                {messages[user.username]?.slice(-1)[0]?.content ||
                  "No messages yet"}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* RIGHT CHAT WINDOW */}
      <div className="flex-1 flex flex-col">
        {!selectedUser ? (
          <div className="flex items-center justify-center h-full text-gray-400 text-lg">
            Select a conversation
          </div>
        ) : (
          <>
            {/* HEADER */}
            <div className="flex items-center gap-3 p-4 border-b border-gray-300 bg-white">
              <img
                src={selectedUser.avatar}
                className="w-10 h-10 rounded-full border border-gray-300"
                alt=""
              />
              <span className="font-semibold text-lg">
                {selectedUser.displayName}
              </span>
            </div>

            {/* MESSAGES */}
            <div className="flex-1 overflow-y-scroll p-4 space-y-4 bg-gray-50">
              {(messages[selectedUser.username] || []).map((msg, index) => (
                <div
                  key={index}
                  className={`flex ${
                    msg.sender === currentUser
                      ? "justify-end"
                      : "justify-start"
                  }`}
                >
                  <div
                    className={`px-4 py-2 rounded-2xl max-w-xs shadow ${
                      msg.sender === currentUser
                        ? "bg-(--yorku-red) text-white"
                        : "bg-white border border-gray-300"
                    }`}
                  >
                    {msg.content}
                  </div>
                </div>
              ))}
            </div>

            {/* INPUT */}
            <div className="p-4 border-t border-gray-300 flex items-center gap-3 bg-white">
              <input
                type="text"
                placeholder="Message..."
                value={text}
                onChange={(e) => setText(e.target.value)}
                className="flex-1 p-3 rounded-full border border-gray-300 outline-none"
              />
              <button
                onClick={handleSend}
                className="bg-(--yorku-red) text-white px-6 py-2 rounded-full font-semibold hover:bg-red-700 transition"
              >
                Send
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
