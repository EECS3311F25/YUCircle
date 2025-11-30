package main.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import main.entity.Student;
import java.util.Optional;




public interface StudentRepo extends JpaRepository<Student, Long> {

    Student findByStudentNumber(Long studentNumber);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<Student> findByVerificationToken(String token);

    Optional<Student> findByUsername(String username);
}



