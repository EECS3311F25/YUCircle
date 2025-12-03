import React, { useEffect, useState } from "react";
import "react-edit-text/dist/index.css";
import useFetch from "../Hooks/useFetch";

export default function AvailabilityHeatmap() {
  const [data, setData] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch("http://localhost:8080/api/availability", {
      headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
    })
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then(setData)
      .catch(err => {
        console.error("Failed to fetch availability:", err);
        setError(err.message);
      });
  }, []);

  if (error) return <p className="text-center text-red-500">Error: {error}</p>;
  if (data.length === 0) return <p className="text-center text-gray-500">No availability data found.</p>;

  const max = Math.max(...data.map(d => d.availableCount)) || 1;

  const groupedByDay = data.reduce((acc, { day, startTime, endTime, availableCount }) => {
    (acc[day] ||= []).push({ startTime, endTime, availableCount });
    return acc;
  }, {});

  Object.keys(groupedByDay).forEach(day => {
    groupedByDay[day].sort((a, b) => a.startTime.localeCompare(b.startTime));
  });

  const timeSlots = [];
  for (let hour = 8; hour < 22; hour++) {
    timeSlots.push(`${String(hour).padStart(2, "0")}:00:00`);
    timeSlots.push(`${String(hour).padStart(2, "0")}:30:00`);
  }
  timeSlots.push("22:00:00");

  const days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"];

  return (
    <div className="grid gap-px bg-gray-300" style={{ gridTemplateColumns: `100px repeat(${days.length}, 1fr)` }}>
      <div className="bg-white"></div>
      {days.map(day => (
        <div key={day} className="bg-white text-center font-bold py-2">{day}</div>
      ))}

      {timeSlots.slice(0, -1).map((slot, i) => {
        const nextSlot = timeSlots[i + 1];

        return (
          <React.Fragment key={slot}>
            <div className="bg-white text-right pr-2 text-xs font-semibold py-2">{slot.slice(0,5)}</div>

            {days.map(day => {
              const blocks = groupedByDay[day] || [];
              const block = blocks.find(b => b.startTime === slot && b.endTime === nextSlot);
              const availableCount = block ? block.availableCount : 0;
              const intensity = availableCount / max;
              const color = availableCount > 0
                ? `rgba(34,197,94,${intensity})`
                : `rgba(100,116,139,0.1)`;

              const tooltip = `${day}, ${slot.slice(0,5)}–${nextSlot.slice(0,5)}: ${availableCount} available`;

              return (
                <div
                  key={`${day}-${slot}`}
                  className="h-8 border text-xs text-center flex items-center justify-center relative group"
                  style={{ backgroundColor: color, cursor: "pointer" }}
                  title={tooltip}
                >
                  {/* Only show count on hover */}
                  <div className="opacity-0 group-hover:opacity-100 transition-opacity">
                    {availableCount}
                  </div>
                </div>
              );
            })}
          </React.Fragment>
        );
      })}
    </div>
  );
}
