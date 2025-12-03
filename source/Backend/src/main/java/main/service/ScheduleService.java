package main.service;

import main.entity.Course;
import main.entity.CourseSession;
import main.entity.Student;
import main.repository.CourseRepo;
import main.repository.CourseSessionRepo;
import main.repository.StudentRepo;
import main.dto.ParsedScheduleDTO;
import main.mapper.ScheduleMapper;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.time.LocalTime;

@Service
public class ScheduleService {

    private final CourseSessionRepo sessionRepo;
    private final CourseRepo courseRepo;
    private final StudentRepo studentRepo;

    public ScheduleService(CourseSessionRepo sessionRepo,
                              CourseRepo courseRepo,
                              StudentRepo studentRepo) {
        this.sessionRepo = sessionRepo;
        this.courseRepo = courseRepo;
        this.studentRepo = studentRepo;
    }

    // GET all sessions for a student (aggregate across their courses)
    public List<ParsedScheduleDTO> getSchedule(String username) {
        Student student = studentRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Student with username '" + username + "' not found"));

        return sessionRepo.findByCourse_Students_Username(username).stream()
                .map(ScheduleMapper::toDto)
                .sorted(Comparator.comparing(ParsedScheduleDTO::courseCode))
                .collect(Collectors.toList());
    }

    // Edit a course session
    public ParsedScheduleDTO updateSession(String username, Long sessionId, Map<String, String> updates) {
        studentRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Student with username '" + username + "' not found"));

        CourseSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Session with ID '" + sessionId + "' not found"));

        if (updates.containsKey("day")) session.setDay(updates.get("day"));
        if (updates.containsKey("startTime")) session.setStartTime(LocalTime.parse(updates.get("startTime")));
        if (updates.containsKey("endTime")) session.setEndTime(LocalTime.parse(updates.get("endTime")));
        if (updates.containsKey("location")) session.setLocation(updates.get("location"));
        if (updates.containsKey("type")) session.setType(updates.get("type"));

        sessionRepo.save(session);
        return ScheduleMapper.toDto(session);
    }

    // Delete a course session
    public void deleteSession(String username, Long sessionId) {
        Student student = studentRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Student with username '" + username + "' not found"));

        CourseSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Session with ID '" + sessionId + "' not found"));

        Course course = session.getCourse();

        // Delete the session
        sessionRepo.delete(session);

        // If this student no longer has any sessions for the course, unlink the course
        boolean stillHasSessionsForStudent = sessionRepo
                .findByCourse_Students_Username(student.getUsername()).stream()
                .anyMatch(s -> s.getCourse().equals(course));

        if (!stillHasSessionsForStudent) {
            // Remove the link in both directions
            student.getCourses().remove(course);
            course.getStudents().remove(student);
            // persist the unlink
            studentRepo.save(student);
            courseRepo.save(course);
        }

        // Only delete the course if it has no sessions AND no students
        if (course.getSessions().isEmpty() && course.getStudents().isEmpty()) {
            courseRepo.delete(course);
        }
    }

    // Add new course sessions
    public ParsedScheduleDTO addSession(String username, Map<String, String> body) {
        Student student = studentRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        String courseCode = body.get("courseCode");
        String section = body.get("section");

        Course course = courseRepo.findByCourseCodeAndCourseSection(courseCode, section)
                .orElseGet(() -> courseRepo.save(new Course(courseCode, section)));

        if (!course.getStudents().contains(student)) {
            course.getStudents().add(student);
            student.getCourses().add(course);
        }

        CourseSession session = new CourseSession(
                course,
                body.get("type"),
                body.get("day"),
                LocalTime.parse(body.get("startTime")),
                LocalTime.parse(body.get("endTime")),
                body.get("location")
        );

        sessionRepo.save(session);
        return ScheduleMapper.toDto(session);
    }
}