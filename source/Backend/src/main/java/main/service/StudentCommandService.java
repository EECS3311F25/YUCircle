package main.service;

import main.dto.StudentDTO;
import main.entity.Student;
import main.repository.StudentRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class StudentCommandService {

    private final StudentRepo repo;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public StudentCommandService(StudentRepo repo) {
        this.repo = repo;
    }

    // Check if password matches
    public boolean checkPassword(Student student, String rawPassword) {
        return passwordEncoder.matches(rawPassword, student.getPassword());
    }

    // Update student password securely
    public void updatePassword(Student student, String newPassword) {
        student.setPassword(passwordEncoder.encode(newPassword));
    }

    // Convert to sidebar DTO (minimal data)
    public StudentDTO toSidebar(Long id) {
        Student s = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        return new StudentDTO(
                s.getStudentNumber(),
                s.getFirstName(),
                s.getLastName(),
                s.getUsername(),
                s.getEmail(),
                s.getMajor()
        );
    }

    // Retrieve student by ID
    public Optional<Student> getStudentById(Long id) {
        return repo.findById(id);
    }

    // Retrieve all students
    public List<Student> getAllStudents() {
        return repo.findAll();
    }

    // Create a student
    public Student createStudent(Student s) {
        return repo.save(s);
    }

    // Delete a student
    public void deleteStudent(Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        repo.deleteById(id);
    }

    // Find by username
    public Optional<Student> getStudentByUsername(String username) {
        return repo.findByUsername(username);
    }

    // Update student fields (partial update)
    public Student updateStudent(Long id, Student changes) {
        Student s = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        if (changes.getUsername() != null) s.setUsername(changes.getUsername());
        if (changes.getFirstName() != null) s.setFirstName(changes.getFirstName());
        if (changes.getLastName() != null) s.setLastName(changes.getLastName());
        if (changes.getEmail() != null) s.setEmail(changes.getEmail());
        if (changes.getMajor() != null) s.setMajor(changes.getMajor());
        if (changes.getBio() != null) s.setBio(changes.getBio());

        return repo.save(s);
    }
    public Student saveDirect(Student s) {
        return repo.save(s);
    }
    
}
