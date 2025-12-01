import React, { useState } from "react";

export default function AddSessionModal({ isOpen, onClose, onSave }) {
  const [formData, setFormData] = useState({
    courseCode: "",
    section: "",
    day: "",
    startTime: "",
    endTime: "",
    location: "",
    type: "Lecture"
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // Clears the modal when it's closed
  const handleClose = () => {
    setFormData({
      courseCode: "",
      section: "",
      day: "",
      startTime: "",
      endTime: "",
      location: "",
      type: "Lecture"
    });
    onClose();
  };

  const handleSubmit = () => {
    onSave(formData);  // pass data back to parent
    setFormData({
        courseCode: "",
        section: "",
        day: "",
        startTime: "",
        endTime: "",
        location: "",
        type: "Lecture"
      });
      onClose();  // close the modal
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-gray-800 bg-opacity-50 flex items-center justify-center">
      <div className="bg-white p-6 rounded shadow-lg w-96">
        <h2 className="text-xl font-bold mb-4">Add New Session</h2>

        <input
          name="courseCode"
          placeholder="Course Code, e.g. EECS3311"
          value={formData.courseCode}
          onChange={handleChange}
          className="border p-2 w-full mb-2"
        />
        <input
          name="section"
          placeholder="Section"
          value={formData.section}
          onChange={handleChange}
          className="border p-2 w-full mb-2"
        />
        <input
          name="day"
          placeholder="Day"
          value={formData.day}
          onChange={handleChange}
          className="border p-2 w-full mb-2"
        />
        <input
          type="time"
          name="startTime"
          value={formData.startTime}
          onChange={handleChange}
          className="border p-2 w-full mb-2"
        />
        <input
          type="time"
          name="endTime"
          value={formData.endTime}
          onChange={handleChange}
          className="border p-2 w-full mb-2"
        />
        <input
          name="location"
          placeholder="Location, e.g. Keele: LSB 106"
          value={formData.location}
          onChange={handleChange}
          className="border p-2 w-full mb-2"
        />
        <select
          name="type"
          value={formData.type}
          onChange={handleChange}
          className="border p-2 w-full mb-4"
        >
          <option>Lecture</option>
          <option>Lab</option>
          <option>Tutorial</option>
          <option>Language Classes</option>
        </select>

        <div className="flex justify-end space-x-2">
          <button
            className="bg-gray-400 text-white px-4 py-2 rounded"
            onClick={onClose}
          >
            Cancel
          </button>
          <button
            className="bg-yellow-500 text-white px-4 py-2 rounded"
            onClick={() =>
              setFormData({
                courseCode: "",
                section: "",
                day: "",
                startTime: "",
                endTime: "",
                location: "",
                type: "Lecture"
              })
            }
          >
            Clear
          </button>
          <button
            className="bg-green-500 text-white px-4 py-2 rounded"
            onClick={handleSubmit}
          >
            Save
          </button>
        </div>
      </div>
    </div>
  );
}