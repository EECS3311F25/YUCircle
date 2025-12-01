package main.service;

import main.dto.StudentDTO;
import main.dto.ParsedScheduleDTO;
import main.entity.Student;
import main.repository.StudentRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class StudentCommandService {

    private final StudentRepo repo;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final CourseRepo courseRepo;
    private final CourseSessionRepo sessionRepo;
    private final AzureOcrService ocrService;

    public StudentCommandService(StudentRepo repo, CourseRepo courseRepo,
                                 CourseSessionRepo sessionRepo, AzureOcrService ocrService) {
        this.repo = repo;
        this.courseRepo = courseRepo;
        this.sessionRepo = sessionRepo;
        this.ocrService = ocrService;
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



    @Transactional
    public List<ParsedScheduleDTO> uploadSchedule(String username, MultipartFile file) {
        // Get Azure Document Intelligence to parse the file into DTOs
        try {
            List<ParsedScheduleDTO> parsed = ocrService.extractScheduleFromFile(file);

            // Find the student who uploaded the schedule
            Student student = repo.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

            // Iterate through DTOs
            for (ParsedScheduleDTO dto : parsed) {
                // Get the course if it already exists in the database
                Course course = courseRepo.findByCourseCodeAndCourseSection(dto.courseCode(), dto.section())
                        .orElseGet(() -> {
                            // If not found, create a new Course entity
                            Course newCourse = ScheduleMapper.toCourse(dto);
                            // Persist the new course
                            return courseRepo.save(newCourse);
                        });

                // Link the course to the student and vice versa
                course.getStudents().add(student);
                student.getCourses().add(course);

                // Check if the session already exists
                boolean exists = sessionRepo.existsByCourseAndDayAndStartTime(course, dto.day(), dto.startTime());
                if (!exists) {
                    // Create a new CourseSession entity
                    CourseSession session = ScheduleMapper.toSession(dto, course);

                    // Link the session to its associated course and vice versa
                    session.setCourse(course);
                    course.getSessions().add(session);

                    // Persist the new session
                    sessionRepo.save(session);
                }
            }
            // Persist the updated student with course link
            repo.save(student);

            // Print parsed info in terminal
            System.out.println("Parsed schedule entries:");
            parsed.forEach(dto -> System.out.println(dto));
            return parsed;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to process schedule file", e);
        }
    }

}
