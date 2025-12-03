package main.service;

import main.entity.CourseSession;
import main.entity.Student;
import main.repository.CourseSessionRepo;
import main.repository.StudentRepo;
import main.dto.AvailabilityDTO;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalTime;

@Service
public class AvailabilityService {
    private final CourseSessionRepo sessionRepo;
    private final StudentRepo studentRepo;

    public AvailabilityService(CourseSessionRepo sessionRepo, StudentRepo studentRepo) {
        this.sessionRepo = sessionRepo;
        this.studentRepo = studentRepo;
    }

    public List<AvailabilityDTO> computeAvailability() {
        long totalStudents = studentRepo.count();

        // Build 30-minute endpoints from 08:00 to 22:00 (inclusive of final endpoint)
        List<LocalTime> timePoints = new ArrayList<>();
        for (int hour = 8; hour < 22; hour++) {
            timePoints.add(LocalTime.of(hour, 0));
            timePoints.add(LocalTime.of(hour, 30));
        }
        timePoints.add(LocalTime.of(22, 0)); // ensure 21:30–22:00 exists

        List<String> weekdays = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        List<CourseSession> sessions = sessionRepo.findAll();

        List<AvailabilityDTO> result = new ArrayList<>();

        for (String day : weekdays) {
            for (int i = 0; i < timePoints.size() - 1; i++) {
                LocalTime start = timePoints.get(i);
                LocalTime end = timePoints.get(i + 1);

                // Half-open overlap: session overlaps the block if (start < end && end > start)
                long busyCount = sessions.stream()
                        .filter(s -> s.getDay().equals(day))
                        .filter(s -> s.getStartTime().isBefore(end) && s.getEndTime().isAfter(start))
                        .flatMap(s -> s.getCourse().getStudents().stream())
                        .map(Student::getStudentNumber)
                        .distinct()
                        .count();

                long availableCount = totalStudents - busyCount;
                result.add(new AvailabilityDTO(day, start, end, availableCount));
            }
        }

        return result;
    }
}
