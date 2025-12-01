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

    // MIGHT REMOVE: Upload schedule
//    @PostMapping
//    public ResponseEntity<List<ParsedScheduleDTO>> uploadSchedule(@PathVariable String username, @RequestParam("file") MultipartFile file) {
//        // Get student username
//        // Check a student with this username exists
//        Student student = studentRepo.findByUsername(username)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
//
//        // Print out passed in username
//        System.out.println("Upload endpoint triggered for username=" + username);
//
//        // Check uploaded file properties
//        System.out.println("Received file contentType=" + file.getContentType() + ", size=" + file.getSize());
//
//        // Check if file is empty
//        if (file.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
//        }
//
//        // Check the file type
//        String contentType = file.getContentType();
//        if (contentType == null) {
//            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
//                    "File type could not be determined");
//        }
//        if (!Arrays.asList(allowedTypes).contains(contentType)) {
//            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
//                    "Only " + String.join(", ", allowedTypes) + " files are supported");
//        }
//
//        // Check file size
//        if (file.getSize() > maxSize) {
//            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
//                    "File size exceeds " + (maxSize / (1024 * 1024)) + " MB limit");
//        }
//
//        // Upload and parse schedule for the user with the given username
//        List<ParsedScheduleDTO> parsed = service.uploadSchedule(username, file);
//        return ResponseEntity.ok(parsed);
//    }

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