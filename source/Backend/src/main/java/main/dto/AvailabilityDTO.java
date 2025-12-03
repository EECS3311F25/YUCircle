package main.dto;

import java.time.LocalTime;

public class AvailabilityDTO {
    private final String day;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final long availableCount;

    // Constructor must match JPQL argument order exactly
    public AvailabilityDTO(String day, LocalTime startTime, LocalTime endTime, Long availableCount) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.availableCount = availableCount;
    }

    public String getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public long getAvailableCount() {
        return availableCount;
    }
}