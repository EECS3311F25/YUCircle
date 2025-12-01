import React, { useEffect, useState } from "react";
import { EditText } from "react-edit-text";
import "react-edit-text/dist/index.css";
import useFetch from "../hooks/useFetch";

export default function Schedule() {
  const [sessions, setSessions] = useState([]);
  const api = useFetch("http://localhost:8080/api"); // baseUrl

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
    // Uncomment when backend DELETE is ready
    // doFetch(`/api/students/${username}/schedule/${id}`, "DELETE")
    //   .then(() => setSessions(sessions.filter(s => s.cSessionId !== id)));
  };

  const addSession = () => {
    const newSession = {
      day: "Monday",
      startTime: "09:00",
      endTime: "10:00",
      room: "TBD",
      type: "Lecture"
    };

    // Uncomment when backend POST is ready
    // doFetch(`/api/students/${username}/schedule`, "POST", newSession)
    //   .then(res => setSessions([...sessions, res]));
  };

  return (
    <div className="p-4">
      <table className="table-auto w-full border">
        <thead>
          <tr>
            <th>Day</th>
            <th>Time</th>
            <th>Location</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {sessions
            /*only keep sessions with non-null IDs*/
            .filter(session => session.cSessionId != null)
            .map(session => (
              /*have fallbacks just in case*/
              <tr key={session.cSessionId ?? `${session.courseCode}-${session.day}-${session.startTime}`}>
                <td>{session.courseCode} ({session.section})</td>
                {/* Day field */}
                <td>
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
                <td>
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
                <td>
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
                <td>
                  <button
                    className="bg-red-500 text-white px-2 py-1 rounded"
                    onClick={() => deleteSession(session.cSessionId)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
        </tbody>
      </table>
      <button
        className="mt-4 bg-green-500 text-white px-4 py-2 rounded"
        onClick={addSession}
      >
        Add Session
      </button>
    </div>
  );
}