package main.controller;

import main.service.ScheduleService;
import main.dto.ParsedScheduleDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students/{username}/schedule")
@CrossOrigin(origins = "http://localhost:5173")
public class ScheduleController {

    private final ScheduleService service;

    public ScheduleController(ScheduleService service) {
        this.service = service;
    }

    // GET all sessions for a student (aggregate across their courses)
    @GetMapping
    public ResponseEntity<List<ParsedScheduleDTO>> getSchedule(@PathVariable String username) {
        return ResponseEntity.ok(service.getSchedule(username));
    }

    // Edit a course session
    @PatchMapping("/{sessionId}")
    public ResponseEntity<ParsedScheduleDTO> updateSession(@PathVariable String username,
                                                           @PathVariable Long sessionId,
                                                           @RequestBody Map<String, String> updates) {
        return ResponseEntity.ok(service.updateSession(username, sessionId, updates));
    }

    // Delete a course session
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String username,
                                              @PathVariable Long sessionId) {
        service.deleteSession(username, sessionId);
        return ResponseEntity.noContent().build();
    }

    // Add new course sessions
    @PostMapping("/add")
    public ResponseEntity<ParsedScheduleDTO> addSession(@PathVariable String username,
                                                        @RequestBody Map<String, String> body) {
        ParsedScheduleDTO dto = service.addSession(username, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}