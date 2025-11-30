package main.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import main.entity.Course;
import main.entity.CourseSession;
import java.time.LocalTime;

public interface CourseSessionRepo extends JpaRepository<CourseSession, Long> {
    // Check if a course session exists
    boolean existsByCourseAndDayAndStartTime(Course course, String day, LocalTime startTime);
}