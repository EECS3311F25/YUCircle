package main.dto;

import java.time.LocalTime;

public record ParsedScheduleDTO(
        Long cSessionId,     // unique identifier for the session (nullable for OCR-parsed)
        String courseCode,
        String section,
        String type,         // Lecture, Lab, Tutorial
        String day,          // Monday, Tuesday, etc.
        LocalTime startTime,
        LocalTime endTime,
        String location
) {
    // Convenience constructor for OCR (no ID yet)
    public ParsedScheduleDTO(
            String courseCode,
            String section,
            String type,
            String day,
            LocalTime startTime,
            LocalTime endTime,
            String location
    ) {
        this(null, courseCode, section, type, day, startTime, endTime, location);
    }
}