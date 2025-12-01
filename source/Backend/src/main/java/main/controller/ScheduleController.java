package main.controller;

import main.entity.Course;
import main.entity.CourseSession;
import main.entity.Student;
import main.repository.CourseRepo;
import main.repository.CourseSessionRepo;
import main.repository.StudentRepo;
import main.dto.ParsedScheduleDTO;
import main.service.StudentCommandService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/students/{username}/schedule")
@CrossOrigin(origins = "http://localhost:5173")
public class ScheduleController {

    private final CourseSessionRepo sessionRepo;
    private final CourseRepo courseRepo;
    private final StudentRepo studentRepo;
    private final StudentCommandService service;

    public ScheduleController(CourseSessionRepo sessionRepo,
                              CourseRepo courseRepo,
                              StudentRepo studentRepo,
                              StudentCommandService service) {
        this.sessionRepo = sessionRepo;
        this.courseRepo = courseRepo;
        this.studentRepo = studentRepo;
        this.service = service;
    }

    // GET all sessions for a student (aggregate across their courses)
    @GetMapping
    public ResponseEntity<List<ParsedScheduleDTO>> getSchedule(@PathVariable String username) {
        // Get student username
        Optional<Student> studentOpt = service.getStudentByUsername(username);
        // Check if this student's username exists
        if (studentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Student with username '" + username + "' not found");
        }

        // Fetch sessions directly
        List<CourseSession> sessions = sessionRepo.findByCourse_Students_Username(username);

        // Map to DTOs
        List<ParsedScheduleDTO> dtoList = sessions.stream()
                .map(s -> new ParsedScheduleDTO(
                        s.getCSessionId(),
                        s.getCourse().getCourseCode(),
                        s.getCourse().getCourseSection(),
                        s.getType(),
                        s.getDay(),
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getLocation()
                ))
                .sorted(Comparator.comparing(ParsedScheduleDTO::courseCode))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    // Edit a course session
    @PatchMapping("/{sessionId}")
    public ResponseEntity<ParsedScheduleDTO> updateSession(@PathVariable String username,
                                                           @PathVariable Long sessionId,
                                                           @RequestBody Map<String, String> updates) {
        Optional<Student> studentOpt = service.getStudentByUsername(username);
        if (studentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Student with username '" + username + "' not found");
        }

        CourseSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Session with ID '" + sessionId + "' not found"));

        // Apply updates (parse times as needed)
        if (updates.containsKey("day")) session.setDay(updates.get("day"));
        if (updates.containsKey("startTime")) session.setStartTime(LocalTime.parse(updates.get("startTime")));
        if (updates.containsKey("endTime")) session.setEndTime(LocalTime.parse(updates.get("endTime")));
        if (updates.containsKey("location")) session.setLocation(updates.get("location"));
        if (updates.containsKey("type")) session.setType(updates.get("type"));

        sessionRepo.save(session);

        ParsedScheduleDTO dto = new ParsedScheduleDTO(
                session.getCSessionId(),
                session.getCourse().getCourseCode(),
                session.getCourse().getCourseSection(),
                session.getType(),
                session.getDay(),
                session.getStartTime(),
                session.getEndTime(),
                session.getLocation()
        );

        return ResponseEntity.ok(dto);
    }

    // DOUBLE CHECK
//    // POST add new session to a course the student is enrolled in
//    @PostMapping("/{courseId}")
//    public ResponseEntity<CourseSession> addSession(@PathVariable String username,
//                                                    @PathVariable Long courseId,
//                                                    @RequestBody CourseSession newSession) {
//        // Get student username
//        // Check a student with this username exists
//        Student student = studentRepo.findByUsername(username)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
//
//        Course course = courseRepo.findById(courseId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
//
//        if (!student.getCourses().contains(course)) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student not enrolled in this course");
//        }
//
//        newSession.setCourse(course);
//        CourseSession saved = sessionRepo.save(newSession);
//        return ResponseEntity.ok(saved);
//    }
//
//    // PUT update a session (only if it belongs to a course the student is enrolled in)
//    @PutMapping("/{courseId}/{sessionId}")
//    public ResponseEntity<CourseSession> updateSession(@PathVariable String username,
//                                                       @PathVariable Long courseId,
//                                                       @PathVariable Long sessionId,
//                                                       @RequestBody CourseSession updated) {
//        // Get student username
//        // Check a student with this username exists
//        Student student = studentRepo.findByUsername(username)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
//
//        Course course = courseRepo.findById(courseId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
//
//        if (!student.getCourses().contains(course)) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student not enrolled in this course");
//        }
//
//        return sessionRepo.findById(sessionId)
//                .filter(s -> s.getCourse().equals(course))
//                .map(session -> {
//                    session.setType(updated.getType());
//                    session.setDay(updated.getDay());
//                    session.setStartTime(updated.getStartTime());
//                    session.setEndTime(updated.getEndTime());
//                    session.setRoom(updated.getRoom());
//                    return ResponseEntity.ok(sessionRepo.save(session));
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // DELETE a session (only if it belongs to a course the student is enrolled in)
//    @DeleteMapping("/{courseId}/{sessionId}")
//    public ResponseEntity<Void> deleteSession(@PathVariable String username,
//                                              @PathVariable Long courseId,
//                                              @PathVariable Long sessionId) {
//        // Get student username
//        // Check a student with this username exists
//        Student student = studentRepo.findByUsername(username)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
//
//        Course course = courseRepo.findById(courseId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
//
//        if (!student.getCourses().contains(course)) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student not enrolled in this course");
//        }
//
//        return sessionRepo.findById(sessionId)
//                .filter(s -> s.getCourse().equals(course))
//                .map(session -> {
//                    sessionRepo.delete(session);
//                    return ResponseEntity.noContent().build();
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
}