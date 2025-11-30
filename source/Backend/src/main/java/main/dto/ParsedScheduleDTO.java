package main.dto;

import java.time.LocalTime;

public record ParsedScheduleDTO(
        String courseCode,
        String section,
        String type,         // Lecture, Lab, Tutorial
        String day,          // Monday, Tuesday, etc.
        LocalTime startTime,
        LocalTime endTime,
        String location
) {}