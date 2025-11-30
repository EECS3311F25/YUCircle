package main.service;

import main.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import main.requestDTO.StudentRequest;
import main.entity.Student;
import main.repository.StudentRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class AuthCommandService {

    private final StudentRepo studentRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailCommandService emailService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthCommandService(StudentRepo studentRepo, EmailCommandService emailService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.studentRepo = studentRepo;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.emailService = emailService;
    }

    public Student registerStudent(StudentRequest request) {

        // If username already exists.
        if (studentRepo.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken.");
        }

        // If email already exists.
        if (studentRepo.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered.");
        }

        String token = UUID.randomUUID().toString();

        Student student = new Student();
        student.setUsername(request.getUsername());
        student.setEmail(request.getEmail());
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setVerificationToken(token);

        String link = "http://localhost:8080/auth/verify?token=" + student.getVerificationToken();
        emailService.sendEmail(
          student.getEmail(),
          "Verify your YUCircle account",
          "Click to verify your account:" + link
        );


        studentRepo.save(student);
        return student;
    }

    public boolean verifyStudentEmail(String token) {
        Student student = studentRepo.findByVerificationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid verification token."));

        student.setVerified(true);
        student.setVerificationToken(null);
        studentRepo.save(student);
        return true;
    }

    public String authenticateLogin(String username, String password) {
        Optional<Student> studentOpt = studentRepo.findByUsername(username);

        if (studentOpt.isPresent()) {
            boolean verified = studentOpt.get().isVerified();
            if (!verified) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please verify your email.");
            }
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails user = (UserDetails) authentication.getPrincipal();
        return jwtUtil.generateToken(user.getUsername());
    }

}


