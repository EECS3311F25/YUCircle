import React, { useEffect, useState } from "react";
import { EditText } from "react-edit-text";
import "react-edit-text/dist/index.css";
import useFetch from "../Hooks/useFetch";
import AddSessionModal from "../Components/AddSessionModal";

export default function Schedule() {
  const [sessions, setSessions] = useState([]);
  const api = useFetch("http://localhost:8080/api"); // baseUrl
  const [isModalOpen, setModalOpen] = useState(false);

  // Get username from localStorage
    const username = localStorage.getItem("username");

  useEffect(() => {
    if (!username) {
      console.error("Username not found in localStorage. Please log in again.");
      return;
    }

    api.get(`/students/${username}/schedule`)
      .then(data => {
        console.log("Schedule data:", data);
        setSessions(data);
      })
      .catch(err => console.error("Fetch error:", err));
  }, [username]);

  const updateSession = (id, field, value) => {
    const updated = sessions.find(s => s.cSessionId === id);
    if (!updated) return;

    const newSession = { ...updated, [field]: value };

    api.patch(`/students/${username}/schedule/${id}`, { [field]: value })
      .then(res => {
        setSessions(sessions.map(s => (s.cSessionId === id ? res : s)));
      })
      .catch(err => console.error("Update error:", err));
  };

  const deleteSession = (id) => {
    // Optimistically update UI
    setSessions(prevSessions => prevSessions.filter(s => s.cSessionId !== id));

    // Call backend
    api.del(`/students/${username}/schedule/${id}`)
      .then(() => {
        // Re-fetch to ensure consistency
        return api.get(`/students/${username}/schedule`);
      })
      .then(data => {
        setSessions(data); // overwrite with fresh backend state
      })
      .catch(err => console.error("Delete error:", err));
  };

  const addSession = (newSession) => {
      api.post(`/students/${username}/schedule/add`, newSession)
        .then(res => setSessions([...sessions, res]));
    };

  return (
    <div className="p-4">
      <table className="table-auto w-full border-collapse border border-(--yorku-light-grey) text-center">
        <thead>
          <tr>
            <th class="border border-(--yorku-light-grey)">Course</th>
            <th class="border border-(--yorku-light-grey)">Day</th>
            <th class="border border-(--yorku-light-grey)">Time</th>
            <th class="border border-(--yorku-light-grey)">Location</th>
            <th class="border border-(--yorku-light-grey)">Actions</th>
          </tr>
        </thead>
        <tbody>
          {sessions
            /*only keep sessions with non-null IDs*/
            .filter(session => session.cSessionId != null)
            .map(session => (
              /*have fallbacks just in case*/
              <tr key={session.cSessionId ?? `${session.courseCode}-${session.day}-${session.startTime}`}>
                <td class="border border-(--yorku-light-grey)">{session.courseCode} ({session.section})</td>
                {/* Day field */}
                <td class="border border-(--yorku-light-grey)">
                  <EditText
                    name="day"
                    value={session.day}
                    onChange={(e) => {
                      const newSessions = sessions.map(s =>
                        s.cSessionId === session.cSessionId ? { ...s, day: e.target.value } : s
                      );
                      setSessions(newSessions);
                    }}
                    onSave={({ value }) => updateSession(session.cSessionId, "day", value)}
                  />
                </td>

                {/* Time fields */}
                <td class="border border-(--yorku-light-grey)">
                  <EditText
                    name="startTime"
                    value={session.startTime}
                    onChange={(e) => {
                      const newSessions = sessions.map(s =>
                        s.cSessionId === session.cSessionId ? { ...s, startTime: e.target.value } : s
                      );
                      setSessions(newSessions);
                    }}
                    onSave={({ value }) => updateSession(session.cSessionId, "startTime", value)}
                  />
                  {" – "}
                  <EditText
                    name="endTime"
                    value={session.endTime}
                    onChange={(e) => {
                      const newSessions = sessions.map(s =>
                        s.cSessionId === session.cSessionId ? { ...s, endTime: e.target.value } : s
                      );
                      setSessions(newSessions);
                    }}
                    onSave={({ value }) => updateSession(session.cSessionId, "endTime", value)}
                  />
                </td>

                {/* Location field */}
                <td class="border border-(--yorku-light-grey)">
                  <EditText
                    name="location"
                    value={session.location ?? ""}
                    onChange={(e) => {
                      const newSessions = sessions.map(s =>
                        s.cSessionId === session.cSessionId ? { ...s, location: e.target.value } : s
                      );
                      setSessions(newSessions);
                    }}
                    onSave={({ value }) => updateSession(session.cSessionId, "location", value)}
                  />
                </td>
                <td class="border border-(--yorku-light-grey)">
                  <button
                    className="bg-gray-400 text-white px-2 py-1 rounded"
                    onClick={() => {
                      if (window.confirm("Are you sure you want to delete this session?")) {
                        deleteSession(session.cSessionId);
                      }
                    }}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
        </tbody>
      </table>
      <button
        className="bg-(--yorku-red) text-white px-2 py-1 m-4 rounded"
        onClick={() => setModalOpen(true)}
      >
        Add Session
      </button>
      <AddSessionModal
        isOpen={isModalOpen}
        onClose={() => setModalOpen(false)}
        onSave={addSession}
      />
    </div>
  );
}