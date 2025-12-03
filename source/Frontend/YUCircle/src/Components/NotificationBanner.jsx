import { useEffect, useState, useRef, useCallback } from "react";
import useFetch from "../Hooks/useFetch"; // adjust path if your hook is elsewhere

export default function NotificationBanner({ username, pollInterval = 10000 }) {
  const { get, patch } = useFetch("http://localhost:8080/api/notifications");
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [showDropdown, setShowDropdown] = useState(false);
  const [topAlert, setTopAlert] = useState(null);
  const mounted = useRef(false);
  const timerRef = useRef(null);

  const load = useCallback(async () => {
    if (!username) return;
    try {
      const all = await get(`/user/${username}`);
      if (!mounted.current) return;
      setNotifications(all || []);
      const unread = (all || []).filter((n) => !n.readFlag);
      setUnreadCount(unread.length);
      setTopAlert(unread.length ? unread[0] : null);
    } catch (e) {
      // ignore for now
    }
  }, [username, get]);

  useEffect(() => {
    mounted.current = true;
    if (!username) return;

    load();
    timerRef.current = setInterval(() => load(), pollInterval);

    function onRefresh() { load(); }
    window.addEventListener("refreshNotifications", onRefresh);

    return () => {
      mounted.current = false;
      window.removeEventListener("refreshNotifications", onRefresh);
      if (timerRef.current) { clearInterval(timerRef.current); timerRef.current = null; }
    };
  }, [username, pollInterval, load]);

  async function markAsRead(id) {
    try {
      await patch(`/${id}/read`);
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, readFlag: true } : n)));
      setUnreadCount((prev) => Math.max(0, prev - 1));
      if (topAlert && topAlert.id === id) setTopAlert(null);
    } catch (e) {
      console.error("Failed to mark notification read", e);
    }
  }

  async function markAllRead() {
    try {
      await patch(`/mark-all-read/${username}`);
      setNotifications((prev) => prev.map((n) => ({ ...n, readFlag: true })));
      setUnreadCount(0);
      setTopAlert(null);
    } catch (e) {
      console.error("Failed to mark all read", e);
    }
  }

  if (!username) return null;

  return (
    <>
      {topAlert && (
        <div className="fixed left-0 right-0 top-0 z-50 flex justify-center">
          <div className="max-w-3xl w-full mx-4 mt-4 p-3 rounded shadow-lg bg-yellow-300 text-black flex items-center justify-between">
            <div>
              <strong className="mr-2">{topAlert.actorUsername}</strong>
              <span>{topAlert.message}</span>
              <div className="text-xs text-black/60 mt-1">
                {new Date(topAlert.timestamp).toLocaleString()}
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button className="underline text-sm" onClick={() => markAsRead(topAlert.id)}>Dismiss</button>
              <button className="bg-black text-white px-2 py-1 rounded text-sm" onClick={() => { setShowDropdown(true); }}>
                View
              </button>
            </div>
          </div>
        </div>
      )}

      <div style={{ position: "fixed", right: 16, top: 16, zIndex: 60 }}>
        <div className="relative">
          <button
            className="bg-white/90 text-black px-3 py-2 rounded shadow flex items-center gap-2"
            onClick={() => setShowDropdown(prev => !prev)}
          >
            🔔
            {unreadCount > 0 && (
              <span className="bg-red-600 text-white px-2 py-0.5 rounded text-xs">{unreadCount}</span>
            )}
          </button>

          {showDropdown && (
            <div className="mt-2 w-80 max-h-96 overflow-auto bg-white rounded shadow p-2">
              <div className="flex justify-between items-center mb-2">
                <div className="font-semibold">Notifications</div>
                <div className="text-xs">
                  <button className="underline" onClick={markAllRead}>Mark all read</button>
                </div>
              </div>

              {notifications.length === 0 && <div className="text-sm text-gray-600">No notifications</div>}

              {notifications.map(n => (
                <div key={n.id} className={`p-2 rounded mb-1 ${n.readFlag ? "bg-gray-100" : "bg-white"}`}>
                  <div className="flex justify-between items-start">
                    <div>
                      <div><strong>{n.actorUsername}</strong> <span className="text-sm text-gray-600">• {n.type}</span></div>
                      <div className="text-sm">{n.message}</div>
                      <div className="text-xs text-gray-500">{new Date(n.timestamp).toLocaleString()}</div>
                    </div>

                    {!n.readFlag && (
                      <div className="ml-2">
                        <button className="text-xs underline" onClick={() => markAsRead(n.id)}>Mark read</button>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
}
