package main.controller;

import jakarta.validation.Valid;
import main.dto.AuthRequest;
import main.dto.AuthResponse;
import main.entity.Student;
import main.service.*;
import main.requestDTO.StudentRequest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthCommandService commandService;

    @Autowired // Automatically inject command service (DP)
    public AuthController(AuthCommandService commandService) {
        this.commandService = commandService;
    }


    @PostMapping("/auth/register")
    public ResponseEntity<Student> registerStudent(@Valid @RequestBody StudentRequest request) {
        Student created = commandService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/auth/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {

        if (commandService.verifyStudentEmail(token)) {
            return ResponseEntity.ok(Map.of("message", "Email verified successfully!"));
        }
        return ResponseEntity.badRequest().build();

    }

    @PostMapping(value = "/auth/authenticate", produces = "application/json")
    public ResponseEntity<?> authenticate(@Valid @RequestBody AuthRequest request) {
        try {
            String token = commandService.authenticateLogin(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(new AuthResponse(token, request.getUsername()));
        } catch (BadCredentialsException ex) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Unauthorized");
            body.put("message", "Invalid username or password");
            body.put("errors", new HashMap<>());
            body.put("status", HttpStatus.UNAUTHORIZED.value());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }









}