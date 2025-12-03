package main.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import main.entity.Course;

public interface CourseRepo extends JpaRepository<Course, Long> {

    // Custom query to find a course by its code (used for deduplication)
    Optional<Course> findByCourseCodeAndCourseSection(String courseCode, String courseSection);
}