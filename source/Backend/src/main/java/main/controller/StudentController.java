package main.controller;

import main.dto.StudentDTO;
import main.dto.ParsedScheduleDTO;
import main.entity.Student;
import main.service.StudentCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;


@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    private final StudentCommandService service;

    // For uploadSchedule validation
    @Value("${upload.max-size}")
    private long maxSize;  // // Current limit: 4 MB (https://learn.microsoft.com/en-us/azure/ai-services/document-intelligence/prebuilt/layout?view=doc-intel-4.0.0&tabs=rest%2Csample-code#input-requirements)
    @Value("${upload.allowed-types}")
    private String[] allowedTypes;

    public StudentController(StudentCommandService service) {
        this.service = service;
    }

    // Data Transfer Over/summary endpoint
    @GetMapping("/{id}/sidebar")
    public StudentDTO sidebar(@PathVariable Long id) {
        return service.toSidebar(id);
    }

    // Full student/profile
    @GetMapping("/{id}")
    public Student getStudent(@PathVariable Long id) {
        return service.getStudentById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    // Delete a student by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    // List all students
    @GetMapping
    public List<Student> all() {
        return service.getAllStudents();
    }

    // Finding user via username
    @GetMapping("/by-username/{username}")
    public ResponseEntity<?> getByUserName(@PathVariable String username) {
        Optional<Student> student = service.getStudentByUsername(username);

        if (student.isPresent()) {
            return ResponseEntity.ok(student.get());
        } else {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Not Found");
            body.put("message", "Student with username '" + username + "' not found");
            body.put("errors", new HashMap<>());
            body.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
    }

    // Updating info
    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @RequestBody Student changes) {
        return service.updateStudent(id, changes);
    }

    // MIGHT MOVE: Upload schedule
    @PostMapping("/{username}/schedule")
    public ResponseEntity<List<ParsedScheduleDTO>> uploadSchedule(@PathVariable String username, @RequestParam("file") MultipartFile file) {
        // Print out passed in username
        System.out.println("Upload endpoint triggered for username=" + username);

        // Check uploaded file properties
        System.out.println("Received file contentType=" + file.getContentType() + ", size=" + file.getSize());

        // Check if file is empty
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }

        // Check the file type
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "File type could not be determined");
        }
        if (!Arrays.asList(allowedTypes).contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only " + String.join(", ", allowedTypes) + " files are supported");
        }

        // Check file size
        if (file.getSize() > maxSize) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "File size exceeds " + (maxSize / (1024 * 1024)) + " MB limit");
        }

        // Upload and parse schedule for the user with the given username
        List<ParsedScheduleDTO> parsed = service.uploadSchedule(username, file);
        return ResponseEntity.ok(parsed);
    }

    // PATCH endpoint to update specific fields via username
    @PatchMapping("/update/{username}")
    public ResponseEntity<?> updateFields(
            @PathVariable String username,
            @RequestBody Map<String, String> updates) {

        Optional<Student> studentOpt = service.getStudentByUsername(username);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Student not found"));
        }

        Student student = studentOpt.get();

        // Apply allowed updates
        updates.forEach((key, value) -> {
            if (value == null) return;

            switch (key) {
                case "firstName" -> student.setFirstName(value);
                case "lastName" -> student.setLastName(value);
                case "major" -> student.setMajor(value);
                case "bio" -> student.setBio(value);
                case "newPassword" -> {
                    String current = updates.get("currentPassword");
                    if (current == null || !service.checkPassword(student, current)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password incorrect");
                    }
                    service.updatePassword(student, value);
                }
                // username & email NOT allowed
            }
        });

        // DIRECT SAVE — do NOT call updateStudent()
        Student saved = service.saveDirect(student);

        return ResponseEntity.ok(saved);
    }

}